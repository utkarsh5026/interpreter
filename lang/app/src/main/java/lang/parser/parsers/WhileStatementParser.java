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

/**
 * 🔄 WhileStatementParser - The Persistent Loop Specialist 🔄
 * 
 * Specialized parser for handling while statements that create condition-based
 * loops.
 * Think of it as a patient guardian who keeps asking "Should we continue?" 🤔⏰
 * 
 * This parser handles statements like:
 * - while (count < 10) { count++; } 🔢
 * - while (hasNext()) { process(getNext()); } 📊
 * - while (isRunning) { handleEvents(); } 🔄
 * - while (input != "quit") { processInput(); } 💬
 * 
 * A while loop has two essential components:
 * 1. 🎯 **Condition**: When to continue looping (count < 10)
 * 2. 🏗️ **Body**: What to do each time (the block of code)
 * 
 * The parsing process follows these steps:
 * 1. 🔍 Recognize the 'while' keyword
 * 2. 📝 Expect opening parenthesis '('
 * 3. 🎯 Parse the condition expression
 * 4. 📝 Expect closing parenthesis ')'
 * 5. 🏗️ Parse the body block
 * 
 * Like setting up a persistent worker: "Keep checking this condition,
 * and as long as it's true, keep doing this work!" 🤖🔄
 */
public class WhileStatementParser implements StatementParser<WhileStatement> {

    private final StatementParse statementParser; // 🔧 The main statement parser for delegation

    /**
     * 🏗️ Creates a new while statement parser
     * 
     * Sets up the parser with access to the main statement parser.
     * Like equipping a persistent worker with access to all necessary tools! 🔄🔧
     * 
     * @param statementParser The main statement parser for handling nested parsing
     *                        🔧
     */
    public WhileStatementParser(StatementParse statementParser) {
        this.statementParser = statementParser;
    }

    /**
     * 🔍 Checks if this parser can handle the current token
     * 
     * @param context The parsing context containing current token information 🎯
     * @return True if current token is 'while', false otherwise ✅❌
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
     * Parsing steps:
     * 1. 🎫 Capture the 'while' token for AST construction
     * 2. 📝 Consume the opening parenthesis '('
     * 3. 🎯 Parse the condition expression (the "keep going?" test)
     * 4. 📝 Consume the closing parenthesis ')'
     * 5. 🏗️ Parse the body block (enter loop context first!)
     * 
     * Loop context management:
     * - 🔄 Calls `context.enterLoop()` before parsing body
     * - 🔄 Calls `context.exitLoop()` after parsing body
     * - This enables proper validation of break/continue statements
     * 
     * Error handling:
     * - Missing parentheses: "Expected '(' after 'while'" 🚫
     * - Invalid condition: "Expected condition in while statement" 🚫
     * - Invalid body: "Expected block statement for while body" 🚫
     * 
     * @param context The parsing context with tokens and error reporting 🎯
     * @return A complete WhileStatement AST node, or null if parsing fails 🔄
     */
    @Override
    public WhileStatement parse(ParsingContext context) {
        TokenStream tokens = context.getTokenStream();
        Token whileToken = tokens.getCurrentToken(); // 🎫 Save the 'while' token

        // 📝 Expect opening parenthesis
        tokens.consume(TokenType.LPAREN);

        // 🎯 Parse condition expression (the "should we continue?" test)
        ExpressionParser expressionParser = new ExpressionParser(statementParser);
        Expression condition = expressionParser.parseExpression(context,
                PrecedenceTable.Precedence.LOWEST);

        if (condition == null) {
            context.addError("Expected condition in while statement", tokens.getCurrentToken());
            return null;
        }

        tokens.consume(TokenType.RPAREN);

        tokens.consume(TokenType.LBRACE);

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