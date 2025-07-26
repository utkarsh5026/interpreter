package lang.exec.objects.builtins;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;

import lang.exec.objects.classes.*;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.*;
import lang.exec.objects.error.ErrorObject;
import lang.exec.objects.literals.*;
import lang.exec.validator.ObjectValidator;

/**
 * ✅ BooleanClass - Built-in Boolean Type Class ✅
 */
public class BooleanClass extends ClassObject {

    public static final String BOOLEAN_CLASS_NAME = "Boolean";
    private static BooleanClass instance;

    private BooleanClass() {
        super(
                BOOLEAN_CLASS_NAME,
                Optional.of(BaseObjectClass.getInstance()),
                Optional.empty(),
                createBooleanMethods(),
                new Environment());
    }

    public static BooleanClass getInstance() {
        if (instance == null) {
            instance = new BooleanClass();
        }
        return instance;
    }

    private static Map<String, MethodObject> createBooleanMethods() {
        var env = EnvironmentFactory.empty();
        Map<String, MethodObject> methods = new HashMap<>();

        methods.put("toString", createBooleanToStringMethod(env));
        methods.put(BaseObjectClass.DUNDER_EQ, createBooleanEqMethod(env));
        methods.put(BaseObjectClass.DUNDER_NE, createBooleanNeMethod(env));
        methods.put(BaseObjectClass.DUNDER_AND, createBooleanAndMethod(env));
        methods.put(BaseObjectClass.DUNDER_OR, createBooleanOrMethod(env));
        methods.put(BaseObjectClass.DUNDER_NOT, createBooleanNotMethod(env));

        return methods;
    }

    private static MethodObject createBooleanToStringMethod(Environment env) {
        return new BuiltInMethod(
                "toString",
                "Returns string representation of this boolean",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("toString() takes no arguments, got " + args.length);
                    }

                    boolean value = getBooleanValue(instance);
                    return StringClass.createStringInstance(String.valueOf(value));
                },
                env);
    }

    private static MethodObject createBooleanEqMethod(Environment env) {
        return new BuiltInMethod(
                BaseObjectClass.DUNDER_EQ,
                "Equality operator for booleans",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__eq__() takes exactly 1 argument, got " + args.length);
                    }

                    boolean leftValue = getBooleanValue(instance);

                    if (isBoolean(args[0])) {
                        boolean rightValue = getBooleanValue(args[0]);
                        return createBooleanInstance(leftValue == rightValue);
                    }

                    return createBooleanInstance(false);
                },
                env);
    }

    private static MethodObject createBooleanNeMethod(Environment env) {
        return new BuiltInMethod(
                BaseObjectClass.DUNDER_NE,
                "Inequality operator for booleans",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__ne__() takes exactly 1 argument, got " + args.length);
                    }

                    boolean leftValue = getBooleanValue(instance);

                    if (isBoolean(args[0])) {
                        boolean rightValue = getBooleanValue(args[0]);
                        return createBooleanInstance(leftValue != rightValue);
                    }

                    return createBooleanInstance(true);
                },
                env);
    }

    private static MethodObject createBooleanAndMethod(Environment env) {
        return new BuiltInMethod(
                BaseObjectClass.DUNDER_AND,
                "Logical AND operator for booleans",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__and__() takes exactly 1 argument, got " + args.length);
                    }

                    boolean leftValue = getBooleanValue(instance);

                    if (isBoolean(args[0])) {
                        boolean rightValue = getBooleanValue(args[0]);
                        return createBooleanInstance(leftValue && rightValue);
                    }

                    return new ErrorObject("Cannot perform logical AND with boolean and " + args[0].type());
                },
                env);
    }

    private static MethodObject createBooleanOrMethod(Environment env) {
        return new BuiltInMethod(
                BaseObjectClass.DUNDER_OR,
                "Logical OR operator for booleans",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__or__() takes exactly 1 argument, got " + args.length);
                    }

                    boolean leftValue = getBooleanValue(instance);

                    if (isBoolean(args[0])) {
                        boolean rightValue = getBooleanValue(args[0]);
                        return createBooleanInstance(leftValue || rightValue);
                    }

                    return new ErrorObject("Cannot perform logical OR with boolean and " + args[0].type());
                },
                env);
    }

    private static MethodObject createBooleanNotMethod(Environment env) {
        return new BuiltInMethod(
                "__not__",
                "Logical NOT operator for booleans",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("__not__() takes no arguments, got " + args.length);
                    }

                    boolean value = getBooleanValue(instance);
                    return createBooleanInstance(!value);
                },
                env);
    }

    /**
     * 🔍 Gets the boolean value of the object
     */
    private static boolean getBooleanValue(BaseObject obj) {
        if (obj instanceof BooleanInstance) {
            return ((BooleanInstance) obj).getValue().getValue();
        }

        if (ObjectValidator.isInstance(obj)) {
            InstanceObject instance = ObjectValidator.asInstance(obj);
            Optional<BaseObject> valueProperty = instance.getProperty("value");
            if (valueProperty.isPresent() && ObjectValidator.isBoolean(valueProperty.get())) {
                return ObjectValidator.asBoolean(valueProperty.get()).getValue();
            }
        }
        if (ObjectValidator.isBoolean(obj)) {
            return ObjectValidator.asBoolean(obj).getValue();
        }
        return false;
    }

    /**
     * 🔍 Checks if the object is a boolean instance
     */
    private static boolean isBoolean(BaseObject obj) {
        if (obj instanceof BooleanInstance) {
            return true;
        }

        if (!ObjectValidator.isInstance(obj)) {
            return false;
        }
        InstanceObject instance = ObjectValidator.asInstance(obj);
        Optional<BaseObject> valueProperty = instance.getProperty("value");
        return valueProperty.isPresent() && ObjectValidator.isBoolean(valueProperty.get());
    }

    /**
     * 🏗️ Creates a Boolean instance with the given value
     */
    public static InstanceObject createBooleanInstance(boolean value) {
        return new BooleanInstance(getInstance(), new Environment(), value);
    }

    /**
     * 🔍 BooleanInstance - Boolean instance class
     */
    private static class BooleanInstance extends InstanceObject {
        public BooleanInstance(ClassObject classObject, Environment instanceEnvironment, boolean value) {
            super(classObject, instanceEnvironment);
            setProperty("value", new BooleanObject(value));
        }

        @Override
        public boolean isTruthy() {
            return getBooleanValue(this);
        }

        @Override
        public String inspect() {
            return getBooleanValue(this) ? "true" : "false";
        }

        public BooleanObject getValue() {
            return getProperty("value").map(ObjectValidator::asBoolean).orElse(null);
        }
    }

}
