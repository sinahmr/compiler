package scanner;

public class Token
{
    public enum Type {ID, NUM, INT, VOID, IF, ELSE, WHILE, RETURN,
        SEMICOLON, BRACKET_O, BRACKET_C, PARAN_O, PARAN_C, COLON, ACCOlADE_O, ACCOLADE_C, SLASH,
        ASSIGN, AND, EQUAL, SMALLER, PLUS, MINUS, MULT, DIVIDE};
    //all keywords and IDs are identical up to this point, change tokenType by using the keyword symbol table
    public Type type;
    public int attribute;
    public Token(SymbolTable keywordTable, SymbolTable IDTable, Type type, byte[] name1, byte[] name2) throws Exception
    {
        byte[] result = new byte[name1.length+name2.length];
        for(int i=0;i<name1.length;i++)
            result[i] = name1[i];
        for(int i=0;i<name2.length;i++)
            result[i+name1.length] = name2[i];
        make(keywordTable, IDTable, type, result);
    }

    public Token(SymbolTable keywordTable, SymbolTable IDTable, Type type, byte[] name) throws Exception
    {
        make(keywordTable, IDTable, type, name);
    }

    private void make(SymbolTable keywordTable, SymbolTable IDTable, Type tokenType, byte[] name)throws Exception
    {
        String str = new String(name, "UTF-8");
        if(tokenType == Type.ID)
        {
            if(keywordTable.lookUp(str) != null)
                type = keywordTable.lookUp(str);
            else if(IDTable.lookUp(str) != null)
            {
                type = Type.ID;
                attribute = IDTable.find(str);
            }else
            {
                IDTable.insert(str);
                type = Type.ID;
                attribute = IDTable.find(str);
            }
        }else if(tokenType == Type.NUM)
        {
            type = tokenType;
            attribute = new Integer(str);
        }else
            type = tokenType;
    }
}
