import errorhandler.ErrorHandler;
import parser.Parser;
import scanner.Scanner;
import scanner.Token;

import java.io.*;

public class UserInterface
{
    public static void main(String[] args) {
        ErrorHandler errorHandler = new ErrorHandler();
        File codeFile = new File("./src/resource/testCodes/input1.txt");
        Scanner scanner;
        try {
            scanner = new Scanner(new FileInputStream(codeFile), errorHandler);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        //new Parser(scanner, errorHandler).parse();
        while(true)
        {
            Token next = scanner.getNextToken();
            if(next.type == Token.Type.EOF)
                break;
            System.out.println(next.type + "  ***  " + next.attribute);
        }
    }
}
