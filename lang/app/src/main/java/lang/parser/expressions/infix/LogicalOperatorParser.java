package lang.parser.expressions.infix;

import lang.parser.interfaces.InfixExpressionParser;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.core.ParsingContext;
import lang.ast.base.Expression;
import lang.token.TokenType;
import java.util.Set;

/**
 * 🔗 LogicalOperatorParser - Boolean Logic Operations 🔗
 * 
 * Handles logical operations between boolean expressions.
 * These operations combine boolean values using logical rules.
 * 
 * Supported operations:
 * - && (logical AND): true && false → false
 * - || (logical OR): true || false → true
 * 
 * Note: These operators typically use short-circuit evaluation
 * in the evaluator (not implemented here, just parsing).
 */
public class LogicalOperatorParser implements InfixExpressionParser {

    private final BinaryOperatorParser delegate;

    public LogicalOperatorParser(ExpressionParser expressionParser) {
        Set<TokenType> handledTokenTypes = Set.of(
                TokenType.AND,
                TokenType.OR);

        this.delegate = new BinaryOperatorParser(
                expressionParser,
                handledTokenTypes);
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
