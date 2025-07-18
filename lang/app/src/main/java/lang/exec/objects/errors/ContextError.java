package lang.exec.objects.errors;

import lang.exec.objects.ErrorObject;

/**
 * Error for context-related issues (this, super, etc.).
 * Examples: "'this' is not available in this context", "'super' can only be
 * used inside instance methods"
 */
public class ContextError extends ErrorObject {
    private final String keyword;

    public ContextError(String keyword, String message) {
        super(String.format("'%s' %s", keyword, message));
        this.keyword = keyword;
    }

    public static ContextError thisNotAvailable() {
        return new ContextError("this", "is not available in this context");
    }

    public static ContextError superNotInMethod() {
        return new ContextError("super", "can only be used inside instance methods");
    }

    public static ContextError superNoParent(String className) {
        return new ContextError("super", String.format("cannot be used - class '%s' has no parent class", className));
    }

    public String getKeyword() {
        return keyword;
    }

    @Override
    public String inspect() {
        return "CONTEXT_ERROR: " + getMessage();
    }
}