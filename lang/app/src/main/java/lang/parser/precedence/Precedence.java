package lang.parser.precedence;

public enum Precedence {
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
