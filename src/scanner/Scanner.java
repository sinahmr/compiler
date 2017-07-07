package scanner;

// DELETE THIS COMMENT
// WILL DO SIR

import errorhandler.ErrorHandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Scanner
{
    public final int bufferLength=1000; //TODO
    public final int symbolTableInitAddress = 1008;

    public int currentToken;
    public int lexemeBeginning;

    public boolean loadedFirstHalf=false;

    public Token.Type lastTokenType;

    public ErrorHandler errorHandler;
    private DFA dfa;
    private SymbolTable keywordTable;
    private SymbolTable IDTable;

    private FileInputStream code;
    public byte[] buffer;

    public int currentLine;
    public int currentInLine;

    public Scanner(FileInputStream code, ErrorHandler errorHandler)
    {
        this.code = code;
        keywordTable = new SymbolTable(symbolTableInitAddress, errorHandler);
        keywordTable.addKeywords();
        IDTable = new SymbolTable(symbolTableInitAddress, errorHandler);
        this.errorHandler = errorHandler;
        currentToken = 0;
        lexemeBeginning = 0;
        buffer = new byte[bufferLength*2];
        dfa = new DFA(this, 30);

        currentLine = 1;
        currentInLine = 1;
    }

    public Token getNextToken()
    {

        lexemeBeginning = currentToken;
        lastTokenType = dfa.run();
        if(currentToken > lexemeBeginning)
            return new Token(keywordTable, IDTable, lastTokenType, currentLine, currentInLine, Arrays.copyOfRange(buffer, lexemeBeginning, currentToken));
        else
            return new Token(keywordTable, IDTable, lastTokenType, currentLine, currentInLine, Arrays.copyOfRange(buffer, lexemeBeginning, 2*bufferLength) , Arrays.copyOfRange(buffer, 0, currentToken));
    }

    public void loadBuffer()
    {

        try{
        if(loadedFirstHalf)
        {
            int amount = code.read(buffer, bufferLength, bufferLength);
            if(amount < 0)
                amount = 0;
            for(int i=bufferLength+amount;i<bufferLength*2;i++)
                    buffer[i] = 0;
        }
        else
        {
            int amount = code.read(buffer, 0, bufferLength);
            if(amount < 0)
                amount = 0;
            for(int i=0+amount;i<bufferLength;i++)
                buffer[i] = 0;
        }
        loadedFirstHalf = !loadedFirstHalf;
        }catch(Exception e)
        {
            errorHandler.scannerError("Buffer error");
        }
    }

    public SymbolTable getSymbolTable() {
        return this.IDTable;
    }

}

class DFA
{
    private int size;
    private Scanner scn;
    private String[] errors; //contains specific and default errors
    private HashMap< TupleIB, Integer> tranValid;
    private HashMap< TupleIB, Integer> tranError;
    private int[] tranDef; //pos default transition, neg default error (*(-1) to find error index)
    private HashMap< Integer, Token.Type> finalStates;
    private ArrayList<Integer> undoFinalStates;
    private ArrayList<Integer> conFinalStates;
    private ArrayList<Token.Type[]> finalConditions;

    private byte[][] groups;

    private int currState;

    enum Group {LETTER, NUM, DELIM}

