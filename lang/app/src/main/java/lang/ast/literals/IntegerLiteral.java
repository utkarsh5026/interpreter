package lang.ast.literals;

import lang.ast.visitor.AstVisitor;
import lang.ast.base.Expression;
import lang.token.Token;

/**
 * Represents an integer literal: 5, 42, -10
 */
public class IntegerLiteral extends Expression {
    private final int value;

    public IntegerLiteral(Token token, int value) {
        super(token);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIntegerLiteral(this);
    }

}