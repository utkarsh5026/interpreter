package lang.parser.parsers;

import lang.ast.statements.LetStatement;
import lang.parser.core.ParsingContext;
import lang.parser.core.TokenStream;
import lang.ast.base.Expression;
import lang.ast.base.Identifier;
import lang.parser.core.PrecedenceTable;

import lang.token.Token;
import lang.token.TokenType;

public class LetStatementParser implements StatementParser<LetStatement> {

    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokens().isCurrentToken(TokenType.LET);
    }

    @Override
    public LetStatement parse(ParsingContext context) {
        TokenStream tokens = context.getTokens();
        Token letToken = tokens.getCurrentToken();

        Token nameToken = tokens.consume(TokenType.IDENTIFIER);
        Identifier name = new Identifier(nameToken, nameToken.literal());

        tokens.consume(TokenType.ASSIGN);

        ExpressionParser expressionParser = new ExpressionParser();
        Expression value = expressionParser.parseExpression(context, PrecedenceTable.Precedence.LOWEST);

        if (value == null) {
            context.addError("Expected expression after '='", tokens.getCurrentToken());
            return null;
        }

        if (!tokens.expect(TokenType.SEMICOLON)) {
            context.addError("Expected ';' after expression", tokens.getCurrentToken());
            return null;
        }

        return new LetStatement(letToken, name, value);

    }
}
