package lang.exec.objects.builtins;

import java.util.*;
import java.util.stream.Collectors;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.classes.*;
import lang.exec.objects.env.Environment;
import lang.exec.objects.error.ErrorObject;
import lang.exec.objects.structures.*;
import lang.exec.validator.ObjectValidator;

/**
 * 📋 ArrayClass - Built-in Array Type Class 📋
 * 
 * From first principles, arrays should have methods for:
 * - Length and size operations
 * - Element access and modification (append, insert, remove)
 * - Search operations (indexOf, contains)
 * - Transformation operations (map, filter, sort)
 * - Iteration support
 * - Slicing and joining
 */
public class ArrayClass extends ClassObject {

    public static final String ARRAY_CLASS_NAME = "Array";
    private static ArrayClass instance;

    private ArrayClass() {
        super(
                ARRAY_CLASS_NAME,
                Optional.of(BaseObjectClass.getInstance()),
                Optional.empty(), // No constructor - arrays are created via literals
                createArrayMethods(),
                new Environment());
    }

    public static ArrayClass getInstance() {
        if (instance == null) {
            instance = new ArrayClass();
        }
        return instance;
    }

    private static Map<String, MethodObject> createArrayMethods() {
        var env = new Environment();
        Map<String, MethodObject> methods = new HashMap<>();

        // Basic array methods
        methods.put("length", createLengthMethod(env));
        methods.put("size", createLengthMethod(env)); // Alias
        methods.put("isEmpty", createIsEmptyMethod(env));
        methods.put("clear", createClearMethod(env));

        // Element access and modification
        methods.put("get", createGetMethod(env));
        methods.put("set", createSetMethod(env));
        methods.put("append", createAppendMethod(env));
        methods.put("prepend", createPrependMethod(env));
        methods.put("insert", createInsertMethod(env));
        methods.put("removeAt", createRemoveAtMethod(env));
        methods.put("remove", createRemoveMethod(env));
        methods.put("pop", createPopMethod(env));
        methods.put("shift", createShiftMethod(env));

        // Search operations
        methods.put("indexOf", createIndexOfMethod(env));
        methods.put("lastIndexOf", createLastIndexOfMethod(env));
        methods.put("contains", createContainsMethod(env));
        methods.put("count", createCountMethod(env));

        // Array operations
        methods.put("slice", createSliceMethod(env));
        methods.put("concat", createConcatMethod(env));
        methods.put("join", createJoinMethod(env));
        methods.put("reverse", createReverseMethod(env));
        methods.put("sort", createSortMethod(env));

        // Functional programming methods
        methods.put("forEach", createForEachMethod(env));
        methods.put("map", createMapMethod(env));
        methods.put("filter", createFilterMethod(env));
        methods.put("reduce", createReduceMethod(env));
        methods.put("find", createFindMethod(env));
        methods.put("findIndex", createFindIndexMethod(env));
        methods.put("every", createEveryMethod(env));
        methods.put("some", createSomeMethod(env));

        // Dunder methods
        methods.put("__getitem__", createArrayGetItemMethod(env));
        methods.put("__setitem__", createArraySetItemMethod(env));
        methods.put("__len__", createLengthMethod(env));
        methods.put("__add__", createArrayConcatMethod(env));
        methods.put("__mul__", createArrayRepeatMethod(env));
        methods.put("__eq__", createArrayEqMethod(env));
        methods.put("__ne__", createArrayNeMethod(env));

        return methods;
    }

