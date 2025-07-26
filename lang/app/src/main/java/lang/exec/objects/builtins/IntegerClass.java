package lang.exec.objects.builtins;

import java.util.*;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.classes.*;
import lang.exec.objects.env.Environment;
import lang.exec.objects.error.ErrorObject;
import lang.exec.objects.literals.*;
import lang.exec.validator.ObjectValidator;

/**
 * 🔢 IntegerClass - Built-in Integer Type Class 🔢
 * 
 * From first principles, integers should have methods for:
 * - Mathematical operations (abs, pow, etc.)
 * - Type conversion (toString, toFloat)
 * - Comparison utilities
 * - Bitwise operations
 * - Number theory functions (gcd, etc.)
 */
public class IntegerClass extends ClassObject {

    public static final String INTEGER_CLASS_NAME = "Integer";
    private static IntegerClass instance;

    private IntegerClass() {
        super(
                INTEGER_CLASS_NAME,
                Optional.of(NumberClass.getInstance()), // Inherit from Number
                Optional.empty(), // No constructor - integers are created via literals
                createIntegerMethods(),
                new Environment());
    }

    public static IntegerClass getInstance() {
        if (instance == null) {
            instance = new IntegerClass();
        }
        return instance;
    }

    private static Map<String, MethodObject> createIntegerMethods() {
        var env = new Environment();
        Map<String, MethodObject> methods = new HashMap<>();

        // Integer-specific methods (Number methods will be inherited)
        methods.put("isEven", createIsEvenMethod(env));
        methods.put("isOdd", createIsOddMethod(env));

        // Bitwise operations (unique to integers)
        methods.put("bitwiseAnd", createBitwiseAndMethod(env));
        methods.put("bitwiseOr", createBitwiseOrMethod(env));
        methods.put("bitwiseXor", createBitwiseXorMethod(env));
        methods.put("bitwiseNot", createBitwiseNotMethod(env));
        methods.put("leftShift", createLeftShiftMethod(env));
        methods.put("rightShift", createRightShiftMethod(env));

        return methods;
    }

    // Integer-specific methods (implementation details similar to before)
    private static MethodObject createIsEvenMethod(Environment env) {
        return new BuiltInMethod(
                "isEven",
                "Returns true if this integer is even",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("isEven() takes no arguments, got " + args.length);
                    }

                    long value = getIntegerValue(instance);
                    return BooleanClass.createBooleanInstance(value % 2 == 0);
                },
                env);
    }

    private static MethodObject createIsOddMethod(Environment env) {
        return new BuiltInMethod(
                "isOdd",
                "Returns true if this integer is odd",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("isOdd() takes no arguments, got " + args.length);
                    }

                    long value = getIntegerValue(instance);
                    return BooleanClass.createBooleanInstance(value % 2 != 0);
                },
                env);
    }

    // Bitwise operations (integer-specific)
    private static MethodObject createBitwiseAndMethod(Environment env) {
        return new BuiltInMethod(
                "bitwiseAnd",
                "Returns bitwise AND of this integer with another",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("bitwiseAnd() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isIntegerInstance(args[0])) {
                        return new ErrorObject("bitwiseAnd() argument must be an integer");
                    }

                    long left = getIntegerValue(instance);
                    long right = getIntegerValue(args[0]);
                    return createIntegerInstance(left & right);
                },
                env);
    }

    // ... (other bitwise methods similar to before)

    private static MethodObject createBitwiseOrMethod(Environment env) {
        return new BuiltInMethod(
                "bitwiseOr",
                "Returns bitwise OR of this integer with another",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("bitwiseOr() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isIntegerInstance(args[0])) {
                        return new ErrorObject("bitwiseOr() argument must be an integer");
                    }

                    long left = getIntegerValue(instance);
                    long right = getIntegerValue(args[0]);
                    return createIntegerInstance(left | right);
                },
                env);
    }

    private static MethodObject createBitwiseXorMethod(Environment env) {
        return new BuiltInMethod(
                "bitwiseXor",
                "Returns bitwise XOR of this integer with another",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("bitwiseXor() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isIntegerInstance(args[0])) {
                        return new ErrorObject("bitwiseXor() argument must be an integer");
                    }

                    long left = getIntegerValue(instance);
                    long right = getIntegerValue(args[0]);
                    return createIntegerInstance(left ^ right);
                },
                env);
    }

    private static MethodObject createBitwiseNotMethod(Environment env) {
        return new BuiltInMethod(
                "bitwiseNot",
                "Returns bitwise NOT of this integer",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("bitwiseNot() takes no arguments, got " + args.length);
                    }

                    long value = getIntegerValue(instance);
                    return createIntegerInstance(~value);
                },
                env);
    }

    private static MethodObject createLeftShiftMethod(Environment env) {
        return new BuiltInMethod(
                "leftShift",
                "Returns this integer left-shifted by the specified number of bits",
                List.of("bits"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("leftShift() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isIntegerInstance(args[0])) {
                        return new ErrorObject("leftShift() bits must be an integer");
                    }

                    long value = getIntegerValue(instance);
                    long bits = getIntegerValue(args[0]);
                    return createIntegerInstance(value << bits);
                },
                env);
    }

    private static MethodObject createRightShiftMethod(Environment env) {
        return new BuiltInMethod(
                "rightShift",
                "Returns this integer right-shifted by the specified number of bits",
                List.of("bits"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("rightShift() takes exactly 1 argument, got " + args.length);
                    }

                    if (!isIntegerInstance(args[0])) {
                        return new ErrorObject("rightShift() bits must be an integer");
                    }

                    long value = getIntegerValue(instance);
                    long bits = getIntegerValue(args[0]);
                    return createIntegerInstance(value >> bits);
                },
                env);
    }

    // Helper methods
    private static long getIntegerValue(BaseObject obj) {
        if (ObjectValidator.isInstance(obj)) {
            InstanceObject instance = ObjectValidator.asInstance(obj);
            Optional<BaseObject> valueProperty = instance.getProperty("value");
            if (valueProperty.isPresent() && ObjectValidator.isInteger(valueProperty.get())) {
                return ObjectValidator.asInteger(valueProperty.get()).getValue();
            }
        }
        if (ObjectValidator.isInteger(obj)) {
            return ObjectValidator.asInteger(obj).getValue();
        }
        return 0;
    }

    private static boolean isIntegerInstance(BaseObject obj) {
        if (ObjectValidator.isInstance(obj)) {
            InstanceObject instance = ObjectValidator.asInstance(obj);
            return instance.getClassObject().getName().equals("Integer");
        }
        return ObjectValidator.isInteger(obj);
    }

    /**
     * 🏗️ Creates an Integer instance with the given value
     */
    public static InstanceObject createIntegerInstance(long value) {
        return new IntegerInstance(IntegerClass.getInstance(), new Environment(), value);
    }

    /**
     * 🔍 IntegerInstance - Integer instance class
     */
    private static class IntegerInstance extends InstanceObject {
        public IntegerInstance(ClassObject classObject, Environment instanceEnvironment, long value) {
            super(classObject, instanceEnvironment);
            setProperty("value", new IntegerObject(value));
        }

        @Override
        public boolean isTruthy() {
            return getValue().getValue() != 0;
        }

        @Override
        public String inspect() {
            return getValue().inspect();
        }

        public IntegerObject getValue() {
            return getProperty("value").map(ObjectValidator::asInteger).orElse(null);
        }
    }

}
