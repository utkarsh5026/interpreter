package lang.exec.objects.errors;

import lang.exec.objects.ErrorObject;

/**
 * Error for class-related issues.
 * Examples: "Class already defined", "Parent class not found", "Circular
 * inheritance"
 */
public class ClassError extends ErrorObject {
    private final String className;
    private final String parentClassName;

    public ClassError(String message) {
        super(message);
        this.className = null;
        this.parentClassName = null;
    }

    public ClassError(String className, String message) {
        super(String.format("Class '%s' %s", className, message));
        this.className = className;
        this.parentClassName = null;
    }

    public ClassError(String className, String parentClassName, String message) {
        super(message);
        this.className = className;
        this.parentClassName = parentClassName;
    }

    public static ClassError alreadyDefined(String className) {
        return new ClassError(className, "already defined in this scope");
    }

    public static ClassError parentNotFound(String parentClassName) {
        return new ClassError(String.format("Parent class '%s' not found", parentClassName));
    }

    public static ClassError notAClass(String name) {
        return new ClassError(String.format("'%s' is not a class", name));
    }

    public static ClassError circularInheritance(String className, String parentClassName) {
        return new ClassError(className, parentClassName,
                String.format("Circular inheritance detected: %s cannot inherit from %s", className, parentClassName));
    }

    public String getClassName() {
        return className;
    }

    public String getParentClassName() {
        return parentClassName;
    }

    @Override
    public String inspect() {
        return "CLASS_ERROR: " + getMessage();
    }
}