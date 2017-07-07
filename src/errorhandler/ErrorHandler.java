package errorhandler;

import scanner.Token;

public class ErrorHandler {

    public void scannerError(String msg) {
        System.out.println("Scanner Error: " + msg);
    }

    public void parserError(String msg) {
        System.out.println("Parser Error: " + msg);
    }

    public void parserError(String msg, Token token) {
        System.out.println("Parser Error: " + msg + ", At " + token.lineNumber + ":" + token.offsetInLine);
    }

    public void semanticError(String msg) {
        System.out.println("Semantic Error: " + msg);
    }

    public void semanticError(String msg, Token token) {
        System.out.println("Semantic Error: " + msg + ", At " + token.lineNumber + ":" + token.offsetInLine);
    }
}
