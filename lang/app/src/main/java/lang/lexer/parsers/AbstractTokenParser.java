package lang.lexer.parsers;

import lang.lexer.core.*;

/**
 * 🧩 AbstractTokenParser - Base implementation with common dependencies 🧩
 */
public abstract class AbstractTokenParser implements TokenParser {

    protected final CharacterStream stream;
    protected final TokenFactory tokenFactory;

    protected AbstractTokenParser(CharacterStream stream, TokenFactory tokenFactory) {
        this.stream = stream;
        this.tokenFactory = tokenFactory;
    }

    /**
     * 🔤 Utility: Check if character is a letter or underscore
     */
    protected static boolean isLetter(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
    }

    /**
     * 🔢 Utility: Check if character is a digit
     */
    protected static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * ⚪ Utility: Check if character is whitespace
     */
    protected static boolean isWhitespace(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r' || ch == '\f';
    }
}