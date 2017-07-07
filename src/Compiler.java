import codegenerator.CodeGenerator;
import errorhandler.ErrorHandler;
import parser.Parser;
import scanner.Scanner;

import java.io.*;

public class Compiler {
    public static void main(String[] args) {
        ErrorHandler errorHandler = new ErrorHandler();
        File codeFile = new File("./src/resource/testCodes/em1.txt");
        Scanner scanner;
        try {
            scanner = new Scanner(new FileInputStream(codeFile), errorHandler);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        CodeGenerator codeGenerator = new CodeGenerator(scanner.getSymbolTable(), errorHandler);
        new Parser(scanner, codeGenerator, errorHandler).parse();
    }
}
