package codegenerator;

import scanner.SymbolTable;
import scanner.Token;

import java.util.ArrayList;
import java.util.Stack;

public class CodeGenerator {

    ArrayList<String> tempBuffAction = new ArrayList<>();
    ArrayList<Token> tempBuffToken = new ArrayList<>();
    ArrayList<Token[]> tempBuffPrev = new ArrayList<>();

    final int CODE_SIZE = 1000;
    final int CONTROL_REGISTERS_SIZE = 4 + 4;
    final int VARS_BLOCK_SIZE = 1500 - 1008;
    final int TEMP_SIZE = 500;

    int tempPointer=0;
    int p = 0;

    int lastPrinted = 0;

    SymbolTable symbolTable;
    Stack<Integer> semanticStack;
    InterCode[] PB;

    enum  CodeType {ADD, AND, ASSIGN, EQ, JPF, JP, LT, MULT, NOT, PRINT, SUB, DIVIDE}
    enum AddressType {INDIRECT, DIRECT, IMMEDIATE}

    public CodeGenerator(SymbolTable symbolTable)
    {
        this.symbolTable = symbolTable;
        semanticStack = new Stack<>();
        PB = new InterCode[CODE_SIZE];
    }

    public void generateCode2(String action, Token currentToken, Token[] prevTokens)
    {
        tempBuffAction.add(action);
        tempBuffToken.add(currentToken);
        tempBuffPrev.add(prevTokens);
    }

    public void run()
    {
        java.util.Scanner scn = new java.util.Scanner(System.in);

        while(true)
        {
            String command = scn.nextLine();
            if(command.startsWith("run"))
            {
                int cnt = tempBuffAction.size();
                if(command.split(" ").length > 1)
                    cnt = new Integer(command.split(" ")[1]);
                for(int i=0; i<cnt; i++)
                {
                    generateCode2(tempBuffAction.remove(0), tempBuffToken.remove(0), tempBuffPrev.remove(0));
                }
            }else if(command.startsWith("pb"))
            {
                int cnt=p;
                if(command.split(" ").length > 1)
                    cnt = new Integer(command.split(" ")[1]);
                for(int i=p-cnt;i<p;i++)
                    PB[i].print();
            }else if(command.startsWith("st"))
            {
                if(command.split(" ").length > 1)
                    symbolTable.printFull();
                else
                    symbolTable.print();
            }
        }

    }

