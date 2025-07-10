package lang.token;

import java.util.Map;
import java.util.Set;

/**
 * Utility class for managing keywords in the Mutant language.
 * Provides methods to lookup keywords, check if a string is a keyword,
 * and get all keywords.
 */
public final class Keywords {

    private Keywords() {
        throw new UnsupportedOperationException("Utility class - prevent instantiation");
    }

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("fn", TokenType.FUNCTION),
            Map.entry("let", TokenType.LET),
            Map.entry("true", TokenType.TRUE),
            Map.entry("false", TokenType.FALSE),
            Map.entry("if", TokenType.IF),
            Map.entry("elif", TokenType.ELIF),
            Map.entry("else", TokenType.ELSE),
            Map.entry("return", TokenType.RETURN),
            Map.entry("while", TokenType.WHILE),
            Map.entry("break", TokenType.BREAK),
            Map.entry("continue", TokenType.CONTINUE),
            Map.entry("for", TokenType.FOR),
            Map.entry("const", TokenType.CONST),
            Map.entry("class", TokenType.CLASS),
            Map.entry("extends", TokenType.EXTENDS),
            Map.entry("super", TokenType.SUPER),
            Map.entry("this", TokenType.THIS),
            Map.entry("new", TokenType.NEW),
            Map.entry("null", TokenType.NULL));

    /**
     * Looks up the token type for a given identifier.
     * 
     * @param identifier the identifier to look up
     * @return the corresponding TokenType if it's a keyword, otherwise IDENTIFIER
     */
    public static TokenType lookupIdentifier(String identifier) {
        return KEYWORDS.getOrDefault(identifier, TokenType.IDENTIFIER);
    }

    /**
     * Returns all keywords in the language.
     * 
     * @return an unmodifiable set of all keywords
     */
    public static Set<String> getAllKeywords() {
        return KEYWORDS.keySet();
    }

    /**
     * Checks if a string is a keyword.
     * 
     * @param str the string to check
     * @return true if the string is a keyword, false otherwise
     */
    public static boolean isKeyword(String str) {
        return KEYWORDS.containsKey(str);
    }
}
