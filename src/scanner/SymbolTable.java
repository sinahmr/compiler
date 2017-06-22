package scanner;

import errorhandler.ErrorHandler;

import java.util.ArrayList;

public class SymbolTable
{
    public ArrayList<String> lexemes;
    public ArrayList<Token.Type> tokenTypes;
    public ArrayList<IDType> IDTypes;
    public ArrayList<RetType> retTypes;
    public ArrayList<Integer> addresses;
    public ArrayList<Integer> args;

    public ArrayList<Integer> scopeStack;
    public ArrayList<Integer> currOffset;

    private int currAddress;
    private ErrorHandler errorHandler;

    private int lastDefinedFunc;
    private RetType lastRetType;

    enum IDType {FUNC, VAR};
    enum RetType {VOID, INT};

    public SymbolTable(int initAddress, ErrorHandler errorHandler)
    {
        lexemes = new ArrayList<>();
        tokenTypes = new ArrayList<>();
        addresses = new ArrayList<>();
        args = new ArrayList<>();
        IDTypes = new ArrayList<>();
        retTypes = new ArrayList<>();
        this.currAddress = initAddress;
        this.errorHandler = errorHandler;

        scopeStack = new ArrayList<>();
        currOffset = new ArrayList<>();
        scopeStack.add(0);
        currOffset.add(currAddress);
    }

    public void startScope()
    {
        scopeStack.add(lexemes.size());
        currOffset.add(currAddress);
    }

    public void endScope()
    {
        int scp = scopeStack.remove(scopeStack.size()-1);
        while(lexemes.size()>scp)
        {
            lexemes.remove(lexemes.size()-1);
            tokenTypes.remove(tokenTypes.size()-1);
            addresses.remove(addresses.size()-1);
            args.remove(args.size()-1);
            IDTypes.remove(IDTypes.size()-1);
            retTypes.remove(retTypes.size()-1);
        }
        currAddress = currOffset.remove(currOffset.size() - 1);
    }

    public void setRetType(RetType retType) // farz kardam ke ghabl az farakhani defineFunc in tabe' seda mishe
    {
        lastRetType = retType;
    }

    public void defineFunc(String func, int address)
    {
        int index = find(func);
        if(index < 0)
        {
            errorHandler.semanticError("Function defined before name declaration"); // hesam ine ke in khata hichvaght nabayad ettefagh biofte!! magar moghe'i ke code compiler eshkal dashte bashe
            insert(func);
            index = lexemes.size()-1;
        }
        if(IDTypes.get(index) != null)
        {
            errorHandler.semanticError("ID already defined"); // mishe be payam error in ro ezafe kard ke bege ghablan be onvan func ta'rif shode ya var
        }
        if(lastRetType == null)
        {
            errorHandler.semanticError("Return Type not specified"); // hesam ine ke in khata hichvaght nabayad ettefagh biofte!! magar moghe'i ke code compiler eshkal dashte bashe
            lastRetType = RetType.VOID; // default ro void dar nazar gereftam
        }

        addresses.set(index, address);
        args.set(index, 0);
        IDTypes.set(index, IDType.FUNC);
        retTypes.set(index, lastRetType);
        lastRetType = null;

        lastDefinedFunc = index;
    }

    public void addFuncParam() //farz kardam ke adade tabe' ro az farakhani defineFunc hefz mikone, in moshkeli be vujud miare?
    {
        args.set(lastDefinedFunc, args.get(lastDefinedFunc)+1);
    }

    public void defineVar(String var)
    {
        int index = find(var);
        if(index < 0)
        {
            errorHandler.semanticError("Var defined before name declaration"); // hesam ine ke in khata hichvaght nabayad ettefagh biofte!! magar moghe'i ke code compiler eshkal dashte bashe
            insert(var);
            index = lexemes.size()-1;
        }
        if(IDTypes.get(index) != null)
        {
            errorHandler.semanticError("ID already defined"); // mishe be payam error in ro ezafe kard ke bege ghablan be onvan func ta'rif shode ya var
        }

        addresses.set(index, currAddress);
        currAddress += 4; // chon har moteghayyer 4 byte hafeze mikhad
        IDTypes.set(index, IDType.VAR);

    }

    public int getAddress(String ID)
    {
        int index = find(ID);
        if(index < 0)
        {
            errorHandler.semanticError("Address demanded before name declaration"); // hesam ine ke in khata hichvaght nabayad ettefagh biofte!! magar moghe'i ke code compiler eshkal dashte bashe
            insert(ID);
            index = lexemes.size()-1;
        }
        if(IDTypes.get(index) == null)
        {
            errorHandler.semanticError("ID not defined yet");
            defineVar(ID);
        }

        if(IDTypes.get(index) == IDType.FUNC)
        {
            return addresses.get(index);
        }
        else if(IDTypes.get(index) == IDType.VAR)
        {
            int i;
            for (i = scopeStack.size() - 1; i >= 0; i--)
            {
                if (scopeStack.get(i) <= index)
                    return addresses.get(index) - currOffset.get(i);
            }
        }
        return -1;
    }

    public int getScopeLevel(String ID) // farz bar ine ke getAddress ghabl az in seda zade mishe, baraye hamine ke halat haye khata tu un barresi shodan
    {
        int index = find(ID);
        int i;
        for(i=scopeStack.size()-1; i>=0; i--)
        {
            if(scopeStack.get(i) <= index)
                return scopeStack.size()-1-i;
        }
    return -1;
    }

    public void addKeywords()
    {
        lexemes.add("int"); tokenTypes.add(Token.Type.INT); addresses.add(-1); args.add(-1); IDTypes.add(null); retTypes.add(null);
        lexemes.add("void"); tokenTypes.add(Token.Type.VOID); addresses.add(-1); args.add(-1); IDTypes.add(null); retTypes.add(null);
        lexemes.add("if"); tokenTypes.add(Token.Type.IF); addresses.add(-1); args.add(-1); IDTypes.add(null); retTypes.add(null);
        lexemes.add("else"); tokenTypes.add(Token.Type.ELSE); addresses.add(-1); args.add(-1); IDTypes.add(null); retTypes.add(null);
        lexemes.add("while"); tokenTypes.add(Token.Type.WHILE); addresses.add(-1); args.add(-1); IDTypes.add(null); retTypes.add(null);
        lexemes.add("return"); tokenTypes.add(Token.Type.RETURN); addresses.add(-1); args.add(-1); IDTypes.add(null); retTypes.add(null);
    }

    public Token.Type lookUp(String lex)
    {
        int index = find(lex);
        if(index >= 0)
            return tokenTypes.get(index);
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
        addresses.add(-1);
        args.add(-1);
        IDTypes.add(null);
        retTypes.add(null);
    }

    public void insert(String lex) {
        insert(lex, Token.Type.ID);
    }
}
