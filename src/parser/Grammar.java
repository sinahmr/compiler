package parser;

import java.util.ArrayList;

public class Grammar {
    ArrayList<Rule> rules = new ArrayList<>();

    public Grammar() {
        Rule dummyRule = new Rule(null, null, 0);
        rules.add(dummyRule);
    }

    public void addRule(String rule) {
        Rule r = new Rule(rule, rules.size());
        rules.add(r);
    }
}

class Rule {
    String lhs;
    String[] rhs;
    int ruleNumber;

    public Rule(String lhs, String[] rhs, int ruleNumber) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.ruleNumber = ruleNumber;
    }

    public Rule(String rule, int ruleNumber) {
        String lhs = rule.split("~")[0].trim();
        String[] parts = rule.split("~")[1].trim().split(" ");
        this.lhs = lhs;
        if (rule.contains("!"))
            this.rhs = new String[0];
        else
            this.rhs = parts;
        this.ruleNumber = ruleNumber;
    }
}