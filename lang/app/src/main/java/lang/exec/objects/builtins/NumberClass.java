package lang.exec.objects.builtins;

import java.util.*;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.classes.*;
import lang.exec.objects.env.Environment;
import lang.exec.objects.env.EnvironmentFactory;
import lang.exec.objects.error.ErrorObject;
import lang.exec.validator.ObjectValidator;

/**
 * 🔢 NumberClass - Base Class for All Numeric Types 🔢
 * 
 * From first principles, a Number base class should provide:
 * - Common mathematical operations (abs, pow, sqrt, min, max)
 * - Arithmetic dunder methods (+, -, *, /, etc.)
 * - Comparison operations (==, !=, <, >, etc.)
 * - Type conversion methods
 * - Sign and magnitude operations
 * 
 * This establishes a proper type hierarchy:
 * Object → Number → Integer/Float
 * 
 * Benefits:
 * - Code reuse for common numeric operations
 * - Polymorphism (functions can accept "any number")
 * - Consistent behavior across numeric types
 * - Natural mixed-type arithmetic
 */
public class NumberClass extends ClassObject {

    public static final String NUMBER_CLASS_NAME = "Number";
    private static NumberClass instance;

    private NumberClass() {
        super(
                NUMBER_CLASS_NAME,
                Optional.of(BaseObjectClass.getInstance()),
                Optional.empty(), // Abstract class - no direct instantiation
                createNumberMethods(),
                new Environment());
    }

    public static NumberClass getInstance() {
        if (instance == null) {
            instance = new NumberClass();
        }
        return instance;
    }

    private static Map<String, MethodObject> createNumberMethods() {
        var env = EnvironmentFactory.empty();
        Map<String, MethodObject> methods = new HashMap<>();

        // Mathematical operations (common to all numbers)
        methods.put("abs", createAbsMethod(env));
        methods.put("pow", createPowMethod(env));
        methods.put("sqrt", createSqrtMethod(env));
        methods.put("min", createMinMethod(env));
        methods.put("max", createMaxMethod(env));
        methods.put("sign", createSignMethod(env));

        // Type checking
        methods.put("isPositive", createIsPositiveMethod(env));
        methods.put("isNegative", createIsNegativeMethod(env));
        methods.put("isZero", createIsZeroMethod(env));
        methods.put("isInteger", createIsIntegerMethod(env));
        methods.put("isFloat", createIsFloatMethod(env));

        // Type conversion (abstract - to be overridden)
        methods.put("toInteger", createToIntegerMethod(env));
        methods.put("toFloat", createToFloatMethod(env));
        methods.put("toString", createToStringMethod(env));

        // Arithmetic dunder methods (handle mixed-type operations)
        methods.put("__add__", createNumberAddMethod(env));
        methods.put("__sub__", createNumberSubMethod(env));
        methods.put("__mul__", createNumberMulMethod(env));
        methods.put("__div__", createNumberDivMethod(env));
        methods.put("__floordiv__", createNumberFloorDivMethod(env));
        methods.put("__mod__", createNumberModMethod(env));
        methods.put("__pow__", createNumberPowMethod(env));
        methods.put("__neg__", createNumberNegMethod(env));

        // Comparison dunder methods
        methods.put("__eq__", createNumberEqMethod(env));
        methods.put("__ne__", createNumberNeMethod(env));
        methods.put("__lt__", createNumberLtMethod(env));
        methods.put("__le__", createNumberLeMethod(env));
        methods.put("__gt__", createNumberGtMethod(env));
        methods.put("__ge__", createNumberGeMethod(env));

        return methods;
    }

