package lang.exec.objects.structures;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import lang.exec.base.BaseObject;
import lang.exec.base.ObjectType;

/**
 * HashObject represents a hash map in the language.
 * 
 * This class is used to store key-value pairs and provide a way to inspect it.
 */
public class HashObject implements BaseObject {
    private final Map<String, BaseObject> pairs;

    public HashObject(Map<String, BaseObject> pairs) {
        this.pairs = pairs;
    }

    public Map<String, BaseObject> getPairs() {
        return Collections.unmodifiableMap(pairs);
    }

    /**
     * 🔧 Sets or updates a key-value pair in this hash
     */
    public BaseObject set(String key, BaseObject value) {
        pairs.put(key, value);
        return value;
    }

    /**
     * 🔍 Gets a value by key, returning null if not found
     */
    public BaseObject get(String key) {
        return pairs.get(key);
    }

    /**
     * ❓ Checks if a key exists in this hash
     */
    public boolean hasKey(String key) {
        return pairs.containsKey(key);
    }

    /**
     * ➖ Removes a key-value pair from this hash
     */
    public BaseObject remove(String key) {
        return pairs.remove(key);
    }

    /**
     * 📊 Gets the number of key-value pairs in this hash
     */
    public int size() {
        return pairs.size();
    }

    /**
     * 🧹 Removes all key-value pairs from this hash
     */
    public void clear() {
        pairs.clear();
    }

    @Override
    public ObjectType type() {
        return ObjectType.HASH;
    }

    @Override
    public String inspect() {
        List<String> pairsString = pairs.entrySet()
                .stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue().inspect())
                .toList();
        return "{" + String.join(", ", pairsString) + "}";
    }

    @Override
    public boolean isTruthy() {
        return !pairs.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof HashObject))
            return false;
        HashObject other = (HashObject) obj;
        return pairs.equals(other.pairs);
    }
}
