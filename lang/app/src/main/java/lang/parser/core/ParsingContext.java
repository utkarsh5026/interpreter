package lang.parser.core;

import lang.lexer.Lexer;
import lang.token.Token;
import lang.token.TokenType;
import lang.parser.error.ErrorReporter;
import lang.parser.precedence.PrecedenceTable;
import lang.parser.error.ParserException;
import java.util.Optional;

/**
 * 🎯 ParsingContext - The Parser's Control Center 🎯
 * 
 * The central coordination hub that holds all shared state for parsing
 * operations.
 * Think of it as mission control for parsing - everything flows through here!
 * 🚀🏢
 * 
 * This context manages:
 * - 🌊 Token stream navigation (what tokens we're looking at)
 * - 🚨 Error collection and reporting (what problems we've found)
 * - 📊 Operator precedence rules (math order of operations)
 * - 🔄 Loop depth tracking (for break/continue validation)
 * 
 */
public class ParsingContext {
    private final TokenStream tokens; // 🌊 Token navigation manager
    private final ErrorReporter errors; // 🚨 Error collection service
    private final PrecedenceTable precedenceTable; // 📊 Operator precedence rules
    private int loopDepth = 0; // 🔄 Current nesting level of loops

    /**
     * 🏗️ Creates a new parsing context from a lexer
     * 
     * Sets up the complete parsing environment with all necessary components.
     * 
     * @param lexer The lexer that will provide tokens for parsing 🔍
     */
    public ParsingContext(Lexer lexer) {
        this.tokens = new TokenStream(lexer);
        this.errors = new ErrorReporter(lexer.getInputLines());
        this.precedenceTable = new PrecedenceTable();
    }

    /**
     * 🌊 Gets the token stream manager
     * 
     * Returns the TokenStream that handles token navigation and consumption.
     * 
     * Use this to:
     * - Check current and upcoming tokens 👀
     * - Advance through the token sequence ➡️
     * - Consume expected tokens 🎯
     * 
     * @return The TokenStream for token navigation 🌊
     */
    public TokenStream getTokenStream() {
        return tokens;
    }

    /**
     * 🚨 Gets the error reporter
     * 
     * Returns the ErrorReporter that collects all parsing problems.
     * 
     * @return The ErrorReporter for error management 🚨
     */
    public ErrorReporter getErrors() {
        return errors;
    }

    /**
     * 📊 Gets the precedence table
     * 
     */
    public PrecedenceTable getPrecedenceTable() {
        return precedenceTable;
    }

    /**
     * 🔄 Enters a new loop level
     */
    public void enterLoop() {
        loopDepth++;
    }

    /**
     * 🔄 Exits the current loop level
     * 
     * Decrements the loop depth counter when parsing leaves a loop structure.
     * Like climbing back up one level in a nested loop maze! 🏗️⬆️
     * 
     * Call this when finishing parsing of any loop construct.
     * Must be balanced with enterLoop() calls!
     */
    public void exitLoop() {
        loopDepth--;
    }

    /**
     * ❓ Checks if currently inside any loop
     */
    public boolean isInLoop() {
        return loopDepth > 0;
    }

    /**
     * 🚨 Adds a custom error message
     */
    public void addError(String message, Token token) {
        errors.addError(message, token);
    }

    /**
     * 🎯 Adds a token expectation error
     */
    public void addTokenError(TokenType expected, Token actual) {
        errors.addTokenError(expected, actual);
    }

    public Token consumeCurrentToken(TokenType type, String messageOnError) throws ParserException {
        Token token = tokens.getCurrentToken();
        try {
            return tokens.consume(type);
        } catch (ParserException e) {
            String error = e.getMessage();
            Optional<Token> errorToken = e.getToken();

            if (errorToken.isPresent()) {
                throw new ParserException(String.format("%s: %s", messageOnError, error), errorToken.get());
            }
            throw new ParserException(String.format("%s: %s", messageOnError, error), token);
        }
    }

    public Token consumeCurrentToken(TokenType type) throws ParserException {
        return consumeCurrentToken(type, "");
    }

    public boolean isCurrentToken(TokenType type) {
        return tokens.isCurrentToken(type);
    }

    public boolean isPeekToken(TokenType type) {
        return tokens.isPeekToken(type);
    }

    public Token getCurrentToken() {
        return tokens.getCurrentToken();
    }

    public boolean isAtEnd() {
        return tokens.isAtEnd();
    }
}