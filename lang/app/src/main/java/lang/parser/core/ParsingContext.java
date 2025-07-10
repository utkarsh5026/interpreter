package lang.parser.core;

import lang.lexer.Lexer;
import lang.token.Token;
import lang.token.TokenType;

/**
 * ParsingContext holds shared state for all parsers.
 */
public class ParsingContext {
    private final TokenStream tokens;
    private final ErrorReporter errors;
    private final PrecedenceTable precedenceTable;
    private int loopDepth = 0;

    public ParsingContext(Lexer lexer) {
        this.tokens = new TokenStream(lexer);
        this.errors = new ErrorReporter();
        this.precedenceTable = new PrecedenceTable();
    }

    public TokenStream getTokens() {
        return tokens;
    }

    public ErrorReporter getErrors() {
        return errors;
    }

    public PrecedenceTable getPrecedenceTable() {
        return precedenceTable;
    }

    public void enterLoop() {
        loopDepth++;
    }

    public void exitLoop() {
        loopDepth--;
    }

    public boolean isInLoop() {
        return loopDepth > 0;
    }

    public void addError(String message, Token token) {
        errors.addError(message, token);
    }

    public void addTokenError(TokenType expected, Token actual) {
        errors.addTokenError(expected, actual);
    }
}