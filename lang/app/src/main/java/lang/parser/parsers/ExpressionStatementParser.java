package lang.parser.parsers;

import lang.ast.statements.ExpressionStatement;
import lang.parser.core.ParsingContext;
import lang.parser.core.TokenStream;
import lang.ast.base.Expression;
import lang.parser.core.PrecedenceTable;

import lang.token.Token;
import lang.token.TokenType;

import lang.parser.core.StatementParse;

/**
 * Parses expression statements: 5 + 3; or functionCall();
 */
public class ExpressionStatementParser implements StatementParser<ExpressionStatement> {

    private final StatementParse statementParser;

    public ExpressionStatementParser(StatementParse statementParser) {
        this.statementParser = statementParser;
    }

    @Override
    public boolean canParse(ParsingContext context) {
        // Expression statements can start with many different tokens
        // This should be the fallback parser
        return true;
    }

    @Override
    public ExpressionStatement parse(ParsingContext context) {
        TokenStream tokens = context.getTokens();
        Token token = tokens.getCurrentToken();

        ExpressionParser expressionParser = new ExpressionParser(statementParser);
        Expression expression = expressionParser.parseExpression(context,
                PrecedenceTable.Precedence.LOWEST);

        if (expression == null) {
            context.addError("Expected expression", tokens.getCurrentToken());
            return null;
        }

        // Optional semicolon
        if (tokens.isPeekToken(TokenType.SEMICOLON)) {
            tokens.advance();
        }

        return new ExpressionStatement(token, expression);
    }
}