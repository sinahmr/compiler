import errorhandler.ErrorHandler;
import parser.Parser;
import scanner.Scanner;
import scanner.Token;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

public class UserInterface
{
    public static void main(String[] args) throws Exception
    {
        ErrorHandler errorHandler = new ErrorHandler();
        File codeFile = new File("./src/resource/testCodes/testCode5.txt");
        Scanner scanner = new Scanner(new FileInputStream(codeFile), errorHandler);
        new Parser(scanner, errorHandler).parse();
//        while(true)
//        {
//            Token next = scanner.getNextToken();
//            if(next.type == Token.Type.EOF)
//                break;
//            System.out.println(next.type + "  ***  " + next.attribute);
//        }
    }
}
