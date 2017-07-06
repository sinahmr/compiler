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
    public ArrayList<Integer> args_size;
    public ArrayList<Integer> addressOffset;

    public ArrayList<Integer> scopeStack;
    public ArrayList<Integer> currOffset;

    private int currAddress;
    private ErrorHandler errorHandler;

    private int lastDefinedFunc;
    private int lastDefinedArray;
//    private RetType lastRetType; TODO delete

    enum IDType {FUNC, VAR, ARRAY};
    public enum RetType {VOID, INT};

    boolean startedFuncScope=false;

    public SymbolTable(int initAddress, ErrorHandler errorHandler)
    {
        lexemes = new ArrayList<>();
        tokenTypes = new ArrayList<>();
        addresses = new ArrayList<>();
        args_size = new ArrayList<>();
        IDTypes = new ArrayList<>();
        retTypes = new ArrayList<>();
        addressOffset = new ArrayList<>();
        this.currAddress = initAddress;
        this.errorHandler = errorHandler;

        scopeStack = new ArrayList<>();
        currOffset = new ArrayList<>();
        scopeStack.add(0);
        currOffset.add(currAddress);
    }

    public void startScope()
    {
        if(startedFuncScope)
        {
            startedFuncScope = false;
            return;
        }
        scopeStack.add(lexemes.size());
        currOffset.add(currAddress);
    }

    public void startScopeFunc()
    {
        startScope();
        startedFuncScope = true;
    }

    public void endScope()
    {
        int scp = scopeStack.remove(scopeStack.size()-1);
        while(lexemes.size()>scp)
        {
            lexemes.remove(lexemes.size()-1);
            tokenTypes.remove(tokenTypes.size()-1);
            addresses.remove(addresses.size()-1);
            args_size.remove(args_size.size()-1);
            addressOffset.remove(addressOffset.size()-1);
            IDTypes.remove(IDTypes.size()-1);
            retTypes.remove(retTypes.size()-1);
        }
        currAddress = currOffset.remove(currOffset.size() - 1);
    }

