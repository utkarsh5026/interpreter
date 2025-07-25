package lang.exec.objects.literals;

import lang.exec.objects.base.*;

/**
 * Null object representing the absence of a value.
 * This should be implemented as a singleton since all null values are
 * equivalent.
 */
public final class NullObject implements BaseObject {
    public static final NullObject INSTANCE = new NullObject();

    private NullObject() {
    } // Prevent external instantiation

    @Override
    public ObjectType type() {
        return ObjectType.NULL;
    }

    @Override
    public String inspect() {
        return "null";
    }

    @Override
    public boolean isTruthy() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NullObject;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    /**
     * Returns the singleton instance of NullObject.
     */
    public static BaseObject getInstance() {
        return INSTANCE;
    }
}