    // Mathematical operations
    private static MethodObject createAbsMethod(Environment env) {
        return new BuiltInMethod(
                "abs",
                "Returns the absolute value of this number",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("abs() takes no arguments, got " + args.length);
                    }

                    double value = getNumericValue(instance);

                    if (isIntegerInstance(instance) && value == (long) value) {
                        return IntegerClass.createIntegerInstance((long) Math.abs(value));
                    } else {
                        return FloatClass.createFloatInstance(Math.abs(value));
                    }
                },
                env);
    }

    private static MethodObject createPowMethod(Environment env) {
        return new BuiltInMethod(
                "pow",
                "Returns this number raised to the power of the exponent",
                List.of("exponent"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("pow() takes exactly 1 argument, got " + args.length);
                    }

                    double base = getNumericValue(instance);
                    double exponent = getNumericValue(args[0]);

                    double result = Math.pow(base, exponent);

                    // Return integer if both operands are integers and result is whole
                    if (isIntegerInstance(instance) && isIntegerInstance(args[0]) &&
                            exponent >= 0 && result == (long) result) {
                        return IntegerClass.createIntegerInstance((long) result);
                    } else {
                        return FloatClass.createFloatInstance(result);
                    }
                },
                env);
    }

    private static MethodObject createSqrtMethod(Environment env) {
        return new BuiltInMethod(
                "sqrt",
                "Returns the square root of this number",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("sqrt() takes no arguments, got " + args.length);
                    }

                    double value = getNumericValue(instance);
                    if (value < 0) {
                        return new ErrorObject("Cannot take square root of negative number");
                    }

                    double result = Math.sqrt(value);
                    return FloatClass.createFloatInstance(result);
                },
                env);
    }

    private static MethodObject createMinMethod(Environment env) {
        return new BuiltInMethod(
                "min",
                "Returns the minimum of this number and another",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("min() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isNumericInstance(args[0])) {
                        return new ErrorObject("min() argument must be a number");
                    }

                    double thisValue = getNumericValue(instance);
                    double otherValue = getNumericValue(args[0]);
                    double result = Math.min(thisValue, otherValue);

                    // Preserve type if both are same type and result equals one of them
                    if (result == thisValue) {
                        return instance; // Return this instance
                    } else {
                        return args[0]; // Return other instance
                    }
                },
                env);
    }

    private static MethodObject createMaxMethod(Environment env) {
        return new BuiltInMethod(
                "max",
                "Returns the maximum of this number and another",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("max() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isNumericInstance(args[0])) {
                        return new ErrorObject("max() argument must be a number");
                    }

                    double thisValue = getNumericValue(instance);
                    double otherValue = getNumericValue(args[0]);

                    // Preserve type if both are same type and result equals one of them
                    if (thisValue >= otherValue) {
                        return instance; // Return this instance
                    } else {
                        return args[0]; // Return other instance
                    }
                },
                env);
    }

    private static MethodObject createSignMethod(Environment env) {
        return new BuiltInMethod(
                "sign",
                "Returns -1, 0, or 1 depending on the sign of this number",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("sign() takes no arguments, got " + args.length);
                    }

                    double value = getNumericValue(instance);
                    long sign = (long) Math.signum(value);
                    return IntegerClass.createIntegerInstance(sign);
                },
                env);
    }

    // Type checking methods
    private static MethodObject createIsPositiveMethod(Environment env) {
        return new BuiltInMethod(
                "isPositive",
                "Returns true if this number is positive",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("isPositive() takes no arguments, got " + args.length);
                    }

                    double value = getNumericValue(instance);
                    return BooleanClass.createBooleanInstance(value > 0);
                },
                env);
    }

    private static MethodObject createIsNegativeMethod(Environment env) {
        return new BuiltInMethod(
                "isNegative",
                "Returns true if this number is negative",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("isNegative() takes no arguments, got " + args.length);
                    }

                    double value = getNumericValue(instance);
                    return BooleanClass.createBooleanInstance(value < 0);
                },
                env);
    }

    private static MethodObject createIsZeroMethod(Environment env) {
        return new BuiltInMethod(
                "isZero",
                "Returns true if this number is zero",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("isZero() takes no arguments, got " + args.length);
                    }

                    double value = getNumericValue(instance);
                    return BooleanClass.createBooleanInstance(value == 0.0);
                },
                env);
    }

    private static MethodObject createIsIntegerMethod(Environment env) {
        return new BuiltInMethod(
                "isInteger",
                "Returns true if this number is an integer",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("isInteger() takes no arguments, got " + args.length);
                    }

                    return BooleanClass.createBooleanInstance(isIntegerInstance(instance));
                },
                env);
    }

    private static MethodObject createIsFloatMethod(Environment env) {
        return new BuiltInMethod(
                "isFloat",
                "Returns true if this number is a float",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("isFloat() takes no arguments, got " + args.length);
                    }

                    return BooleanClass.createBooleanInstance(isFloatInstance(instance));
                },
                env);
    }

    // Type conversion methods (to be overridden by subclasses)
    private static MethodObject createToIntegerMethod(Environment env) {
        return new BuiltInMethod(
                "toInteger",
                "Converts this number to an integer",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("toInteger() takes no arguments, got " + args.length);
                    }

                    double value = getNumericValue(instance);
                    return IntegerClass.createIntegerInstance((long) value);
                },
                env);
    }

    private static MethodObject createToFloatMethod(Environment env) {
        return new BuiltInMethod(
                "toFloat",
                "Converts this number to a float",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("toFloat() takes no arguments, got " + args.length);
                    }

                    double value = getNumericValue(instance);
                    return FloatClass.createFloatInstance(value);
                },
                env);
    }

    private static MethodObject createToStringMethod(Environment env) {
        return new BuiltInMethod(
                "toString",
                "Converts this number to a string",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("toString() takes no arguments, got " + args.length);
                    }

                    double value = getNumericValue(instance);
                    if (isIntegerInstance(instance)) {
                        return StringClass.createStringInstance(String.valueOf((long) value));
                    } else {
                        return StringClass.createStringInstance(String.valueOf(value));
                    }
                },
                env);
    }

    // Arithmetic dunder methods with intelligent type promotion
    private static MethodObject createNumberAddMethod(Environment env) {
        return new BuiltInMethod(
                "__add__",
                "Addition operator for numbers",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__add__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isNumericInstance(args[0])) {
                        return new ErrorObject("Cannot add number and " + getTypeName(args[0]));
                    }

                    double leftValue = getNumericValue(instance);
                    double rightValue = getNumericValue(args[0]);
                    double result = leftValue + rightValue;

                    // Type promotion: Integer + Integer = Integer, otherwise Float
                    if (isIntegerInstance(instance) && isIntegerInstance(args[0])) {
                        return IntegerClass.createIntegerInstance((long) result);
                    } else {
                        return FloatClass.createFloatInstance(result);
                    }
                },
                env);
    }

    private static MethodObject createNumberSubMethod(Environment env) {
        return new BuiltInMethod(
                "__sub__",
                "Subtraction operator for numbers",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__sub__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isNumericInstance(args[0])) {
                        return new ErrorObject("Cannot subtract " + getTypeName(args[0]) + " from number");
                    }

                    double leftValue = getNumericValue(instance);
                    double rightValue = getNumericValue(args[0]);
                    double result = leftValue - rightValue;

                    // Type promotion: Integer - Integer = Integer, otherwise Float
                    if (isIntegerInstance(instance) && isIntegerInstance(args[0])) {
                        return IntegerClass.createIntegerInstance((long) result);
                    } else {
                        return FloatClass.createFloatInstance(result);
                    }
                },
                env);
    }

    private static MethodObject createNumberMulMethod(Environment env) {
        return new BuiltInMethod(
                "__mul__",
                "Multiplication operator for numbers",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__mul__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isNumericInstance(args[0])) {
                        return new ErrorObject("Cannot multiply number and " + getTypeName(args[0]));
                    }

                    double leftValue = getNumericValue(instance);
                    double rightValue = getNumericValue(args[0]);
                    double result = leftValue * rightValue;

                    // Type promotion: Integer * Integer = Integer, otherwise Float
                    if (isIntegerInstance(instance) && isIntegerInstance(args[0])) {
                        return IntegerClass.createIntegerInstance((long) result);
                    } else {
                        return FloatClass.createFloatInstance(result);
                    }
                },
                env);
    }

    private static MethodObject createNumberDivMethod(Environment env) {
        return new BuiltInMethod(
                "__div__",
                "Division operator for numbers (always returns float)",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__div__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isNumericInstance(args[0])) {
                        return new ErrorObject("Cannot divide number by " + getTypeName(args[0]));
                    }

                    double leftValue = getNumericValue(instance);
                    double rightValue = getNumericValue(args[0]);

                    if (rightValue == 0.0) {
                        return new ErrorObject("Division by zero");
                    }

                    double result = leftValue / rightValue;
                    return FloatClass.createFloatInstance(result);
                },
                env);
    }

    private static MethodObject createNumberFloorDivMethod(Environment env) {
        return new BuiltInMethod(
                "__floordiv__",
                "Floor division operator for numbers",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__floordiv__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isNumericInstance(args[0])) {
                        return new ErrorObject("Cannot floor divide number by " + getTypeName(args[0]));
                    }

                    double leftValue = getNumericValue(instance);
                    double rightValue = getNumericValue(args[0]);

                    if (rightValue == 0.0) {
                        return new ErrorObject("Floor division by zero");
                    }

                    long result = (long) Math.floor(leftValue / rightValue);
                    return IntegerClass.createIntegerInstance(result);
                },
                env);
    }

    private static MethodObject createNumberModMethod(Environment env) {
        return new BuiltInMethod(
                "__mod__",
                "Modulo operator for numbers",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__mod__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isNumericInstance(args[0])) {
                        return new ErrorObject("Cannot take modulo of number and " + getTypeName(args[0]));
                    }

                    double leftValue = getNumericValue(instance);
                    double rightValue = getNumericValue(args[0]);
                    double result = leftValue % rightValue;

                    // Type promotion: Integer % Integer = Integer, otherwise Float
                    if (isIntegerInstance(instance) && isIntegerInstance(args[0])) {
                        return IntegerClass.createIntegerInstance((long) result);
                    } else {
                        return FloatClass.createFloatInstance(result);
                    }
                },
                env);
    }

    private static MethodObject createNumberPowMethod(Environment env) {
        return new BuiltInMethod(
                "__pow__",
                "Power operator for numbers",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__pow__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isNumericInstance(args[0])) {
                        return new ErrorObject("Cannot raise number to power of " + getTypeName(args[0]));
                    }

                    double base = getNumericValue(instance);
                    double exponent = getNumericValue(args[0]);
                    double result = Math.pow(base, exponent);

                    // Return integer if both operands are integers and result is whole
                    if (isIntegerInstance(instance) && isIntegerInstance(args[0]) &&
                            exponent >= 0 && result == (long) result) {
                        return IntegerClass.createIntegerInstance((long) result);
                    } else {
                        return FloatClass.createFloatInstance(result);
                    }
                },
                env);
    }

    private static MethodObject createNumberNegMethod(Environment env) {
        return new BuiltInMethod(
                "__neg__",
                "Negation operator for numbers",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("__neg__() takes no arguments, got " + args.length);
                    }

                    double value = getNumericValue(instance);

                    if (isIntegerInstance(instance)) {
                        long intValue = (long) value;
                        if (intValue == Long.MIN_VALUE) {
                            // Handle overflow by returning float
                            return FloatClass.createFloatInstance(-value);
                        }
                        return IntegerClass.createIntegerInstance(-intValue);
                    } else {
                        return FloatClass.createFloatInstance(-value);
                    }
                },
                env);
    }

    // Comparison dunder methods
    private static MethodObject createNumberEqMethod(Environment env) {
        return new BuiltInMethod(
                "__eq__",
                "Equality operator for numbers",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__eq__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isNumericInstance(args[0])) {
                        return BooleanClass.createBooleanInstance(false);
                    }

                    double leftValue = getNumericValue(instance);
                    double rightValue = getNumericValue(args[0]);

                    return BooleanClass.createBooleanInstance(Double.compare(leftValue, rightValue) == 0);
                },
                env);
    }

    private static MethodObject createNumberNeMethod(Environment env) {
        return new BuiltInMethod(
                "__ne__",
                "Inequality operator for numbers",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__ne__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isNumericInstance(args[0])) {
                        return BooleanClass.createBooleanInstance(true);
                    }

                    double leftValue = getNumericValue(instance);
                    double rightValue = getNumericValue(args[0]);

                    return BooleanClass.createBooleanInstance(Double.compare(leftValue, rightValue) != 0);
                },
                env);
    }

    private static MethodObject createNumberLtMethod(Environment env) {
        return new BuiltInMethod(
                "__lt__",
                "Less than operator for numbers",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__lt__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isNumericInstance(args[0])) {
                        return new ErrorObject("Cannot compare number with " + getTypeName(args[0]));
                    }

                    double leftValue = getNumericValue(instance);
                    double rightValue = getNumericValue(args[0]);

                    return BooleanClass.createBooleanInstance(Double.compare(leftValue, rightValue) < 0);
                },
                env);
    }

    private static MethodObject createNumberLeMethod(Environment env) {
        return new BuiltInMethod(
                "__le__",
                "Less than or equal operator for numbers",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__le__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isNumericInstance(args[0])) {
                        return new ErrorObject("Cannot compare number with " + getTypeName(args[0]));
                    }

                    double leftValue = getNumericValue(instance);
                    double rightValue = getNumericValue(args[0]);

                    return BooleanClass.createBooleanInstance(Double.compare(leftValue, rightValue) <= 0);
                },
                env);
    }

    private static MethodObject createNumberGtMethod(Environment env) {
        return new BuiltInMethod(
                "__gt__",
                "Greater than operator for numbers",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__gt__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isNumericInstance(args[0])) {
                        return new ErrorObject("Cannot compare number with " + getTypeName(args[0]));
                    }

                    double leftValue = getNumericValue(instance);
                    double rightValue = getNumericValue(args[0]);

                    return BooleanClass.createBooleanInstance(Double.compare(leftValue, rightValue) > 0);
                },
                env);
    }

    private static MethodObject createNumberGeMethod(Environment env) {
        return new BuiltInMethod(
                "__ge__",
                "Greater than or equal operator for numbers",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__ge__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isNumericInstance(args[0])) {
                        return new ErrorObject("Cannot compare number with " + getTypeName(args[0]));
                    }

                    double leftValue = getNumericValue(instance);
                    double rightValue = getNumericValue(args[0]);

                    return BooleanClass.createBooleanInstance(Double.compare(leftValue, rightValue) >= 0);
                },
                env);
    }

    // Helper methods
    private static double getNumericValue(BaseObject obj) {
        if (ObjectValidator.isInstance(obj)) {
            InstanceObject instance = ObjectValidator.asInstance(obj);
            Optional<BaseObject> valueProperty = instance.getProperty("value");
            if (valueProperty.isPresent()) {
                BaseObject value = valueProperty.get();
                if (ObjectValidator.isInteger(value)) {
                    return (double) ObjectValidator.asInteger(value).getValue();
                } else if (ObjectValidator.isFloat(value)) {
                    return ObjectValidator.asFloat(value).getValue();
                }
            }
        }

        // Fallback for old primitive objects
        if (ObjectValidator.isInteger(obj)) {
            return (double) ObjectValidator.asInteger(obj).getValue();
        } else if (ObjectValidator.isFloat(obj)) {
            return ObjectValidator.asFloat(obj).getValue();
        }

        throw new RuntimeException("Expected numeric value");
    }

    private static boolean isNumericInstance(BaseObject obj) {
        return isIntegerInstance(obj) || isFloatInstance(obj);
    }

    private static boolean isIntegerInstance(BaseObject obj) {
        if (ObjectValidator.isInstance(obj)) {
            InstanceObject instance = ObjectValidator.asInstance(obj);
            String className = instance.getClassObject().getName();
            return className.equals("Integer");
        }
        return ObjectValidator.isInteger(obj);
    }

    private static boolean isFloatInstance(BaseObject obj) {
        if (ObjectValidator.isInstance(obj)) {
            InstanceObject instance = ObjectValidator.asInstance(obj);
            String className = instance.getClassObject().getName();
            return className.equals("Float");
        }
        return ObjectValidator.isFloat(obj);
    }

    private static String getTypeName(BaseObject obj) {
        if (ObjectValidator.isInstance(obj)) {
            InstanceObject instance = ObjectValidator.asInstance(obj);
            return instance.getClassObject().getName();
        }
        return obj.type().toString();
    }
}
