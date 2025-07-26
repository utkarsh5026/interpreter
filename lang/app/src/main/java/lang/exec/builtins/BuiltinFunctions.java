package lang.exec.builtins;

import java.util.*;
import java.util.stream.Collectors;

import lang.exec.validator.ObjectValidator;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.error.ErrorObject;
import lang.exec.objects.functions.BuiltinObject;
import lang.exec.objects.literals.*;
import lang.exec.objects.structures.ArrayObject;
import lang.exec.objects.structures.HashObject;
import lang.exec.objects.builtins.*;

/**
 * 🏗️ BuiltinFunctions - Comprehensive Function Library 🏗️
 * 
 * This class contains all the built-in functions available in the language.
 * Built-in functions are pre-defined functions that provide essential
 * functionality without requiring user implementation.
 * 
 * Think of this as the "standard library" - a toolbox of useful functions
 * that every program can use! 🧰✨
 * 
 * The functions are organized into logical categories:
 * - 📊 Core Data Operations: type checking, conversion, length
 * - 🗂️ Array Operations: manipulation, filtering, transformation
 * - 📝 String Operations: text processing and manipulation
 * - 🔢 Mathematical Operations: arithmetic, rounding, random numbers
 * - 🖥️ I/O Operations: input/output functionality
 * - 🛠️ Utility Functions: helper functions for common tasks
 * - 🚨 Error Handling: error creation and assertions
 * 
 * Design Philosophy:
 * - Functions are pure (no side effects except I/O)
 * - Consistent error handling with descriptive messages
 * - Type safety with runtime checks
 * - Performance-conscious implementations
 * - Familiar names from common programming languages
 */
public final class BuiltinFunctions {

    private BuiltinFunctions() {
        throw new UnsupportedOperationException("Utility class - cannot instantiate");
    }

    // ============================================================================
    // 1. CORE DATA OPERATIONS
    // ============================================================================

    /**
     * 📏 len(obj) - Returns the length of arrays, strings, or hash objects
     * 
     * Examples:
     * - len([1, 2, 3]) → 3
     * - len("hello") → 5
     * - len({"a": 1, "b": 2}) → 2
     */
    private static final BuiltinFunction LEN_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject arg = args[0];

        if (arg instanceof ArrayObject) {
            return IntegerClass.createIntegerInstance(((ArrayObject) arg).getElements().size());
        }

        if (ObjectValidator.isString(arg)) {
            return IntegerClass.createIntegerInstance(ObjectValidator.asString(arg).getValue().length());
        }

        if (arg instanceof HashObject) {
            return IntegerClass.createIntegerInstance(((HashObject) arg).getPairs().size());
        }

