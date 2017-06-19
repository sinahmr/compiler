import errorhandler.ErrorHandler;
import scanner.Scanner;
import scanner.Token;

import java.io.FileInputStream;

/**
 * Created by Pooya-Laptop on 6/19/2017.
 */
public class UserInterface
{
    public static void main(String[] args) throws Exception
    {
        ErrorHandler errorHandler = new ErrorHandler();
        Scanner scanner = new Scanner(new FileInputStream("D:/testCode4.txt"), errorHandler);
        while(true)
        {
            Token next = scanner.getNextToken();
            if(next.type == Token.Type.EOF)
                break;
            System.out.println(next.type + "  ***  " + next.attribute);
        }
    }
}
