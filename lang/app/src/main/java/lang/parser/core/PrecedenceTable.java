package lang.parser.core;

import java.util.HashMap;
import java.util.Map;

import lang.token.TokenType;

/**
 * 📊 PrecedenceTable - The Math Order of Operations Rulebook 📊
 * 
 * Manages operator precedence rules that determine the order of operations in
 * expressions.
 * Think of it as the mathematical rulebook that says "multiply before add!"
 * 📐📚
 * 
 * Just like in math class, operations have different priorities:
 * - 🔢 2 + 3 * 4 = 2 + 12 = 14 (not 5 * 4 = 20)
 * - 🔢 (2 + 3) * 4 = 5 * 4 = 20 (parentheses change the order)
 * 
 * This table ensures expressions are parsed correctly according to these rules!
 * ✅
 * 
 * Precedence levels (highest to lowest):
 * 1. 🔗 INDEX: array[index] - Array/object access
 * 2. 📞 CALL: function() - Function calls
 * 3. 🔄 PREFIX: -x, !x - Unary operators
 * 4. ✖️ PRODUCT: *, /, % - Multiplication, division, modulus
 * 5. ➕ SUM: +, - - Addition, subtraction
 * 6. 🔢 LESS_GREATER: <, >, <=, >= - Comparison operators
 * 7. 🟰 EQUALS: ==, != - Equality operators
 * 8. 🔗 LOGICAL_AND: && - Logical AND
 * 9. 🔗 LOGICAL_OR: || - Logical OR
 * 10. 🔽 LOWEST: Default for unknown operators
 */
public class PrecedenceTable {

    /**
     * 📈 Precedence - The Operator Priority Levels 📈
     * 
     * Defines the hierarchy of operator precedence with numeric levels.
     * Like a ranking system where higher numbers = higher priority! 🏆🔢
     * 
     * Each precedence level has a numeric value and can be compared to others.
     * This makes it easy to determine which operators should be evaluated first!
     */
    enum Precedence {
        LOWEST(1), // 🔽 Default/unknown operators
        LOGICAL_OR(2), // 🔗 || - Logical OR
        LOGICAL_AND(3), // 🔗 && - Logical AND
        EQUALS(4), // 🟰 ==, != - Equality comparison
        LESS_GREATER(5), // 🔢 >, <, >=, <= - Relational comparison
        SUM(6), // ➕ +, - - Addition and subtraction
        PRODUCT(7), // ✖️ *, /, % - Multiplication, division, modulus
        PREFIX(8), // 🔄 -x, !x - Unary operators
        CALL(9), // 📞 function() - Function calls
        INDEX(10); // 🔗 array[index] - Array/object access

        private final int level; // 🔢 The numeric priority level

        /**
         * 🏗️ Creates a precedence level with a numeric value
         * 
         * @param level The numeric priority (higher = more important) 🔢
         */
        Precedence(int level) {
            this.level = level;
        }

        /**
         * 🔢 Gets the numeric priority level
         * 
         * Returns the number that represents this precedence level.
         * Like getting the score in a ranking system! 🏆📊
         * 
         * @return The numeric precedence level 🔢
         */
        public int getLevel() {
            return level;
        }

        /**
         * 🏆 Checks if this precedence is higher than another
         * 
         * Compares two precedence levels to determine priority.
         * Like asking "Who goes first in line?" 🏆❓
         * 
         * @param other The other precedence to compare against 📊
         * @return True if this precedence is higher, false otherwise ✅❌
         */
        public boolean isHigherThan(Precedence other) {
            return this.level > other.level;
        }
    }

    private final Map<TokenType, Precedence> precedences = new HashMap<>(); // 📋 The precedence lookup table

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

        // 📝 Assignment
        precedences.put(TokenType.ASSIGN, Precedence.EQUALS);

        // 📞 Function calls and indexing
        precedences.put(TokenType.LPAREN, Precedence.CALL);
        precedences.put(TokenType.LBRACKET, Precedence.INDEX);
    }

    /**
     * 🔍 Gets the precedence for a token type
     * 
     * Looks up the precedence level for a specific operator.
     * Like checking the rulebook to see how important an operator is! 📚🔍
     * 
     * If the token type isn't found, returns LOWEST precedence as a safe default.
     * This ensures unknown operators don't break the parsing process.
     * 
     * Examples:
     * - TokenType.ASTERISK → PRODUCT (high precedence) ✖️
     * - TokenType.PLUS → SUM (medium precedence) ➕
     * - TokenType.OR → LOGICAL_OR (low precedence) 🔗
     * 
     * @param tokenType The token type to look up 🎫
     * @return The precedence level for that token type 📊
     */
    public Precedence getPrecedence(TokenType tokenType) {
        return precedences.getOrDefault(tokenType, Precedence.LOWEST);
    }

    /**
     * ⚙️ Sets a custom precedence for a token type
     * 
     * Allows overriding or adding new precedence rules.
     * Like writing a new rule in the mathematical rulebook! 📝📚
     * 
     * This is useful for:
     * - Adding custom operators 🔧
     * - Adjusting existing precedence 📊
     * - Language-specific modifications 🌐
     * 
     * Example:
     * ```
     * table.setPrecedence(TokenType.POWER, Precedence.PRODUCT);
     * ```
     * 
     * @param tokenType  The token type to set precedence for 🎫
     * @param precedence The precedence level to assign 📊
     */
    public void setPrecedence(TokenType tokenType, Precedence precedence) {
        precedences.put(tokenType, precedence);
    }
}
