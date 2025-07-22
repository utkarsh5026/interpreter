package lang.lexer.core;

import lang.token.Token;
import lang.token.TokenType;
import lang.token.TokenPosition;

/**
 * 🏭 TokenFactory - Centralized Token Creation 🏭
 * 
 * From first principles, token creation involves:
 * 1. Packaging token type and literal value
 * 2. Attaching position information for error reporting
 * 3. Ensuring consistent token structure
 * 
 * By centralizing this logic, we ensure all tokens are created
 * consistently and can easily add features like token metadata.
 */
public class TokenFactory {

    private final CharacterStream stream;

    public TokenFactory(CharacterStream stream) {
        this.stream = stream;
    }

    /**
     * 🎫 Creates a token with current position
     */
    public Token createToken(TokenType type, String literal) {
        var pos = stream.getPosition();
        int line = pos.line();
        int column = pos.column();
        return new Token(type, literal, new TokenPosition(line, column));
    }

    /**
     * 🎫 Creates a token from a single character
     */
    public Token createToken(TokenType type, char literal) {
        return createToken(type, String.valueOf(literal));
    }

    /**
     * 🎫 Creates a token with empty literal (like EOF)
     */
    public Token createToken(TokenType type) {
        return createToken(type, "");
    }
}