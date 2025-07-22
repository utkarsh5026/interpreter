package lang.lexer;

import lang.lexer.core.*;
import lang.lexer.parsers.*;
import lang.lexer.debug.LexerDebugger;
import lang.lexer.debug.DebugEvent;
import lang.token.Token;
import lang.token.TokenType;

import java.util.List;
import java.util.ArrayList;

/**
 * 🎼 ModularLexer - Orchestrates Token Parsing 🎼
 * 
 * From first principles, the lexer's job is to:
 * 1. Read input character by character
 * 2. Identify token boundaries
 * 3. Delegate parsing to appropriate specialists
 * 4. Return structured tokens
 * 
 * This modular design separates concerns:
 * - CharacterStream: handles input reading
 * - TokenFactory: creates tokens consistently
 * - Parsers: handle specific token types
 * - Lexer: orchestrates the process
 */
public class Lexer {

    private final CharacterStream stream;
    private final TokenFactory tokenFactory;
    private final WhitespaceHandler whitespaceHandler;
    private final List<TokenParser> parsers;
    private final LexerDebugger debugger;

    public Lexer(String input) {
        this(input, null);
    }

    public Lexer(String input, LexerDebugger debugger) {
        this.stream = new CharacterStream(input);
        this.tokenFactory = new TokenFactory(stream);
        this.whitespaceHandler = new WhitespaceHandler(stream);
        this.debugger = debugger;

        this.parsers = List.of(
                new FStringParser(stream, tokenFactory),
                new StringParser(stream, tokenFactory),
                new NumberParser(stream, tokenFactory),
                new IdentifierParser(stream, tokenFactory),
                new OperatorParser(stream, tokenFactory));

        if (debugger != null) {
            debugger.onSessionStart(input);
        }
    }

    /**
     * 🎯 Main tokenization method - returns next token
     */
    public Token nextToken() {
        int preTokenPosition = stream.getCurrentPosition();
        char triggerChar = stream.currentCharacter();

        // Skip whitespace and comments
        whitespaceHandler.skipNonTokens();

        // Check for end of input
        if (stream.currentCharacter() == TokenDescription.EOF) {
            Token eofToken = tokenFactory.createToken(TokenType.EOF);
            logDebugEvent(eofToken, preTokenPosition, triggerChar);
            return eofToken;
        }

        // Try each parser in order
        for (TokenParser parser : parsers) {
            if (parser.canParse(stream.currentCharacter())) {
                Token token = parser.parse();
                stream.advance();
                logDebugEvent(token, preTokenPosition, triggerChar);
                return token;
            }
        }

        Token illegalToken = tokenFactory.createToken(TokenType.ILLEGAL,
                stream.currentCharacter());
        stream.advance();
        logDebugEvent(illegalToken, preTokenPosition, triggerChar);
        return illegalToken;
    }

    /**
     * 📜 Tokenizes entire input and returns all tokens
     */
    public List<Token> tokenizeAll() {
        List<Token> tokens = new ArrayList<>();

        Token token;
        do {
            token = nextToken();
            tokens.add(token);
        } while (token.type() != TokenType.EOF);

        reset();
        endSession();
        return tokens;
    }

    /**
     * 🔄 Resets lexer to beginning of input
     */
    public void reset() {
        stream.reset();
        if (debugger != null) {
            debugger.onSessionReset();
        }
    }

    /**
     * 🏁 Ends the tokenization session
     */
    public void endSession() {
        if (debugger != null) {
            debugger.onSessionEnd();
        }
    }

    /**
     * 📊 Gets debug information (if debugging enabled)
     */
    public LexerDebugger getDebugger() {
        return debugger;
    }

    /**
     * 📖 Gets current character for external inspection
     */
    public char getCurrentChar() {
        return stream.currentCharacter();
    }

    /**
     * 📄 Gets input lines for error reporting
     */
    public String[] getInputLines() {
        return stream.getLines();
    }

    /**
     * 🎯 Factory method for creating lexer with debugging
     */
    public static Lexer withDebugger(String input, LexerDebugger debugger) {
        return new Lexer(input, debugger);
    }

    private void logDebugEvent(Token token, int preTokenPosition, char triggerChar) {
        if (debugger != null) {
            DebugEvent event = new DebugEvent(
                    token,
                    preTokenPosition,
                    triggerChar,
                    stream.getCurrentPosition(),
                    System.nanoTime());
            debugger.onTokenCreated(event);
        }
    }
}