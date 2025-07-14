package lang.parser.parsers;

import lang.ast.statements.ForStatement;
import lang.ast.statements.LetStatement;
import lang.ast.statements.BlockStatement;
import lang.ast.base.Expression;

import lang.parser.core.ParsingContext;
import lang.parser.interfaces.TypedStatementParser;
import lang.parser.precedence.Precedence;

import lang.token.Token;
import lang.token.TokenType;
import lang.parser.core.StatementParse;
import lang.parser.interfaces.ExpressionParser;

/**
 * 🔁 ForStatementParser - The Controlled Loop Architect 🔁
 * 
 * Specialized parser for handling for statements that create controlled
 * iteration loops.
 * Think of it as a skilled factory supervisor who sets up assembly line
 * processes! 🏭⚙️
 * 
 * This parser handles statements like:
 * - for (let i = 0; i < 10; i++) { print(i); } 🔢
 * - for (let x = 1; x <= 100; x *= 2) { process(x); } 📈
 * - for (let item = first; item != null; item = item.next) { handle(item); } 🔗
 * 
 * A for loop has four essential components:
 * 1. 🚀 **Initializer**: Set up the loop variable (let i = 0)
 * 2. 🎯 **Condition**: When to continue looping (i < 10)
 * 3. 📈 **Update**: How to advance each iteration (i++)
 * 4. 🏗️ **Body**: What to do each time (the block of code)
 * 
 * The parsing process follows these steps:
 * 1. 🔍 Recognize the 'for' keyword
 * 2. 📝 Expect opening parenthesis '('
 * 3. 🚀 Parse the initializer statement
 * 4. 🎯 Parse the condition expression
 * 5. 📈 Parse the update expression
 * 6. 📝 Expect closing parenthesis ')'
 * 7. 🏗️ Parse the body block
 * 
 * Like setting up a precise machine: "Initialize the counter, check if we
 * should continue,
 * do the work, update the counter, repeat!" 🤖🔄
 */
public class ForStatementParser implements TypedStatementParser<ForStatement> {

    private final StatementParse statementParser;
    private final ExpressionParser expressionParser;

    public ForStatementParser(ExpressionParser expressionParser, StatementParse statementParse) {
        this.statementParser = statementParse;
        this.expressionParser = expressionParser;
    }

    /**
     * 🔍 Checks if this parser can handle the current token
     */
    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokenStream().isCurrentToken(TokenType.FOR);
    }

    /**
     * 🔁 Parses a complete for statement
     */
    @Override
    public ForStatement parse(ParsingContext context) {
        Token forToken = context.consumeCurrentToken(TokenType.FOR, "Expected 'for' keyword");

        context.consumeCurrentToken(TokenType.LPAREN, "Expected '(' after 'for' keyword");

        LetStatementParser letParser = new LetStatementParser(expressionParser);
        LetStatement initializer = letParser.parse(context);

        // 🎯 Parse condition expression (like "i < 10")
        Expression condition = expressionParser.parseExpression(context,
                Precedence.LOWEST);

        // 📝 Expect semicolon after condition
        context.consumeCurrentToken(TokenType.SEMICOLON, "Expected ';' after condition");

        // 📈 Parse update expression (like "i++")
        Expression update = expressionParser.parseExpression(
                context,
                Precedence.LOWEST);

        context.consumeCurrentToken(TokenType.RPAREN, "Expected ')' after update");

        // 🔄 Enter loop context for proper break/continue validation
        context.enterLoop();
        BlockStatementParser blockParser = new BlockStatementParser(statementParser);
        BlockStatement body = blockParser.parse(context);
        context.exitLoop(); // 🔄 Exit loop context

        // ✅ Successfully create the for statement
        return new ForStatement(forToken, initializer, condition, update, body);
    }
}