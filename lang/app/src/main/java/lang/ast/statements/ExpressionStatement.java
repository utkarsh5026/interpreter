package lang.ast.statements;

import lang.ast.base.Statement;
import lang.ast.base.Expression;
import lang.token.Token;
import lang.ast.visitor.AstVisitor;

/**
 * Represents an expression statement: 5 + 3;
 * This wraps an expression to make it a statement.
 */
public class ExpressionStatement extends Statement {
    private final Expression expression;

    public ExpressionStatement(Token token, Expression expression) {
        super(token);
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return expression.toString();
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitExpressionStatement(this);
    }

}