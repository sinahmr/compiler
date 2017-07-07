package scanner;

public class Token
{
    public enum Type {ID, NUM, INT, VOID, IF, ELSE, WHILE, RETURN,
        SEMICOLON, BRACKET_O, BRACKET_C, PARAN_O, PARAN_C, COMMA, ACCOlADE_O, ACCOLADE_C,
        ASSIGN, AND, EQUAL, SMALLER, PLUS, MINUS, MULT, DIVIDE, EOF, OUTPUT}
    //all keywords and IDs are identical up to this point, change tokenType by using the keyword symbol table
    public Type type;
    public int attribute;
    public int lineNumber;
    public int offsetInLine;
    public Token(SymbolTable keywordTable, SymbolTable IDTable, Type type, int line, int inLine, byte[] name1, byte[] name2)
    {
        byte[] result = new byte[name1.length+name2.length];
        for(int i=0;i<name1.length;i++)
            result[i] = name1[i];
        for(int i=0;i<name2.length;i++)
            result[i+name1.length] = name2[i];
        make(keywordTable, IDTable, type, line, inLine, result);
    }

    public Token(SymbolTable keywordTable, SymbolTable IDTable, Type type, int line, int inLine, byte[] name)
    {
        make(keywordTable, IDTable, type, line, inLine, name);
    }

    private void make(SymbolTable keywordTable, SymbolTable IDTable, Type tokenType, int line, int inLine, byte[] name)
    {
        try {
            String str = new String(name, "UTF-8");
            if (tokenType == Type.ID) {
                if (keywordTable.lookUp(str) != null)
                    type = keywordTable.lookUp(str);
                else if (IDTable.lookUp(str) != null) {
                    type = Type.ID;
                    attribute = IDTable.find(str);
                } else {
                    IDTable.insert(str);
                    type = Type.ID;
                    attribute = IDTable.find(str);
                }
            } else if (tokenType == Type.NUM) {
                type = tokenType;
                attribute = new Integer(str);
            } else
                type = tokenType;

            lineNumber = line;
            offsetInLine = inLine;

        }catch(Exception e)
        {
            System.out.println(e.getMessage()); // TODO inja error ma'ni dar khuruji bedim?
        }
//        System.out.println("Token: " + type + " " + attribute);
    }

    @Override
    public String toString() {  // This is written so that scanner and parser are distinct modules and parse table column index is not dependant on scanner's token to integer mapping
        switch (this.type) {
            case ID: return "ID";
            case IF: return "if";
            case AND: return "&&";
            case EOF: return "EOF";
            case INT: return "int";
            case NUM: return "NUM";
            case ELSE: return "else";
            case MULT: return "*";
            case PLUS: return "+";
            case VOID: return "void";
            case COMMA: return ",";
            case EQUAL: return "==";
            case MINUS: return "-";
            case WHILE: return "while";
            case ASSIGN: return "=";
            case DIVIDE: return "/";
            case RETURN: return "return";
            case PARAN_C: return ")";
            case PARAN_O: return "(";
            case SMALLER: return "<";
            case BRACKET_C: return "]";
            case BRACKET_O: return "[";
            case SEMICOLON: return ";";
            case ACCOLADE_C: return "}";
            case ACCOlADE_O: return "{";
			case OUTPUT: return "output";
        }
        return null;
    }

}
