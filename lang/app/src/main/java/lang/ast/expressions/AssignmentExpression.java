package lang.ast.expressions;

import lang.ast.base.Expression;
import lang.ast.base.Identifier;
import lang.ast.utils.AstCaster;
import lang.ast.utils.AstValidator;
import lang.token.Token;
import lang.ast.visitor.AstVisitor;

/**
 * Represents an assignment expression that can handle multiple target types:
 * - Simple identifier assignment: x = value
 * - Array index assignment: array[0] = value
 * - Hash key assignment: hash["key"] = value
 */
public class AssignmentExpression extends Expression {
    private final Expression target;
    private final Expression value;

    public AssignmentExpression(Token token, Expression target, Expression value) {
        super(token);
        this.target = target;
        this.value = value;
    }

    /**
     * Gets the target expression (can be Identifier or IndexExpression)
     */
    public Expression getTarget() {
        return target;
    }

    /**
     * Gets the identifier name (only valid when target is an Identifier)
     * 
     * @deprecated Use getTarget() and check the type instead
     */
    @Deprecated
    public Identifier getName() {
        if (!AstValidator.isIdentifier(target)) {
            throw new IllegalStateException(
                    "Assignment target is not an identifier: " + target.getClass().getSimpleName());
        }
        return AstCaster.asIdentifier(target);
    }

    /**
     * Gets the value expression to assign
     */
    public Expression getValue() {
        return value;
    }

    /**
     * Checks if this assignment targets a simple identifier
     */
    public boolean isIdentifierAssignment() {
        return AstValidator.isIdentifier(target);
    }

    /**
     * Checks if this assignment targets an index expression (array[i] or
     * hash["key"])
     */
    public boolean isIndexAssignment() {
        return AstValidator.isIndexExpression(target);
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