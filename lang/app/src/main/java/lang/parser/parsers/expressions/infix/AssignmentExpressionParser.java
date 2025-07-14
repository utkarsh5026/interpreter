package lang.parser.parsers.expressions.infix;

import lang.parser.interfaces.InfixExpressionParser;
import lang.parser.core.ParsingContext;
import lang.ast.base.Expression;
import lang.parser.interfaces.ExpressionParser;

import lang.token.*;
import lang.parser.error.ParserException;
import lang.parser.precedence.Precedence;
import lang.ast.expressions.AssignmentExpression;
import lang.ast.utils.*;
import java.util.Set;

/**
 * 📝 AssignmentExpressionParser - Variable Assignment Specialist 📝
 * 
 * Handles assignment expressions where we store a value in a variable.
 * Assignment is an infix operation that takes a variable on the left
 * and a value expression on the right.
 * 
 * Examples:
 * - x = 42 - assign number to variable
 * - name = "Alice" - assign string to variable
 * - result = calculate() - assign function result to variable
 * - array[i] = value - assign to array element (handled differently)
 * 
 * Note: This parser only handles simple variable assignment.
 * Complex left-hand sides like array[i] = value require more
 * sophisticated parsing logic.
 */
public class AssignmentExpressionParser implements InfixExpressionParser {
    private final ExpressionParser expressionParser;

    public AssignmentExpressionParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public Expression parseInfix(ParsingContext context, Expression left) {
        System.out.println("Parsing assignment expression: " + context.getTokenStream().getCurrentToken());

        if (!AstValidator.isIdentifier(left) && !AstValidator.isIndexExpression(left)) {
            throw new ParserException("Invalid assignment target - must be an identifier or index expression",
                    context.getTokenStream().getCurrentToken());
        }

        Token assignToken = context.consumeCurrentToken(TokenType.ASSIGN, "Expected '=' after identifier");
        Expression value = expressionParser.parseExpression(context, Precedence.LOWEST);

        if (context.getTokenStream().isCurrentToken(TokenType.SEMICOLON)) {
            context.consumeCurrentToken(TokenType.SEMICOLON);
        }

        return new AssignmentExpression(assignToken, left, value);
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.ASSIGN);
    }
}
