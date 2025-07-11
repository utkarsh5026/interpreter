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
public class ForStatementParser implements StatementParser<ForStatement> {

    private final StatementParse statementParser; // 🔧 The main statement parser for delegation

    /**
     * 🏗️ Creates a new for statement parser
     * 
     * Sets up the parser with access to the main statement parser.
     * Like equipping a factory supervisor with access to all the specialist tools!
     * 🏭🔧
     * 
     * @param statementParser The main statement parser for handling nested parsing
     *                        🔧
     */
    public ForStatementParser(StatementParse statementParser) {
        this.statementParser = statementParser;
    }

    /**
     * 🔍 Checks if this parser can handle the current token
     * 
     * Determines whether the current token starts a for statement.
     * Like a specialist saying "Yes, this is my area of expertise!" 🎯👨‍🔬
     * 
     * This method is called by the parser dispatcher to find the right
     * parser for each statement type.
     * 
     * @param context The parsing context containing current token information 🎯
     * @return True if current token is 'for', false otherwise ✅❌
     */
    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokens().isCurrentToken(TokenType.FOR);
    }

    /**
     * 🔁 Parses a complete for statement
     * 
     * Transforms a sequence of tokens into a ForStatement AST node.
     * Like assembling a complex machine from its individual components! 🏭🔧➡️⚙️
     * 
     * Parsing steps:
     * 1. 🎫 Capture the 'for' token for AST construction
     * 2. 📝 Consume the opening parenthesis '('
     * 3. 🚀 Parse the initializer statement (usually `let i = 0`)
     * 4. 🎯 Parse the condition expression (usually `i < 10`)
     * 5. 📝 Consume the semicolon after condition
     * 6. 📈 Parse the update expression (usually `i++`)
     * 7. 📝 Consume the closing parenthesis ')'
     * 8. 🏗️ Parse the body block (enter loop context first!)
     * 
     * Loop context management:
     * - 🔄 Calls `context.enterLoop()` before parsing body
     * - 🔄 Calls `context.exitLoop()` after parsing body
     * - This enables proper validation of break/continue statements
     * 
     * Error handling:
     * - Missing parentheses: "Expected '(' after 'for'" 🚫
     * - Invalid initializer: "Expected initializer in for statement" 🚫
     * - Invalid condition: "Expected condition in for statement" 🚫
     * - Invalid update: "Expected update expression in for statement" 🚫
     * - Invalid body: "Expected block statement for for body" 🚫
     * 
     * 
     * @param context The parsing context with tokens and error reporting 🎯
     * @return A complete ForStatement AST node, or null if parsing fails 🔁
     */
    @Override
    public ForStatement parse(ParsingContext context) {
        TokenStream tokens = context.getTokens();
        Token forToken = tokens.getCurrentToken(); // 🎫 Save the 'for' token

        // 📝 Expect opening parenthesis
        tokens.consume(TokenType.LPAREN);

        // 🚀 Parse initializer (usually a let statement like "let i = 0")
        Statement initializer = statementParser.parseStatement(context);

        if (initializer == null) {
            context.addError("Expected initializer in for statement", tokens.getCurrentToken());
            return null;
        }

        // 🎯 Parse condition expression (like "i < 10")
        ExpressionParser expressionParser = new ExpressionParser(statementParser);
        Expression condition = expressionParser.parseExpression(context,
                PrecedenceTable.Precedence.LOWEST);

        if (condition == null) {
            context.addError("Expected condition in for statement", tokens.getCurrentToken());
            return null;
        }

        // 📝 Expect semicolon after condition
        tokens.consume(TokenType.SEMICOLON);

        // 📈 Parse update expression (like "i++")
        Expression update = expressionParser.parseExpression(
                context,
                PrecedenceTable.Precedence.LOWEST);

        if (update == null) {
            context.addError("Expected update expression in for statement", tokens.getCurrentToken());
            return null;
        }

        // 📝 Expect closing parenthesis
        tokens.consume(TokenType.RPAREN);

        // 🏗️ Expect opening brace for body
        tokens.consume(TokenType.LBRACE);

        // 🔄 Enter loop context for proper break/continue validation
        context.enterLoop();
        BlockStatementParser blockParser = new BlockStatementParser(statementParser);
        BlockStatement body = blockParser.parse(context);
        context.exitLoop(); // 🔄 Exit loop context

        if (body == null) {
            context.addError("Expected block statement for for body", tokens.getCurrentToken());
            return null;
        }

        // ✅ Successfully create the for statement
        return new ForStatement(forToken, initializer, condition, update, body);
    }
}