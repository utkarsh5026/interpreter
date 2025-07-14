package lang.parser.expressions.prefix;

import lang.parser.interfaces.PrefixExpressionParser;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.core.ParsingContext;
import lang.ast.base.Expression;
import lang.ast.expressions.PrefixExpression;

import java.util.Set;
import lang.token.TokenType;
import lang.token.Token;
import lang.parser.error.ParserException;
import lang.parser.precedence.Precedence;

/**
 * 🔄 PrefixOperatorParser - Unary Operator Specialist 🔄
 * 
 * Handles prefix (unary) operators that appear before expressions.
 * These operators take one operand and transform it in some way.
 * 
 * Examples:
 * - !true (logical NOT - negates boolean values)
 * - -42 (arithmetic negation - makes numbers negative)
 * - +value (unary plus - explicit positive, rarely used)
 * 
 * The parser works by:
 * 1. Capturing the operator token
 * 2. Recursively parsing the right operand with PREFIX precedence
 * 3. Creating a PrefixExpression AST node
 */
public class PrefixOperatorParser implements PrefixExpressionParser {

    private final ExpressionParser expressionParser;

    public PrefixOperatorParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public Expression parsePrefix(ParsingContext context) {
        Token operatorToken = context.getTokenStream().getCurrentToken();
        String operator = operatorToken.literal();

        if (!getHandledTokenTypes().contains(operatorToken.type())) {
            throw new ParserException("Invalid operator: " + operatorToken.type(), operatorToken);
        }

        context.consumeCurrentToken(operatorToken.type());
        Expression right = expressionParser.parseExpression(context, Precedence.PREFIX);

        return new PrefixExpression(operatorToken, operator, right);
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.MINUS, TokenType.BANG);
    }
}