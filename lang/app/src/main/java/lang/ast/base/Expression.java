package lang.ast.base;

import lang.token.Token;
import lang.token.TokenPosition;

/**
 * Base class for all expression nodes.
 * Expressions produce values when evaluated.
 * Examples: identifiers, literals, function calls, arithmetic operations
 */
public abstract class Expression implements Node {
    protected final Token token;

    protected Expression(Token token) {
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