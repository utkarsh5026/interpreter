package lang.exec.objects;

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
