package lang.exec.objects.errors;

import lang.exec.objects.ErrorObject;

/**
 * General runtime error for issues that don't fit into specific categories.
 * This is useful for miscellaneous runtime errors or as a fallback.
 */
public class RuntimeError extends ErrorObject {
    private final String operation;
    private final String context;

    public RuntimeError(String message) {
        super(message);
        this.operation = null;
        this.context = null;
    }

    public RuntimeError(String operation, String context, String message) {
        super(String.format("%s: %s", operation, message));
        this.operation = operation;
        this.context = context;
    }

    public static RuntimeError evaluationError(String message) {
        return new RuntimeError("Evaluation error", "runtime", message);
    }

    public static RuntimeError unknownOperator(String operator, String type) {
        return new RuntimeError(String.format("unknown operator: %s%s", operator, type));
    }

    public String getOperation() {
        return operation;
    }

    public String getContext() {
        return context;
    }

    @Override
    public String inspect() {
        return "RUNTIME_ERROR: " + getMessage();
    }
}