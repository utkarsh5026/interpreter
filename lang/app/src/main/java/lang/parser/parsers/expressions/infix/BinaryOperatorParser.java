package lang.parser.parsers.expressions.infix;

import lang.parser.interfaces.InfixExpressionParser;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.core.ParsingContext;
import lang.ast.base.Expression;
import lang.token.TokenType;
import java.util.Set;

import lang.token.Token;
import lang.parser.precedence.Precedence;
import lang.ast.expressions.InfixExpression;
import lang.parser.error.ParserException;

/**
 * ⚡ BinaryOperatorParser - Generic Binary Operation Handler ⚡
 * 
 * Base class for parsers that handle binary operations between two expressions.
 * This eliminates duplication between arithmetic, comparison, and other binary
 * operators.
 * 
 * The parser follows a standard pattern:
 * 1. Validate the current token is a handled operator
 * 2. Get the operator's precedence
 * 3. Parse the right operand
 * 4. Create an InfixExpression node
 * 5. Handle error cases appropriately
 */
public class BinaryOperatorParser implements InfixExpressionParser {

    private final ExpressionParser expressionParser;
    private final Set<TokenType> handledTokenTypes;

    public BinaryOperatorParser(ExpressionParser expressionParser,
            Set<TokenType> handledTokenTypes) {
        this.expressionParser = expressionParser;
        this.handledTokenTypes = handledTokenTypes;
    }

    @Override
    public Expression parseInfix(ParsingContext context, Expression left) {
        Token operatorToken = context.getTokenStream().getCurrentToken();
        String operator = operatorToken.literal();

        if (!handledTokenTypes.contains(operatorToken.type())) {
            throw new ParserException("Invalid operator: " + operatorToken.type(), operatorToken);
        }

        Precedence precedence = context.getPrecedenceTable()
                .getPrecedence(operatorToken.type());

        context.consumeCurrentToken(operatorToken.type());

        Expression right = expressionParser.parseExpression(context, precedence);
        return new InfixExpression(operatorToken, left, operator, right);
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return handledTokenTypes;
    }
}