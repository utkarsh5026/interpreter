package lang.ast.base;

import lang.token.Token;
import lang.ast.visitor.AstVisitor;

/**
 * Represents an identifier in the AST.
 * Examples: variable names, function names
 */
public class Identifier extends Expression {
    private final String value;

    public Identifier(Token token, String value) {
        super(token);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIdentifier(this);
    }

}
