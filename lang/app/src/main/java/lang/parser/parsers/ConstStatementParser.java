package lang.parser.parsers;

import lang.ast.statements.ConstStatement;
import lang.parser.core.ParsingContext;
import lang.parser.core.TokenStream;

import lang.ast.base.Expression;
import lang.ast.base.Identifier;
import lang.parser.core.PrecedenceTable;

import lang.token.Token;
import lang.token.TokenType;

import lang.parser.core.StatementParse;

public class ConstStatementParser implements StatementParser<ConstStatement> {

    private final StatementParse statementParser;

    public ConstStatementParser(StatementParse statementParser) {
        this.statementParser = statementParser;
    }

    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokens().isCurrentToken(TokenType.CONST);
    }

    @Override
    public ConstStatement parse(ParsingContext context) {
        TokenStream tokens = context.getTokens();
        Token constToken = tokens.getCurrentToken();

        if (constToken.type() != TokenType.CONST) {
            context.addError("Expected 'const' keyword", constToken);
            return null;
        }

        Token nameToken = tokens.consume(TokenType.IDENTIFIER);
        Identifier name = new Identifier(nameToken, nameToken.literal());

        tokens.consume(TokenType.ASSIGN);

        ExpressionParser expressionParser = new ExpressionParser(statementParser);
        Expression value = expressionParser.parseExpression(context, PrecedenceTable.Precedence.LOWEST);

        if (value == null) {
            context.addError("Expected expression after '='", tokens.getCurrentToken());
            return null;
        }

        if (!tokens.expect(TokenType.SEMICOLON)) {
            context.addError("Expected ';' after expression", tokens.getCurrentToken());
            return null;
        }

        return new ConstStatement(constToken, name, value);
    }
}
