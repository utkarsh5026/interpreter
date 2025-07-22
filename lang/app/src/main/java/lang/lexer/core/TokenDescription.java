package lang.lexer.core;

public class TokenDescription {
    public static final char EOF = '\0';
    public static final char NEWLINE = '\n';
    public static final char TAB = '\t';
    public static final char SPACE = ' ';
    public static final char COMMENT_START = '#';
    public static final char STRING_START_DOUBLE_QUOTE = '"';
    public static final char STRING_END_DOUBLE_QUOTE = '"';
    public static final char STRING_START_SINGLE_QUOTE = '\'';
    public static final char STRING_END_SINGLE_QUOTE = '\'';
    public static final char F_STRING_START = 'f';

    public static final char EQUAL = '=';
    public static final char NOT_EQUAL = '!';
    public static final char LESS_THAN = '<';
    public static final char GREATER_THAN = '>';
    public static final char PLUS = '+';
    public static final char MINUS = '-';
    public static final char ASTERISK = '*';
    public static final char SLASH = '/';
    public static final char PERCENT = '%';
    public static final char AMPERSAND = '&';
    public static final char PIPE = '|';
    public static final char TILDE = '~';
    public static final char DOT = '.';

    public static final char LBRACE = '{';
    public static final char RBRACE = '}';

    public static boolean isWhitespace(char ch) {
        return ch == SPACE || ch == TAB || ch == NEWLINE || ch == '\r' || ch == '\f';
    }
}
