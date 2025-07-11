package lang.parser.parsers;

import lang.ast.statements.ReturnStatement;
import lang.parser.core.ParsingContext;
import lang.parser.core.TokenStream;
import lang.ast.base.Expression;
import lang.parser.core.PrecedenceTable;

import lang.token.Token;
import lang.token.TokenType;

public class ReturnStatementParser implements StatementParser<ReturnStatement> {

    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokens().isCurrentToken(TokenType.RETURN);
    }

    @Override
    public ReturnStatement parse(ParsingContext context) {
        TokenStream tokens = context.getTokens();
        Token returnToken = tokens.getCurrentToken();

        if (!canParse(context)) {
            context.addError("Expected 'return' keyword", returnToken);
            return null;
        }

        tokens.advance();

        ExpressionParser expressionParser = new ExpressionParser();
        Expression returnValue = expressionParser.parseExpression(context,
                PrecedenceTable.Precedence.LOWEST);

        if (returnValue == null) {
            context.addError("Expected expression after 'return'", tokens.getCurrentToken());
            return null;
        }

        if (!tokens.expect(TokenType.SEMICOLON)) {
            context.addError("Expected ';' after expression", tokens.getCurrentToken());
            return null;
        }

        return new ReturnStatement(returnToken, returnValue);
    }
}