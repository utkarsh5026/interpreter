package lang.ast.literals;

import lang.ast.base.Expression;
import lang.token.Token;
import lang.ast.visitor.AstVisitor;

/**
 * Represents an f-string literal: f"Hello {name}!"
 * This is a formatted string with embedded expressions.
 */
public class FStringLiteral extends Expression {
    private final String value;

    public FStringLiteral(Token token, String value) {
        super(token);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return tokenLiteral();
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFStringLiteral(this);
    }

}
