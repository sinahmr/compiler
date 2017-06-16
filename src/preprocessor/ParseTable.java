package preprocessor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class ParseTable {
    private static final int MAX_STATES = 200;
    public HashMap<String, Integer> tntToColumnNumber = new HashMap<>();
    public String[][] table;
    public int statesCount = 0;

    public ParseTable(String csv) {
        String[] lines = csv.split("\n");
        String[] header = lines[0].split("\t");
        for (int i = 1; i < header.length; i++)
            tntToColumnNumber.put(header[i], i - 1);
        statesCount = lines.length - 1;
        table = new String[statesCount][header.length - 1];
        for (int i = 1; i < lines.length; i++) {
            String[] values = lines[i].split("\t");
            for (int j = 1; j < values.length; j++)
                table[i - 1][j - 1] = values[j];
        }
    }

    public ParseTable(HashSet<String> terminals, HashSet<String> nonTerminals) {
        String[] ts = terminals.toArray(new String[terminals.size()]);
        String[] nts = nonTerminals.toArray(new String[nonTerminals.size()]);
        Arrays.sort(ts);
        Arrays.sort(nts);
        table = new String[MAX_STATES][ts.length + nts.length];
        int i = 0;
        for (String terminal : ts)
            tntToColumnNumber.put(terminal, i++);
        for (String nonTerminal : nts)
            tntToColumnNumber.put(nonTerminal, i++);
    }

    public void put(String text, int state, String tnt) {
        int column = tntToColumnNumber.get(tnt);
        if (table[state][column] != null)
            table[state][column] += "/" + text;
        else
            table[state][column] = text;
        if (state + 1 > statesCount)
            statesCount = state + 1;
    }
}