        return new ErrorObject(String.format(
                "argument to 'len' not supported, got %s", arg.type()));
    };

    /**
     * 🏷️ type(obj) - Returns the type of an object as a string
     * 
     * Examples:
     * - type(42) → "INTEGER"
     * - type("hello") → "STRING"
     * - type([1, 2]) → "ARRAY"
     */
    private static final BuiltinFunction TYPE_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        return StringClass.createStringInstance(args[0].type().toString());
    };

    /**
     * 📝 str(obj) - Converts any value to its string representation
     * 
     * Examples:
     * - str(42) → "42"
     * - str(true) → "true"
     * - str(null) → "null"
     */
    private static final BuiltinFunction STR_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject arg = args[0];

        // Handle special cases for better string conversion
        if (arg instanceof NullObject) {
            return StringClass.createStringInstance("null");
        }

        if (arg instanceof BooleanObject) {
            return StringClass.createStringInstance(ObjectValidator.asBoolean(arg).getValue() ? "true" : "false");
        }

        return StringClass.createStringInstance(arg.inspect());
    };

    /**
     * 🔢 int(str) - Converts a string or number to an integer
     * 
     * Examples:
     * - int("42") → 42
     * - int("123abc") → Error
     * - int(42) → 42 (already integer)
     */
    private static final BuiltinFunction INT_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject arg = args[0];

        if (arg instanceof IntegerObject) {
            return arg; // Already an integer
        }

        if (ObjectValidator.isString(arg)) {
            try {
                long parsed = Long.parseLong(ObjectValidator.asString(arg).getValue());
                return IntegerClass.createIntegerInstance(parsed);
            } catch (NumberFormatException e) {
                return new ErrorObject(String.format(
                        "cannot convert \"%s\" to integer", ObjectValidator.asString(arg).getValue()));
            }
        }

        return new ErrorObject(String.format("argument to 'int' not supported, got %s", arg.type()));
    };

    /**
     * ✅ bool(obj) - Converts any value to boolean using truthiness rules
     * 
     * Examples:
     * - bool(0) → false
     * - bool(1) → true
     * - bool("") → false
     * - bool("hello") → true
     */
    private static final BuiltinFunction BOOL_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        return BooleanClass.createBooleanInstance(args[0].isTruthy());
    };

    // ============================================================================
    // 2. ARRAY OPERATIONS
    // ============================================================================

    /**
     * 🔝 first(array) - Returns the first element of an array
     */
    private static final BuiltinFunction FIRST_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject arg = args[0];
        if (!(arg instanceof ArrayObject)) {
            return new ErrorObject(String.format(
                    "argument to 'first' must be ARRAY, got %s", arg.type()));
        }

        ArrayObject array = (ArrayObject) arg;
        List<BaseObject> elements = array.getElements();

        if (!elements.isEmpty()) {
            return elements.get(0);
        }

        return NullObject.INSTANCE;
    };

    /**
     * 🔚 last(array) - Returns the last element of an array
     */
    private static final BuiltinFunction LAST_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject arg = args[0];
        if (!(arg instanceof ArrayObject)) {
            return new ErrorObject(String.format(
                    "argument to 'last' must be ARRAY, got %s", arg.type()));
        }

        ArrayObject array = (ArrayObject) arg;
        List<BaseObject> elements = array.getElements();

        if (!elements.isEmpty()) {
            return elements.get(elements.size() - 1);
        }

        return NullObject.INSTANCE;
    };

    /**
     * 🔄 rest(array) - Returns a new array with all elements except the first
     */
    private static final BuiltinFunction REST_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject arg = args[0];
        if (!(arg instanceof ArrayObject)) {
            return new ErrorObject(String.format(
                    "argument to 'rest' must be ARRAY, got %s", arg.type()));
        }

        ArrayObject array = (ArrayObject) arg;
        List<BaseObject> elements = array.getElements();

        if (!elements.isEmpty()) {
            List<BaseObject> newElements = elements.subList(1, elements.size());
            return new ArrayObject(new ArrayList<>(newElements));
        }

        return NullObject.INSTANCE;
    };

    /**
     * ➕ push(array, element) - Returns a new array with the element added to the
     * end
     */
    private static final BuiltinFunction PUSH_FUNCTION = (args) -> {
        if (args.length != 2) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=2", args.length));
        }

        BaseObject arr = args[0];
        BaseObject element = args[1];

        if (!(arr instanceof ArrayObject)) {
            return new ErrorObject(String.format(
                    "argument to 'push' must be ARRAY, got %s", arr.type()));
        }

        ArrayObject array = (ArrayObject) arr;
        List<BaseObject> newElements = new ArrayList<>(array.getElements());
        newElements.add(element);

        return new ArrayObject(newElements);
    };

    /**
     * ➖ pop(array) - Returns a new array with the last element removed
     */
    private static final BuiltinFunction POP_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject arg = args[0];
        if (!(arg instanceof ArrayObject)) {
            return new ErrorObject(String.format(
                    "argument to 'pop' must be ARRAY, got %s", arg.type()));
        }

        ArrayObject array = (ArrayObject) arg;
        List<BaseObject> elements = array.getElements();

        if (elements.isEmpty()) {
            return new ErrorObject("cannot pop from empty array");
        }

        List<BaseObject> newElements = new ArrayList<>(elements.subList(0, elements.size() - 1));
        return new ArrayObject(newElements);
    };

    /**
     * ✂️ slice(array, start, end?) - Returns a portion of the array
     */
    private static final BuiltinFunction SLICE_FUNCTION = (args) -> {
        if (args.length < 2 || args.length > 3) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=2 or 3", args.length));
        }

        BaseObject arr = args[0];
        BaseObject startArg = args[1];
        BaseObject endArg = args.length == 3 ? args[2] : null;

        if (!(arr instanceof ArrayObject)) {
            return new ErrorObject(String.format(
                    "first argument to 'slice' must be ARRAY, got %s", arr.type()));
        }

        if (!(startArg instanceof IntegerObject)) {
            return new ErrorObject(String.format(
                    "second argument to 'slice' must be INTEGER, got %s", startArg.type()));
        }

        ArrayObject array = (ArrayObject) arr;
        List<BaseObject> elements = array.getElements();

        long startLong = ObjectValidator.asInteger(startArg).getValue();
        long endLong = elements.size();

        if (endArg != null) {
            if (!ObjectValidator.isInteger(endArg)) {
                return new ErrorObject(String.format(
                        "third argument to 'slice' must be INTEGER, got %s", endArg.type()));
            }
            endLong = ObjectValidator.asInteger(endArg).getValue();
        }

        // Handle negative indices
        int start = (int) (startLong < 0 ? Math.max(0, elements.size() + startLong) : startLong);
        int end = (int) (endLong < 0 ? Math.max(0, elements.size() + endLong) : endLong);

        // Clamp to array bounds
        start = Math.max(0, Math.min(start, elements.size()));
        end = Math.max(start, Math.min(end, elements.size()));

        List<BaseObject> newElements = new ArrayList<>(elements.subList(start, end));
        return new ArrayObject(newElements);
    };

    /**
     * 🔗 concat(array1, array2) - Concatenates two arrays
     */
    private static final BuiltinFunction CONCAT_FUNCTION = (args) -> {
        if (args.length != 2) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=2", args.length));
        }

        BaseObject arr1 = args[0];
        BaseObject arr2 = args[1];

        if (!(arr1 instanceof ArrayObject)) {
            return new ErrorObject(String.format(
                    "first argument to 'concat' must be ARRAY, got %s", arr1.type()));
        }

        if (!(arr2 instanceof ArrayObject)) {
            return new ErrorObject(String.format(
                    "second argument to 'concat' must be ARRAY, got %s", arr2.type()));
        }

        List<BaseObject> newElements = new ArrayList<>(((ArrayObject) arr1).getElements());
        newElements.addAll(((ArrayObject) arr2).getElements());

        return new ArrayObject(newElements);
    };

    /**
     * 🔄 reverse(array) - Returns a new array with elements in reverse order
     */
    private static final BuiltinFunction REVERSE_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject arg = args[0];
        if (!(arg instanceof ArrayObject)) {
            return new ErrorObject(String.format(
                    "argument to 'reverse' must be ARRAY, got %s", arg.type()));
        }

        ArrayObject array = (ArrayObject) arg;
        List<BaseObject> newElements = new ArrayList<>(array.getElements());
        Collections.reverse(newElements);

        return new ArrayObject(newElements);
    };

    /**
     * 🔗 join(array, separator?) - Joins array elements into a string
     */
    private static final BuiltinFunction JOIN_FUNCTION = (args) -> {
        if (args.length < 1 || args.length > 2) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1 or 2", args.length));
        }

        BaseObject arr = args[0];
        if (!(arr instanceof ArrayObject)) {
            return new ErrorObject(String.format(
                    "first argument to 'join' must be ARRAY, got %s", arr.type()));
        }

        String separator = ",";
        if (args.length == 2) {
            BaseObject sepArg = args[1];
            if (!ObjectValidator.isString(sepArg)) {
                return new ErrorObject(String.format(
                        "second argument to 'join' must be STRING, got %s", sepArg.type()));
            }
            separator = ObjectValidator.asString(sepArg).getValue();
        }

        ArrayObject array = (ArrayObject) arr;
        String result = array.getElements().stream()
                .map(BaseObject::inspect)
                .collect(Collectors.joining(separator));

        return StringClass.createStringInstance(result);
    };

    // ============================================================================
    // 3. STRING OPERATIONS
    // ============================================================================

    /**
     * ✂️ split(string, delimiter) - Splits a string by delimiter
     */
    private static final BuiltinFunction SPLIT_FUNCTION = (args) -> {
        if (args.length != 2) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=2", args.length));
        }

        BaseObject str = args[0];
        BaseObject delimiter = args[1];

        if (!ObjectValidator.isString(str)) {
            return new ErrorObject(String.format(
                    "first argument to 'split' must be STRING, got %s", str.type()));
        }

        if (!ObjectValidator.isString(delimiter)) {
            return new ErrorObject(String.format(
                    "second argument to 'split' must be STRING, got %s", delimiter.type()));
        }

        String[] parts = ObjectValidator.asString(str).getValue()
                .split(ObjectValidator.asString(delimiter).getValue(), -1);

        List<BaseObject> elements = Arrays.stream(parts)
                .map(StringObject::new)
                .collect(Collectors.toList());

        return new ArrayObject(elements);
    };

    /**
     * 🔄 replace(string, search, replace) - Replace occurrences in string
     */
    private static final BuiltinFunction REPLACE_FUNCTION = (args) -> {
        if (args.length != 3) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=3", args.length));
        }

        BaseObject str = args[0];
        BaseObject search = args[1];
        BaseObject replace = args[2];

        if (!ObjectValidator.isString(str)) {
            return new ErrorObject(String.format(
                    "first argument to 'replace' must be STRING, got %s", str.type()));
        }

        if (!ObjectValidator.isString(search)) {
            return new ErrorObject(String.format(
                    "second argument to 'replace' must be STRING, got %s", search.type()));
        }

        if (!ObjectValidator.isString(replace)) {
            return new ErrorObject(String.format(
                    "third argument to 'replace' must be STRING, got %s", replace.type()));
        }

        String result = ObjectValidator.asString(str).getValue()
                .replace(ObjectValidator.asString(search).getValue(), ObjectValidator.asString(replace).getValue());

        return StringClass.createStringInstance(result);
    };

    /**
     * ✨ trim(string) - Remove whitespace from both ends
     */
    private static final BuiltinFunction TRIM_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject str = args[0];
        if (!ObjectValidator.isString(str)) {
            return new ErrorObject(String.format(
                    "argument to 'trim' must be STRING, got %s", str.type()));
        }

        return StringClass.createStringInstance(ObjectValidator.asString(str).getValue().trim());
    };

    /**
     * 🔠 upper(string) - Convert to uppercase
     */
    private static final BuiltinFunction UPPER_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject str = args[0];
        if (!ObjectValidator.isString(str)) {
            return new ErrorObject(String.format(
                    "argument to 'upper' must be STRING, got %s", str.type()));
        }

        return StringClass.createStringInstance(ObjectValidator.asString(str).getValue().toUpperCase());
    };

    /**
     * 🔡 lower(string) - Convert to lowercase
     */
    private static final BuiltinFunction LOWER_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject str = args[0];
        if (!ObjectValidator.isString(str)) {
            return new ErrorObject(String.format(
                    "argument to 'lower' must be STRING, got %s", str.type()));
        }

        return StringClass.createStringInstance(ObjectValidator.asString(str).getValue().toLowerCase());
    };

    /**
     * 📏 substr(string, start, length?) - Extract substring
     */
    private static final BuiltinFunction SUBSTR_FUNCTION = (args) -> {
        if (args.length < 2 || args.length > 3) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=2 or 3", args.length));
        }

        BaseObject str = args[0];
        BaseObject startArg = args[1];
        BaseObject lengthArg = args.length == 3 ? args[2] : null;

        if (!ObjectValidator.isString(str)) {
            return new ErrorObject(String.format(
                    "first argument to 'substr' must be STRING, got %s", str.type()));
        }

        if (!ObjectValidator.isInteger(startArg)) {
            return new ErrorObject(String.format(
                    "second argument to 'substr' must be INTEGER, got %s", startArg.type()));
        }

        String string = ObjectValidator.asString(str).getValue();
        long startLong = ObjectValidator.asInteger(startArg).getValue();
        long lengthLong = string.length() - startLong;

        if (lengthArg != null) {
            if (!ObjectValidator.isInteger(lengthArg)) {
                return new ErrorObject(String.format(
                        "third argument to 'substr' must be INTEGER, got %s", lengthArg.type()));
            }
            lengthLong = ObjectValidator.asInteger(lengthArg).getValue();
        }

        int start = (int) (startLong < 0 ? Math.max(0, string.length() + startLong) : startLong);
        start = Math.max(0, Math.min(start, string.length()));

        int length = (int) Math.max(0, Math.min(lengthLong, string.length() - start));

        String result = string.substring(start, start + length);
        return StringClass.createStringInstance(result);
    };

    /**
     * 🔍 indexOf(string, substring) - Find index of substring
     */
    private static final BuiltinFunction INDEX_OF_FUNCTION = (args) -> {
        if (args.length != 2) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=2", args.length));
        }

        BaseObject str = args[0];
        BaseObject substring = args[1];

        if (!ObjectValidator.isString(str)) {
            return new ErrorObject(String.format(
                    "first argument to 'indexOf' must be STRING, got %s", str.type()));
        }

        if (!ObjectValidator.isString(substring)) {
            return new ErrorObject(String.format(
                    "second argument to 'indexOf' must be STRING, got %s", substring.type()));
        }

        int index = ObjectValidator.asString(str).getValue()
                .indexOf(ObjectValidator.asString(substring).getValue());

        return IntegerClass.createIntegerInstance(index);
    };

    /**
     * ✅ contains(string, substring) - Check if string contains substring
     */
    private static final BuiltinFunction CONTAINS_FUNCTION = (args) -> {
        if (args.length != 2) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=2", args.length));
        }

        BaseObject str = args[0];
        BaseObject substring = args[1];

        if (!ObjectValidator.isString(str)) {
            return new ErrorObject(String.format(
                    "first argument to 'contains' must be STRING, got %s", str.type()));
        }

        if (!ObjectValidator.isString(substring)) {
            return new ErrorObject(String.format(
                    "second argument to 'contains' must be STRING, got %s", substring.type()));
        }

        boolean contains = ObjectValidator.asString(str).getValue()
                .contains(ObjectValidator.asString(substring).getValue());

        return BooleanClass.createBooleanInstance(contains);
    };

    private static final BuiltinFunction CHAR_AT_FUNCTION = (args) -> {
        if (args.length != 2) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=2", args.length));
        }

        BaseObject str = args[0];
        BaseObject index = args[1];

        if (!(ObjectValidator.isString(str) || ObjectValidator.isInteger(index))) {
            return new ErrorObject(String.format(
                    "first argument to 'charAt' must be STRING or INTEGER, got %s and %s", str.type(), index.type()));
        }

        String string = ObjectValidator.asString(str).getValue();
        int indexInt = (int) ObjectValidator.asInteger(index).getValue();

        if (indexInt < 0 || indexInt >= string.length()) {
            return new ErrorObject(
                    String.format("index out of bounds: %d for string of length %d", indexInt, string.length()));
        }

        return StringClass.createStringInstance(String.valueOf(string.charAt(indexInt)));
    };

    // ============================================================================
    // 4. MATHEMATICAL OPERATIONS
    // ============================================================================

    /**
     * 📐 abs(number) - Absolute value
     */
    private static final BuiltinFunction ABS_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject arg = args[0];
        if (!ObjectValidator.isInteger(arg)) {
            return new ErrorObject(String.format(
                    "argument to 'abs' must be INTEGER, got %s", arg.type()));
        }

        long value = ObjectValidator.asInteger(arg).getValue();
        return IntegerClass.createIntegerInstance(Math.abs(value));
    };

    /**
     * 📈 max(...numbers) - Maximum value
     */
    private static final BuiltinFunction MAX_FUNCTION = (args) -> {
        if (args.length == 0) {
            return new ErrorObject("max() expected at least 1 argument, got 0");
        }

        long maxVal = Long.MIN_VALUE;

        for (BaseObject arg : args) {
            if (!ObjectValidator.isInteger(arg)) {
                return new ErrorObject(String.format(
                        "all arguments to 'max' must be INTEGER, got %s", arg.type()));
            }

            long value = ObjectValidator.asInteger(arg).getValue();
            if (value > maxVal) {
                maxVal = value;
            }
        }

        return IntegerClass.createIntegerInstance(maxVal);
    };

    /**
     * 📉 min(...numbers) - Minimum value
     */
    private static final BuiltinFunction MIN_FUNCTION = (args) -> {
        if (args.length == 0) {
            return new ErrorObject("min() expected at least 1 argument, got 0");
        }

        long minVal = Long.MAX_VALUE;

        for (BaseObject arg : args) {
            if (!ObjectValidator.isInteger(arg)) {
                return new ErrorObject(String.format(
                        "all arguments to 'min' must be INTEGER, got %s", arg.type()));
            }

            long value = ObjectValidator.asInteger(arg).getValue();
            if (value < minVal) {
                minVal = value;
            }
        }

        return IntegerClass.createIntegerInstance(minVal);
    };

    /**
     * 🔢 round(number) - Round to nearest integer
     */
    private static final BuiltinFunction ROUND_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject arg = args[0];
        if (!(arg instanceof IntegerObject)) {
            return new ErrorObject(String.format(
                    "argument to 'round' must be INTEGER, got %s", arg.type()));
        }

        // For integers, round is identity function
        return arg;
    };

    /**
     * ⬇️ floor(number) - Round down to integer
     */
    private static final BuiltinFunction FLOOR_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject arg = args[0];
        if (!(arg instanceof IntegerObject)) {
            return new ErrorObject(String.format(
                    "argument to 'floor' must be INTEGER, got %s", arg.type()));
        }

        // For integers, floor is identity function
        return arg;
    };

    /**
     * ⬆️ ceil(number) - Round up to integer
     */
    private static final BuiltinFunction CEIL_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject arg = args[0];
        if (!(arg instanceof IntegerObject)) {
            return new ErrorObject(String.format(
                    "argument to 'ceil' must be INTEGER, got %s", arg.type()));
        }

        // For integers, ceil is identity function
        return arg;
    };

    /**
     * 💪 pow(base, exponent) - Power function
     */
    private static final BuiltinFunction POW_FUNCTION = (args) -> {
        if (args.length != 2) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=2", args.length));
        }

        BaseObject base = args[0];
        BaseObject exponent = args[1];

        if (!ObjectValidator.isInteger(base)) {
            return new ErrorObject(String.format(
                    "first argument to 'pow' must be INTEGER, got %s", base.type()));
        }

        if (!ObjectValidator.isInteger(exponent)) {
            return new ErrorObject(String.format(
                    "second argument to 'pow' must be INTEGER, got %s", exponent.type()));
        }

        long baseVal = ObjectValidator.asInteger(base).getValue();
        long expVal = ObjectValidator.asInteger(exponent).getValue();

        if (expVal < 0) {
            return new ErrorObject("negative exponents not supported for integer power");
        }

        double result = Math.pow(baseVal, expVal);
        return IntegerClass.createIntegerInstance((long) result);
    };

    /**
     * ⚖️ sqrt(number) - Square root
     */
    private static final BuiltinFunction SQRT_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject arg = args[0];
        if (!ObjectValidator.isInteger(arg)) {
            return new ErrorObject(String.format(
                    "argument to 'sqrt' must be INTEGER, got %s", arg.type()));
        }

        long value = ObjectValidator.asInteger(arg).getValue();
        if (value < 0) {
            return new ErrorObject("cannot take square root of negative number");
        }

        double result = Math.sqrt(value);
        return IntegerClass.createIntegerInstance((long) result);
    };

    /**
     * 🎲 random(max?) - Random number
     */
    private static final BuiltinFunction RANDOM_FUNCTION = (args) -> {
        if (args.length > 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=0 or 1", args.length));
        }

        if (args.length == 0) {
            return IntegerClass.createIntegerInstance(new Random().nextInt(2));
        }

        BaseObject maxArg = args[0];
        if (!ObjectValidator.isInteger(maxArg)) {
            return new ErrorObject(String.format(
                    "argument to 'random' must be INTEGER, got %s", maxArg.type()));
        }

        long maxVal = ObjectValidator.asInteger(maxArg).getValue();
        if (maxVal <= 0) {
            return new ErrorObject("argument to 'random' must be positive");
        }

        long randomValue = new Random().nextLong(maxVal);
        return IntegerClass.createIntegerInstance(randomValue);
    };

    // ============================================================================
    // 5. I/O OPERATIONS
    // ============================================================================

    /**
     * 🖨️ print(...args) - Print values to console
     */
    private static final BuiltinFunction PRINT_FUNCTION = (args) -> {
        String output = Arrays.stream(args)
                .map(BaseObject::inspect)
                .collect(Collectors.joining(" "));

        System.out.print(output);
        return NullObject.INSTANCE;
    };

    /**
     * 📄 println(...args) - Print values with newline
     */
    private static final BuiltinFunction PRINTLN_FUNCTION = (args) -> {
        String output = Arrays.stream(args)
                .map(BaseObject::inspect)
                .collect(Collectors.joining(" "));

        System.out.println(output);
        return NullObject.INSTANCE;
    };

    // ============================================================================
    // 6. UTILITY FUNCTIONS
    // ============================================================================

    /**
     * 📊 range(start, end?, step?) - Generate range of numbers
     */
    private static final BuiltinFunction RANGE_FUNCTION = (args) -> {
        if (args.length < 1 || args.length > 3) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1, 2, or 3", args.length));
        }

        long start = 0;
        long end;
        long step = 1;

        if (args.length == 1) {
            // range(end)
            BaseObject endArg = args[0];
            if (!ObjectValidator.isInteger(endArg)) {
                return new ErrorObject(String.format(
                        "argument to 'range' must be INTEGER, got %s", endArg.type()));
            }
            end = ObjectValidator.asInteger(endArg).getValue();
        } else {
            // range(start, end, step?)
            BaseObject startArg = args[0];
            BaseObject endArg = args[1];

            if (!ObjectValidator.isInteger(startArg)) {
                return new ErrorObject(String.format(
                        "first argument to 'range' must be INTEGER, got %s", startArg.type()));
            }

            if (!ObjectValidator.isInteger(endArg)) {
                return new ErrorObject(String.format(
                        "second argument to 'range' must be INTEGER, got %s", endArg.type()));
            }

            start = ObjectValidator.asInteger(startArg).getValue();
            end = ObjectValidator.asInteger(endArg).getValue();

            if (args.length == 3) {
                BaseObject stepArg = args[2];
                if (!(stepArg instanceof IntegerObject)) {
                    return new ErrorObject(String.format(
                            "third argument to 'range' must be INTEGER, got %s", stepArg.type()));
                }
                step = ObjectValidator.asInteger(stepArg).getValue();

                if (step == 0) {
                    return new ErrorObject("step cannot be zero");
                }
            }
        }

        List<BaseObject> elements = new ArrayList<>();

        if (step > 0) {
            for (long i = start; i < end; i += step) {
                elements.add(IntegerClass.createIntegerInstance(i));
            }
        } else {
            for (long i = start; i > end; i += step) {
                elements.add(IntegerClass.createIntegerInstance(i));
            }
        }

        return new ArrayObject(elements);
    };

    /**
     * 🗝️ keys(hash) - Get all keys from hash object
     */
    private static final BuiltinFunction KEYS_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject arg = args[0];
        if (!(arg instanceof HashObject)) {
            return new ErrorObject(String.format(
                    "argument to 'keys' must be HASH, got %s", arg.type()));
        }

        HashObject hash = (HashObject) arg;
        List<BaseObject> keyElements = hash.getPairs().keySet().stream()
                .map(StringObject::new)
                .collect(Collectors.toList());

        return new ArrayObject(keyElements);
    };

    /**
     * 💎 values(hash) - Get all values from hash object
     */
    private static final BuiltinFunction VALUES_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject arg = args[0];
        if (!(arg instanceof HashObject)) {
            return new ErrorObject(String.format(
                    "argument to 'values' must be HASH, got %s", arg.type()));
        }

        HashObject hash = (HashObject) arg;
        List<BaseObject> valueElements = new ArrayList<>(hash.getPairs().values());

        return new ArrayObject(valueElements);
    };

    // ============================================================================
    // 7. ERROR HANDLING
    // ============================================================================

    /**
     * 🚨 error(message) - Create an error object
     */
    private static final BuiltinFunction ERROR_FUNCTION = (args) -> {
        if (args.length != 1) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1", args.length));
        }

        BaseObject message = args[0];
        if (!ObjectValidator.isString(message)) {
            return new ErrorObject(String.format(
                    "argument to 'error' must be STRING, got %s", message.type()));
        }

        return new ErrorObject(ObjectValidator.asString(message).getValue());
    };

    /**
     * ✅ assert(condition, message?) - Assert condition is true
     */
    private static final BuiltinFunction ASSERT_FUNCTION = (args) -> {
        if (args.length < 1 || args.length > 2) {
            return new ErrorObject(String.format(
                    "wrong number of arguments. got=%d, want=1 or 2", args.length));
        }

        BaseObject condition = args[0];

        if (!condition.isTruthy()) {
            String message = "Assertion failed";
            if (args.length == 2) {
                BaseObject messageArg = args[1];
                if (ObjectValidator.isString(messageArg)) {
                    message = ObjectValidator.asString(messageArg).getValue();
                }
            }
            return new ErrorObject(message);
        }

        return NullObject.INSTANCE;
    };

    // ============================================================================
    // BUILTIN REGISTRY AND MANAGEMENT
    // ============================================================================

    /**
     * 📚 All available builtin functions organized by category
     */
    public static final Map<String, BuiltinObject> BUILTINS = createBuiltinsMap();

    /**
     * 🏗️ Creates the complete builtins map with all functions
     */
    private static Map<String, BuiltinObject> createBuiltinsMap() {
        Map<String, BuiltinObject> builtins = new HashMap<>();

        // Core data operations
        builtins.put("len",
                new BuiltinObject(LEN_FUNCTION, "len", "Returns the length of arrays, strings, or hash objects"));
        builtins.put("type", new BuiltinObject(TYPE_FUNCTION, "type", "Returns the type of an object as a string"));
        builtins.put("str", new BuiltinObject(STR_FUNCTION, "str", "Converts any value to its string representation"));
        builtins.put("int", new BuiltinObject(INT_FUNCTION, "int", "Converts a string or number to an integer"));
        builtins.put("bool",
                new BuiltinObject(BOOL_FUNCTION, "bool", "Converts any value to boolean using truthiness rules"));

        // Array operations
        builtins.put("first", new BuiltinObject(FIRST_FUNCTION, "first", "Returns the first element of an array"));
        builtins.put("last", new BuiltinObject(LAST_FUNCTION, "last", "Returns the last element of an array"));
        builtins.put("rest",
                new BuiltinObject(REST_FUNCTION, "rest", "Returns a new array with all elements except the first"));
        builtins.put("push",
                new BuiltinObject(PUSH_FUNCTION, "push", "Returns a new array with the element added to the end"));
        builtins.put("pop",
                new BuiltinObject(POP_FUNCTION, "pop", "Returns a new array with the last element removed"));
        builtins.put("slice", new BuiltinObject(SLICE_FUNCTION, "slice", "Returns a portion of the array"));
        builtins.put("concat", new BuiltinObject(CONCAT_FUNCTION, "concat", "Concatenates two arrays"));
        builtins.put("reverse",
                new BuiltinObject(REVERSE_FUNCTION, "reverse", "Returns a new array with elements in reverse order"));
        builtins.put("join", new BuiltinObject(JOIN_FUNCTION, "join", "Joins array elements into a string"));

        // String operations
        builtins.put("split", new BuiltinObject(SPLIT_FUNCTION, "split", "Splits a string by delimiter"));
        builtins.put("replace", new BuiltinObject(REPLACE_FUNCTION, "replace", "Replace occurrences in string"));
        builtins.put("trim", new BuiltinObject(TRIM_FUNCTION, "trim", "Remove whitespace from both ends"));
        builtins.put("upper", new BuiltinObject(UPPER_FUNCTION, "upper", "Convert to uppercase"));
        builtins.put("lower", new BuiltinObject(LOWER_FUNCTION, "lower", "Convert to lowercase"));
        builtins.put("substr", new BuiltinObject(SUBSTR_FUNCTION, "substr", "Extract substring"));
        builtins.put("indexOf", new BuiltinObject(INDEX_OF_FUNCTION, "indexOf", "Find index of substring"));
        builtins.put("contains",
                new BuiltinObject(CONTAINS_FUNCTION, "contains", "Check if string contains substring"));
        builtins.put("charAt", new BuiltinObject(CHAR_AT_FUNCTION, "charAt", "Get character at index"));

        // Mathematical operations
        builtins.put("abs", new BuiltinObject(ABS_FUNCTION, "abs", "Absolute value"));
        builtins.put("max", new BuiltinObject(MAX_FUNCTION, "max", "Maximum value from arguments"));
        builtins.put("min", new BuiltinObject(MIN_FUNCTION, "min", "Minimum value from arguments"));
        builtins.put("round", new BuiltinObject(ROUND_FUNCTION, "round", "Round to nearest integer"));
        builtins.put("floor", new BuiltinObject(FLOOR_FUNCTION, "floor", "Round down to integer"));
        builtins.put("ceil", new BuiltinObject(CEIL_FUNCTION, "ceil", "Round up to integer"));
        builtins.put("pow", new BuiltinObject(POW_FUNCTION, "pow", "Power function"));
        builtins.put("sqrt", new BuiltinObject(SQRT_FUNCTION, "sqrt", "Square root"));
        builtins.put("random", new BuiltinObject(RANDOM_FUNCTION, "random", "Random number"));

        // I/O operations
        builtins.put("print", new BuiltinObject(PRINT_FUNCTION, "print", "Print values to console"));
        builtins.put("println", new BuiltinObject(PRINTLN_FUNCTION, "println", "Print values with newline"));

        // Utility functions
        builtins.put("range", new BuiltinObject(RANGE_FUNCTION, "range", "Generate range of numbers"));
        builtins.put("keys", new BuiltinObject(KEYS_FUNCTION, "keys", "Get all keys from hash object"));
        builtins.put("values", new BuiltinObject(VALUES_FUNCTION, "values", "Get all values from hash object"));

        // Error handling
        builtins.put("error", new BuiltinObject(ERROR_FUNCTION, "error", "Create an error object"));
        builtins.put("assert", new BuiltinObject(ASSERT_FUNCTION, "assert", "Assert condition is true"));

        return Collections.unmodifiableMap(builtins);
    }

    /**
     * 🔍 Checks if a name is a builtin function
     */
    public static boolean isBuiltin(String name) {
        return BUILTINS.containsKey(name);
    }

    /**
     * 📖 Gets a builtin function by name
     */
    public static BuiltinObject getBuiltin(String name) {
        return BUILTINS.get(name);
    }

    /**
     * 📋 Gets all builtin function names sorted alphabetically
     */
    public static List<String> getAllBuiltinNames() {
        return BUILTINS.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 📂 Gets builtins organized by functional category
     */
    public static Map<String, List<String>> getBuiltinsByCategory() {
        Map<String, List<String>> categories = new LinkedHashMap<>();

        categories.put("Core Data Operations", Arrays.asList(
                "len", "type", "str", "int", "bool"));

        categories.put("Array Operations", Arrays.asList(
                "first", "last", "rest", "push", "pop", "slice",
                "concat", "reverse", "join"));

        categories.put("String Operations", Arrays.asList(
                "split", "replace", "trim", "upper", "lower",
                "substr", "indexOf", "contains"));

        categories.put("Mathematical Operations", Arrays.asList(
                "abs", "max", "min", "round", "floor", "ceil",
                "pow", "sqrt", "random"));

        categories.put("I/O Operations", Arrays.asList(
                "print", "println"));

        categories.put("Utility Functions", Arrays.asList(
                "range", "keys", "values"));

        categories.put("Error Handling", Arrays.asList(
                "error", "assert"));

        return Collections.unmodifiableMap(categories);
    }

    /**
     * 🚀 Initializes the builtin registry with all functions
     * 
     * Call this method during interpreter startup to register all builtin
     * functions.
     * This populates the global builtin registry with all available functions.
     */
    public static void initializeBuiltins() {
        BUILTINS.forEach(BuiltinRegistry::addBuiltin);
    }

    /**
     * 📊 Returns statistics about the builtin function library
     */
    public static String getBuiltinStatistics() {
        Map<String, List<String>> categories = getBuiltinsByCategory();

        StringBuilder stats = new StringBuilder();
        stats.append("📚 Builtin Function Library Statistics\n");
        stats.append("═".repeat(45)).append("\n");
        stats.append(String.format("Total Functions: %d\n\n", BUILTINS.size()));

        categories.forEach((category, functions) -> {
            stats.append(String.format("📂 %s: %d functions\n", category, functions.size()));
            functions.forEach(func -> stats.append(String.format("   • %s\n", func)));
            stats.append("\n");
        });

        return stats.toString();
    }
}
