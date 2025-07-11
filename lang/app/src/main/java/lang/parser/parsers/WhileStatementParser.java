package lang.parser.parsers;

import lang.ast.statements.WhileStatement;
import lang.ast.statements.BlockStatement;

import lang.parser.core.ParsingContext;
import lang.parser.core.TokenStream;
import lang.parser.core.StatementParse;

import lang.ast.base.Expression;
import lang.parser.core.PrecedenceTable;

import lang.token.Token;
import lang.token.TokenType;

public class WhileStatementParser implements StatementParser<WhileStatement> {

    private final StatementParse statementParser;

    public WhileStatementParser(StatementParse statementParser) {
        this.statementParser = statementParser;
    }

    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokens().isCurrentToken(TokenType.WHILE);
    }

    @Override
    public WhileStatement parse(ParsingContext context) {
        TokenStream tokens = context.getTokens();
        Token whileToken = tokens.getCurrentToken();

        // Expect '('
        tokens.consume(TokenType.LPAREN);

        // Parse condition
        ExpressionParser expressionParser = new ExpressionParser();
        Expression condition = expressionParser.parseExpression(context,
                PrecedenceTable.Precedence.LOWEST);

        if (condition == null) {
            context.addError("Expected condition in while statement", tokens.getCurrentToken());
            return null;
        }

        // Expect ')'
        tokens.consume(TokenType.RPAREN);

        // Expect '{'
        tokens.consume(TokenType.LBRACE);

        // Parse body (block statement)
        context.enterLoop();
        BlockStatementParser blockParser = new BlockStatementParser(statementParser);
        BlockStatement body = blockParser.parse(context);
        context.exitLoop();

        if (body == null) {
            context.addError("Expected block statement for while body", tokens.getCurrentToken());
            return null;
        }

        return new WhileStatement(whileToken, condition, body);
    }
}