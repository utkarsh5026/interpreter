package lang.exec.objects.classes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Collections;
import java.util.HashMap;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.error.ErrorObject;
import lang.exec.objects.literals.*;
import lang.exec.validator.ObjectValidator;

/**
 * 🌟 Enhanced BaseObjectClass with Dunder Methods Support 🌟
 * 
 * From first principles, dunder methods are special methods that:
 * 1. Define how objects interact with operators (+, -, ==, etc.)
 * 2. Enable seamless integration with built-in language features
 * 3. Allow user-defined classes to customize behavior
 * 4. Follow a predictable naming convention (__methodName__)
 * 
 * This enhanced base class provides default implementations for all
 * standard dunder methods, which user classes can override.
 */
public class BaseObjectClass extends ClassObject {

    public static final String BASE_OBJECT_CLASS_NAME = "Object";

    // 🔑 Dunder method names - these define the operator overloading contract
    public static final String DUNDER_ADD = "__add__"; // +
    public static final String DUNDER_SUB = "__sub__"; // -
    public static final String DUNDER_MUL = "__mul__"; // *
    public static final String DUNDER_DIV = "__div__"; // /
    public static final String DUNDER_FLOORDIV = "__floordiv__"; // //
    public static final String DUNDER_MOD = "__mod__"; // %
    public static final String DUNDER_EQ = "__eq__"; // ==
    public static final String DUNDER_NE = "__ne__"; // !=
    public static final String DUNDER_LT = "__lt__"; // <
    public static final String DUNDER_LE = "__le__"; // <=
    public static final String DUNDER_GT = "__gt__"; // >
    public static final String DUNDER_GE = "__ge__"; // >=
    public static final String DUNDER_STR = "__str__"; // String conversion
    public static final String DUNDER_BOOL = "__bool__"; // Boolean conversion
    public static final String DUNDER_GETITEM = "__getitem__"; // obj[key]
    public static final String DUNDER_SETITEM = "__setitem__"; // obj[key] = value
    public static final String DUNDER_NEG = "__neg__"; // -obj
    public static final String DUNDER_NOT = "__not__"; // !obj
    public static final String DUNDER_AND = "__and__"; // and
    public static final String DUNDER_OR = "__or__"; // or
    public static final String DUNDER_LEN = "__len__"; // len(obj)

    private static BaseObjectClass instance;

    private BaseObjectClass() {
        super(
                BASE_OBJECT_CLASS_NAME,
                Optional.empty(), // No parent - this IS the root
                Optional.empty(), // No constructor needed
                createBaseMethodsWithDunders(),
                new Environment() // Root environment
        );
    }

    /**
     * 🏗️ Creates all base methods including dunder methods
     */
    private static Map<String, MethodObject> createBaseMethodsWithDunders() {
        var env = new Environment();
        Map<String, MethodObject> methods = new HashMap<>();

        // Original base methods
        methods.put("getClass", createGetClassMethod(env));

        // Dunder methods for arithmetic operations
        methods.put(DUNDER_ADD, createArithmeticDunder(DUNDER_ADD, "+", env));
        methods.put(DUNDER_SUB, createArithmeticDunder(DUNDER_SUB, "-", env));
        methods.put(DUNDER_MUL, createArithmeticDunder(DUNDER_MUL, "*", env));
        methods.put(DUNDER_DIV, createArithmeticDunder(DUNDER_DIV, "/", env));
        methods.put(DUNDER_FLOORDIV, createArithmeticDunder(DUNDER_FLOORDIV, "//", env));
        methods.put(DUNDER_MOD, createArithmeticDunder(DUNDER_MOD, "%", env));

        // Dunder methods for comparison operations
        methods.put(DUNDER_EQ, createComparisonDunder(DUNDER_EQ, "==", env));
        methods.put(DUNDER_NE, createComparisonDunder(DUNDER_NE, "!=", env));
        methods.put(DUNDER_LT, createComparisonDunder(DUNDER_LT, "<", env));
        methods.put(DUNDER_LE, createComparisonDunder(DUNDER_LE, "<=", env));
        methods.put(DUNDER_GT, createComparisonDunder(DUNDER_GT, ">", env));
        methods.put(DUNDER_GE, createComparisonDunder(DUNDER_GE, ">=", env));
        methods.put(DUNDER_AND, createComparisonDunder(DUNDER_AND, "&&", env));
        methods.put(DUNDER_OR, createComparisonDunder(DUNDER_OR, "||", env));

        // Dunder methods for unary operations
        methods.put(DUNDER_NEG, createUnaryDunder(DUNDER_NEG, "-", env));
        methods.put(DUNDER_NOT, createUnaryDunder(DUNDER_NOT, "!", env));

        // Dunder methods for special behaviors
        methods.put(DUNDER_STR, createStrDunder(env));
        methods.put(DUNDER_BOOL, createBoolDunder(env));
        methods.put(DUNDER_GETITEM, createGetItemDunder(env));
        methods.put(DUNDER_SETITEM, createSetItemDunder(env));

        return methods;
    }

