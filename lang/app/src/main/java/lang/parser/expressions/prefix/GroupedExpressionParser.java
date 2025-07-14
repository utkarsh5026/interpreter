package lang.parser.expressions.prefix;

import lang.parser.interfaces.PrefixExpressionParser;
import lang.parser.core.ParsingContext;
import lang.ast.base.Expression;
import lang.token.TokenType;
import lang.parser.precedence.Precedence;
import lang.parser.interfaces.ExpressionParser;
import java.util.Set;

/**
 * 🔗 GroupedExpressionParser - Parentheses Handler 🔗
 * 
 * Handles grouped expressions wrapped in parentheses for precedence control.
 * This is a prefix parser because parentheses can start an expression.
 * 
 * Examples:
 * - (2 + 3) * 4 - parentheses override normal precedence
 * - (x > 0) && (y < 10) - group boolean expressions for clarity
 * - ((a + b) * c) + d - nested grouping
 * 
 * Note: LPAREN can be both prefix (grouped expression) and infix (function
 * call).
 * This class only handles the prefix case. CallExpressionParser handles infix.
 */
public class GroupedExpressionParser implements PrefixExpressionParser {

    private final ExpressionParser expressionParser;

    public GroupedExpressionParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public Expression parsePrefix(ParsingContext context) {
        context.consumeCurrentToken(TokenType.LPAREN);
        Expression expression = expressionParser.parseExpression(context, Precedence.LOWEST);
        context.consumeCurrentToken(TokenType.RPAREN);
        return expression;
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.LPAREN);
    }
}
