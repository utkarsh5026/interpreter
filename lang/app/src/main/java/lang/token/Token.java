package lang.token;

/**
 * Represents a token in the Mutant language.
 * Immutable value object containing token type, literal value, and position.
 */
public record Token(TokenType type, String literal, TokenPosition position) {

    public Token {
        if (type == null) {
            throw new IllegalArgumentException("Token type cannot be null");
        }
        if (literal == null) {
            throw new IllegalArgumentException("Token literal cannot be null");
        }
        if (position == null) {
            throw new IllegalArgumentException("Token position cannot be null");
        }
    }

    @Override
    public String toString() {
        return String.format("""
                Token {
                    type: %s,
                    literal: '%s',
                    position: %s
                }""", type, literal, position);
    }
}
