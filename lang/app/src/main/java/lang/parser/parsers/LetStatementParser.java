package lang.parser.parsers;

import lang.ast.statements.LetStatement;
import lang.parser.core.ParsingContext;
import lang.parser.core.TokenStream;
import lang.ast.base.Expression;
import lang.ast.base.Identifier;
import lang.parser.core.PrecedenceTable;

import lang.token.Token;
import lang.token.TokenType;

import lang.parser.core.StatementParse;

public class LetStatementParser implements StatementParser<LetStatement> {

    private final StatementParse statementParser;

    public LetStatementParser(StatementParse statementParser) {
        this.statementParser = statementParser;
    }

    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokenStream().isCurrentToken(TokenType.LET);
    }

    @Override
    public LetStatement parse(ParsingContext context) {
        TokenStream tokenStream = context.getTokenStream();
        Token letToken = tokenStream.getCurrentToken();

        Token nameToken = tokenStream.consume(TokenType.IDENTIFIER);
        Identifier name = new Identifier(nameToken, nameToken.literal());

        tokenStream.consume(TokenType.ASSIGN);

        ExpressionParser expressionParser = new ExpressionParser(statementParser);
        Expression value = expressionParser.parseExpression(context, PrecedenceTable.Precedence.LOWEST);

        if (value == null) {
            context.addError("Expected expression after '='", tokenStream.getCurrentToken());
            return null;
        }

        if (!tokenStream.expect(TokenType.SEMICOLON)) {
            context.addError("Expected ';' after expression", tokenStream.getCurrentToken());
            return null;
        }

        return new LetStatement(letToken, name, value);
    }
}
