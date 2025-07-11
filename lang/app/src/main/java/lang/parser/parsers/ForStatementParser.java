package lang.parser.parsers;

import lang.ast.statements.ForStatement;
import lang.ast.statements.BlockStatement;
import lang.ast.base.Statement;
import lang.ast.base.Expression;

import lang.parser.core.ParsingContext;
import lang.parser.core.TokenStream;
import lang.parser.core.PrecedenceTable;

import lang.token.Token;
import lang.token.TokenType;
import lang.parser.core.StatementParse;

/**
 * Parses for statements: for (init; condition; update) { body }
 */
public class ForStatementParser implements StatementParser<ForStatement> {

    private final StatementParse statementParser;

    public ForStatementParser(StatementParse statementParser) {
        this.statementParser = statementParser;
    }

    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokens().isCurrentToken(TokenType.FOR);
    }

    @Override
    public ForStatement parse(ParsingContext context) {
        TokenStream tokens = context.getTokens();

        Token forToken = tokens.getCurrentToken();

        tokens.consume(TokenType.LPAREN);

        // Parse initializer (usually a let statement)
        Statement initializer = statementParser.parseStatement(context);

        if (initializer == null) {
            context.addError("Expected initializer in for statement", tokens.getCurrentToken());
            return null;
        }

        ExpressionParser expressionParser = new ExpressionParser(statementParser);
        Expression condition = expressionParser.parseExpression(context,
                PrecedenceTable.Precedence.LOWEST);

        if (condition == null) {
            context.addError("Expected condition in for statement", tokens.getCurrentToken());
            return null;
        }

        tokens.consume(TokenType.SEMICOLON);

        Expression update = expressionParser.parseExpression(
                context,
                PrecedenceTable.Precedence.LOWEST);

        if (update == null) {
            context.addError("Expected update expression in for statement", tokens.getCurrentToken());
            return null;
        }

        tokens.consume(TokenType.RPAREN);
        tokens.consume(TokenType.LBRACE);

        context.enterLoop();
        BlockStatementParser blockParser = new BlockStatementParser(statementParser);
        BlockStatement body = blockParser.parse(context);
        context.exitLoop();

        if (body == null) {
            context.addError("Expected block statement for for body", tokens.getCurrentToken());
            return null;
        }

        return new ForStatement(forToken, initializer, condition, update, body);
    }
}