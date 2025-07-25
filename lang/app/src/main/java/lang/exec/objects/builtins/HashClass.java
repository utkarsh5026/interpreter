package lang.exec.objects.builtins;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import lang.exec.objects.classes.*;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.error.ErrorObject;
import lang.exec.objects.literals.*;
import lang.exec.validator.ObjectValidator;
import lang.exec.objects.structures.HashObject;

class HashClass extends ClassObject {

    public static final String HASH_CLASS_NAME = "Hash";
    private static HashClass instance;

    private HashClass() {
        super(
                HASH_CLASS_NAME,
                Optional.of(BaseObjectClass.getInstance()),
                Optional.empty(), // No constructor - hashes are created via literals
                createHashMethods(),
                new Environment());
    }

    public static HashClass getInstance() {
        if (instance == null) {
            instance = new HashClass();
        }
        return instance;
    }

    private static Map<String, MethodObject> createHashMethods() {
        var env = new Environment();
        Map<String, MethodObject> methods = new HashMap<>();

        // Basic hash methods
        methods.put("size", createHashSizeMethod(env));
        methods.put("length", createHashSizeMethod(env)); // Alias
        methods.put("isEmpty", createHashIsEmptyMethod(env));
        methods.put("clear", createHashClearMethod(env));

        // Key-value operations
        methods.put("get", createHashGetMethod(env));
        methods.put("set", createHashSetMethod(env));
        methods.put("has", createHashHasMethod(env));
        methods.put("hasKey", createHashHasMethod(env)); // Alias
        methods.put("remove", createHashRemoveMethod(env));
        methods.put("delete", createHashRemoveMethod(env)); // Alias

        // Key and value retrieval
        methods.put("keys", createHashKeysMethod(env));
        methods.put("values", createHashValuesMethod(env));
        methods.put("entries", createHashEntriesMethod(env));
        methods.put("items", createHashEntriesMethod(env)); // Alias

        // Hash operations
        methods.put("merge", createHashMergeMethod(env));
        methods.put("update", createHashUpdateMethod(env));
        methods.put("copy", createHashCopyMethod(env));
        methods.put("clone", createHashCopyMethod(env)); // Alias

        // Utility methods
        methods.put("forEach", createHashForEachMethod(env));
        methods.put("filter", createHashFilterMethod(env));
        methods.put("map", createHashMapMethod(env));

        // Dunder methods
        methods.put("__getitem__", createHashGetItemMethod(env));
        methods.put("__setitem__", createHashSetItemMethod(env));
        methods.put("__len__", createHashSizeMethod(env));
        methods.put("__eq__", createHashEqMethod(env));
        methods.put("__ne__", createHashNeMethod(env));
        methods.put("__contains__", createHashContainsMethod(env));

        return methods;
    }

