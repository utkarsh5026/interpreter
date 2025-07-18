package lang.exec.objects.errors;

import lang.exec.objects.ErrorObject;

/**
 * Error for arithmetic operations that cannot be performed.
 * Examples: "division by zero", "modulo by zero"
 */
public class ArithmeticError extends ErrorObject {

    public ArithmeticError(String operation) {
        super(operation);
    }

    @Override
    public String inspect() {
        return "ARITHMETIC_ERROR: " + getMessage();
    }
}