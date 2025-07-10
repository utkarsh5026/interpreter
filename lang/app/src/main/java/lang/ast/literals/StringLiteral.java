package lang.ast.literals;

import lang.ast.base.Expression;
import lang.token.Token;

/**
 * Represents a string literal: "hello", "world"
 */
public class StringLiteral extends Expression {
    private final String value;

    public StringLiteral(Token token, String value) {
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

}
