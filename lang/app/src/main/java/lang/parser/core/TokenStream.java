package lang.parser.core;

import lang.lexer.Lexer;
import lang.parser.error.ParserException;
import lang.token.Token;
import lang.token.TokenType;
import lang.token.TokenPosition;

/**
 * 🌊 TokenStream - The Token Flow Manager 🌊
 * 
 * Manages token navigation and consumption for the parser.
 * Think of it as a smart conveyor belt that feeds tokens to the parser! 🏭➡️
 * 
 * This class is the parser's interface to the lexer, providing:
 * - 👀 Current and peek token access (what's now and what's next)
 * - ➡️ Token advancement (move to the next token)
 * - 🎯 Token expectation with error handling
 * - 🔍 Token type checking utilities
 * 
 * Like reading a book:
 * - Current token = the word you're reading now 📖👀
 * - Peek token = the next word you can see 👁️
 * - Advance = move to the next word ➡️
 * 
 * Example usage:
 * ```
 * TokenStream stream = new TokenStream(lexer);
 * 
 * if (stream.isCurrentToken(TokenType.LET)) {
 * stream.advance(); // Move past 'let'
 * Token name = stream.consume(TokenType.IDENTIFIER); // Expect variable name
 * }
 * ```
 */
public class TokenStream {
    private final Lexer lexer; // 🔍 The lexer that produces tokens
    private Token currentToken; // 👀 The token we're currently looking at
    private Token peekToken; // 👁️ The next token coming up

    /**
     * 🏗️ Creates a new token stream from a lexer
     * 
     * Sets up the token stream and reads the first two tokens.
     * 
     * @param lexer The lexer that will provide tokens 🔍
     */
    public TokenStream(Lexer lexer) {
        this.lexer = lexer;
        this.currentToken = createEofToken();
        this.peekToken = createEofToken();

        // Read first two tokens
        advance();
        advance();
    }

    /**
     * 🏁 Creates a default EOF token
     * 
     * @return A default EOF token at position (0, 0) 🎫
     */
    private Token createEofToken() {
        return new Token(TokenType.EOF, "", new TokenPosition(1, 0));
    }

    /**
     * 👀 Gets the current token
     */
    public Token getCurrentToken() {
        return currentToken;
    }

    /**
     * 👁️ Gets the next token (peek ahead)
     * 
     * @return The next token in the stream 🎫
     */
    public Token getPeekToken() {
        return peekToken;
    }

    /**
     * ➡️ Advances to the next token
     */
    public void advance() {
        currentToken = peekToken;
        peekToken = lexer.nextToken();
    }

    /**
     * 🎯 Checks if the current token matches a specific type
     */
    public boolean isCurrentToken(TokenType type) {
        return currentToken.type() == type;
    }

    /**
     * 👁️ Checks if the peek token matches a specific type
     */
    public boolean isPeekToken(TokenType type) {
        return peekToken.type() == type;
    }

    /**
     * 🤔 Optionally consumes a token if it matches the expected type
     */
    public boolean expect(TokenType expectedType) {
        if (isPeekToken(expectedType)) {
            advance();
            return true;
        }
        return false;
    }

    /**
     * 🎯 Consumes the current token, requiring it to match the expected type
     * 
     */
    public Token consume(TokenType expectedType) throws ParserException {

        if (!isCurrentToken(expectedType)) {
            System.out.println("Throwing ParserException");
            throw new ParserException(String.format(
                    "Expected %s, got %s at %s",
                    expectedType,
                    currentToken.type(),
                    currentToken.position()), currentToken);
        }

        Token consumed = currentToken;
        advance();
        return consumed;
    }

    /**
     * 🎯 Consume next token (peek) and advance
     * 
     */
    public Token consumeNext(TokenType expectedType) throws ParserException {
        if (!isPeekToken(expectedType)) {
            throw new ParserException(String.format(
                    "Expected %s, got %s at %s",
                    expectedType,
                    peekToken.type(),
                    peekToken.position()), peekToken);
        }
        advance();
        return currentToken;
    }

    /**
     * 🏁 Checks if we've reached the end of the token stream
     * 
     * Tests whether the current token is EOF (End Of File).
     * Like asking "Have we reached the end of the book?" 📖🏁
     * 
     * @return True if at the end of the stream, false otherwise ✅❌
     */
    public boolean isAtEnd() {
        return isCurrentToken(TokenType.EOF);
    }

    public void advanceIfPeek(TokenType type) {
        if (isPeekToken(type)) {
            advance();
        }
    }
}