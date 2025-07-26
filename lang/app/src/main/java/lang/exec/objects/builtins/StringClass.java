package lang.exec.objects.builtins;

import java.util.*;
import java.util.regex.Pattern;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.classes.*;
import lang.exec.objects.env.Environment;
import lang.exec.objects.error.ErrorObject;
import lang.exec.objects.literals.*;
import lang.exec.objects.structures.ArrayObject;
import lang.exec.validator.ObjectValidator;

/**
 * 🔤 StringClass - Built-in String Type Class 🔤
 * 
 * From first principles, strings should have methods for:
 * - Length calculation
 * - Substring operations
 * - Case conversion
 * - Search and replace
 * - Splitting and joining
 * - Character access
 * - Formatting operations
 */
public class StringClass extends ClassObject {

    public static final String STRING_CLASS_NAME = "String";
    private static StringClass instance;

    private StringClass() {
        super(
                STRING_CLASS_NAME,
                Optional.of(BaseObjectClass.getInstance()),
                Optional.empty(), // No constructor - strings are created via literals
                createStringMethods(),
                new Environment());
    }

    public static StringClass getInstance() {
        if (instance == null) {
            instance = new StringClass();
        }
        return instance;
    }

    private static Map<String, MethodObject> createStringMethods() {
        var env = new Environment();
        Map<String, MethodObject> methods = new HashMap<>();

        // Basic string methods
        methods.put("length", createLengthMethod(env));
        methods.put("isEmpty", createIsEmptyMethod(env));
        methods.put("charAt", createCharAtMethod(env));
        methods.put("substring", createSubstringMethod(env));

        // Case conversion
        methods.put("toUpperCase", createToUpperCaseMethod(env));
        methods.put("toLowerCase", createToLowerCaseMethod(env));

        // Search methods
        methods.put("indexOf", createIndexOfMethod(env));
        methods.put("contains", createContainsMethod(env));
        methods.put("startsWith", createStartsWithMethod(env));
        methods.put("endsWith", createEndsWithMethod(env));

        // Transformation methods
        methods.put("replace", createReplaceMethod(env));
        methods.put("trim", createTrimMethod(env));
        methods.put("split", createSplitMethod(env));

        // Dunder methods
        methods.put(BaseObjectClass.DUNDER_ADD, createStringConcatMethod(env));
        methods.put(BaseObjectClass.DUNDER_MUL, createStringRepeatMethod(env));
        methods.put(BaseObjectClass.DUNDER_GETITEM, createStringIndexMethod(env));
        methods.put(BaseObjectClass.DUNDER_LEN, createLengthMethod(env)); // Alias for length()

        return methods;
    }