    // Basic methods
    private static MethodObject createHashSizeMethod(Environment env) {
        return new BuiltInMethod(
                "size",
                "Returns the number of key-value pairs in this hash",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("size() takes no arguments, got " + args.length);
                    }

                    HashObject hash = getHashValue(instance);
                    return IntegerClass.createIntegerInstance(hash.size());
                },
                env);
    }

    private static MethodObject createHashIsEmptyMethod(Environment env) {
        return new BuiltInMethod(
                "isEmpty",
                "Returns true if this hash has no key-value pairs",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("isEmpty() takes no arguments, got " + args.length);
                    }

                    HashObject hash = getHashValue(instance);
                    return BooleanClass.createBooleanInstance(hash.size() == 0);
                },
                env);
    }

    private static MethodObject createHashClearMethod(Environment env) {
        return new BuiltInMethod(
                "clear",
                "Removes all key-value pairs from this hash",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("clear() takes no arguments, got " + args.length);
                    }

                    HashObject hash = getHashValue(instance);
                    hash.clear();
                    return instance; // Return self for method chaining
                },
                env);
    }

    // Key-value operations
    private static MethodObject createHashGetMethod(Environment env) {
        return new BuiltInMethod(
                "get",
                "Returns the value for the specified key, or a default value",
                List.of("key", "defaultValue"),
                (instance, args) -> {
                    if (args.length < 1 || args.length > 2) {
                        return new ErrorObject("get() takes 1 or 2 arguments, got " + args.length);
                    }

                    String key = getStringValue(args[0]);
                    HashObject hash = getHashValue(instance);

                    if (hash.hasKey(key)) {
                        return hash.get(key);
                    } else if (args.length == 2) {
                        return args[1]; // Return default value
                    } else {
                        return NullObject.INSTANCE;
                    }
                },
                env);
    }

    private static MethodObject createHashSetMethod(Environment env) {
        return new BuiltInMethod(
                "set",
                "Sets the value for the specified key",
                List.of("key", "value"),
                (instance, args) -> {
                    if (args.length != 2) {
                        return new ErrorObject("set() takes exactly 2 arguments, got " + args.length);
                    }

                    String key = getStringValue(args[0]);
                    BaseObject value = args[1];
                    HashObject hash = getHashValue(instance);

                    return hash.set(key, value);
                },
                env);
    }

    private static MethodObject createHashHasMethod(Environment env) {
        return new BuiltInMethod(
                "has",
                "Returns true if the hash contains the specified key",
                List.of("key"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("has() takes exactly 1 argument, got " + args.length);
                    }

                    String key = getStringValue(args[0]);
                    HashObject hash = getHashValue(instance);

                    return BooleanClass.createBooleanInstance(hash.hasKey(key));
                },
                env);
    }

    private static MethodObject createHashRemoveMethod(Environment env) {
        return new BuiltInMethod(
                "remove",
                "Removes and returns the value for the specified key",
                List.of("key"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("remove() takes exactly 1 argument, got " + args.length);
                    }

                    String key = getStringValue(args[0]);
                    HashObject hash = getHashValue(instance);

                    if (hash.hasKey(key)) {
                        return hash.remove(key);
                    } else {
                        return NullObject.INSTANCE;
                    }
                },
                env);
    }

    // Key and value retrieval
    private static MethodObject createHashKeysMethod(Environment env) {
        return new BuiltInMethod(
                "keys",
                "Returns an array of all keys in this hash",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("keys() takes no arguments, got " + args.length);
                    }

                    HashObject hash = getHashValue(instance);
                    List<BaseObject> keys = hash.getPairs().keySet().stream()
                            .map(StringClass::createStringInstance)
                            .collect(Collectors.toList());

                    return ArrayClass.createArrayInstance(keys);
                },
                env);
    }

    private static MethodObject createHashValuesMethod(Environment env) {
        return new BuiltInMethod(
                "values",
                "Returns an array of all values in this hash",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("values() takes no arguments, got " + args.length);
                    }

                    HashObject hash = getHashValue(instance);
                    List<BaseObject> values = new ArrayList<>(hash.getPairs().values());

                    return ArrayClass.createArrayInstance(values);
                },
                env);
    }

    private static MethodObject createHashEntriesMethod(Environment env) {
        return new BuiltInMethod(
                "entries",
                "Returns an array of [key, value] pairs",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("entries() takes no arguments, got " + args.length);
                    }

                    HashObject hash = getHashValue(instance);
                    List<BaseObject> entries = hash.getPairs().entrySet().stream()
                            .map(entry -> {
                                List<BaseObject> pair = List.of(
                                        StringClass.createStringInstance(entry.getKey()),
                                        entry.getValue());
                                return ArrayClass.createArrayInstance(pair);
                            })
                            .collect(Collectors.toList());

                    return ArrayClass.createArrayInstance(entries);
                }, env);
    }

    // Hash operations
    private static MethodObject createHashMergeMethod(Environment env) {
        return new BuiltInMethod(
                "merge",
                "Returns a new hash that is the merge of this hash and another",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("merge() takes exactly 1 argument, got " + args.length);
                    }

                    HashObject thisHash = getHashValue(instance);
                    HashObject otherHash = getHashValue(args[0]);

                    Map<String, BaseObject> merged = new HashMap<>(thisHash.getPairs());
                    merged.putAll(otherHash.getPairs());

                    return createHashInstance(merged);
                },
                env);
    }

    private static MethodObject createHashUpdateMethod(Environment env) {
        return new BuiltInMethod(
                "update",
                "Updates this hash with key-value pairs from another hash",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("update() takes exactly 1 argument, got " + args.length);
                    }

                    HashObject thisHash = getHashValue(instance);
                    HashObject otherHash = getHashValue(args[0]);

                    for (Map.Entry<String, BaseObject> entry : otherHash.getPairs().entrySet()) {
                        thisHash.set(entry.getKey(), entry.getValue());
                    }

                    return instance; // Return self for method chaining
                },
                env);
    }

    private static MethodObject createHashCopyMethod(Environment env) {
        return new BuiltInMethod(
                "copy",
                "Returns a shallow copy of this hash",
                Collections.emptyList(),
                (instance, args) -> {
                    if (args.length != 0) {
                        return new ErrorObject("copy() takes no arguments, got " + args.length);
                    }

                    HashObject hash = getHashValue(instance);
                    Map<String, BaseObject> copied = new HashMap<>(hash.getPairs());

                    return createHashInstance(copied);
                },
                env);
    }

    // Utility methods (simplified implementations)
    private static MethodObject createHashForEachMethod(Environment env) {
        return new BuiltInMethod(
                "forEach",
                "Executes a function for each key-value pair in the hash",
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

    private static MethodObject createHashFilterMethod(Environment env) {
        return new BuiltInMethod(
                "filter",
                "Creates a new hash with key-value pairs that pass the test",
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

    private static MethodObject createHashMapMethod(Environment env) {
        return new BuiltInMethod(
                "map",
                "Creates a new hash with transformed values",
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

    // Dunder methods
    private static MethodObject createHashGetItemMethod(Environment env) {
        return new BuiltInMethod(
                "__getitem__",
                "Hash indexing operator",
                List.of("key"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__getitem__() takes exactly 1 argument, got " + args.length);
                    }

                    String key = getStringValue(args[0]);
                    HashObject hash = getHashValue(instance);

                    if (hash.hasKey(key)) {
                        return hash.get(key);
                    } else {
                        return NullObject.INSTANCE;
                    }
                },
                env);
    }

    private static MethodObject createHashSetItemMethod(Environment env) {
        return new BuiltInMethod(
                "__setitem__",
                "Hash index assignment operator",
                List.of("key", "value"),
                (instance, args) -> {
                    if (args.length != 2) {
                        return new ErrorObject("__setitem__() takes exactly 2 arguments, got " + args.length);
                    }

                    String key = getStringValue(args[0]);
                    BaseObject value = args[1];
                    HashObject hash = getHashValue(instance);

                    return hash.set(key, value);
                },
                env);
    }

    private static MethodObject createHashEqMethod(Environment env) {
        return new BuiltInMethod(
                "__eq__",
                "Hash equality operator",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__eq__() takes exactly 1 argument, got " + args.length);
                    }

                    HashObject thisHash = getHashValue(instance);
                    HashObject otherHash = getHashValue(args[0]);

                    if (thisHash.size() != otherHash.size()) {
                        return BooleanClass.createBooleanInstance(false);
                    }

                    for (Map.Entry<String, BaseObject> entry : thisHash.getPairs().entrySet()) {
                        String key = entry.getKey();
                        BaseObject thisValue = entry.getValue();

                        if (!otherHash.hasKey(key)) {
                            return BooleanClass.createBooleanInstance(false);
                        }

                        BaseObject otherValue = otherHash.get(key);
                        if (!areEqual(thisValue, otherValue)) {
                            return BooleanClass.createBooleanInstance(false);
                        }
                    }

                    return BooleanClass.createBooleanInstance(true);
                },
                env);
    }

    private static MethodObject createHashNeMethod(Environment env) {
        return new BuiltInMethod(
                "__ne__",
                "Hash inequality operator",
                List.of("other"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__ne__() takes exactly 1 argument, got " + args.length);
                    }

                    HashObject thisHash = getHashValue(instance);
                    HashObject otherHash = getHashValue(args[0]);

                    if (thisHash.size() != otherHash.size()) {
                        return BooleanClass.createBooleanInstance(true); // Different sizes = not equal
                    }

                    for (Map.Entry<String, BaseObject> entry : thisHash.getPairs().entrySet()) {
                        String key = entry.getKey();
                        BaseObject thisValue = entry.getValue();

                        if (!otherHash.hasKey(key)) {
                            return BooleanClass.createBooleanInstance(true); // Missing key = not equal
                        }

                        BaseObject otherValue = otherHash.get(key);
                        if (!areEqual(thisValue, otherValue)) {
                            return BooleanClass.createBooleanInstance(true); // Different values = not equal
                        }
                    }

                    return BooleanClass.createBooleanInstance(false); // All checks passed = hashes are equal
                },
                env);
    }

    private static MethodObject createHashContainsMethod(Environment env) {
        return new BuiltInMethod(
                "__contains__",
                "Hash key containment operator",
                List.of("key"),
                (instance, args) -> {
                    if (args.length != 1) {
                        return new ErrorObject("__contains__() takes exactly 1 argument, got " + args.length);
                    }

                    String key = getStringValue(args[0]);
                    HashObject hash = getHashValue(instance);

                    return BooleanClass.createBooleanInstance(hash.hasKey(key));
                },
                env);
    }

    // Helper methods
    private static HashObject getHashValue(BaseObject obj) {
        if (ObjectValidator.isInstance(obj)) {
            InstanceObject instance = ObjectValidator.asInstance(obj);
            Optional<BaseObject> valueProperty = instance.getProperty("value");
            if (valueProperty.isPresent() && ObjectValidator.isHash(valueProperty.get())) {
                return ObjectValidator.asHash(valueProperty.get());
            }
        }
        // Fallback for old HashObject
        if (ObjectValidator.isHash(obj)) {
            return ObjectValidator.asHash(obj);
        }
        throw new RuntimeException("Expected hash object");
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
        if (ObjectValidator.isInteger(obj)) {
            return String.valueOf(ObjectValidator.asInteger(obj).getValue());
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
     * 🏗️ Creates a Hash instance with the given key-value pairs
     */
    public static InstanceObject createHashInstance(Map<String, BaseObject> pairs) {
        InstanceObject instance = HashClass.getInstance().createInstance();
        instance.setProperty("value", new HashObject(pairs));
        return instance;
    }
}
