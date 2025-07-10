package lang.ast.expressions;

import lang.ast.base.Expression;
import lang.token.Token;

/**
 * Represents an index expression: array[index], hash["key"]
 */
public class IndexExpression extends Expression {
    private final Expression left;
    private final Expression index;

    public IndexExpression(Token token, Expression left, Expression index) {
        super(token);
        this.left = left;
        this.index = index;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return String.format("(%s[%s])",
                left.toString(),
                index.toString());
    }

}