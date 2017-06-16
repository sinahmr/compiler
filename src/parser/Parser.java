package parser;

import preprocessor.ParseTable;
import scanner.Scanner;
import scanner.Token;

import java.io.*;
import java.util.Stack;

public class Parser {
    Scanner scanner;
    Grammar grammar;
    ParseTable table;
    Stack<Integer> stack = new Stack<>();

    public Parser(Scanner scanner) throws IOException {
        this.scanner = scanner;
        grammar = getGrammar();
        table = getParseTable();
    }

    public void parse() {
//        Token token = scanner.getNextToken();
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
