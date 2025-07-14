package lang.parser.parsers.expressions.prefix;

import lang.parser.interfaces.PrefixExpressionParser;
import lang.parser.core.ParsingContext;
import lang.ast.base.Expression;
import lang.ast.expressions.BooleanExpression;
import lang.token.TokenType;
import lang.token.Token;
import java.util.Set;

public class BooleanLiteralParser implements PrefixExpressionParser {

    @Override
    public Expression parsePrefix(ParsingContext context) {
        Token currToken = context.getTokenStream().getCurrentToken();
        context.consumeCurrentToken(currToken.type());

        return new BooleanExpression(currToken, currToken.type() == TokenType.TRUE);
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.TRUE, TokenType.FALSE);
    }
}
