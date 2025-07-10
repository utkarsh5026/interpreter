package lang.ast.expressions;

import lang.ast.base.Expression;
import lang.ast.base.Identifier;
import lang.token.Token;

/**
 * Represents an assignment expression: x = value
 */
public class AssignmentExpression extends Expression {
    private final Identifier name;
    private final Expression value;

    public AssignmentExpression(Token token, Identifier name, Expression value) {
        super(token);
        this.name = name;
        this.value = value;
    }

    public Identifier getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s = %s",
                name.toString(),
                value.toString());
    }

}