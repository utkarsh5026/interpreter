package lang.parser.parsers.expressions.infix;

import java.util.Set;

import lang.ast.base.Expression;
import lang.ast.base.Identifier;
import lang.ast.expressions.PropertyExpression;
import lang.parser.core.ParsingContext;
import lang.parser.interfaces.InfixExpressionParser;
import lang.token.Token;
import lang.token.TokenType;

/**
 * 🔗 PropertyExpressionParser - Property Access Parser 🔗
 * 
 * Parses dot notation for accessing object properties and methods.
 * 
 * From first principles, property access parsing involves:
 * 1. Left expression is the object
 * 2. Current token is DOT
 * 3. Next token should be the property name
 * 4. Create PropertyExpression AST node
 * 
 * Grammar:
 * ```
 * property-access := expression '.' IDENTIFIER
 * ```
 */
public class PropertyExpressionParser implements InfixExpressionParser {

    @Override
    public Expression parseInfix(ParsingContext context, Expression left) {
        Token dotToken = context.consumeCurrentToken(TokenType.DOT, "Expected '.' for property access");

        Token propertyToken = context.consumeCurrentToken(TokenType.IDENTIFIER,
                "Expected property name after '.'");

        Identifier property = new Identifier(propertyToken, propertyToken.literal());

        return new PropertyExpression(dotToken, left, property);
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.DOT);
    }
}