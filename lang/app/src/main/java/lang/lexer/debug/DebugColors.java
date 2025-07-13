package lang.lexer.debug;

/**
 * 🎨 DebugColors - Color Constants for Output 🎨
 * 
 * Centralized color definitions for consistent debug output.
 */
public final class DebugColors {
    // ANSI escape codes
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";

    // Token type colors
    public static final String KEYWORD = "\u001B[35m"; // Magenta
    public static final String IDENTIFIER = "\u001B[36m"; // Cyan
    public static final String NUMBER = "\u001B[33m"; // Yellow
    public static final String STRING = "\u001B[32m"; // Green
    public static final String OPERATOR = "\u001B[31m"; // Red
    public static final String DELIMITER = "\u001B[34m"; // Blue
    public static final String EOF_COLOR = "\u001B[90m"; // Gray

    // Context colors
    public static final String POSITION = "\u001B[93m"; // Bright Yellow
    public static final String CONTEXT = "\u001B[37m"; // White
    public static final String HIGHLIGHT = "\u001B[43m\u001B[30m"; // Yellow bg, black text

    // Status colors
    public static final String SUCCESS = "\u001B[92m"; // Bright Green
    public static final String WARNING = "\u001B[93m"; // Bright Yellow
    public static final String ERROR = "\u001B[91m"; // Bright Red
    public static final String INFO = "\u001B[94m"; // Bright Blue

    private DebugColors() {
    } // Utility class

    /**
     * 🎨 Gets color for token type
     */
    public static String getTokenColor(lang.token.TokenType type) {
        return switch (type) {
            case LET, CONST, IF, ELSE, ELIF, WHILE, FOR, FUNCTION, RETURN, BREAK, CONTINUE,
                    TRUE, FALSE, NULL, CLASS, EXTENDS, SUPER, THIS, NEW ->
                KEYWORD;
            case IDENTIFIER -> IDENTIFIER;
            case INT -> NUMBER;
            case STRING, F_STRING -> STRING;
            case ASSIGN, PLUS, MINUS, ASTERISK, SLASH, MODULUS, EQ, NOT_EQ, LESS_THAN,
                    GREATER_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN_OR_EQUAL, AND, OR,
                    PLUS_ASSIGN, MINUS_ASSIGN, ASTERISK_ASSIGN, SLASH_ASSIGN,
                    BITWISE_AND, BITWISE_OR, BITWISE_XOR, BITWISE_NOT,
                    BITWISE_LEFT_SHIFT, BITWISE_RIGHT_SHIFT, BANG ->
                OPERATOR;
            case SEMICOLON, COMMA, COLON, DOT, LPAREN, RPAREN, LBRACE, RBRACE,
                    LBRACKET, RBRACKET ->
                DELIMITER;
            case EOF -> EOF_COLOR;
            default -> RESET;
        };
    }

    /**
     * 🎨 Wraps text with color (if colors enabled)
     */
    public static String colorize(String text, String color, boolean useColors) {
        return useColors ? color + text + RESET : text;
    }

    /**
     * 🎨 Makes text bold (if colors enabled)
     */
    public static String bold(String text, boolean useColors) {
        return useColors ? BOLD + text + RESET : text;
    }
}