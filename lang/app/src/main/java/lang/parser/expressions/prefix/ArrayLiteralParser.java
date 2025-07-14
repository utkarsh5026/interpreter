package lang.parser.expressions.prefix;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import lang.parser.interfaces.PrefixExpressionParser;
import lang.parser.precedence.Precedence;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.core.ParsingContext;
import lang.parser.core.TokenStream;
import lang.ast.base.Expression;
import lang.ast.literals.ArrayLiteral;
import lang.token.*;
import lang.parser.error.ParserException;

/**
 * 📋 ArrayLiteralParser - Array Construction Specialist 📋
 * 
 * Handles array literal expressions that create arrays from a list of elements.
 * Arrays are fundamental data structures that hold ordered collections of
 * values.
 * 
 * Examples:
 * - [1, 2, 3] (array of integers)
 * - ["a", "b", "c"] (array of strings)
 * - [true, false, true] (array of booleans)
 * - [func(), getValue(), x + y] (array of expressions)
 * - [] (empty array)
 * - [1, "hello", true, [2, 3]] (mixed types and nested arrays)
 * 
 * Parsing process:
 * 1. Current token is LBRACKET [
 * 2. Parse comma-separated list of expressions
 * 3. Handle empty arrays gracefully
 * 4. Expect RBRACKET ] to close
 * 5. Create ArrayLiteral AST node
 */
public class ArrayLiteralParser implements PrefixExpressionParser {

    private final ExpressionParser expressionParser;

    public ArrayLiteralParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public Expression parsePrefix(ParsingContext context) {
        Token leftBracketToken = context.consumeCurrentToken(TokenType.LBRACKET);
        List<Expression> elements = new ArrayList<>();
        TokenStream tokenStream = context.getTokenStream();

        while (tokenStream.isCurrentToken(TokenType.RBRACKET)) {
            Expression expr = expressionParser.parseExpression(context, Precedence.LOWEST);
            elements.add(expr);

            if (!tokenStream.isPeekToken(TokenType.RBRACKET)
                    && !tokenStream.isPeekToken(TokenType.COMMA)) {
                throw new ParserException("Expected ',' or ']'");
            }

            if (tokenStream.isCurrentToken(TokenType.COMMA)) {
                context.consumeCurrentToken(TokenType.COMMA);
            }
        }

        context.consumeCurrentToken(TokenType.RBRACKET);
        return new ArrayLiteral(leftBracketToken, elements);
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.LBRACKET);
    }
}