//    public void setRetType(RetType retType) // farz kardam ke ghabl az farakhani defineFunc in tabe' seda mishe
//    {
//        lastRetType = retType;
//    } TODO delete

    public void defineFunc(int index, int address, Token.Type returnType) // the former input was the name of the array and the address
    {
        /*int index = find(func);
        if(index < 0)
        {
            errorHandler.semanticError("Function defined before name declaration"); // hesam ine ke in khata hichvaght nabayad ettefagh biofte!! magar moghe'i ke code compiler eshkal dashte bashe
            insert(func);
            index = lexemes.size()-1;
        }*/
        if(IDTypes.get(index) != null)
        {
            errorHandler.semanticError("ID already defined"); // mishe be payam error in ro ezafe kard ke bege ghablan be onvan func ta'rif shode ya var ya array
        }
//        if(lastRetType == null)
//        {
//            errorHandler.semanticError("Return Type not specified"); // hesam ine ke in khata hichvaght nabayad ettefagh biofte!! magar moghe'i ke code compiler eshkal dashte bashe
//            lastRetType = RetType.VOID; // default ro void dar nazar gereftam
//        }  TODO delete

        addresses.set(index, address);
        args_size.set(index, 0);
        addressOffset.set(index, currAddress);
        IDTypes.set(index, IDType.FUNC);
        if (returnType == Token.Type.INT)
            retTypes.set(index, RetType.INT);
        else
            retTypes.set(index, RetType.VOID);
//        lastRetType = null; TODO delete

        lastDefinedFunc = index;
    }

    public void addFuncParam() //farz kardam ke adade tabe' ro az farakhani defineFunc hefz mikone, in moshkeli be vujud miare?
    {
        args_size.set(lastDefinedFunc, args_size.get(lastDefinedFunc)+1);
    }

    public void defineVar(int index) // the former input was the name of the var
    {
        /*int index = find(var);
        if(index < 0)
        {
            errorHandler.semanticError("Var defined before name declaration"); // hesam ine ke in khata hichvaght nabayad ettefagh biofte!! magar moghe'i ke code compiler eshkal dashte bashe
            insert(var);
            index = lexemes.size()-1;
        }*/


        if(IDTypes.get(index) != null)
        {
            errorHandler.semanticError("ID already defined"); // mishe be payam error in ro ezafe kard ke bege ghablan be onvan func ta'rif shode ya var ya array
        }

        addresses.set(index, currAddress);
        currAddress += 4; // chon har moteghayyer 4 byte hafeze mikhad
        IDTypes.set(index, IDType.VAR);
    }


    public void defineArray(int index) // the former input was the name of the array
    {
        /*int index = find(array);
        if(index < 0)
        {
            errorHandler.semanticError("Var defined before name declaration"); // hesam ine ke in khata hichvaght nabayad ettefagh biofte!! magar moghe'i ke code compiler eshkal dashte bashe
            insert(array);
            index = lexemes.size()-1;
        }*/
        if(IDTypes.get(index) != null)
        {
            errorHandler.semanticError("ID already defined"); // mishe be payam error in ro ezafe kard ke bege ghablan be onvan func ta'rif shode ya var ya array
        }

        addresses.set(index, currAddress);
        currAddress += 4;
        IDTypes.set(index, IDType.ARRAY);

        lastDefinedArray = index;
    }

    public void setArraySize(int size) //farz kardam in tabe' ba'd az defineArray farakhani mishe va in ke akharin array ta'rif shode ro hefz mikone
    {
        args_size.set(lastDefinedArray, size);
        currAddress += 4*size; // ino ba'dan ezafe kardam, motmaen nistam
    }

    public int getAddress(int index) // the former input was the name of the array
    {
        /*int index = find(ID);
        if(index < 0)
        {
            errorHandler.semanticError("Address demanded before name declaration"); // hesam ine ke in khata hichvaght nabayad ettefagh biofte!! magar moghe'i ke code compiler eshkal dashte bashe
            insert(ID);
            index = lexemes.size()-1;
        }*/
        if(IDTypes.get(index) == null)
        {
            errorHandler.semanticError("ID not defined yet");
            defineVar(index);
        }

        if(IDTypes.get(index) == IDType.FUNC)
        {
            return addresses.get(index);
        }
        else if(IDTypes.get(index) == IDType.VAR)
        {
            int i;
            /*for (i = scopeStack.size() - 1; i >= 0; i--)
            {
                if (scopeStack.get(i) <= index)
                    return addresses.get(index) - currOffset.get(i);
            }*/
            return addresses.get(index);
        }else if(IDTypes.get(index) == IDType.ARRAY) // address array ro gereftan bayad kamelan moshabeh var bashe dg, na?!
        {
            int i;
            /*for (i = scopeStack.size() - 1; i >= 0; i--)
            {
                if (scopeStack.get(i) <= index)
                    //return addresses.get(index) - currOffset.get(i);
                    return addresses.get(index);
            }*/
            return addresses.get(index);
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
        lexemes.add("int"); tokenTypes.add(Token.Type.INT); addresses.add(-1); args_size.add(-1); IDTypes.add(null); retTypes.add(null); addressOffset.add(-1);
        lexemes.add("void"); tokenTypes.add(Token.Type.VOID); addresses.add(-1); args_size.add(-1); IDTypes.add(null); retTypes.add(null); addressOffset.add(-1);
        lexemes.add("if"); tokenTypes.add(Token.Type.IF); addresses.add(-1); args_size.add(-1); IDTypes.add(null); retTypes.add(null); addressOffset.add(-1);
        lexemes.add("else"); tokenTypes.add(Token.Type.ELSE); addresses.add(-1); args_size.add(-1); IDTypes.add(null); retTypes.add(null); addressOffset.add(-1);
        lexemes.add("while"); tokenTypes.add(Token.Type.WHILE); addresses.add(-1); args_size.add(-1); IDTypes.add(null); retTypes.add(null); addressOffset.add(-1);
        lexemes.add("return"); tokenTypes.add(Token.Type.RETURN); addresses.add(-1); args_size.add(-1); IDTypes.add(null); retTypes.add(null); addressOffset.add(-1);
		lexemes.add("output"); tokenTypes.add(Token.Type.OUTPUT); addresses.add(-1); args_size.add(-1); IDTypes.add(null); retTypes.add(null); addressOffset.add(-1);
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
        args_size.add(-1);
        addressOffset.add(-1);
        IDTypes.add(null);
        retTypes.add(null);
    }

    public int localParamLength()
    {
        int cnt=0;
        for(int i=scopeStack.get(scopeStack.size()-1); i<lexemes.size(); i++)
            if(args_size.get(i) < 0)
                cnt++;
            else
                cnt+= args_size.get(i);
        return cnt;
    }

    public void insert(String lex) {
        insert(lex, Token.Type.ID);
    }

    public int getFuncParamLength()
    {
        return args_size.get(lastDefinedFunc);
    }

    public int getArraySize(int index)
    {
        if(IDTypes.get(index) != IDType.ARRAY)
        {
            errorHandler.semanticError("ID is not an array");
            return -1;
        }
        return args_size.get(index);
    }

    public int getFuncAddressOffset(int index)
    {
        if(IDTypes.get(index) != IDType.FUNC)
        {
            errorHandler.semanticError("ID is not an function");
            return -1;
        }
        return addressOffset.get(index);
    }

    public void print()
    {
        for(int i=0; i<lexemes.size(); i++)
            System.out.println(lexemes.get(i) + " $ " + addresses.get(i));
    }
    public void printFull()
    {
        for(int i=0; i<lexemes.size(); i++)
            System.out.println(lexemes.get(i) + " $ " + addresses.get(i));
    }
}
