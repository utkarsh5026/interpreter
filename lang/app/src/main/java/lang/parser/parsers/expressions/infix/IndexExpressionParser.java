package lang.parser.parsers.expressions.infix;

import lang.ast.base.Expression;
import lang.parser.core.ParsingContext;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.interfaces.InfixExpressionParser;
import lang.parser.precedence.Precedence;
import lang.ast.expressions.IndexExpression;

import lang.token.*;
import java.util.Set;

/**
 * 🗂️ IndexExpressionParser - Array/Object Access Specialist 🗂️
 * 
 * Handles index/bracket access expressions for arrays and objects.
 * This allows accessing elements by index or key.
 * 
 * Examples:
 * - array[0] - access first element of array
 * - array[i] - access element at variable index
 * - hash["key"] - access object property by string key
 * - matrix[row][col] - chained indexing for multi-dimensional structures
 * - getValue()[0] - indexing the result of a function call
 * 
 * Parsing process:
 * 1. Left expression is what we're indexing into
 * 2. Current token is LBRACKET (start of index)
 * 3. Parse the index expression
 * 4. Expect RBRACKET to close the index
 * 5. Create IndexExpression AST node
 */
public class IndexExpressionParser implements InfixExpressionParser {

    private final ExpressionParser expressionParser;

    public IndexExpressionParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public Expression parseInfix(ParsingContext context, Expression left) {
        Token leftBracket = context.consumeCurrentToken(TokenType.LBRACKET);

        Expression index = expressionParser.parseExpression(context, Precedence.LOWEST);

        context.consumeCurrentToken(TokenType.RBRACKET);
        return new IndexExpression(leftBracket, left, index);
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.LBRACKET);
    }
}
