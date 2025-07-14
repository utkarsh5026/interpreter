package lang.parser.parsers;

import lang.ast.statements.ReturnStatement;
import lang.parser.core.ParsingContext;
import lang.parser.core.TokenStream;
import lang.ast.base.Expression;
import lang.parser.core.PrecedenceTable;

import lang.token.Token;
import lang.token.TokenType;

import lang.parser.core.StatementParse;

public class ReturnStatementParser implements StatementParser<ReturnStatement> {

    private final StatementParse statementParser;

    public ReturnStatementParser(StatementParse statementParser) {
        this.statementParser = statementParser;
    }

    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokenStream().isCurrentToken(TokenType.RETURN);
    }

    @Override
    public ReturnStatement parse(ParsingContext context) {
        Token returnToken = context.consume(TokenType.RETURN);

        ExpressionParser expressionParser = new ExpressionParser(statementParser);
        Expression returnValue = expressionParser.parseExpression(context,
                PrecedenceTable.Precedence.LOWEST);

        if (returnValue == null) {
            context.addError("Expected expression after 'return'", returnToken);
            return null;
        }

        context.consume(TokenType.SEMICOLON);

        return new ReturnStatement(returnToken, returnValue);
    }
}