    // currentToken: tokeni ke ba didanesh tasmim gereftim Reduce anjam bedim o hanuz too stack nayoomade
    // prevTokens: tokenhaye ghabli ke barresi shodan o oomadan too stack.
    // "int void ID" -> prevTokens[0] == ID, prevTokens[2] == int
    public void generateCode(String action, Token currentToken, Token[] prevTokens) {
        final AddressType IMMEDIATE = AddressType.IMMEDIATE;
        final AddressType DIRECT = AddressType.DIRECT;
        final AddressType INDIRECT = AddressType.INDIRECT;
        int temp, temp2;
        System.out.println("Called " + action);
        switch (action)
        {
            case "init":
                PB[p++] = new InterCode(CodeType.ASSIGN,
                        IMMEDIATE, CODE_SIZE + CONTROL_REGISTERS_SIZE + VARS_BLOCK_SIZE + TEMP_SIZE, DIRECT, CODE_SIZE);
                //PB[p++] = new InterCode(CodeType.ASSIGN,
                //        IMMEDIATE, CODE_SIZE+12, DIRECT, CODE_SIZE+4);
                //PB[p++] = new InterCode(CodeType.ASSIGN,
                //        IMMEDIATE, CODE_SIZE+12+VARS_BLOCK_SIZE+TEMP_SIZE+12, DIRECT, CODE_SIZE+8);
                push(p); p++;
                break;
            case "def_var":
                symbolTable.defineVar(prevTokens[0].attribute);
                break;
            case "def_func":
                symbolTable.defineFunc(prevTokens[1].attribute, p, prevTokens[2].type);
                break;
            case "def_arr":
                symbolTable.defineArray(prevTokens[1].attribute);
                break;
            case "set_pointer":
                int address = symbolTable.getAddress(prevTokens[1].attribute);
                PB[p++] = new InterCode(CodeType.ASSIGN, IMMEDIATE, address+4,
                        DIRECT, address);
                break;
            case "arr_size":
                symbolTable.setArraySize(prevTokens[0].attribute);
                break;
            case "start_scope":
                symbolTable.startScope();
                break;
            case "start_scope_func":
                symbolTable.startScopeFunc();
                break;
            case "init_func":
                PB[peek(0)] = new InterCode(CodeType.JP, DIRECT, p);
                /*int param_length = symbolTable.getFuncParamLength();
                for(int i=0;i<param_length;i++)
                    PB[p++] = new InterCode(CodeType.ASSIGN,
                            DIRECT)
                push(p); p++;*/
                PB[p++] = new InterCode(CodeType.ADD, IMMEDIATE, 4,
                                                        DIRECT, CODE_SIZE,
                                                        DIRECT, CODE_SIZE);
                break;
            case "end_scope":
                symbolTable.endScope();
                break;
            case "func_add_param":
                symbolTable.addFuncParam();
                break;
            case "set_ret_value":
                PB[p++] = new InterCode(CodeType.ASSIGN, DIRECT, peek(0),
                                                        DIRECT, CODE_SIZE+4);
                pop(1);
                break;
            case "end_func":
                PB[p++] = new InterCode(CodeType.SUB, DIRECT, CODE_SIZE,
                        IMMEDIATE, 4,
                        DIRECT, CODE_SIZE);
                PB[p++] = new InterCode(CodeType.JP, INDIRECT, CODE_SIZE);
                break;
            case "pid":
                push(symbolTable.getAddress(prevTokens[0].attribute));
                break;
            case "assign":
                PB[p++] = new InterCode(CodeType.ASSIGN, peek(0)>0?DIRECT:INDIRECT, abs(peek(0)),
                                                peek(1)>0?DIRECT:INDIRECT, abs(peek(1)) );
                pop(2);
                break;
            case "push_arr_size":
                push(symbolTable.getArraySize(prevTokens[0].attribute));
                break;
            case "arr_addr":
                int size = peek(1);
                temp = getTemp();
                temp2 = getTemp();
                PB[p++] = new InterCode(CodeType.MULT, IMMEDIATE, 4,
                        DIRECT, peek(0),
                        DIRECT, temp);
                PB[p++] = new InterCode(CodeType.ADD, DIRECT, peek(2),
                                                    DIRECT, temp,
                                                    DIRECT, temp2);
                pop(3); push(-temp2);
                break;
            case "num_value":
                temp = getTemp();
                PB[p++] = new InterCode(CodeType.ASSIGN, IMMEDIATE, prevTokens[0].attribute,
                                                    DIRECT, temp);
                push(temp);
                break;
            case "save":
                push(p); p++;
                break;
            case "jpf":
                PB[peek(0)] = new InterCode(CodeType.JPF, peek(1)>0?DIRECT:INDIRECT, abs(peek(1)),
                                                    DIRECT, p);
                pop(2);
                break;
            case "jpf_save":
                PB[peek(0)] = new InterCode(CodeType.JPF, peek(1)>0?DIRECT:INDIRECT, abs(peek(1)),
                                                    DIRECT, p+1);
                pop(2);
                push(p); p++;
                //push(p-1); in ke comment kardam nabayad bashe?
                break;
            case "jp":
                PB[peek(0)] = new InterCode(CodeType.JP, DIRECT, p);
                pop(1);
                break;
            case "label":
                push(p);
                break;
            case "while":
                PB[p++] = new InterCode(CodeType.JP, DIRECT, peek(2));
                PB[peek(0)] = new InterCode(CodeType.JPF, peek(1)>0?DIRECT:INDIRECT, abs(peek(1)),
                                                    DIRECT, p);
                pop(3);
                break;
            case "and":
                temp = getTemp();
                PB[p++] = new InterCode(CodeType.AND, peek(0)>0?DIRECT:INDIRECT, abs(peek(0)),
                        peek(1)>0?DIRECT:INDIRECT, abs(peek(1)),
                                                    DIRECT, temp);
                pop(2); push(temp);
                break;
            case "equal":
                temp = getTemp();
                PB[p++] = new InterCode(CodeType.EQ, peek(0)>0?DIRECT:INDIRECT, abs(peek(0)),
                        peek(1)>0?DIRECT:INDIRECT, abs(peek(1)),
                        DIRECT, temp);
                pop(2); push(temp);
                break;
            case "larger":
                temp = getTemp();
                PB[p++] = new InterCode(CodeType.LT, peek(1)>0?DIRECT:INDIRECT, abs(peek(1)), // inja chera jaye 0 va 1 bar'ask hamas
                        peek(0)>0?DIRECT:INDIRECT, abs(peek(0)),
                        DIRECT, temp);
                pop(2); push(temp);
                break;
            case "output":
                PB[p++] = new InterCode(CodeType.PRINT, peek(0)>0?DIRECT:INDIRECT, abs(peek(0)));
                pop(1);
                break;
            case "plus":
                push('+'); // in haminjuri okeye? mage nabayad int begire? :))) (moshabehan baraye '-' '*' '/')
                break;
            case "minus":
                push('-');
                break;
            case "add":
                temp = getTemp();
                if(peek(1) == '+') // in tasavi kar mikone? (moshabehan baraye '-' '*' '/')
                    PB[p++] = new InterCode(CodeType.ADD, peek(2)>0?DIRECT:INDIRECT, abs(peek(2)),
                            peek(0)>0?DIRECT:INDIRECT, abs(peek(0)),
                                                        DIRECT, temp);
                else if(peek(1) == '-')
                    PB[p++] = new InterCode(CodeType.SUB, peek(2)>0?DIRECT:INDIRECT, abs(peek(2)),
                                            peek(0)>0?DIRECT:INDIRECT, abs(peek(0)),
                                                        DIRECT, temp);

                pop(3); push(temp);
                break;
            case "times":
                push('*');
                break;
            case "divide":
                push('/');
                break;
            case "mult":
                temp = getTemp();
                if(peek(1) == '*')
                    PB[p++] = new InterCode(CodeType.MULT, peek(2)>0?DIRECT:INDIRECT, abs(peek(2)),
                            peek(0)>0?DIRECT:INDIRECT, abs(peek(0)),
                            DIRECT, temp);
                else if(peek(1) == '/')
                    PB[p++] = new InterCode(CodeType.DIVIDE, peek(2)>0?DIRECT:INDIRECT, abs(peek(2)),
                            peek(0)>0?DIRECT:INDIRECT, abs(peek(0)),
                            DIRECT, temp);

                pop(3); push(temp);
                break;
            case "call":
                PB[p] = new InterCode(CodeType.ASSIGN, IMMEDIATE, p+2,
                                                    INDIRECT, CODE_SIZE);
                p++;
                pop(1);  // In hamoon addresse moteghayere voroodie tabe'e ke khodemoon ba didane har voroodi 4 ta mobordimesh jolo
                PB[p++] = new InterCode(CodeType.JP, DIRECT, peek(0));
                pop(1);
                temp = getTemp();
                PB[p++] = new InterCode(CodeType.ASSIGN, DIRECT, CODE_SIZE + 4,
                                                    DIRECT, temp);
                push(temp);
                break;
            //case "sp_param": ino nemikhaim dg?
                //break;
            case "copy_input":
                PB[p++] = new InterCode(CodeType.ASSIGN, DIRECT, peek(0),
                                                        DIRECT, peek(1));
                pop(1);
                push(pop(1)+4);
                break;
            //case "sp_local": mikhaim ino?
            //    break;
            case "init_copy":
                push(symbolTable.getFuncAddressOffset(prevTokens[0].attribute));
                break;
        }
        for(int i=lastPrinted;i<p;i++)
            if(PB[i] != null)
                PB[i].print();
            else
                System.out.println("            ");
        lastPrinted = p;

    }

