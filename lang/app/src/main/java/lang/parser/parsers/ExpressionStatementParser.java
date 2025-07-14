package lang.parser.parsers;

import lang.ast.statements.ExpressionStatement;
import lang.ast.base.Expression;
import lang.parser.core.*;
import lang.parser.error.ParserException;
import lang.parser.interfaces.TypedStatementParser;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.precedence.Precedence;

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

        Expression expression = expressionParser.parseExpression(context,
                Precedence.LOWEST);

        if (expression == null) {
            throw new ParserException("Expected expression", token);
        }

        if (tokens.isCurrentToken(TokenType.SEMICOLON)) {
            context.consumeCurrentToken(TokenType.SEMICOLON);
        }

        return new ExpressionStatement(token, expression);
    }
}