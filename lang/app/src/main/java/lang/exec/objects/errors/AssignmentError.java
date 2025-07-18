package lang.exec.objects.errors;

import lang.exec.objects.ErrorObject;

/**
 * Error for assignment-related issues.
 * Examples: "cannot assign to constant", "Invalid assignment target"
 */
public class AssignmentError extends ErrorObject {
    private final String variableName;
    private final String targetType;

    public AssignmentError(String message) {
        super(message);
        this.variableName = null;
        this.targetType = null;
    }

    public AssignmentError(String variableName, String reason) {
        super(String.format("cannot assign to %s %s", reason, variableName));
        this.variableName = variableName;
        this.targetType = reason;
    }

    public static AssignmentError constantAssignment(String variableName) {
        return new AssignmentError(variableName, "constant");
    }

    public static AssignmentError invalidTarget(String targetType) {
        return new AssignmentError(String.format("Invalid assignment target: %s", targetType));
    }

    public String getVariableName() {
        return variableName;
    }

    public String getTargetType() {
        return targetType;
    }

    @Override
    public String inspect() {
        return "ASSIGNMENT_ERROR: " + getMessage();
    }
}