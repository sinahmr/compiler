package preprocessor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class TableCreator {
    private ArrayList<String> rules = new ArrayList<>();
    private HashSet<String> terminals = new HashSet<>();
    private HashSet<String> nonTerminals = new HashSet<>();
    private HashMap<String, HashSet<String>> firsts = new HashMap<>();
    private HashMap<String, HashSet<String>> follows = new HashMap<>();

    public static void main(String[] args) {
        new TableCreator().run();
    }

    private void readGrammarFile() {
        URL url = getClass().getResource("grammar.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(url.getPath()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String lhs = line.split("→")[0].trim();
                nonTerminals.add(lhs);

                String[] parts = line.split("→")[1].trim().split(" ");
                for (String part : parts)
                    if (!part.startsWith("$"))
                        terminals.add(part.trim());

                HashSet<String> first;
                if (!firsts.containsKey(lhs)) {
                    first = new HashSet<>();
                    firsts.put(lhs, first);
                }
                else
                    first = firsts.get(lhs);
                if (line.contains("ε"))
                    first.add("ε");
                if (!parts[0].startsWith("$"))
                    first.add(parts[0].trim());

                if (!follows.containsKey(lhs))
                    follows.put(lhs, new HashSet<>());
                if (lhs.equals("$Program"))
                    follows.get(lhs).add("EOF");

                rules.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashSet<String> firstOfString(String s) {
        HashSet<String> toReturn = new HashSet<>();
        String[] parts = s.trim().split(" ");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (!part.startsWith("$"))
                break;
            if (!firsts.get(part.trim()).contains("ε")) {
                toReturn.addAll(firsts.get(part.trim()));
                break;
            }
            // reaching here means NT's first contains ε
            for (String f : firsts.get(part.trim()))
                if (!f.equals("ε"))
                    toReturn.add(f);
            if (i == parts.length - 1)
                toReturn.add("ε");
        }
        return toReturn;

    }

    private void calculateFirsts() {
        HashMap<String, Integer> firstSetSize = new HashMap<>();
        boolean shouldContinue = true;
        while (shouldContinue) {
            shouldContinue = false;
            for (String rule : rules) {
                String lhs = rule.split("→")[0].trim();
                HashSet<String> toUnion = firstOfString(rule.split("→")[1].trim());
                firsts.get(lhs).addAll(toUnion);
                int currentSize = firsts.get(lhs).size();
                if (firstSetSize.getOrDefault(lhs, -1) != currentSize)
                    shouldContinue = true;
                firstSetSize.put(lhs, currentSize);
            }
        }
    }

    private void calculateFollows() {
        HashMap<String, Integer> followSetSize = new HashMap<>();
        boolean shouldContinue = true;
        while (shouldContinue) {
            shouldContinue = false;
            for (String rule : rules) {
                String lhs = rule.split("→")[0].trim();
                String[] parts = rule.split("→")[1].trim().split(" ");
                for (int i = 0; i < parts.length - 1; i++) {  // Ignore the last part
                    String part = parts[i];
                    if (part.startsWith("$")) {
                        String s = "";
                        for (int j = i + 1; j < parts.length; j++)
                            s += parts[j] + " ";
                        HashSet<String> toUnion = firstOfString(s.trim());
                        if (toUnion.contains("ε")) {
                            toUnion.remove("ε");
                            toUnion.addAll(follows.get(lhs));
                        }
                        follows.get(part.trim()).addAll(toUnion);
                        int currentSize = follows.get(part.trim()).size();
                        if (followSetSize.getOrDefault(part.trim(), -1) != currentSize)
                            shouldContinue = true;
                        followSetSize.put(part.trim(), currentSize);
                    }
                }
                String lastPart = parts[parts.length - 1].trim();
                if (lastPart.startsWith("$")) {  // Last part
                    follows.get(lastPart).addAll(follows.get(lhs));
                    int currentSize = follows.get(lastPart).size();
                    if (followSetSize.getOrDefault(lastPart, -1) != currentSize)
                        shouldContinue = true;
                    followSetSize.put(lastPart, currentSize);
                }
            }
        }
    }

    private void printFirstsOrFollows(HashMap<String, HashSet<String>> m) {
        for (Map.Entry<String, HashSet<String>> entry : m.entrySet()) {
            String nt = entry.getKey();
            HashSet<String> set = entry.getValue();
            System.out.print(nt + ": ");
            for (String s : set)
                System.out.print(s + ", ");
            System.out.println();
        }
    }

    private Table createTable() {
        Table table = new Table(terminals, nonTerminals);
        ArrayList<State> states = new ArrayList<>();
        State s0 = new State(new Item("$S → $Program", 0), rules);
        states.add(s0);

        for (int i = 0; i < states.size(); i++) {
            State state = states.get(i);
            for (String terminal : terminals) {  // Shift
                Item[] kernel = state.cloneItemsWithDotBeforeTNTAndMoveDot(terminal);
                if (kernel.length == 0)
                    continue;
                State newState = new State(kernel, rules);
                int newStateNum;
                if (!states.contains(newState)) {
                    newStateNum = states.size();
                    states.add(newState);
                } else
                    newStateNum = states.indexOf(newState);
                table.put("S" + newStateNum, i, terminal);
            }
            for (String nonTerminal : nonTerminals) {  // Goto
                Item[] kernel = state.cloneItemsWithDotBeforeTNTAndMoveDot(nonTerminal);
                if (kernel.length == 0)
                    continue;
                State newState = new State(kernel, rules);
                int newStateNum;
                if (!states.contains(newState)) {
                    newStateNum = states.size();
                    states.add(newState);
                } else
                    newStateNum = states.indexOf(newState);
                table.put("" + newStateNum, i, nonTerminal);
            }

           for (Item item : state.items) {  // Reduce
                if (item.lhs.equals("$S") && item.dotBefore == 1) {
                    table.put("Acc", i, "EOF");
                    continue;
                }
                if (item.rhs.length == item.dotBefore)
                    for (String follow : follows.get(item.lhs))
                        table.put("R" + item.ruleNumber, i, follow);
            }
        }
        return table;
    }

    private void run() {
        readGrammarFile();
        calculateFirsts();
        calculateFollows();

        Table table = createTable();
        System.out.println();

//        System.out.println("Follows:");
//        printFirstsOrFollows(follows);

    }
}

class Item {
    String lhs;
    String[] rhs;
    int dotBefore;
    int ruleNumber;

    public Item(String lhs, String[] rhs, int dotBefore, int ruleNumber) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.dotBefore = dotBefore;
        this.ruleNumber = ruleNumber;
    }

    public Item(String rule, int ruleNumber) {
        this.lhs = rule.split("→")[0].trim();
        this.rhs = rule.split("→")[1].trim().split(" ");
        if (this.rhs[0].equals("ε"))
            this.rhs = new String[0];
        this.dotBefore = 0;
        this.ruleNumber = ruleNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Item))
            return false;
        Item i = (Item) obj;
        if (!i.lhs.equals(this.lhs) || i.dotBefore != this.dotBefore || i.rhs.length != this.rhs.length)
            return false;
        for (int k = 0; k < i.rhs.length; k++)
            if (!i.rhs[k].equals(this.rhs[k]))
                return false;
        return true;
    }

    public Item clone() {
        return new Item(lhs, rhs, dotBefore, ruleNumber);
    }

}

