package errorhandler;

public class ErrorHandler {
    public ErrorHandler() {
        // TODO
    }

    public void scannerError(String msg) {
        System.out.println("Scanner Error: " + msg);
    }

    public void parserError(String msg) {
        System.out.println("Parser Error: " + msg);
    }

    public void semanticError(String msg) {
        System.out.println("Semantic Error: " + msg);
    }
}
