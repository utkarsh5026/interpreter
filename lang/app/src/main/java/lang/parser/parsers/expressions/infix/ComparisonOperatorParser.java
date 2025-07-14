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
public class ComparisonOperatorParser implements InfixExpressionParser {

    private final BinaryOperatorParser delegate;

    public ComparisonOperatorParser(ExpressionParser expressionParser) {
        this.delegate = new BinaryOperatorParser(
                expressionParser,
                Set.of(
                        TokenType.EQ, // ==
                        TokenType.NOT_EQ, // !=
                        TokenType.LESS_THAN, // <
                        TokenType.GREATER_THAN, // >
                        TokenType.LESS_THAN_OR_EQUAL, // <=
                        TokenType.GREATER_THAN_OR_EQUAL // >=
                ));
    }

    @Override
    public Expression parseInfix(ParsingContext context, Expression left) {
        return delegate.parseInfix(context, left);
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return delegate.getHandledTokenTypes();
    }
}