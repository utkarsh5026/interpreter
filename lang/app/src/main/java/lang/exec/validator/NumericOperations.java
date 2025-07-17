package lang.exec.validator;

import java.util.Optional;

import lang.exec.base.BaseObject;
import lang.exec.objects.*;

/**
 * Utility class for numeric type conversions and arithmetic operations.
 * This handles type promotion, conversion, and numeric operation utilities.
 */
public final class NumericOperations {

    private NumericOperations() {
    }

    /**
     * Converts a BaseObject to Double if it's a numeric type.
     * 
     * @param obj The object to convert
     * @return Double value if conversion is possible, null otherwise
     */
    public static Optional<Double> toDouble(BaseObject obj) {
        if (ObjectValidator.isInteger(obj)) {
            return Optional.of((double) ObjectValidator.asInteger(obj).getValue());
        } else if (ObjectValidator.isFloat(obj)) {
            return Optional.of(ObjectValidator.asFloat(obj).getValue());
        }
        return Optional.empty();
    }

    /**
     * Converts a BaseObject to Long if it's an integer type.
     * 
     * @param obj The object to convert
     * @return Long value if conversion is possible, null otherwise
     */
    public static Optional<Long> toLong(BaseObject obj) {
        if (ObjectValidator.isInteger(obj)) {
            return Optional.of(ObjectValidator.asInteger(obj).getValue());
        }
        return Optional.empty();
    }

    /**
     * 🔢 Promotes numeric objects to common type for operations
     * 
     * From first principles, when performing operations between integers
     * and floats, we need to promote them to a common type. The rule is:
     * - int + int → int
     * - int + float → float
     * - float + int → float
     * - float + float → float
     * 
     * This method returns the appropriate promoted type.
     * 
     * @param left  First operand
     * @param right Second operand
     * @return Class representing the promoted type (Long or Double)
     */
    public static Class<?> getPromotedType(BaseObject left, BaseObject right) {
        boolean leftIsFloat = ObjectValidator.isFloat(left);
        boolean rightIsFloat = ObjectValidator.isFloat(right);

        if (leftIsFloat || rightIsFloat) {
            return Double.class;
        }

        return Long.class;
    }

    /**
     * Creates the appropriate numeric object based on the promoted type.
     * 
     * @param value      The numeric value to wrap
     * @param targetType The target type (Long.class or Double.class)
     * @return IntegerObject for Long type, FloatObject for Double type
     */
    public static BaseObject createNumericObject(Number value, Class<?> targetType) {
        if (targetType == Double.class) {
            return new FloatObject(value.doubleValue());
        } else if (targetType == Long.class) {
            return new IntegerObject(value.longValue());
        }
        throw new IllegalArgumentException("Unsupported numeric type: " + targetType);
    }

    /**
     * Performs type-safe numeric addition with automatic promotion.
     * 
     * @param left  Left operand
     * @param right Right operand
     * @return Result object with appropriate type, or null if not numeric
     */
    public static Optional<BaseObject> addNumeric(BaseObject left, BaseObject right) {
        if (!isNumericPair(left, right)) {
            return Optional.empty();
        }

        Class<?> targetType = getPromotedType(left, right);

        if (targetType == Double.class) {
            Optional<Double> leftVal = toDouble(left);
            Optional<Double> rightVal = toDouble(right);

            if (leftVal.isPresent() && rightVal.isPresent()) {
                var result = new FloatObject(leftVal.get() + rightVal.get());
                return Optional.of(result);
            }

            return Optional.empty();
        } else {
            Optional<Long> leftVal = toLong(left);
            Optional<Long> rightVal = toLong(right);
            if (leftVal.isPresent() && rightVal.isPresent()) {
                var result = new IntegerObject(leftVal.get() + rightVal.get());
                return Optional.of(result);
            }
            return Optional.empty();
        }
    }

    /**
     * Checks if both objects are numeric types.
     * 
     * @param left  First object
     * @param right Second object
     * @return true if both are numeric, false otherwise
     */
    public static boolean isNumericPair(BaseObject left, BaseObject right) {
        return ObjectValidator.isNumeric(left) && ObjectValidator.isNumeric(right);
    }

    /**
     * Compares two numeric objects for equality, handling type promotion.
     * 
     * @param left  First operand
     * @param right Second operand
     * @return true if numerically equal, false otherwise
     */
    public static boolean areNumericallyEqual(BaseObject left, BaseObject right) {
        if (!isNumericPair(left, right)) {
            return false;
        }

        Optional<Double> leftVal = toDouble(left);
        Optional<Double> rightVal = toDouble(right);

        return leftVal.isPresent() && rightVal.isPresent() && leftVal.get().equals(rightVal.get());
    }
}