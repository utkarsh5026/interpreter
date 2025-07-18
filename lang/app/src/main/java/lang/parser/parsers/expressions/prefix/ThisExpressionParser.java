package lang.parser.parsers.expressions.prefix;

import java.util.Set;

import lang.ast.base.Expression;
import lang.ast.expressions.ThisExpression;
import lang.parser.core.ParsingContext;
import lang.parser.interfaces.PrefixExpressionParser;
import lang.token.Token;
import lang.token.TokenType;

/**
 * 👆 ThisExpressionParser - Current Instance Reference Parser 👆
 * 
 * Parses 'this' expressions for referencing the current object instance.
 * 
 * From first principles, this parsing is simple:
 * 1. Parse 'this' keyword
 * 2. Create ThisExpression AST node
 * 
 * Grammar:
 * ```
 * this-expression := 'this'
 * ```
 */
public class ThisExpressionParser implements PrefixExpressionParser {

    @Override
    public Expression parsePrefix(ParsingContext context) {
        Token thisToken = context.consumeCurrentToken(TokenType.THIS, "Expected 'this' keyword");
        return new ThisExpression(thisToken);
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.THIS);
    }
}