    /**
     * ➕ Creates arithmetic dunder methods
     * 
     * From first principles, arithmetic dunders should:
     * 1. Accept one argument (the right operand)
     * 2. Return an appropriate result object
     * 3. Handle type mismatches gracefully
     * 4. Provide meaningful default behavior
     */
    private static MethodObject createArithmeticDunder(String methodName, String operator, Environment env) {
        return new BuiltInMethod(
                methodName,
                "Default arithmetic operation: " + operator,
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject(methodName + "() takes exactly 1 argument, got " + args.length);
                    }

                    // Default behavior: not supported
                    return new ErrorObject("Arithmetic operation '" + operator + "' not supported for objects of type "
                            + instance.getClass().getSimpleName());
                },
                env);
    }

    /**
     * ⚖️ Creates comparison dunder methods
     */
    private static MethodObject createComparisonDunder(String methodName, String operator, Environment env) {
        return new BuiltInMethod(
                methodName,
                "Default comparison operation: " + operator,
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject(methodName + "() takes exactly 1 argument, got " + args.length);
                    }

                    BaseObject other = args[0];

                    // Default behavior based on operator
                    switch (operator) {
                        case "==":
                            return new BooleanObject(instance == other);
                        case "!=":
                            return new BooleanObject(instance != other);
                        case "&&":
                            return new BooleanObject(instance.isTruthy() && other.isTruthy());
                        case "||":
                            return new BooleanObject(instance.isTruthy() || other.isTruthy());

                        default:
                            // Other comparisons not supported by default
                            return new ErrorObject(
                                    "Comparison operation '" + operator + "' not supported for objects of type "
                                            + instance.getClass().getSimpleName());
                    }
                },
                env);
    }

    /**
     * 🔄 Creates unary dunder methods
     */
    private static MethodObject createUnaryDunder(String methodName, String operator, Environment env) {
        return new BuiltInMethod(
                methodName,
                "Default unary operation: " + operator,
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject(methodName + "() takes no arguments, got " + args.length);
                    }

                    // Default behavior: not supported
                    return new ErrorObject("Unary operation '" + operator + "' not supported for objects of type "
                            + instance.getClass().getSimpleName());
                },
                env);
    }

    /**
     * 📝 Creates the __str__ dunder method
     * 
     * From first principles, __str__ should:
     * 1. Return a human-readable string representation
     * 2. Be called for string conversion operations
     * 3. Default to toString() behavior if not overridden
     */
    private static MethodObject createStrDunder(Environment env) {
        return new BuiltInMethod(
                DUNDER_STR,
                "Returns string representation of this object",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("__str__() takes no arguments, got " + args.length);
                    }

                    // Default behavior: delegate to toString
                    if (!ObjectValidator.isInstance(instance)) {
                        return new ErrorObject("__str__() can only be called on instances");
                    }

                    var instanceObject = ObjectValidator.asInstance(instance);
                    String className = instanceObject.getClassObject().getName();
                    int hashCode = System.identityHashCode(instance);
                    String result = className + "@" + Integer.toHexString(hashCode);

                    return new StringObject(result);
                },
                env);
    }

    /**
     * ✅ Creates the __bool__ dunder method
     * 
     * From first principles, __bool__ should:
     * 1. Return a boolean value representing truthiness
     * 2. Be called for boolean conversion operations
     * 3. Default to true for most objects (they exist, so they're truthy)
     */
    private static MethodObject createBoolDunder(Environment env) {
        return new BuiltInMethod(
                DUNDER_BOOL,
                "Returns boolean representation of this object",
                Collections.emptyList(),
                (_, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("__bool__() takes no arguments, got " + args.length);
                    }

                    // Default behavior: objects are truthy
                    return new BooleanObject(true);
                },
                env);
    }

    /**
     * 🔍 Creates the __getitem__ dunder method
     * 
     * From first principles, __getitem__ should:
     * 1. Accept a key/index argument
     * 2. Return the value at that key/index
     * 3. Handle missing keys appropriately
     */
    private static MethodObject createGetItemDunder(Environment env) {
        return new BuiltInMethod(
                DUNDER_GETITEM,
                "Gets item by key/index",
                List.of("key"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__getitem__() takes exactly 1 argument, got " + args.length);
                    }

                    // Default behavior: not supported
                    return new ErrorObject("Indexing not supported for objects of type "
                            + instance.getClass().getSimpleName());
                },
                env);
    }

    /**
     * 🔧 Creates the __setitem__ dunder method
     */
    private static MethodObject createSetItemDunder(Environment env) {
        return new BuiltInMethod(
                DUNDER_SETITEM,
                "Sets item by key/index",
                List.of("key", "value"),
                (instance, args) -> {
                    if (args.length != 2) {
                        return new ErrorObject("__setitem__() takes exactly 2 arguments, got " + args.length);
                    }

                    // Default behavior: not supported
                    return new ErrorObject("Index assignment not supported for objects of type "
                            + instance.getClass().getSimpleName());
                },
                env);
    }

    private static MethodObject createGetClassMethod(Environment env) {
        return new BuiltInMethod(
                "getClass",
                "Returns the Class object representing the class of this object",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("getClass() takes no arguments, got " + args.length);
                    }
                    if (!ObjectValidator.isInstance(instance)) {
                        return new ErrorObject("getClass() can only be called on instances");
                    }
                    var instanceObject = ObjectValidator.asInstance(instance);
                    return instanceObject.getClassObject();
                },
                env);
    }

    /**
     * 🏗️ Gets the singleton instance
     */
    public static BaseObjectClass getInstance() {
        if (instance == null) {
            instance = new BaseObjectClass();
        }
        return instance;
    }

    /**
     * 🎯 Utility method to get dunder method name for an operator
     */
    public static Optional<String> getDunderMethodForOperator(String operator) {
        return switch (operator) {
            case "+" -> Optional.of(DUNDER_ADD);
            case "-" -> Optional.of(DUNDER_SUB);
            case "*" -> Optional.of(DUNDER_MUL);
            case "/" -> Optional.of(DUNDER_DIV);
            case "//" -> Optional.of(DUNDER_FLOORDIV);
            case "%" -> Optional.of(DUNDER_MOD);
            case "==" -> Optional.of(DUNDER_EQ);
            case "!=" -> Optional.of(DUNDER_NE);
            case "<" -> Optional.of(DUNDER_LT);
            case "<=" -> Optional.of(DUNDER_LE);
            case ">" -> Optional.of(DUNDER_GT);
            case ">=" -> Optional.of(DUNDER_GE);
            case "&&" -> Optional.of(DUNDER_AND);
            case "||" -> Optional.of(DUNDER_OR);
            default -> Optional.empty();
        };
    }

    /**
     * 🎯 Utility method to get dunder method name for unary operators
     */
    public static Optional<String> getDunderMethodForUnaryOperator(String operator) {
        return switch (operator) {
            case "-" -> Optional.of(DUNDER_NEG);
            case "!" -> Optional.of(DUNDER_NOT);
            default -> Optional.empty();
        };
    }
}