package lang.exec.objects;

import lang.exec.base.BaseObject;
import lang.exec.base.ObjectType;

/**
 * String object for text values.
 */
public final class StringObject implements BaseObject {
    private final String value;

    public StringObject(String value) {
        this.value = value != null ? value : "";
    }

    public String getValue() {
        return value;
    }

    @Override
    public ObjectType type() {
        return ObjectType.STRING;
    }

    @Override
    public String inspect() {
        return value;
    }

    @Override
    public boolean isTruthy() {
        return !value.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof StringObject))
            return false;
        StringObject other = (StringObject) obj;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
