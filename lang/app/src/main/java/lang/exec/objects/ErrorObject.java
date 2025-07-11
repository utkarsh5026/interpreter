package lang.exec.objects;

import lang.exec.base.BaseObject;
import lang.exec.base.ObjectType;

/**
 * Error object for representing runtime errors.
 */
public final class ErrorObject implements BaseObject {
    private final String message;

    public ErrorObject(String message) {
        this.message = message != null ? message : "Unknown error";
    }

    public String getMessage() {
        return message;
    }

    @Override
    public ObjectType type() {
        return ObjectType.ERROR;
    }

    @Override
    public String inspect() {
        return "ERROR: " + message;
    }

    @Override
    public boolean isTruthy() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ErrorObject))
            return false;
        ErrorObject other = (ErrorObject) obj;
        return message.equals(other.message);
    }

    @Override
    public int hashCode() {
        return message.hashCode();
    }
}