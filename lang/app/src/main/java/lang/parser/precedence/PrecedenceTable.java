package lang.parser.precedence;

import java.util.HashMap;
import java.util.Map;

import lang.token.TokenType;

public class PrecedenceTable {

    private final Map<TokenType, Precedence> precedences = new HashMap<>();

    /**
     * 🏗️ Creates a new precedence table with default rules
     * 
     * Sets up the standard mathematical and logical operator precedence.
     * Like creating a fresh rulebook with all the standard rules! 📚✨
     */
    public PrecedenceTable() {
        setupDefaultPrecedences();
    }

    /**
     * ⚙️ Sets up the default operator precedence rules
     * 
     * Configures the standard precedence for all built-in operators.
     * Like writing the fundamental rules of mathematics! 📐📝
     * 
     * This establishes the classic order:
     * - Parentheses and indexing (highest priority) 🔗
     * - Multiplication and division ✖️
     * - Addition and subtraction ➕
     * - Comparisons 🔢
     * - Logical operations 🔗
     * - Assignment (lowest priority) 📝
     */
    private void setupDefaultPrecedences() {
        // 🟰 Comparison operators
        precedences.put(TokenType.EQ, Precedence.EQUALS);
        precedences.put(TokenType.NOT_EQ, Precedence.EQUALS);
        precedences.put(TokenType.LESS_THAN, Precedence.LESS_GREATER);
        precedences.put(TokenType.GREATER_THAN, Precedence.LESS_GREATER);
        precedences.put(TokenType.LESS_THAN_OR_EQUAL, Precedence.LESS_GREATER);
        precedences.put(TokenType.GREATER_THAN_OR_EQUAL, Precedence.LESS_GREATER);

        // 🔗 Logical operators
        precedences.put(TokenType.AND, Precedence.LOGICAL_AND);
        precedences.put(TokenType.OR, Precedence.LOGICAL_OR);

        // ➕ Arithmetic operators
        precedences.put(TokenType.PLUS, Precedence.SUM);
        precedences.put(TokenType.MINUS, Precedence.SUM);
        precedences.put(TokenType.ASTERISK, Precedence.PRODUCT);
        precedences.put(TokenType.SLASH, Precedence.PRODUCT);
        precedences.put(TokenType.MODULUS, Precedence.PRODUCT);
        precedences.put(TokenType.INT_DIVISION, Precedence.PRODUCT);

        // 📝 Assignment
        precedences.put(TokenType.ASSIGN, Precedence.EQUALS);

        // 📞 Function calls and indexing
        precedences.put(TokenType.LPAREN, Precedence.CALL);
        precedences.put(TokenType.LBRACKET, Precedence.INDEX);
    }

    /**
     * 🔍 Gets the precedence for a token type
     */
    public Precedence getPrecedence(TokenType tokenType) {
        return precedences.getOrDefault(tokenType, Precedence.LOWEST);
    }

    /**
     * ⚙️ Sets a custom precedence for a token type
     * 
     * @param tokenType  The token type to set precedence for 🎫
     * @param precedence The precedence level to assign 📊
     */
    public void setPrecedence(TokenType tokenType, Precedence precedence) {
        precedences.put(tokenType, precedence);
    }
}