    private static MethodObject createLengthMethod(Environment env) {
        return new BuiltInMethod(
                "length",
                "Returns the length of this string",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("length() takes no arguments, got " + args.length);
                    }

                    String value = getStringValue(instance);
                    return new IntegerObject(value.length());
                },
                env);
    }

    private static MethodObject createIsEmptyMethod(Environment env) {
        return new BuiltInMethod(
                "isEmpty",
                "Returns true if this string is empty",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("isEmpty() takes no arguments, got " + args.length);
                    }

                    String value = getStringValue(instance);
                    return new BooleanObject(value.isEmpty());
                },
                env);
    }

    private static MethodObject createCharAtMethod(Environment env) {
        return new BuiltInMethod(
                "charAt",
                "Returns the character at the specified index",
                List.of("index"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("charAt() takes exactly 1 argument, got " + args.length);
                    }

                    if (!ObjectValidator.isInteger(args[0])) {
                        return new ErrorObject("charAt() index must be an integer");
                    }

                    String value = getStringValue(instance);
                    int index = (int) ObjectValidator.asInteger(args[0]).getValue();

                    if (index < 0 || index >= value.length()) {
                        return new ErrorObject("String index out of bounds: " + index);
                    }

                    return createStringInstance(String.valueOf(value.charAt(index)));
                },
                env);
    }

    private static MethodObject createSubstringMethod(Environment env) {
        return new BuiltInMethod(
                "substring",
                "Returns a substring from start to end (exclusive)",
                List.of("start", "end"),
                (instance, args) -> {
                    if (args.length < 1 || args.length > 2) {
                        return new ErrorObject("substring() takes 1 or 2 arguments, got " + args.length);
                    }

                    if (!ObjectValidator.isInteger(args[0])) {
                        return new ErrorObject("substring() start index must be an integer");
                    }

                    String value = getStringValue(instance);
                    int start = (int) ObjectValidator.asInteger(args[0]).getValue();
                    int end = value.length();

                    if (args.length == 2) {
                        if (!ObjectValidator.isInteger(args[1])) {
                            return new ErrorObject("substring() end index must be an integer");
                        }
                        end = (int) ObjectValidator.asInteger(args[1]).getValue();
                    }

                    if (start < 0 || start > value.length() || end < start || end > value.length()) {
                        return new ErrorObject("Invalid substring bounds");
                    }

                    return createStringInstance(value.substring(start, end));
                },
                env);
    }

    private static MethodObject createToUpperCaseMethod(Environment env) {
        return new BuiltInMethod(
                "toUpperCase",
                "Returns this string in uppercase",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("toUpperCase() takes no arguments, got " + args.length);
                    }

                    String value = getStringValue(instance);
                    return createStringInstance(value.toUpperCase());
                },
                env);
    }

    private static MethodObject createToLowerCaseMethod(Environment env) {
        return new BuiltInMethod(
                "toLowerCase",
                "Returns this string in lowercase",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("toLowerCase() takes no arguments, got " + args.length);
                    }

                    String value = getStringValue(instance);
                    return createStringInstance(value.toLowerCase());
                },
                env);
    }

    private static MethodObject createIndexOfMethod(Environment env) {
        return new BuiltInMethod(
                "indexOf",
                "Returns the index of the first occurrence of the substring",
                List.of("substring"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("indexOf() takes exactly 1 argument, got " + args.length);
                    }

                    String value = getStringValue(instance);
                    String searchStr = getStringValue(args[0]);

                    int index = value.indexOf(searchStr);
                    return new IntegerObject(index);
                },
                env);
    }

    private static MethodObject createContainsMethod(Environment env) {
        return new BuiltInMethod(
                "contains",
                "Returns true if this string contains the specified substring",
                List.of("substring"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("contains() takes exactly 1 argument, got " + args.length);
                    }

                    String value = getStringValue(instance);
                    String searchStr = getStringValue(args[0]);

                    return new BooleanObject(value.contains(searchStr));
                },
                env);
    }

    private static MethodObject createStartsWithMethod(Environment env) {
        return new BuiltInMethod(
                "startsWith",
                "Returns true if this string starts with the specified prefix",
                List.of("prefix"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("startsWith() takes exactly 1 argument, got " + args.length);
                    }

                    String value = getStringValue(instance);
                    String prefix = getStringValue(args[0]);

                    return new BooleanObject(value.startsWith(prefix));
                },
                env);
    }

    private static MethodObject createEndsWithMethod(Environment env) {
        return new BuiltInMethod(
                "endsWith",
                "Returns true if this string ends with the specified suffix",
                List.of("suffix"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("endsWith() takes exactly 1 argument, got " + args.length);
                    }

                    String value = getStringValue(instance);
                    String suffix = getStringValue(args[0]);

                    return new BooleanObject(value.endsWith(suffix));
                },
                env);
    }

    private static MethodObject createReplaceMethod(Environment env) {
        return new BuiltInMethod(
                "replace",
                "Returns a string with all occurrences of old replaced with new",
                List.of("old", "new"),
                (instance, args) -> {
                    if (args.length != 2) {
                        return new ErrorObject("replace() takes exactly 2 arguments, got " + args.length);
                    }

                    String value = getStringValue(instance);
                    String oldStr = getStringValue(args[0]);
                    String newStr = getStringValue(args[1]);

                    return createStringInstance(value.replace(oldStr, newStr));
                },
                env);
    }

    private static MethodObject createTrimMethod(Environment env) {
        return new BuiltInMethod(
                "trim",
                "Returns a string with leading and trailing whitespace removed",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("trim() takes no arguments, got " + args.length);
                    }

                    String value = getStringValue(instance);
                    return createStringInstance(value.trim());
                },
                env);
    }

    private static MethodObject createSplitMethod(Environment env) {
        return new BuiltInMethod(
                "split",
                "Splits this string into an array using the specified delimiter",
                List.of("delimiter"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("split() takes exactly 1 argument, got " + args.length);
                    }

                    String value = getStringValue(instance);
                    String delimiter = getStringValue(args[0]);

                    String[] parts = value.split(Pattern.quote(delimiter));
                    List<BaseObject> elements = new ArrayList<>();

                    for (String part : parts) {
                        elements.add(createStringInstance(part));
                    }

                    return new ArrayObject(elements);
                },
                env);
    }

    // Dunder methods
    private static MethodObject createStringConcatMethod(Environment env) {
        return new BuiltInMethod(
                BaseObjectClass.DUNDER_ADD,
                "String concatenation operator",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__add__() takes exactly 1 argument, got " + args.length);
                    }

                    String leftValue = getStringValue(instance);
                    String rightValue = getStringValue(args[0]);

                    return createStringInstance(leftValue + rightValue);
                },
                env);
    }

    private static MethodObject createStringRepeatMethod(Environment env) {
        return new BuiltInMethod(
                BaseObjectClass.DUNDER_MUL,
                "String repetition operator",
                List.of("count"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__mul__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!ObjectValidator.isInteger(args[0])) {
                        return new ErrorObject("String repetition count must be an integer");
                    }

                    String value = getStringValue(instance);
                    int count = (int) ObjectValidator.asInteger(args[0]).getValue();

                    if (count < 0) {
                        return new ErrorObject("String repetition count cannot be negative");
                    }

                    return createStringInstance(value.repeat(count));
                },
                env);
    }

    private static MethodObject createStringIndexMethod(Environment env) {
        return new BuiltInMethod(
                BaseObjectClass.DUNDER_GETITEM,
                "String character access by index",
                List.of("index"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__getitem__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!ObjectValidator.isInteger(args[0])) {
                        return new ErrorObject("String index must be an integer");
                    }

                    String value = getStringValue(instance);
                    int index = (int) ObjectValidator.asInteger(args[0]).getValue();

                    if (index < 0 || index >= value.length()) {
                        return new ErrorObject("String index out of bounds: " + index);
                    }

                    return createStringInstance(String.valueOf(value.charAt(index)));
                },
                env);
    }

    // Helper methods
    private static String getStringValue(BaseObject obj) {
        if (obj instanceof StringInstance) {
            return ((StringInstance) obj).getValue().inspect();
        }

        if (ObjectValidator.isInstance(obj)) {
            InstanceObject instance = ObjectValidator.asInstance(obj);
            Optional<BaseObject> valueProperty = instance.getProperty("value");
            if (valueProperty.isPresent() && ObjectValidator.isString(valueProperty.get())) {
                return ObjectValidator.asString(valueProperty.get()).inspect();
            }
        }

        if (ObjectValidator.isString(obj)) {
            return ObjectValidator.asString(obj).inspect();
        }
        return obj.inspect();
    }

    /**
     * 🏗️ Creates a String instance with the given value
     */
    public static InstanceObject createStringInstance(String value) {
        return new StringInstance(StringClass.getInstance(), new Environment(), value);
    }

    public static boolean isStringInstance(BaseObject obj) {
        return obj instanceof StringInstance;
    }

    /**
     * 🔍 Gets the StringObject from the given BaseObject
     */
    public static Optional<StringObject> getStringObject(BaseObject obj) {
        if (obj instanceof StringInstance) {
            return Optional.of(((StringInstance) obj).getValue());
        }
        return Optional.empty();
    }

    /**
     * 🔍 StringInstance - String instance class
     */
    private static class StringInstance extends InstanceObject {
        public StringInstance(ClassObject classObject, Environment instanceEnvironment, String value) {
            super(classObject, instanceEnvironment);
            setProperty("value", new StringObject(value));
        }

        @Override
        public boolean isTruthy() {
            return !getStringValue(this).isEmpty();
        }

        @Override
        public String inspect() {
            return getValue().inspect();
        }

        public StringObject getValue() {
            Optional<BaseObject> valueProperty = getProperty("value");
            if (valueProperty.isPresent() && valueProperty.get() instanceof StringObject) {
                return (StringObject) valueProperty.get();
            }
            return null;
        }
    }

}