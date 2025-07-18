package lang.exec.objects.errors;

import lang.exec.objects.ErrorObject;

/**
 * Error for property access issues.
 * Examples: "Property not found", "Invalid property name"
 */
public class PropertyError extends ErrorObject {
    private final String propertyName;
    private final String className;

    public PropertyError(String message) {
        super(message);
        this.propertyName = null;
        this.className = null;
    }

    public PropertyError(String propertyName, String className) {
        super(String.format("Property '%s' not found on instance of %s", propertyName, className));
        this.propertyName = propertyName;
        this.className = className;
    }

    public static PropertyError invalidName() {
        return new PropertyError("Invalid property name in property access");
    }

    public static PropertyError notFound(String propertyName, String className) {
        return new PropertyError(propertyName, className);
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String inspect() {
        return "PROPERTY_ERROR: " + getMessage();
    }
}