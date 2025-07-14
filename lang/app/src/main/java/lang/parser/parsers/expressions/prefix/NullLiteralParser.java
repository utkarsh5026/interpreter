package lang.parser.parsers.expressions.prefix;

import lang.parser.interfaces.PrefixExpressionParser;
import lang.parser.core.ParsingContext;
import lang.ast.base.Expression;
import lang.ast.expressions.NullExpression;
import lang.token.TokenType;
import lang.token.Token;
import java.util.Set;

/**
 * 🔄 NullLiteralParser - Null Literal Specialist 🔄
 * 
 * Handles null literal expressions.
 * 
 */
public class NullLiteralParser implements PrefixExpressionParser {

    @Override
    public Expression parsePrefix(ParsingContext context) {
        Token currToken = context.getTokenStream().getCurrentToken();
        context.consumeCurrentToken(currToken.type());
        return new NullExpression(currToken);
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.NULL);
    }

}
