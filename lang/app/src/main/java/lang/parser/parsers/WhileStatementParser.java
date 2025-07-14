package lang.parser.parsers;

import lang.ast.statements.WhileStatement;
import lang.ast.statements.BlockStatement;

import lang.parser.core.*;
import lang.parser.error.ParserException;
import lang.parser.interfaces.TypedStatementParser;
import lang.ast.base.Expression;
import lang.parser.precedence.Precedence;
import lang.parser.interfaces.ExpressionParser;

import lang.token.Token;
import lang.token.TokenType;

/**
 * 🔄 WhileStatementParser - The Persistent Loop Specialist 🔄
 * 
 * Specialized parser for handling while statements that create condition-based
 * loops.
 */
public class WhileStatementParser implements TypedStatementParser<WhileStatement> {

    private final ExpressionParser expressionParser;
    private final StatementParse statementParser;

    public WhileStatementParser(ExpressionParser expressionParser, StatementParse statementParser) {
        this.expressionParser = expressionParser;
        this.statementParser = statementParser;
    }

    /**
     * 🔍 Checks if this parser can handle the current token
     */
    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokenStream().isCurrentToken(TokenType.WHILE);
    }

    /**
     * 🔄 Parses a complete while statement
     * 
     * Transforms a sequence of tokens into a WhileStatement AST node.
     * Like setting up a persistent monitoring system! 🔄🛠️➡️⚙️
     * 
     */
    @Override
    public WhileStatement parse(ParsingContext context) throws ParserException {
        System.out.println("Parsing while statement");
        Token whileToken = context.consumeCurrentToken(TokenType.WHILE);
        context.consumeCurrentToken(TokenType.LPAREN, "Expected '(' after 'while'");
        Expression condition = expressionParser.parseExpression(context,
                Precedence.LOWEST);

        context.consumeCurrentToken(TokenType.RPAREN, "Expected ')' after while condition");

        context.enterLoop();
        BlockStatementParser blockParser = new BlockStatementParser(statementParser);
        BlockStatement body = blockParser.parse(context);
        context.exitLoop();

        return new WhileStatement(whileToken, condition, body);
    }
}