package lang.ast.base;

import lang.token.Token;
import lang.token.TokenPosition;

/**
 * Base class for all statement nodes.
 * Statements perform actions but don't produce values.
 * Examples: let statements, return statements, if statements
 */
public abstract class Statement implements Node {
    protected final Token token;

    protected Statement(Token token) {
        this.token = token;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public TokenPosition position() {
        return token.position();
    }

}
