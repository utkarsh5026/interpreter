package lang.parser.parsers.statements;

import lang.ast.statements.ExpressionStatement;
import lang.ast.base.Expression;
import lang.ast.base.Identifier;
import lang.ast.expressions.AssignmentExpression;
import lang.ast.expressions.InfixExpression;
import lang.parser.core.*;
import lang.parser.error.ParserException;
import lang.parser.interfaces.TypedStatementParser;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.precedence.Precedence;
import java.util.Set;
import lang.token.*;

/**
 * Parses expression statements: 5 + 3; or functionCall();
 */
public class ExpressionStatementParser implements TypedStatementParser<ExpressionStatement> {

    private final ExpressionParser expressionParser;

    public ExpressionStatementParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public boolean canParse(ParsingContext context) {
        // Expression statements can start with many different tokens
        // This should be the fallback parser
        return true;
    }

    @Override
    public ExpressionStatement parse(ParsingContext context) {
        TokenStream tokens = context.getTokenStream();
        Token token = tokens.getCurrentToken();

        if (context.getTokenStream().isCurrentToken(TokenType.IDENTIFIER)
                && isCompoundAssignmentOperator(tokens.getPeekToken())) {
            return parseCompoundAssignment(context);
        }

        Expression expression = expressionParser.parseExpression(context,
                Precedence.LOWEST);

        if (tokens.isCurrentToken(TokenType.SEMICOLON)) {
            context.consumeCurrentToken(TokenType.SEMICOLON);
        }

        return new ExpressionStatement(token, expression);
    }

    private ExpressionStatement parseCompoundAssignment(ParsingContext context) {
        Token starToken = context.consumeCurrentToken(TokenType.IDENTIFIER,
                "Expected identifier at start of compound statement");

        Identifier identifier = new Identifier(starToken, starToken.literal());

        Token operatorToken = context.getTokenStream().getCurrentToken();
        String operator = getBaseOperator(operatorToken);
        context.consumeCurrentToken(operatorToken.type(), "Expected operator after identifier");

        Expression right = expressionParser.parseExpression(context, Precedence.LOWEST);
        context.consumeCurrentToken(TokenType.SEMICOLON, "Expected semicolon after compound statement");

        InfixExpression infixExpression = new InfixExpression(
                operatorToken,
                identifier,
                operator,
                right);
        AssignmentExpression assignmentExpression = new AssignmentExpression(operatorToken, identifier,
                infixExpression);
        return new ExpressionStatement(operatorToken, assignmentExpression);
    }

    private boolean isCompoundAssignmentOperator(Token token) {
        Set<TokenType> operators = Set.of(TokenType.PLUS_ASSIGN,
                TokenType.MINUS_ASSIGN,
                TokenType.ASTERISK_ASSIGN,
                TokenType.SLASH_ASSIGN,
                TokenType.MODULUS_ASSIGN);

        return operators.contains(token.type());
    }

    private String getBaseOperator(Token token) {
        switch (token.type()) {
            case PLUS_ASSIGN:
                return "+";
            case MINUS_ASSIGN:
                return "-";
            case ASTERISK_ASSIGN:
                return "*";
            case SLASH_ASSIGN:
                return "/";
            case MODULUS_ASSIGN:
                return "%";
            default:
                throw new ParserException("Invalid compound assignment operator", token);
        }
    }
}