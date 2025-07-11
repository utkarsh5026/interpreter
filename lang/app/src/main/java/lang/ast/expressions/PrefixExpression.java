package lang.ast.expressions;

import lang.ast.base.Expression;
import lang.token.Token;
import lang.ast.visitor.AstVisitor;

/**
 * Represents a prefix expression: !x, -x
 */
public class PrefixExpression extends Expression {
    private final String operator;
    private final Expression right;

    public PrefixExpression(Token token, String operator, Expression right) {
        super(token);
        this.operator = operator;
        this.right = right;
    }

    public String getOperator() {
        return operator;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public String toString() {
        return String.format("(%s%s)", operator, right.toString());
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitPrefixExpression(this);
    }

}