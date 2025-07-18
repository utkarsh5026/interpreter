package lang.exec.objects.errors;

import lang.exec.objects.ErrorObject;

/**
 * Error for when an identifier (variable, function, etc.) is not found in the
 * current scope.
 * Examples: "identifier not found: myVar", "variable 'x' not defined"
 */
public class IdentifierNotFoundError extends ErrorObject {
    private final String identifierName;

    public IdentifierNotFoundError(String identifierName) {
        super("identifier not found: " + identifierName);
        this.identifierName = identifierName;
    }

    public String getIdentifierName() {
        return identifierName;
    }

    @Override
    public String inspect() {
        return "IDENTIFIER_ERROR: " + getMessage();
    }
}