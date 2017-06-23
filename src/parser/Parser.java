package parser;

import errorhandler.ErrorHandler;
import preprocessor.ParseTable;
import scanner.Scanner;
import scanner.Token;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;


// TODO: ezafe va handle kardane $#yechizi ha va $!yechizi ha (tahlilgare manayi)


public class Parser {
    Scanner scanner;
    ErrorHandler errorHandler;
    Grammar grammar;
    ParseTable table;
    HashMap<String, HashSet<String>> follows;
    Set<String> nonTerminals;
    Stack<Integer> stack = new Stack<>();

    public Parser(Scanner scanner, ErrorHandler errorHandler) throws IOException {
        this.scanner = scanner;
        this.errorHandler = errorHandler;
        grammar = getGrammar();
        table = getParseTable();
        follows = getFollows();
        nonTerminals = follows.keySet();
    }

    public void parse() throws Exception { // TODO delete exception
        stack.push(0);
        boolean getNewToken = true;
        Token token = null;
        do {
            if (getNewToken)
                token = scanner.getNextToken();

            String action = table.get(stack.peek(), token.toString());

            if (action == null || action.equals("")) {
                token = handleErrorAndReturnLastToken();
                if (token == null) {  // Parser is finished
                    throw new Exception("Parse could not be complete, even after recovery.");
                }
                action = table.get(stack.peek(), token.toString());
            }

            if (action.equals("Acc")) {
                break;
            }

            if (action.startsWith("S")) {  // Shift
                int destinationState = Integer.parseInt(action.substring(1));
                stack.push(destinationState);
                getNewToken = true;
            } else {  // Reduce
                int ruleNumber = Integer.parseInt(action.substring(1));
                Rule rule = grammar.rules.get(ruleNumber);
                int popTimes = rule.rhs.length;
                for (int i = 0; i < popTimes; i++)
                    stack.pop();
                int gotoState = Integer.parseInt(table.get(stack.peek(), rule.lhs));
                stack.push(gotoState);
                getNewToken = false;
            }
        } while (!stack.isEmpty());

    }

    private Token handleErrorAndReturnLastToken() throws Exception { // TODO delete
        HashSet<String> nts = removeFromStackAndGetNTSet();

        // discard input
        String selectedNT = null;
        Token token;
        errorHandler.parserError("Some tokens are ezafi and discarded.");
        do {
            token = scanner.getNextToken();
            for (String nt : nts)
                if (follows.get(nt).contains(token.toString()))
                    selectedNT = nt;
        } while (selectedNT == null || !token.toString().equals("EOF"));

        // push the new state
        if (selectedNT == null) {
            errorHandler.parserError("End of input is reached but parse is failed.");
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
            errorHandler.parserError("Missing ye chizi, state " + discardedState + " is discarded.");
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
        File grammarFile = new File("./src/resource/grammar.txt");
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

}