    private void push(int value)
    {
        semanticStack.push(value);
    }

    private int pop(int cnt)
    {
        int result=0;
        for(int i=0;i<cnt;i++)
            result = semanticStack.pop();
        return result;
    }

    private int peek(int depth)
    {
        return semanticStack.get(semanticStack.size()-1-depth);
    }

    private int getTemp()
    {
        tempPointer+=4;
        return CODE_SIZE + CONTROL_REGISTERS_SIZE + VARS_BLOCK_SIZE + (tempPointer-4);
    }

    public void printCode() {
        for (int i = 0; i < p; i++) {
            if (i < 10)
                System.out.print("0");
            System.out.print(i + ": ");
            PB[i].print();
        }
    }

    private int abs(int v)
    {
        if(v >= 0)
            return v;
        return -v;
    }
}


class InterCode
{

    public CodeGenerator.CodeType type;
    public CodeGenerator.AddressType[] addressTypes;
    public int[] addresses;

    public InterCode(CodeGenerator.CodeType type, CodeGenerator.AddressType at0, int address0, CodeGenerator.AddressType at1, int address1, CodeGenerator.AddressType at2, int address2)
    {
        this.type = type;

        addressTypes = new CodeGenerator.AddressType[3];
        addressTypes[0] = at0;
        addressTypes[1] = at1;
        addressTypes[2] = at2;

        addresses = new int[3];
        addresses[0] = address0;
        addresses[1] = address1;
        addresses[2] = address2;
    }

    public InterCode(CodeGenerator.CodeType type, CodeGenerator.AddressType at0, int address0, CodeGenerator.AddressType at1, int address1)
    {
        this.type = type;

        addressTypes = new CodeGenerator.AddressType[2];
        addressTypes[0] = at0;
        addressTypes[1] = at1;

        addresses = new int[2];
        addresses[0] = address0;
        addresses[1] = address1;
    }

    public InterCode(CodeGenerator.CodeType type, CodeGenerator.AddressType at0, int address0)
    {
        this.type = type;

        addressTypes = new CodeGenerator.AddressType[1];
        addressTypes[0] = at0;

        addresses = new int[1];
        addresses[0] = address0;
    }

    public void print()
    {
        String result = "(";
        result += type.toString();
        for(int i=0;i<addressTypes.length;i++)
        {
            result += ", ";
            if (addressTypes[i] == CodeGenerator.AddressType.INDIRECT)
                result += "@";
            if (addressTypes[i] == CodeGenerator.AddressType.IMMEDIATE)
                result += "#";
            result += addresses[i];
        }
        result += ")";
        System.out.println(result);
    }

}