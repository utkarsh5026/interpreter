package lang.exec.objects.literals;

import lang.exec.base.BaseObject;
import lang.exec.base.ObjectType;

/**
 * 🌊 FloatObject - Runtime Floating-Point Number Object 🌊
 * 
 * Represents floating-point values during program execution.
 */
public final class FloatObject implements BaseObject {

    private final double value; // 🔢 The actual floating-point value

    /**
     * 🏗️ Creates a new FloatObject with the given value
     * 
     * @param value The double value to store
     */
    public FloatObject(double value) {
        this.value = value;
    }

    /**
     * 🔢 Gets the floating-point value
     * 
     * @return The double value stored in this object
     */
    public double getValue() {
        return value;
    }

    /**
     * 🏷️ Returns the object type identifier
     * 
     * @return FLOAT object type
     */
    @Override
    public ObjectType type() {
        return ObjectType.FLOAT;
    }

    /**
     * 📝 Returns string representation of this float
     * 
     * From first principles, float-to-string conversion should:
     * - Show decimal point for whole numbers (2.0 not 2)
     * - Use scientific notation for very large/small numbers
     * - Handle special values appropriately
     * - Remove unnecessary trailing zeros where appropriate
     * 
     * @return String representation of the float value
     */
    @Override
    public String inspect() {
        // Handle special values
        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? "Infinity" : "-Infinity";
        }

        String str = Double.toString(value);

        boolean isWholeNumber = Double.isFinite(value) && value == Math.floor(value);
        if (isWholeNumber && !str.contains(".") && !str.contains("E") && !str.contains("e")) {
            str += ".0";
        }

        return str;
    }

    /**
     * ✅ Determines truthiness of this float
     * 
     * From first principles, floating-point truthiness rules:
     * - 0.0 is falsy
     * - -0.0 is falsy (IEEE 754 has both +0.0 and -0.0)
     * - NaN is falsy (represents invalid/undefined values)
     * - Infinity is falsy (represents unbounded values)
     * - All other finite non-zero values are truthy
     * 
     * @return true if this float should be considered true in boolean context
     */
    @Override
    public boolean isTruthy() {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return false;
        }

        return value != 0.0;
    }

}