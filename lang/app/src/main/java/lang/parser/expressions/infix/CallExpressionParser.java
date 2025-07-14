package lang.parser.expressions.infix;

import lang.parser.interfaces.InfixExpressionParser;
import lang.parser.core.ParsingContext;
import lang.parser.interfaces.ExpressionParser;
import lang.ast.expressions.CallExpression;
import lang.parser.precedence.Precedence;

import lang.ast.base.Expression;
import lang.token.*;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * 📞 CallExpressionParser - Function Call Specialist 📞
 * 
 * Handles function call expressions where we apply a function to arguments.
 * Function calls are infix operations because they take a left expression
 * (the function) and combine it with arguments on the right.
 * 
 * Examples:
 * - print("hello") - function name with string argument
 * - add(2, 3) - function with multiple arguments
 * - getValue() - function with no arguments
 * - user.getName() - method call (parsed as function call)
 * - higherOrder(func)(args) - chained function calls
 * 
 * Parsing process:
 * 1. Left expression is the function to call
 * 2. Current token is LPAREN (start of argument list)
 * 3. Parse comma-separated list of argument expressions
 * 4. Expect RPAREN to close the argument list
 * 5. Create CallExpression AST node
 */
public class CallExpressionParser implements InfixExpressionParser {

    private final ExpressionParser expressionParser;

    public CallExpressionParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public Expression parseInfix(ParsingContext context, Expression left) {
        Token token = context.consumeCurrentToken(TokenType.LPAREN);
        List<Expression> arguments = parseExpressionList(context, TokenType.RPAREN);

        return new CallExpression(token, left, arguments);
    }

    /**
     * 📋 Parses a comma-separated list of expressions
     * 
     * This is a common pattern used for function arguments, array elements, etc.
     * Handles the case of empty lists gracefully.
     */
    private List<Expression> parseExpressionList(ParsingContext context, TokenType endToken) {
        List<Expression> expressions = new ArrayList<>();

        if (context.getTokenStream().isCurrentToken(endToken)) {
            context.consumeCurrentToken(endToken);
            return expressions;
        }

        Expression expression = expressionParser.parseExpression(context, Precedence.LOWEST);
        expressions.add(expression);

        while (context.getTokenStream().isPeekToken(TokenType.COMMA)) {
            context.getTokenStream().advance();
            context.consumeCurrentToken(TokenType.COMMA);

            expression = expressionParser.parseExpression(context, Precedence.LOWEST);
            expressions.add(expression);
        }

        context.consumeCurrentToken(endToken);
        return expressions;
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.LPAREN);
    }
}
