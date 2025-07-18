package lang.exec.objects.errors;

import lang.exec.objects.ErrorObject;

/**
 * Error for function/method argument-related issues.
 * Examples: wrong number of arguments, wrong argument types
 */
public class ArgumentError extends ErrorObject {
    private final int expected;
    private final int actual;

    public ArgumentError(String message) {
        super(message);
        this.expected = -1;
        this.actual = -1;
    }

    public ArgumentError(int expected, int actual) {
        super(String.format("wrong number of arguments. got=%d, want=%d", actual, expected));
        this.expected = expected;
        this.actual = actual;
    }

    public ArgumentError(String functionName, String expectedType, String actualType) {
        super(String.format("argument to '%s' must be %s, got %s", functionName, expectedType, actualType));
        this.expected = -1;
        this.actual = -1;
    }

    public int getExpected() {
        return expected;
    }

    public int getActual() {
        return actual;
    }

    @Override
    public String inspect() {
        return "ARGUMENT_ERROR: " + getMessage();
    }
}