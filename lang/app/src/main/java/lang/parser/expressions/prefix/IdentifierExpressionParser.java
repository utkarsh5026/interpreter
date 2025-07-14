package lang.parser.expressions.prefix;

import lang.parser.interfaces.PrefixExpressionParser;
import lang.parser.core.ParsingContext;
import lang.ast.base.Expression;
import lang.ast.base.Identifier;
import lang.token.TokenType;
import lang.token.Token;
import java.util.Set;

/**
 * 🔤 IdentifierExpressionParser - Identifier Specialist 🔤
 * 
 * Handles identifier expressions.
 * 
 * Examples:
 * - x
 * - my_variable
 * - some_function_call
 * 
 */
public class IdentifierExpressionParser implements PrefixExpressionParser {

    @Override
    public Expression parsePrefix(ParsingContext context) {
        Token identifierToken = context.consumeCurrentToken(TokenType.IDENTIFIER, "Expected identifier");
        return new Identifier(identifierToken, identifierToken.literal());
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.IDENTIFIER);
    }
}