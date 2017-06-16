package scanner;

import java.util.ArrayList;

public class SymbolTable
{
    public ArrayList<String> lexemes;
    public ArrayList<Token.Type> tokenTypes;

    public SymbolTable()
    {
        lexemes = new ArrayList<>();
        tokenTypes = new ArrayList<>();
    }

    public void addKeywords()
    {
        lexemes.add("int"); tokenTypes.add(Token.Type.INT);
        lexemes.add("void"); tokenTypes.add(Token.Type.VOID);
        lexemes.add("if"); tokenTypes.add(Token.Type.IF);
        lexemes.add("else"); tokenTypes.add(Token.Type.ELSE);
        lexemes.add("while"); tokenTypes.add(Token.Type.WHILE);
        lexemes.add("return"); tokenTypes.add(Token.Type.RETURN);
    }

    public Token.Type lookUp(String lex)
    {
        for(int i=0;i<lexemes.size();i++)
            if(lex.equals(lexemes.get(i)))
                return tokenTypes.get(i);
        return null;
    }

    public int find(String lex)
    {
        for(int i=0;i<lexemes.size();i++)
            if(lex.equals(lexemes.get(i)))
                return i;
        return -1;
    }
    public void insert(String lex, Token.Type type) {
        lexemes.add(lex);
        tokenTypes.add(type);
    }

    public void insert(String lex) {
        insert(lex, Token.Type.ID);
    }
}
