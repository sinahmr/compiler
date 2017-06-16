package parser;

import preprocessor.ParseTable;

import java.io.*;
import java.util.Scanner;

public class Parser {
    Grammar grammar;
    ParseTable table;

    public Parser() throws IOException {
        grammar = getGrammar();
        table = getParseTable();

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
        String csv = new Scanner(tableFile).useDelimiter("\\Z").next();
        return new ParseTable(csv);
    }

}
