package lang.token;

public enum TokenType {

    ILLEGAL("ILLEGAL"),
    EOF("EOF"),

    IDENTIFIER("IDENTIFIER"),
    INT("INT"),
    STRING("STRING"),

    // Operators
    ASSIGN("="),
    PLUS("+"),
    MINUS("-"),
    BANG("!"),
    ASTERISK("*"),
    SLASH("/"),
    MODULUS("%"),

    // Comparison operators
    LESS_THAN("<"),
    GREATER_THAN(">"),
    LESS_THAN_OR_EQUAL("<="),
    GREATER_THAN_OR_EQUAL(">="),
    EQ("=="),
    NOT_EQ("!="),

    // Logical operators
    AND("&&"),
    OR("||"),

    // Compound assignment operators
    PLUS_ASSIGN("+="),
    MINUS_ASSIGN("-="),
    ASTERISK_ASSIGN("*="),
    SLASH_ASSIGN("/="),
    MODULUS_ASSIGN("%="),

    // Bitwise operators
    BITWISE_AND("&"),
    BITWISE_OR("|"),
    BITWISE_XOR("^"),
    BITWISE_NOT("~"),
    BITWISE_LEFT_SHIFT("<<"),
    BITWISE_RIGHT_SHIFT(">>"),

    // Delimiters
    COMMA(","),
    SEMICOLON(";"),
    COLON(":"),
    DOT("."),

    // Brackets and braces
    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),
    LBRACKET("["),
    RBRACKET("]"),

    // Keywords
    FUNCTION("fn"),
    LET("let"),
    TRUE("true"),
    FALSE("false"),
    IF("if"),
    ELSE("else"),
    ELIF("elif"),
    RETURN("return"),
    WHILE("while"),
    BREAK("break"),
    CONTINUE("continue"),
    FOR("for"),
    CONST("const"),
    CLASS("class"),
    EXTENDS("extends"),
    SUPER("super"),
    THIS("this"),
    NEW("new"),
    NULL("null"),

    // Special
    F_STRING("F_STRING");

    private final String literal;

    TokenType(String literal) {
        this.literal = literal;
    }

    public String getLiteral() {
        return literal;
    }

    @Override
    public String toString() {
        return literal;
    }
}