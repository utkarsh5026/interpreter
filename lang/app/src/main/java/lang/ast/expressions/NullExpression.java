package lang.ast.expressions;

import lang.ast.base.Expression;
import lang.token.Token;
import lang.ast.visitor.AstVisitor;

/**
 * Represents a null expression: null
 */
public class NullExpression extends Expression {

    public NullExpression(Token token) {
        super(token);
    }

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitNullExpression(this);
    }
}
