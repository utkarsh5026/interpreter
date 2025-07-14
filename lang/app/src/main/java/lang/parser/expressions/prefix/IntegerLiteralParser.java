package lang.parser.expressions.prefix;

import lang.parser.interfaces.PrefixExpressionParser;
import lang.parser.core.ParsingContext;
import lang.parser.error.ParserException;
import lang.ast.base.Expression;
import lang.ast.literals.IntegerLiteral;
import lang.token.TokenType;
import lang.token.Token;
import java.util.Set;

/**
 * 🔢 IntegerLiteralParser - Number Literal Specialist 🔢
 * 
 * Handles integer literal expressions.
 * 
 */
public class IntegerLiteralParser implements PrefixExpressionParser {

    @Override
    public Expression parsePrefix(ParsingContext context) {
        Token intToken = context.consumeCurrentToken(TokenType.INT);

        try {
            int value = Integer.parseInt(intToken.literal());
            return new IntegerLiteral(intToken, value);
        } catch (NumberFormatException e) {
            throw new ParserException(
                    "Invalid integer literal: " + intToken.literal(),
                    intToken);
        }
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.INT);
    }
}
