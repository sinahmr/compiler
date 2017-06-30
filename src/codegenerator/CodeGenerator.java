package codegenerator;

import scanner.Token;

public class CodeGenerator {

    // currentToken: tokeni ke ba didanesh tasmim gereftim Reduce anjam bedim o hanuz too stack nayoomade
    // prevTokens: tokenhaye ghabli ke barresi shodan o oomadan too stack.
    // "int void ID" -> prevTokens[0] == ID, prevTokens[2] == int
    public void generateCode(String action, Token currentToken, Token[] prevTokens) {
        switch (action) {
            case "#pid": pid(prevTokens[0]);
            // TODO
        }
    }

    private void pid(Token token) {
        // TODO
    }
}