class State {
    ArrayList<String> grammarRules;
    ArrayList<Item> items = new ArrayList<>();

    public State(Item[] kernel, ArrayList<String> grammarRules) {
        this.grammarRules = grammarRules;
        items.addAll(Arrays.asList(kernel));
        closure();
    }

    public State(Item kernel, ArrayList<String> grammarRules) {
        this.grammarRules = grammarRules;
        items.add(kernel);
        closure();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof State))
            return false;
        State s = (State) obj;
        for (Item item : s.items)
            if (!this.items.contains(item))
                return false;
        return true;
    }

    private void closure() {
        int i = 0;
        while (i < items.size()) {
            HashSet<String> shouldAddRulesOf = new HashSet<>();
            for (; i < items.size(); i++) {
                Item current = items.get(i);
                if (current.rhs.length == current.dotBefore)
                    continue;
                if (current.rhs[current.dotBefore].startsWith("$"))
                    shouldAddRulesOf.add(current.rhs[current.dotBefore]);
            }
            for (int j = 0; j < grammarRules.size(); j++) {
                String rule = grammarRules.get(j);
                String lhs = rule.split("→")[0].trim();
                if (shouldAddRulesOf.contains(lhs)) {
                    Item newItem = new Item(rule, j + 1);  // Number 0 is S → Program
                    if (!items.contains(newItem))
                        items.add(newItem);
                }
            }
        }
    }

    // TODO ghable hameye item.rhs[item.dotBefore] ha bayad check kard noghte akhar nabashe
    public Item[] cloneItemsWithDotBeforeTNTAndMoveDot(String tnt) {
        ArrayList<Item> toReturn = new ArrayList<>();
        for (Item item : items) {
            if (item.rhs.length == item.dotBefore)
                continue;
            if (item.rhs[item.dotBefore].equals(tnt)) {
                Item newItem = item.clone();
                newItem.dotBefore++;
                toReturn.add(newItem);
            }
        }
        return toReturn.toArray(new Item[toReturn.size()]);
    }
}

class Table {
    final int MAX_STATES = 200;
    HashSet<String> terminals;
    HashSet<String> nonTerminals;
    HashMap<String, Integer> tntToColumnNumber = new HashMap<>();
    String[][] table;

    public Table(HashSet<String> terminals, HashSet<String> nonTerminals) {
        this.terminals = terminals;
        this.nonTerminals = nonTerminals;
        table = new String[MAX_STATES][terminals.size() + nonTerminals.size()];
        int i = 0;
        for (String terminal : terminals)
            tntToColumnNumber.put(terminal, i++);
        for (String nonTerminal : nonTerminals)
            tntToColumnNumber.put(nonTerminal, i++);
    }

    public void put(String text, int state, String tnt) {
        int column = tntToColumnNumber.get(tnt);
        if (table[state][column] != null)
            table[state][column] += text;
        else
            table[state][column] = text;
    }


}
