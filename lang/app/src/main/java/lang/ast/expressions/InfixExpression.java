package lang.ast.expressions;

import lang.ast.base.Expression;
import lang.token.Token;

public class InfixExpression extends Expression {
    private final Expression left;
    private final String operator;
    private final Expression right;

    public InfixExpression(Token token, Expression left, String operator, Expression right) {
        super(token);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public String getOperator() {
        return operator;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public String toString() {
        return String.format("(%s %s %s)",
                left.toString(),
                operator,
                right.toString());
    }
}