    // Basic methods
    private static MethodObject createLengthMethod(Environment env) {
        return new BuiltInMethod(
                "length",
                "Returns the number of elements in this array",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("length() takes no arguments, got " + args.length);
                    }

                    ArrayObject array = getArrayValue(instance);
                    return IntegerClass.createIntegerInstance(array.size());
                },
                env);
    }

    private static MethodObject createIsEmptyMethod(Environment env) {
        return new BuiltInMethod(
                "isEmpty",
                "Returns true if this array has no elements",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("isEmpty() takes no arguments, got " + args.length);
                    }

                    ArrayObject array = getArrayValue(instance);
                    return BooleanClass.createBooleanInstance(array.isEmpty());
                },
                env);
    }

    private static MethodObject createClearMethod(Environment env) {
        return new BuiltInMethod(
                "clear",
                "Removes all elements from this array",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("clear() takes no arguments, got " + args.length);
                    }

                    ArrayObject array = getArrayValue(instance);
                    array.clear();
                    return instance; // Return self for method chaining
                },
                env);
    }

    // Element access and modification
    private static MethodObject createGetMethod(Environment env) {
        return new BuiltInMethod(
                "get",
                "Returns the element at the specified index",
                List.of("index"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("get() takes exactly 1 argument, got " + args.length);
                    }

                    if (!ObjectValidator.isInteger(args[0])) {
                        return new ErrorObject("Array index must be an integer");
                    }

                    ArrayObject array = getArrayValue(instance);
                    int index = (int) getIntegerValue(args[0]);

                    if (!array.isValidIndex(index)) {
                        return new ErrorObject("Array index out of bounds: " + index);
                    }

                    return array.get(index);
                },
                env);
    }

    private static MethodObject createSetMethod(Environment env) {
        return new BuiltInMethod(
                "set",
                "Sets the element at the specified index",
                List.of("index", "value"),
                (instance, args) -> {
                    if (args.length != 2) {
                        return new ErrorObject("set() takes exactly 2 arguments, got " + args.length);
                    }

                    if (!ObjectValidator.isInteger(args[0])) {
                        return new ErrorObject("Array index must be an integer");
                    }

                    ArrayObject array = getArrayValue(instance);
                    int index = (int) getIntegerValue(args[0]);
                    BaseObject value = args[1];

                    if (!array.isValidIndex(index)) {
                        return new ErrorObject("Array index out of bounds: " + index);
                    }

                    return array.set(index, value);
                },
                env);
    }

    private static MethodObject createAppendMethod(Environment env) {
        return new BuiltInMethod(
                "append",
                "Adds an element to the end of this array",
                List.of("element"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("append() takes exactly 1 argument, got " + args.length);
                    }

                    ArrayObject array = getArrayValue(instance);
                    array.append(args[0]);
                    return instance; // Return self for method chaining
                },
                env);
    }

    private static MethodObject createPrependMethod(Environment env) {
        return new BuiltInMethod(
                "prepend",
                "Adds an element to the beginning of this array",
                List.of("element"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("prepend() takes exactly 1 argument, got " + args.length);
                    }

                    ArrayObject array = getArrayValue(instance);
                    array.insert(0, args[0]);
                    return instance; // Return self for method chaining
                },
                env);
    }

    private static MethodObject createInsertMethod(Environment env) {
        return new BuiltInMethod(
                "insert",
                "Inserts an element at the specified index",
                List.of("index", "element"),
                (instance, args) -> {
                    if (args.length != 2) {
                        return new ErrorObject("insert() takes exactly 2 arguments, got " + args.length);
                    }

                    if (!ObjectValidator.isInteger(args[0])) {
                        return new ErrorObject("Insert index must be an integer");
                    }

                    ArrayObject array = getArrayValue(instance);
                    int index = (int) getIntegerValue(args[0]);
                    BaseObject element = args[1];

                    if (index < 0 || index > array.size()) {
                        return new ErrorObject("Insert index out of bounds: " + index);
                    }

                    array.insert(index, element);
                    return instance; // Return self for method chaining
                },
                env);
    }

    private static MethodObject createRemoveAtMethod(Environment env) {
        return new BuiltInMethod(
                "removeAt",
                "Removes and returns the element at the specified index",
                List.of("index"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("removeAt() takes exactly 1 argument, got " + args.length);
                    }

                    if (!ObjectValidator.isInteger(args[0])) {
                        return new ErrorObject("Remove index must be an integer");
                    }

                    ArrayObject array = getArrayValue(instance);
                    int index = (int) getIntegerValue(args[0]);

                    if (!array.isValidIndex(index)) {
                        return new ErrorObject("Array index out of bounds: " + index);
                    }

                    return array.removeAt(index);
                },
                env);
    }

    private static MethodObject createRemoveMethod(Environment env) {
        return new BuiltInMethod(
                "remove",
                "Removes the first occurrence of the specified element",
                List.of("element"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("remove() takes exactly 1 argument, got " + args.length);
                    }

                    ArrayObject array = getArrayValue(instance);
                    boolean removed = array.remove(args[0]);
                    return BooleanClass.createBooleanInstance(removed);
                },
                env);
    }

    private static MethodObject createPopMethod(Environment env) {
        return new BuiltInMethod(
                "pop",
                "Removes and returns the last element",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("pop() takes no arguments, got " + args.length);
                    }

                    ArrayObject array = getArrayValue(instance);
                    if (array.isEmpty()) {
                        return new ErrorObject("Cannot pop from empty array");
                    }

                    return array.removeAt(array.size() - 1);
                },
                env);
    }

    private static MethodObject createShiftMethod(Environment env) {
        return new BuiltInMethod(
                "shift",
                "Removes and returns the first element",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("shift() takes no arguments, got " + args.length);
                    }

                    ArrayObject array = getArrayValue(instance);
                    if (array.isEmpty()) {
                        return new ErrorObject("Cannot shift from empty array");
                    }

                    return array.removeAt(0);
                },
                env);
    }

    // Search operations
    private static MethodObject createIndexOfMethod(Environment env) {
        return new BuiltInMethod(
                "indexOf",
                "Returns the index of the first occurrence of the element",
                List.of("element"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("indexOf() takes exactly 1 argument, got " + args.length);
                    }

                    ArrayObject array = getArrayValue(instance);
                    BaseObject target = args[0];

                    for (int i = 0; i < array.size(); i++) {
                        BaseObject element = array.get(i);
                        if (areEqual(element, target)) {
                            return IntegerClass.createIntegerInstance(i);
                        }
                    }

                    return IntegerClass.createIntegerInstance(-1);
                },
                env);
    }

    private static MethodObject createLastIndexOfMethod(Environment env) {
        return new BuiltInMethod(
                "lastIndexOf",
                "Returns the index of the last occurrence of the element",
                List.of("element"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("lastIndexOf() takes exactly 1 argument, got " + args.length);
                    }

                    ArrayObject array = getArrayValue(instance);
                    BaseObject target = args[0];

                    for (int i = array.size() - 1; i >= 0; i--) {
                        BaseObject element = array.get(i);
                        if (areEqual(element, target)) {
                            return IntegerClass.createIntegerInstance(i);
                        }
                    }

                    return IntegerClass.createIntegerInstance(-1);
                },
                env);
    }

    private static MethodObject createContainsMethod(Environment env) {
        return new BuiltInMethod(
                "contains",
                "Returns true if the array contains the specified element",
                List.of("element"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("contains() takes exactly 1 argument, got " + args.length);
                    }

                    ArrayObject array = getArrayValue(instance);
                    BaseObject target = args[0];

                    for (int i = 0; i < array.size(); i++) {
                        BaseObject element = array.get(i);
                        if (areEqual(element, target)) {
                            return BooleanClass.createBooleanInstance(true);
                        }
                    }

                    return BooleanClass.createBooleanInstance(false);
                },
                env);
    }

    private static MethodObject createCountMethod(Environment env) {
        return new BuiltInMethod(
                "count",
                "Returns the number of occurrences of the element",
                List.of("element"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("count() takes exactly 1 argument, got " + args.length);
                    }

                    ArrayObject array = getArrayValue(instance);
                    BaseObject target = args[0];
                    int count = 0;

                    for (int i = 0; i < array.size(); i++) {
                        BaseObject element = array.get(i);
                        if (areEqual(element, target)) {
                            count++;
                        }
                    }

                    return IntegerClass.createIntegerInstance(count);
                },
                env);
    }

    // Array operations
    private static MethodObject createSliceMethod(Environment env) {
        return new BuiltInMethod(
                "slice",
                "Returns a new array containing elements from start to end (exclusive)",
                List.of("start", "end"),
                (instance, args) -> {
                    if (args.length < 1 || args.length > 2) {
                        return new ErrorObject("slice() takes 1 or 2 arguments, got " + args.length);
                    }

                    if (!ObjectValidator.isInteger(args[0])) {
                        return new ErrorObject("Slice start index must be an integer");
                    }

                    ArrayObject array = getArrayValue(instance);
                    int start = (int) getIntegerValue(args[0]);
                    int end = array.size();

                    if (args.length == 2) {
                        if (!ObjectValidator.isInteger(args[1])) {
                            return new ErrorObject("Slice end index must be an integer");
                        }
                        end = (int) getIntegerValue(args[1]);
                    }

                    // Handle negative indices
                    if (start < 0)
                        start += array.size();
                    if (end < 0)
                        end += array.size();

                    // Clamp to valid range
                    start = Math.max(0, Math.min(start, array.size()));
                    end = Math.max(start, Math.min(end, array.size()));

                    List<BaseObject> sliced = array.getElements().subList(start, end);
                    return createArrayInstance(new ArrayList<>(sliced));
                },
                env);
    }

    private static MethodObject createConcatMethod(Environment env) {
        return new BuiltInMethod(
                "concat",
                "Returns a new array that is the concatenation of this array and another",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("concat() takes exactly 1 argument, got " + args.length);
                    }

                    ArrayObject array = getArrayValue(instance);
                    ArrayObject other = getArrayValue(args[0]);

                    List<BaseObject> combined = new ArrayList<>(array.getElements());
                    combined.addAll(other.getElements());

                    return createArrayInstance(combined);
                },
                env);
    }

    private static MethodObject createJoinMethod(Environment env) {
        return new BuiltInMethod(
                "join",
                "Joins all elements into a string using the specified separator",
                List.of("separator"),
                (instance, args) -> {
                    if (args.length > 1) {
                        return new ErrorObject("join() takes 0 or 1 arguments, got " + args.length);
                    }

                    String separator = ",";
                    if (args.length == 1) {
                        separator = getStringValue(args[0]);
                    }

                    ArrayObject array = getArrayValue(instance);
                    List<String> stringElements = array.getElements().stream()
                            .map(element -> convertToString(element))
                            .collect(Collectors.toList());

                    String joined = String.join(separator, stringElements);
                    return StringClass.createStringInstance(joined);
                },
                env);
    }

    private static MethodObject createReverseMethod(Environment env) {
        return new BuiltInMethod(
                "reverse",
                "Reverses the order of elements in this array",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("reverse() takes no arguments, got " + args.length);
                    }

                    ArrayObject array = getArrayValue(instance);
                    Collections.reverse(array.getElements());
                    return instance; // Return self for method chaining
                },
                env);
    }

    private static MethodObject createSortMethod(Environment env) {
        return new BuiltInMethod(
                "sort",
                "Sorts the elements in this array",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("sort() takes no arguments, got " + args.length);
                    }

                    ArrayObject array = getArrayValue(instance);

                    // Basic sort - convert elements to strings and sort lexicographically
                    array.getElements().sort((a, b) -> {
                        String aStr = convertToString(a);
                        String bStr = convertToString(b);
                        return aStr.compareTo(bStr);
                    });

                    return instance; // Return self for method chaining
                },
                env);
    }

    // Dunder methods
    private static MethodObject createArrayGetItemMethod(Environment env) {
        return new BuiltInMethod(
                "__getitem__",
                "Array indexing operator",
                List.of("index"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__getitem__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!ObjectValidator.isInteger(args[0])) {
                        return new ErrorObject("Array index must be an integer");
                    }

                    ArrayObject array = getArrayValue(instance);
                    int index = (int) getIntegerValue(args[0]);

                    if (!array.isValidIndex(index)) {
                        return new ErrorObject("Array index out of bounds: " + index);
                    }

                    return array.get(index);
                },
                env);
    }

    private static MethodObject createArraySetItemMethod(Environment env) {
        return new BuiltInMethod(
                "__setitem__",
                "Array index assignment operator",
                List.of("index", "value"),
                (instance, args) -> {
                    if (args.length != 2) {
                        return new ErrorObject("__setitem__() takes exactly 2 arguments, got " + args.length);
                    }

                    if (!ObjectValidator.isInteger(args[0])) {
                        return new ErrorObject("Array index must be an integer");
                    }

                    ArrayObject array = getArrayValue(instance);
                    int index = (int) getIntegerValue(args[0]);
                    BaseObject value = args[1];

                    if (!array.isValidIndex(index)) {
                        return new ErrorObject("Array index out of bounds: " + index);
                    }

                    return array.set(index, value);
                },
                env);
    }

    private static MethodObject createArrayConcatMethod(Environment env) {
        return new BuiltInMethod(
                "__add__",
                "Array concatenation operator",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__add__() takes exactly 1 argument, got " + args.length);
                    }

                    ArrayObject array = getArrayValue(instance);
                    ArrayObject other = getArrayValue(args[0]);

                    List<BaseObject> combined = new ArrayList<>(array.getElements());
                    combined.addAll(other.getElements());

                    return createArrayInstance(combined);
                },
                env);
    }

    private static MethodObject createArrayRepeatMethod(Environment env) {
        return new BuiltInMethod(
                "__mul__",
                "Array repetition operator",
                List.of("count"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__mul__() takes exactly 1 argument, got " + args.length);
                    }

                    if (!ObjectValidator.isInteger(args[0])) {
                        return new ErrorObject("Array repetition count must be an integer");
                    }

                    ArrayObject array = getArrayValue(instance);
                    int count = (int) getIntegerValue(args[0]);

                    if (count < 0) {
                        return new ErrorObject("Array repetition count cannot be negative");
                    }

                    List<BaseObject> repeated = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        repeated.addAll(array.getElements());
                    }

                    return createArrayInstance(repeated);
                },
                env);
    }

    private static MethodObject createArrayEqMethod(Environment env) {
        return new BuiltInMethod(
                "__eq__",
                "Array equality operator",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__eq__() takes exactly 1 argument, got " + args.length);
                    }

                    ArrayObject thisArray = getArrayValue(instance);
                    ArrayObject otherArray = getArrayValue(args[0]);

                    if (thisArray.size() != otherArray.size()) {
                        return BooleanClass.createBooleanInstance(false);
                    }

                    for (int i = 0; i < thisArray.size(); i++) {
                        if (!areEqual(thisArray.get(i), otherArray.get(i))) {
                            return BooleanClass.createBooleanInstance(false);
                        }
                    }

                    return BooleanClass.createBooleanInstance(true);
                },
                env);
    }

    private static MethodObject createArrayNeMethod(Environment env) {
        return new BuiltInMethod(
                "__ne__",
                "Array inequality operator",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__ne__() takes exactly 1 argument, got " + args.length);
                    }

                    ArrayObject thisArray = getArrayValue(instance);
                    ArrayObject otherArray = getArrayValue(args[0]);

                    if (thisArray.size() != otherArray.size()) {
                        return BooleanClass.createBooleanInstance(true); // Different sizes = not equal
                    }

                    for (int i = 0; i < thisArray.size(); i++) {
                        if (!areEqual(thisArray.get(i), otherArray.get(i))) {
                            return BooleanClass.createBooleanInstance(true); // Found difference = not equal
                        }
                    }

                    return BooleanClass.createBooleanInstance(false); // All elements equal = arrays are equal
                },
                env);
    }

    // Functional programming methods (simplified implementations)
    private static MethodObject createForEachMethod(Environment env) {
        return new BuiltInMethod(
                "forEach",
                "Executes a function for each element in the array",
                List.of("callback"),
                (_, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("forEach() takes exactly 1 argument, got " + args.length);
                    }

                    // For now, return a placeholder since we'd need function execution support
                    return new ErrorObject("forEach() not yet implemented - requires function execution");
                },
                env);
    }

    private static MethodObject createMapMethod(Environment env) {
        return new BuiltInMethod(
                "map",
                "Creates a new array with the results of calling a function for every element",
                List.of("callback"),
                (_, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("map() takes exactly 1 argument, got " + args.length);
                    }

                    // For now, return a placeholder since we'd need function execution support
                    return new ErrorObject("map() not yet implemented - requires function execution");
                },
                env);
    }

    private static MethodObject createFilterMethod(Environment env) {
        return new BuiltInMethod(
                "filter",
                "Creates a new array with all elements that pass the test",
                List.of("callback"),
                (_, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("filter() takes exactly 1 argument, got " + args.length);
                    }

                    // For now, return a placeholder since we'd need function execution support
                    return new ErrorObject("filter() not yet implemented - requires function execution");
                },
                env);
    }

    private static MethodObject createReduceMethod(Environment env) {
        return new BuiltInMethod(
                "reduce",
                "Reduces the array to a single value using a reducer function",
                List.of("callback", "initialValue"),
                (_, args) -> {
                    if (args.length < 1 || args.length > 2) {
                        return new ErrorObject("reduce() takes 1 or 2 arguments, got " + args.length);
                    }

                    // For now, return a placeholder since we'd need function execution support
                    return new ErrorObject("reduce() not yet implemented - requires function execution");
                },
                env);
    }

    private static MethodObject createFindMethod(Environment env) {
        return new BuiltInMethod(
                "find",
                "Returns the first element that satisfies the testing function",
                List.of("callback"),
                (_, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("find() takes exactly 1 argument, got " + args.length);
                    }

                    // For now, return a placeholder since we'd need function execution support
                    return new ErrorObject("find() not yet implemented - requires function execution");
                },
                env);
    }

    private static MethodObject createFindIndexMethod(Environment env) {
        return new BuiltInMethod(
                "findIndex",
                "Returns the index of the first element that satisfies the testing function",
                List.of("callback"),
                (_, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("findIndex() takes exactly 1 argument, got " + args.length);
                    }

                    // For now, return a placeholder since we'd need function execution support
                    return new ErrorObject("findIndex() not yet implemented - requires function execution");
                },
                env);
    }

    private static MethodObject createEveryMethod(Environment env) {
        return new BuiltInMethod(
                "every",
                "Tests whether all elements pass the test implemented by the function",
                List.of("callback"),
                (_, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("every() takes exactly 1 argument, got " + args.length);
                    }

                    // For now, return a placeholder since we'd need function execution support
                    return new ErrorObject("every() not yet implemented - requires function execution");
                },
                env);
    }

    private static MethodObject createSomeMethod(Environment env) {
        return new BuiltInMethod(
                "some",
                "Tests whether at least one element passes the test implemented by the function",
                List.of("callback"),
                (_, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("some() takes exactly 1 argument, got " + args.length);
                    }

                    // For now, return a placeholder since we'd need function execution support
                    return new ErrorObject("some() not yet implemented - requires function execution");
                },
                env);
    }

    // Helper methods
    private static ArrayObject getArrayValue(BaseObject obj) {
        if (ObjectValidator.isInstance(obj)) {
            InstanceObject instance = ObjectValidator.asInstance(obj);
            Optional<BaseObject> valueProperty = instance.getProperty("value");
            if (valueProperty.isPresent() && ObjectValidator.isArray(valueProperty.get())) {
                return ObjectValidator.asArray(valueProperty.get());
            }
        }
        // Fallback for old ArrayObject
        if (ObjectValidator.isArray(obj)) {
            return ObjectValidator.asArray(obj);
        }
        throw new RuntimeException("Expected array object");
    }

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

    private static String getStringValue(BaseObject obj) {
        if (ObjectValidator.isInstance(obj)) {
            InstanceObject instance = ObjectValidator.asInstance(obj);
            Optional<BaseObject> valueProperty = instance.getProperty("value");
            if (valueProperty.isPresent() && ObjectValidator.isString(valueProperty.get())) {
                return ObjectValidator.asString(valueProperty.get()).getValue();
            }
        }
        if (ObjectValidator.isString(obj)) {
            return ObjectValidator.asString(obj).getValue();
        }
        return obj.inspect();
    }

    private static String convertToString(BaseObject obj) {
        if (obj == null)
            return "null";

        // Try to call toString method if it's an instance
        if (ObjectValidator.isInstance(obj)) {
            InstanceObject instance = ObjectValidator.asInstance(obj);
            Optional<MethodObject> toStringMethod = instance.findMethod("toString");
            if (toStringMethod.isPresent()) {
                // For now, just use inspect() since we'd need evaluation context
                return obj.inspect();
            }
        }

        return obj.inspect();
    }

    private static boolean areEqual(BaseObject a, BaseObject b) {
        if (a == b)
            return true;
        if (a == null || b == null)
            return false;

        // Try to use __eq__ method if available
        if (ObjectValidator.isInstance(a)) {
            InstanceObject instance = ObjectValidator.asInstance(a);
            Optional<MethodObject> eqMethod = instance.findMethod("__eq__");
            if (eqMethod.isPresent()) {
                // For now, use basic equality since we'd need evaluation context
                return a.equals(b);
            }
        }

        return a.equals(b);
    }

    /**
     * 🏗️ Creates an Array instance with the given elements
     */
    public static InstanceObject createArrayInstance(List<BaseObject> elements) {
        InstanceObject instance = ArrayClass.getInstance().createInstance();
        instance.setProperty("value", new ArrayObject(elements));
        return instance;
    }
}