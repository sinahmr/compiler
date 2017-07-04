package codegenerator;

import com.sun.org.apache.bcel.internal.classfile.Code;
import scanner.SymbolTable;
import scanner.Token;

import java.util.ArrayList;
import java.util.Stack;

public class CodeGenerator {

    final int CODE_SIZE = 1000;
    final int STATIC_SIZE = 20;
    final int TEMP_SIZE = 100;

    int p = 0;

    SymbolTable symbolTable;
    Stack<Integer> semanticStack;
    InterCode[] PB;

    enum  CodeType {ADD, AND, ASSIGN, EQ, JPF, JP, LT, MULT, NOT, PRINT, SUB, DIVIDE};
    enum AddressType {INDIRECT, DIRECT, IMMEDIATE};

    public CodeGenerator(SymbolTable symbolTable)
    {
        this.symbolTable = symbolTable;
        semanticStack = new Stack<>();
        PB = new InterCode[CODE_SIZE];
    }

    // currentToken: tokeni ke ba didanesh tasmim gereftim Reduce anjam bedim o hanuz too stack nayoomade
    // prevTokens: tokenhaye ghabli ke barresi shodan o oomadan too stack.
    // "int void ID" -> prevTokens[0] == ID, prevTokens[2] == int
    public void generateCode(String action, Token currentToken, Token[] prevTokens) {
        switch (action)
        {
            case "init":
                PB[p++] = new InterCode(CodeType.ASSIGN,
                        AddressType.IMMEDIATE, CODE_SIZE+12+STATIC_SIZE+TEMP_SIZE+12, AddressType.DIRECT, CODE_SIZE);
                PB[p++] = new InterCode(CodeType.ASSIGN,
                        AddressType.IMMEDIATE, CODE_SIZE+12, AddressType.DIRECT, CODE_SIZE+4);
                PB[p++] = new InterCode(CodeType.ASSIGN,
                        AddressType.IMMEDIATE, CODE_SIZE+12+STATIC_SIZE+TEMP_SIZE+12, AddressType.DIRECT, CODE_SIZE+8);
                push(p); p++;
                break;
            case "def_var":
                symbolTable.defineVar(prevTokens[0].attribute);
                break;
            case "def_func":
                symbolTable.defineFunc(prevTokens[0].attribute, p);
                if(prevTokens[1].type == Token.Type.INT)
                    symbolTable.setRetType(SymbolTable.RetType.INT);
                else if(prevTokens[1].type == Token.Type.VOID)
                    symbolTable.setRetType(SymbolTable.RetType.VOID);
                break;
            case "def_arr":
                symbolTable.defineArray(prevTokens[0].attribute);
                break;
            case "set_pointer":
                //TODO
                break;
            case "arr_size":
                symbolTable.setArraySize(prevTokens[0].attribute);
                break;
            case "start_scope":
                symbolTable.startScope();
                break;
            case "init_func":
                PB[semanticStack.peek()] = new InterCode(CodeType.JP, AddressType.IMMEDIATE, p);
                int param_length = symbolTable.getFuncParamLength();
                /*for(int i=0;i<param_length;i++)
                    PB[p++] = new InterCode(CodeType.ASSIGN,
                            AddressType.DIRECT)*/

                push(p); p++;
                break;
            case "end_scope":
                symbolTable.endScope();
                break;
            case "func_add_param":
                symbolTable.addFuncParam();
                break;
            case "set_ret_value":
                break;
            case "end_func":
                break;
            case "pid":
                break;
            case "assign":
                break;
            case "push_arr_size":
                break;
            case "arr_value":
                break;
            case "num_value":
                break;
            case "save":
                break;
            case "jpf":
                break;
            case "jpf_save":
                break;
            case "jp":
                break;
            case "label":
                break;
            case "while":
                break;
            case "and":
                break;
            case "equal":
                break;
            case "larger":
                break;
            case "output":
                break;
            case "plus":
                break;
            case "minus":
                break;
            case "add":
                break;
            case "times":
                break;
            case "divide":
                break;
            case "mult":
                break;
            case "call":
                break;
            case "sp_param":
                break;
            case "copy_input":
                break;
            case "sp_local":
                break;
        }
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
        result = result.concat(type.toString());
        for(int i=0;i<addressTypes.length;i++)
        {
            result.concat(", ");
            switch (addressTypes[i])
            {
                case INDIRECT : result.concat("@"); break;
                case IMMEDIATE: result.concat("#"); break;
            }
            result.concat("" + addresses[i]);
        }
        result.concat(")");
        System.out.println(result);
    }

}