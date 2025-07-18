package lang.exec.objects;

import lang.exec.base.BaseObject;
import lang.exec.base.ObjectType;

public final class BooleanObject implements BaseObject {
    private final boolean value;

    public BooleanObject(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public ObjectType type() {
        return ObjectType.BOOLEAN;
    }

    @Override
    public String inspect() {
        return String.valueOf(value);
    }

    @Override
    public boolean isTruthy() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof BooleanObject))
            return false;
        BooleanObject other = (BooleanObject) obj;
        return value == other.value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }
}
