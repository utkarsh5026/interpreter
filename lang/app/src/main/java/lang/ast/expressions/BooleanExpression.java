package lang.ast.expressions;

import lang.ast.base.Expression;
import lang.token.Token;
import lang.ast.visitor.AstVisitor;

/**
 * Represents a boolean expression: true, false
 */
public class BooleanExpression extends Expression {
    private final boolean value;

    public BooleanExpression(Token token, boolean value) {
        super(token);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitBooleanExpression(this);
    }

}