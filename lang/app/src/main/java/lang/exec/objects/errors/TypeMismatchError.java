package lang.exec.objects.errors;

import lang.exec.base.ObjectType;
import lang.exec.objects.ErrorObject;

/**
 * Error for type-related mismatches and incompatibilities.
 * Examples: "Type mismatch: INTEGER + STRING", "Invalid operator for types"
 */
public class TypeMismatchError extends ErrorObject {
    private final ObjectType leftType;
    private final ObjectType rightType;
    private final String operator;

    public TypeMismatchError(String message) {
        super(message);
        this.leftType = null;
        this.rightType = null;
        this.operator = null;
    }

    public TypeMismatchError(ObjectType leftType, String operator, ObjectType rightType) {
        super(String.format("Type mismatch: %s %s %s. This operation is not supported.",
                leftType, operator, rightType));
        this.leftType = leftType;
        this.rightType = rightType;
        this.operator = operator;
    }

    public TypeMismatchError(String operator, ObjectType leftType, ObjectType rightType, String reason) {
        super(String.format("Invalid operator '%s' for types %s and %s. %s",
                operator, leftType, rightType, reason));
        this.leftType = leftType;
        this.rightType = rightType;
        this.operator = operator;
    }

    public ObjectType getLeftType() {
        return leftType;
    }

    public ObjectType getRightType() {
        return rightType;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public String inspect() {
        return "TYPE_ERROR: " + getMessage();
    }
}