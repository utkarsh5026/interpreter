package lang.exec.objects.errors;

import lang.exec.base.ObjectType;
import lang.exec.objects.ErrorObject;

/**
 * Error for object instantiation issues.
 * Examples: "Cannot instantiate non-class object", "Constructor expects N
 * arguments"
 */
public class InstantiationError extends ErrorObject {
    private final String className;
    private final ObjectType attemptedType;

    public InstantiationError(String message) {
        super(message);
        this.className = null;
        this.attemptedType = null;
    }

    public InstantiationError(ObjectType attemptedType) {
        super(String.format("Cannot instantiate non-class object: %s", attemptedType));
        this.className = null;
        this.attemptedType = attemptedType;
    }

    public InstantiationError(String className, int expected, int actual) {
        super(String.format("Constructor expects %d arguments, got %d", expected, actual));
        this.className = className;
        this.attemptedType = null;
    }

    public static InstantiationError noConstructor(String className) {
        return new InstantiationError(
                String.format("Class '%s' has no constructor but arguments were provided", className));
    }

    public String getClassName() {
        return className;
    }

    public ObjectType getAttemptedType() {
        return attemptedType;
    }

    @Override
    public String inspect() {
        return "INSTANTIATION_ERROR: " + getMessage();
    }
}