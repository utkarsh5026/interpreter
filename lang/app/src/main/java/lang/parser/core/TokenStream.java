package lang.parser.core;

import lang.lexer.Lexer;
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
     * Used as a placeholder when initializing the stream.
     * Like having a "THE END" bookmark ready! 📖🏁
     * 
     * @return A default EOF token at position (0, 0) 🎫
     */
    private Token createEofToken() {
        return new Token(TokenType.EOF, "", new TokenPosition(0, 0));
    }

    /**
     * 👀 Gets the current token
     * 
     * Returns the token that the parser is currently examining.
     * Like asking "What word am I looking at right now?" 📖👀
     * 
     * @return The current token being processed 🎫
     */
    public Token getCurrentToken() {
        return currentToken;
    }

    /**
     * 👁️ Gets the next token (peek ahead)
     * 
     * Returns the next token without consuming it.
     * Like peeking at the next word in a book without turning the page! 📖👁️
     * 
     * This is essential for making parsing decisions based on what's coming next.
     * 
     * @return The next token in the stream 🎫
     */
    public Token getPeekToken() {
        return peekToken;
    }

    /**
     * ➡️ Advances to the next token
     * 
     * Moves the stream forward by one token.
     * Like turning to the next word in a book! 📖➡️
     * 
     * After advancing:
     * - Current token becomes the old peek token
     * - Peek token becomes a new token from the lexer
     */
    public void advance() {
        currentToken = peekToken;
        peekToken = lexer.nextToken();
    }

    /**
     * 🎯 Checks if the current token matches a specific type
     * 
     * Tests whether the current token is of the expected type.
     * Like asking "Is this word a noun?" 📖❓
     * 
     * @param type The token type to check for 🏷️
     * @return True if current token matches the type, false otherwise ✅❌
     */
    public boolean isCurrentToken(TokenType type) {
        return currentToken.type() == type;
    }

    /**
     * 👁️ Checks if the peek token matches a specific type
     * 
     * Tests whether the next token is of the expected type.
     * Like asking "Is the next word a verb?" 📖❓
     * 
     * @param type The token type to check for 🏷️
     * @return True if peek token matches the type, false otherwise ✅❌
     */
    public boolean isPeekToken(TokenType type) {
        return peekToken.type() == type;
    }

    /**
     * 🤔 Optionally consumes a token if it matches the expected type
     * 
     * Advances if the next token matches, otherwise does nothing.
     * Like saying "If the next word is 'and', skip over it" 📖🤔
     * 
     * This is perfect for optional tokens like semicolons that might or might not
     * be there.
     * 
     * @param expectedType The token type we're hoping to find 🎯
     * @return True if the token was consumed, false if it didn't match ✅❌
     */
    public boolean expect(TokenType expectedType) {
        if (isPeekToken(expectedType)) {
            advance();
            return true;
        }
        return false;
    }

    /**
     * 🎯 Consumes the next token, requiring it to match the expected type
     * 
     * Advances to the next token but throws an exception if it's not the expected
     * type.
     * Like demanding "The next word MUST be a semicolon!" 📖💪
     * 
     * This is used when the grammar absolutely requires a specific token.
     * 
     * @param expectedType The token type that MUST come next 🎯
     * @return The consumed token (now the current token) 🎫
     * @throws ParserException if the next token doesn't match 💥
     */
    public Token consume(TokenType expectedType) {
        if (!isPeekToken(expectedType)) {
            throw new ParserException(String.format(
                    "Expected %s, got %s at %s",
                    expectedType,
                    peekToken.type(),
                    peekToken.position()));
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
}