package lang.exec.objects.literals;

import lang.exec.base.BaseObject;
import lang.exec.base.ObjectType;

/**
 * Immutable integer object representing numeric values in the interpreter.
 * 
 * Design decisions:
 * - Uses long for extended range compared to int
 * - Immutable to prevent side effects
 * - Implements value equality semantics
 */
public final class IntegerObject implements BaseObject {
    private final long value;

    public IntegerObject(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public ObjectType type() {
        return ObjectType.INTEGER;
    }

    @Override
    public String inspect() {
        return String.valueOf(value);
    }

    @Override
    public boolean isTruthy() {
        return value != 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof IntegerObject))
            return false;
        IntegerObject other = (IntegerObject) obj;
        return value == other.value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }
}
