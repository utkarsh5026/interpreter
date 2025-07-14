package lang.parser.parsers;

import lang.ast.statements.ReturnStatement;
import lang.ast.base.Expression;
import lang.ast.expressions.NullExpression;
import lang.parser.core.*;
import lang.parser.interfaces.TypedStatementParser;
import lang.parser.precedence.Precedence;
import lang.parser.interfaces.ExpressionParser;
import lang.token.*;

public class ReturnStatementParser implements TypedStatementParser<ReturnStatement> {

    private final ExpressionParser expressionParser;

    public ReturnStatementParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokenStream().isCurrentToken(TokenType.RETURN);
    }

    @Override
    public ReturnStatement parse(ParsingContext context) {
        Token returnToken = context.consumeCurrentToken(TokenType.RETURN);

        if (context.getTokenStream().isCurrentToken(TokenType.SEMICOLON)) {
            context.consumeCurrentToken(TokenType.SEMICOLON);
            return new ReturnStatement(returnToken, new NullExpression(returnToken));
        }

        Expression returnValue = expressionParser.parseExpression(context,
                Precedence.LOWEST);

        if (returnValue == null) {
            context.addError("Expected expression after 'return'", returnToken);
            return null;
        }

        context.consumeCurrentToken(TokenType.SEMICOLON);
        return new ReturnStatement(returnToken, returnValue);
    }
}