    public DFA(Scanner scanner, int size)
    {
        this.size = size;
        this.scn = scanner;


        groups = new byte[3][];
        groups[Group.NUM.ordinal()] = new byte[]{'0','1','2','3','4','5','6','7','8','9'};
        groups[Group.LETTER.ordinal()] = new byte[]{'a','b','c','d','e','f','g','h','i','j','k','l','m',
                                'n','o','p','q','r','s','t','u','v','w','x','y','z',
                'A','B','C','D','E','F','G','H','I','J','K','L','M',
                'N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
        groups[Group.DELIM.ordinal()] = new byte[]{'\r', '\n', ' ', '\t'};

        setErrors();
        setTrans();
        setFinals();

        currState = 0;
    }


    public Token.Type run()
    {
        currState = 0;
        boolean toEnd=false;
        while(true)
        {
            scn.currentToken = scn.currentToken % (2*scn.bufferLength);

            if((scn.currentToken < scn.bufferLength && !scn.loadedFirstHalf) || (scn.currentToken >= scn.bufferLength && scn.loadedFirstHalf))
                scn.loadBuffer();

            if(currState == 0)
                scn.lexemeBeginning = scn.currentToken;

            if(scn.buffer[scn.currentToken] == 0)
                toEnd = true;


            scn.currentInLine++;
            if(scn.buffer[scn.currentToken] == 10)
            {
                scn.currentLine++;
                scn.currentInLine = 1;
            }


            TupleIB next = new TupleIB(currState, scn.buffer[scn.currentToken]);

            if(tranValid.containsKey(next))
            {
                scn.currentToken++;
                currState = tranValid.get(next);
            }
            else if(tranError.containsKey(next))
            {
                scn.currentToken++;
                scn.errorHandler.scannerError(errors[tranError.get(next)]);
                currState = 0;
            }else
            {
                if(tranDef[currState] >= 0)
                {
                    scn.currentToken++;
                    currState = tranDef[currState];
                }else
                {
                    scn.currentToken++;
                    if(currState != 0 || scn.buffer[scn.currentToken] != 0)// check shavad
                        scn.errorHandler.scannerError(errors[-tranDef[currState]]);


                    currState = 0;
                }
            }

            if(finalStates.containsKey(currState))
            {
                boolean doReturn = false;
                if(conFinalStates.contains(currState))
                {
                    int t = conFinalStates.indexOf(currState);
                    for(int i=0;i<finalConditions.get(t).length;i++)
                        if(scn.lastTokenType == finalConditions.get(t)[i])
                            doReturn = true;
                }
                else
                    doReturn = true;

                if(doReturn)
                {
                    if (undoFinalStates.contains(currState))
                    {
                        scn.currentToken--;
                        scn.currentInLine--;
                        if(scn.buffer[scn.currentToken] == 10)
                        {
                            scn.currentLine--;
                            scn.currentInLine=1;
                        }
                    }
                    return finalStates.get(currState);
                }
            }

            if(toEnd)
                return Token.Type.EOF;

        }
    }

    private void setErrors()
    {
        errors = new String[4]; //index 0 was used for EOF error and is currently unassigned
        errors[1] = "Invalid character to begin with";
        errors[2] = "Invalid character after &";
        errors[3] = "A number is instantly followed by a letter";
    }

    private void setTrans()
    {
        tranValid = new HashMap<>();
        tranError = new HashMap<>();
        tranDef = new int[size];

        tranValid.put(new TupleIB(0,(byte)';'), 1);
        tranValid.put(new TupleIB(0,(byte)'['), 2);
        tranValid.put(new TupleIB(0,(byte)']'), 3);
        tranValid.put(new TupleIB(0,(byte)'('), 4);
        tranValid.put(new TupleIB(0,(byte)')'), 5);
        tranValid.put(new TupleIB(0,(byte)','), 6);
        tranValid.put(new TupleIB(0,(byte)'{'), 7);
        tranValid.put(new TupleIB(0,(byte)'}'), 8);
        tranValid.put(new TupleIB(0,(byte)'<'), 9);
        tranValid.put(new TupleIB(0, (byte) '*'), 10);

        tranValid.put(new TupleIB(0, (byte) '='), 11);

        putGroup(tranValid, 0, groups[Group.LETTER.ordinal()], 28);
        putGroup(tranValid, 0, groups[Group.DELIM.ordinal()], 0);
        tranValid.put(new TupleIB(0, (byte) '&'), 14);
        putGroup(tranValid, 0, groups[Group.NUM.ordinal()], 24);
        tranValid.put(new TupleIB(0, (byte) '+'), 20);
        tranValid.put(new TupleIB(0, (byte) '-'), 21);
        tranValid.put(new TupleIB(0, (byte) '/'), 16);
        tranDef[0] = -1;

        tranValid.put(new TupleIB(11, (byte)'='), 12);
        tranDef[11] = 13;

        tranValid.put(new TupleIB(14, (byte)'&'), 15);
        tranDef[14] = -2;

        tranValid.put(new TupleIB(16, (byte)'*'), 18);
        tranDef[16] = 17;

        tranValid.put(new TupleIB(18, (byte)'*'), 19);
        tranDef[18] = 18;

        tranValid.put(new TupleIB(19, (byte)'/'), 0);
        tranDef[19] = 18;

        putGroup(tranValid, 20, groups[Group.NUM.ordinal()], 24);
        tranDef[20] = 22;

        putGroup(tranValid, 21, groups[Group.NUM.ordinal()], 25);
        tranDef[21] = 23;

        putGroup(tranValid, 24, groups[Group.NUM.ordinal()], 24);
        putGroup(tranError, 24, groups[Group.LETTER.ordinal()], 3);
        tranDef[24] = 26;

        putGroup(tranValid, 25, groups[Group.NUM.ordinal()], 25);
        putGroup(tranError, 25, groups[Group.LETTER.ordinal()], 3);
        tranDef[25] = 27;

        putGroup(tranValid, 28, groups[Group.NUM.ordinal()], 28);
        putGroup(tranValid, 28, groups[Group.LETTER.ordinal()], 28);
        tranDef[28] = 29;
    }

    private void setFinals()
    {
        finalStates = new HashMap<>();
        undoFinalStates = new ArrayList<>();
        conFinalStates = new ArrayList<>();
        finalConditions = new ArrayList<>();

        finalStates.put(1 , Token.Type.SEMICOLON);
        finalStates.put(2 , Token.Type.BRACKET_O);
        finalStates.put(3 , Token.Type.BRACKET_C);
        finalStates.put(4 , Token.Type.PARAN_O);
        finalStates.put(5 , Token.Type.PARAN_C);
        finalStates.put(6 , Token.Type.COMMA);
        finalStates.put(7 , Token.Type.ACCOlADE_O);
        finalStates.put(8 , Token.Type.ACCOLADE_C);
        finalStates.put(9 , Token.Type.SMALLER);
        finalStates.put(10, Token.Type.MULT);

        finalStates.put(12, Token.Type.EQUAL);
        finalStates.put(13, Token.Type.ASSIGN);
        undoFinalStates.add(13);
        finalStates.put(15, Token.Type.AND);
        finalStates.put(17, Token.Type.DIVIDE);
        undoFinalStates.add(17);
        finalStates.put(17, Token.Type.DIVIDE);

        finalStates.put(20, Token.Type.PLUS);
        conFinalStates.add(20);
        finalConditions.add(new Token.Type[]{Token.Type.NUM, Token.Type.PARAN_C, Token.Type.BRACKET_C, Token.Type.ID});
        finalStates.put(21, Token.Type.MINUS);
        conFinalStates.add(21);
        finalConditions.add(new Token.Type[]{Token.Type.NUM, Token.Type.PARAN_C, Token.Type.BRACKET_C, Token.Type.ID});

        finalStates.put(22, Token.Type.PLUS);
        undoFinalStates.add(22);
        finalStates.put(23, Token.Type.MINUS);
        undoFinalStates.add(23);
        finalStates.put(26, Token.Type.NUM);
        undoFinalStates.add(26);
        finalStates.put(27, Token.Type.NUM);
        undoFinalStates.add(27);
        finalStates.put(29, Token.Type.ID);
        undoFinalStates.add(29);
    }

    private void putGroup(HashMap<TupleIB, Integer> map, int src, byte[] group, int dest)
    {
        for(int i=0;i<group.length;i++)
            map.put(new TupleIB(src, group[i]), dest);
    }

}

class TupleIB
{
    public Integer i;
    public Byte b;
    public TupleIB(Integer i, byte b)
    {
        this.i = i;
        this.b = b;
    }

    @Override
    public int hashCode() {
        return i.hashCode()*b.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(!(obj instanceof TupleIB))
            return false;
        TupleIB other = (TupleIB) obj;
        if(other.i.equals(i) && other.b.equals(b))
            return true;
        else
            return false;

    }
}
