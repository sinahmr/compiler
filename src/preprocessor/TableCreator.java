package preprocessor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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

    private void run() {
        readGrammarFile();
        calculateFirsts();
        calculateFollows();

        System.out.println("Firsts:");
        printFirstsOrFollows(firsts);
        System.out.println("\n---\n");
        System.out.println("Follows:");
        printFirstsOrFollows(follows);

    }
}
