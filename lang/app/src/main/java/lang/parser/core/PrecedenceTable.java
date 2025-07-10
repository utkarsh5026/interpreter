package lang.parser.core;

import java.util.HashMap;
import java.util.Map;

import lang.token.TokenType;

public class PrecedenceTable {

    enum Precedence {
        LOWEST(1),
        LOGICAL_OR(2), // ||
        LOGICAL_AND(3), // &&
        EQUALS(4), // ==, !=
        LESS_GREATER(5), // >, <, >=, <=
        SUM(6), // +, -
        PRODUCT(7), // *, /, %
        PREFIX(8), // -x, !x
        CALL(9), // function()
        INDEX(10); // array[index]

        private final int level;

        Precedence(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

        public boolean isHigherThan(Precedence other) {
            return this.level > other.level;
        }
    }

    private final Map<TokenType, Precedence> precedences = new HashMap<>();

    public PrecedenceTable() {
        setupDefaultPrecedences();
    }

    private void setupDefaultPrecedences() {
        // Comparison operators
        precedences.put(TokenType.EQ, Precedence.EQUALS);
        precedences.put(TokenType.NOT_EQ, Precedence.EQUALS);
        precedences.put(TokenType.LESS_THAN, Precedence.LESS_GREATER);
        precedences.put(TokenType.GREATER_THAN, Precedence.LESS_GREATER);
        precedences.put(TokenType.LESS_THAN_OR_EQUAL, Precedence.LESS_GREATER);
        precedences.put(TokenType.GREATER_THAN_OR_EQUAL, Precedence.LESS_GREATER);

        // Logical operators
        precedences.put(TokenType.AND, Precedence.LOGICAL_AND);
        precedences.put(TokenType.OR, Precedence.LOGICAL_OR);

        // Arithmetic operators
        precedences.put(TokenType.PLUS, Precedence.SUM);
        precedences.put(TokenType.MINUS, Precedence.SUM);
        precedences.put(TokenType.ASTERISK, Precedence.PRODUCT);
        precedences.put(TokenType.SLASH, Precedence.PRODUCT);
        precedences.put(TokenType.MODULUS, Precedence.PRODUCT);

        // Assignment
        precedences.put(TokenType.ASSIGN, Precedence.EQUALS);

        // Function calls and indexing
        precedences.put(TokenType.LPAREN, Precedence.CALL);
        precedences.put(TokenType.LBRACKET, Precedence.INDEX);
    }

    public Precedence getPrecedence(TokenType tokenType) {
        return precedences.getOrDefault(tokenType, Precedence.LOWEST);
    }

    public void setPrecedence(TokenType tokenType, Precedence precedence) {
        precedences.put(tokenType, precedence);
    }
}