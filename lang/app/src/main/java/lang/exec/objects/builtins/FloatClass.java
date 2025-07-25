package lang.exec.objects.builtins;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.classes.*;
import lang.exec.objects.env.Environment;
import lang.exec.objects.env.EnvironmentFactory;
import lang.exec.objects.error.ErrorObject;
import lang.exec.objects.literals.*;
import lang.exec.validator.ObjectValidator;

/**
 * 🌊 FloatClass - Built-in Float Type Class 🌊
 * 
 * From first principles, floats should have methods for:
 * - Mathematical operations (similar to integers but with float precision)
 * - Precision and rounding operations
 * - Special value handling (NaN, Infinity)
 * - Type conversion
 */
class FloatClass extends ClassObject {

    public static final String FLOAT_CLASS_NAME = "Float";
    private static FloatClass instance;

    private FloatClass() {
        super(
                FLOAT_CLASS_NAME,
                Optional.of(NumberClass.getInstance()), // Inherit from Number
                Optional.empty(), // No constructor - floats are created via literals
                createFloatMethods(),
                new Environment());
    }

    public static FloatClass getInstance() {
        if (instance == null) {
            instance = new FloatClass();
        }
        return instance;
    }

    private static Map<String, MethodObject> createFloatMethods() {
        var env = EnvironmentFactory.empty();
        Map<String, MethodObject> methods = new HashMap<>();

        // Float-specific methods (Number methods will be inherited)
        methods.put("ceil", createCeilMethod(env));
        methods.put("floor", createFloorMethod(env));
        methods.put("round", createRoundMethod(env));

        // Special value checks (unique to floats)
        methods.put("isNaN", createIsNaNMethod(env));
        methods.put("isInfinite", createIsInfiniteMethod(env));
        methods.put("isFinite", createIsFiniteMethod(env));

        return methods;
    }

    // Float-specific methods (implementation details similar to before)
    private static MethodObject createCeilMethod(Environment env) {
        return new BuiltInMethod(
                "ceil",
                "Returns the smallest integer greater than or equal to this float",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("ceil() takes no arguments, got " + args.length);
                    }

                    double value = getFloatValue(instance);
                    return IntegerClass.createIntegerInstance((long) Math.ceil(value));
                },
                env);
    }

    private static MethodObject createFloorMethod(Environment env) {
        return new BuiltInMethod(
                "floor",
                "Returns the largest integer less than or equal to this float",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("floor() takes no arguments, got " + args.length);
                    }

                    double value = getFloatValue(instance);
                    return IntegerClass.createIntegerInstance((long) Math.floor(value));
                },
                env);
    }

    private static MethodObject createRoundMethod(Environment env) {
        return new BuiltInMethod(
                "round",
                "Returns this float rounded to the nearest integer",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("round() takes no arguments, got " + args.length);
                    }

                    double value = getFloatValue(instance);
                    return IntegerClass.createIntegerInstance(Math.round(value));
                },
                env);
    }

    private static MethodObject createIsNaNMethod(Environment env) {
        return new BuiltInMethod(
                "isNaN",
                "Returns true if this float is NaN (Not a Number)",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("isNaN() takes no arguments, got " + args.length);
                    }

                    double value = getFloatValue(instance);
                    return BooleanClass.createBooleanInstance(Double.isNaN(value));
                },
                env);
    }

    private static MethodObject createIsInfiniteMethod(Environment env) {
        return new BuiltInMethod(
                "isInfinite",
                "Returns true if this float is infinite",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("isInfinite() takes no arguments, got " + args.length);
                    }

                    double value = getFloatValue(instance);
                    return BooleanClass.createBooleanInstance(Double.isInfinite(value));
                },
                env);
    }

    private static MethodObject createIsFiniteMethod(Environment env) {
        return new BuiltInMethod(
                "isFinite",
                "Returns true if this float is finite (not NaN or infinite)",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("isFinite() takes no arguments, got " + args.length);
                    }

                    double value = getFloatValue(instance);
                    return BooleanClass.createBooleanInstance(Double.isFinite(value));
                },
                env);
    }

    // Helper methods
    private static double getFloatValue(BaseObject obj) {
        if (ObjectValidator.isInstance(obj)) {
            InstanceObject instance = ObjectValidator.asInstance(obj);
            Optional<BaseObject> valueProperty = instance.getProperty("value");
            if (valueProperty.isPresent() && ObjectValidator.isFloat(valueProperty.get())) {
                return ObjectValidator.asFloat(valueProperty.get()).getValue();
            }
        }
        if (ObjectValidator.isFloat(obj)) {
            return ObjectValidator.asFloat(obj).getValue();
        }
        return 0.0;
    }

    /**
     * 🏗️ Creates a Float instance with the given value
     */
    public static InstanceObject createFloatInstance(double value) {
        InstanceObject instance = FloatClass.getInstance().createInstance();
        instance.setProperty("value", new FloatObject(value));
        return instance;
    }
}