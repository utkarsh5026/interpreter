package lang.ast.literals;

import java.util.Optional;

import lang.ast.base.Expression;
import lang.token.Token;
import lang.ast.visitor.AstVisitor;

/**
 * 🌊 FloatLiteral - Floating-Point Number AST Node 🌊
 * 
 * Represents a floating-point literal in the Abstract Syntax Tree.
 * 
 * From first principles, floating-point numbers are:
 * - Real numbers that can have fractional parts
 * - Stored in binary using IEEE 754 standard
 * - Have finite precision (can't represent all real numbers exactly)
 * - Support same operations as integers plus some float-specific ones
 * 
 * Examples of float literals:
 * - 3.14159 (pi approximation)
 * - 0.5 (half)
 * - 2.0 (whole number as float)
 * - .75 (starts with decimal point)
 * - 1000.0 (large float)
 * - 0.001 (small float)
 * 
 * Design decisions:
 * - Use Java's `double` type for maximum precision (64-bit IEEE 754)
 * - Store the original token for source location tracking
 * - Implement visitor pattern for AST traversal
 * - Provide both value access and string representation
 * 
 * Why double instead of float?
 * - Double provides 64-bit precision vs float's 32-bit
 * - Modern systems handle doubles efficiently
 * - Java's Math library uses double
 * - Reduces precision loss in calculations
 */
public class FloatLiteral extends Expression {

    private final double value; // 🔢 The actual floating-point value

    /**
     * 🏗️ Creates a new FloatLiteral from a token and parsed value
     * 
     * @param token The token that represents this float literal in source code
     * @param value The parsed double value of the float
     */
    public FloatLiteral(Token token, double value) {
        super(token);
        this.value = value;
    }

    /**
     * 🔢 Gets the floating-point value
     * 
     * @return The double value of this float literal
     */
    public double getValue() {
        return value;
    }

    /**
     * 🔍 Checks if this float represents a whole number
     * 
     * Useful for determining if float operations should produce integer results
     * or for type coercion decisions.
     * 
     * Examples:
     * - 3.0 → true (whole number)
     * - 3.14 → false (has fractional part)
     * - -5.0 → true (negative whole number)
     * 
     * @return True if the float has no fractional part, false otherwise
     */
    public boolean isWholeNumber() {
        return value == Math.floor(value);
    }

    /**
     * 🔢 Converts to integer if possible
     * 
     * Useful for operations that mix integers and floats.
     * Only works if the float represents a whole number within integer range.
     * 
     * @return The integer value if this float is a whole number, null otherwise
     */
    public Optional<Long> toIntegerIfWhole() {
        if (isWholeNumber() && value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) {
            return Optional.of((long) value);
        }
        return Optional.empty();
    }

    /**
     * ✅ Checks if this float is a valid finite number
     * 
     * Floating-point numbers can be:
     * - Finite: normal numbers like 3.14, 0.0, -5.2
     * - Infinite: result of 1.0/0.0
     * - NaN: result of 0.0/0.0 or sqrt(-1)
     * 
     * @return True if the float is finite, false if infinite or NaN
     */
    public boolean isFinite() {
        return Double.isFinite(value);
    }

    /**
     * 🔍 Checks if this float is Not-a-Number (NaN)
     * 
     * NaN occurs from invalid operations like:
     * - 0.0 / 0.0
     * - sqrt(-1.0)
     * - infinity - infinity
     * 
     * @return True if the value is NaN, false otherwise
     */
    public boolean isNaN() {
        return Double.isNaN(value);
    }

    /**
     * 🎯 Visitor pattern support for AST traversal
     * 
     * Allows different operations to be performed on this node
     * without modifying the node class itself.
     * 
     * @param visitor The visitor to accept
     * @return The result of the visitor operation
     */
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFloatLiteral(this);
    }

    /**
     * 📝 String representation of this float literal
     * 
     * Uses Java's default double-to-string conversion, which:
     * - Removes unnecessary trailing zeros
     * - Uses scientific notation for very large/small numbers
     * - Handles special values (NaN, Infinity)
     * 
     * @return String representation of the float value
     */
    @Override
    public String toString() {
        // Handle special cases
        if (isNaN()) {
            return "NaN";
        }
        if (!isFinite()) {
            return value > 0 ? "Infinity" : "-Infinity";
        }
        String str = Double.toString(value);

        if (isWholeNumber() && !str.contains(".") && !str.contains("E") && !str.contains("e")) {
            str += ".0";
        }

        return str;
    }
}