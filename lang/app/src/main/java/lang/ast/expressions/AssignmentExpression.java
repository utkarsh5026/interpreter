package lang.ast.expressions;

import lang.ast.base.Expression;
import lang.ast.base.Identifier;
import lang.ast.utils.AstCaster;
import lang.token.Token;
import lang.ast.visitor.AstVisitor;

/**
 * Represents an assignment expression: x = value
 */
public class AssignmentExpression extends Expression {
    private final Expression target;
    private final Expression value;

    public AssignmentExpression(Token token, Expression target, Expression value) {
        super(token);
        this.target = target;
        this.value = value;
    }

    public Identifier getName() {
        return AstCaster.asIdentifier(target);
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s = %s",
                target.toString(),
                value.toString());
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitAssignmentExpression(this);
    }

}