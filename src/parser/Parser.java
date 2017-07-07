package parser;

import codegenerator.CodeGenerator;
import errorhandler.ErrorHandler;
import preprocessor.ParseTable;
import scanner.Scanner;
import scanner.Token;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class Parser {
    Scanner scanner;
    CodeGenerator codeGenerator;
    ErrorHandler errorHandler;
    Grammar grammar;
    ParseTable table;
    HashMap<String, HashSet<String>> follows;
    Set<String> nonTerminals;
    Stack<Integer> stack = new Stack<>();
    final int MAX_PREV_TOKENS = 3;
    Token[] prevTokens;

    public Parser(Scanner scanner, CodeGenerator codeGenerator, ErrorHandler errorHandler) {
        this.scanner = scanner;
        this.codeGenerator = codeGenerator;
        this.errorHandler = errorHandler;
        try {
            grammar = getGrammar();
            table = getParseTable();
            follows = getFollows();
        } catch (IOException e) {
            e.printStackTrace();
        }
        nonTerminals = follows.keySet();
        prevTokens = new Token[MAX_PREV_TOKENS];
    }

    public void parse() {
        stack.push(0);
        boolean getNewToken = true;
        Token token = null;
        do {
            if (getNewToken)
                token = scanner.getNextToken();

            String action = table.get(stack.peek(), token.toString());

            if (action == null || action.equals("")) {
                token = handleErrorAndReturnLastToken(token);
                if (token == null) {  // Parser is finished
                    System.out.println("Code until this point:");
                    codeGenerator.printCode();
                    return;
                }
                action = table.get(stack.peek(), token.toString());
            }

            if (action.equals("Acc")) {
                codeGenerator.printCode();
                break;
            }

            if (action.startsWith("S")) {  // Shift
                int destinationState = Integer.parseInt(action.substring(1));
                stack.push(destinationState);
                pushToPrevTokens(token);
                getNewToken = true;
            } else {  // Reduce
                int ruleNumber = Integer.parseInt(action.substring(1));
                Rule rule = grammar.rules.get(ruleNumber);

                if (rule.lhs.startsWith("$#")) {
                    String codegenAction = rule.lhs.substring(2);
                    boolean success = codeGenerator.generateCode(codegenAction, token, prevTokens);
                    if (!success) {
                        System.out.println("Code until this point:");
                        codeGenerator.printCode();
                        return;
                    }
                }

                int popTimes = rule.rhs.length;
                for (int i = 0; i < popTimes; i++)
                    stack.pop();
                int gotoState = Integer.parseInt(table.get(stack.peek(), rule.lhs));
                stack.push(gotoState);
                getNewToken = false;
            }
        } while (!stack.isEmpty());

    }

    private Token handleErrorAndReturnLastToken(Token lastToken) {
        HashSet<String> nts = removeFromStackAndGetNTSet();

        // discard input
        String selectedNT = null;
        Token token;
        errorHandler.parserError("Some misplaced tokens are discarded", lastToken);
        do {
            token = scanner.getNextToken();
            for (String nt : nts)
                if (follows.get(nt).contains(token.toString()))
                    selectedNT = nt;
        } while (selectedNT == null && !token.toString().equals("EOF"));

        // push the new state
        if (selectedNT == null) {
            errorHandler.parserError("End of input is reached but parse is failed");
            return null;
        }
        stack.push(Integer.parseInt(table.get(stack.peek(), selectedNT)));
        return token;
    }

    private HashSet<String> removeFromStackAndGetNTSet() {
        int stateWithSomethingInGoto = stack.peek();
        HashSet<String> foundNTs;
        while ((foundNTs = getNTsWithAGotoBelow(stateWithSomethingInGoto)).isEmpty()) {
            int discardedState = stack.pop();
//            errorHandler.parserError("Missing ye chizi, state " + discardedState + " is discarded");
            stateWithSomethingInGoto = stack.peek();
        }
        return foundNTs;
    }

    private HashSet<String> getNTsWithAGotoBelow(int state) {
        HashSet<String> toReturn = new HashSet<>();
        for (String nt : nonTerminals)
            if (table.get(state, nt) != null && !table.get(state, nt).equals(""))
                toReturn.add(nt);
        return toReturn;
    }

    private HashMap<String, HashSet<String>> getFollows() throws IOException {
        HashMap<String, HashSet<String>> follows = new HashMap<>();
        File followsFile = new File("./src/resource/follows.txt");
        BufferedReader br = new BufferedReader(new FileReader(followsFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(" ");
            HashSet<String> follow = new HashSet<>();
            for (int i = 1; i < parts.length; i++)
                follow.add(parts[i]);
            follows.put(parts[0], follow);
        }
        return follows;
    }

    private Grammar getGrammar() throws IOException {
        Grammar grammar = new Grammar();
        File grammarFile = new File("./src/resource/grammars/final_grammar.txt");
        BufferedReader br = new BufferedReader(new FileReader(grammarFile));
        String line;
        while ((line = br.readLine()) != null) {
            grammar.addRule(line);
        }
        return grammar;
    }

    private ParseTable getParseTable() throws IOException {
        File tableFile = new File("./src/resource/table.csv");
        String csv = new java.util.Scanner(tableFile).useDelimiter("\\Z").next();
        return new ParseTable(csv);
    }

    private void pushToPrevTokens(Token token) {
        for (int i = prevTokens.length - 1; i > 0; i--) {
            prevTokens[i] = prevTokens[i - 1];
        }
        prevTokens[0] = token;
    }
}
