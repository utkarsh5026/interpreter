package lang.parser.parsers.expressions.prefix;

import lang.parser.interfaces.PrefixExpressionParser;
import lang.parser.core.ParsingContext;
import lang.ast.base.Expression;
import lang.ast.literals.StringLiteral;
import lang.token.TokenType;
import lang.token.Token;
import java.util.Set;

/**
 * 📝 StringLiteralParser - String Literal Specialist 📝
 * 
 * Handles string literal expressions.
 * 
 */
public class StringLiteralParser implements PrefixExpressionParser {
    @Override
    public Expression parsePrefix(ParsingContext context) {
        Token token = context.consumeCurrentToken(TokenType.STRING);
        return new StringLiteral(token, token.literal());
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.STRING);
    }
}
