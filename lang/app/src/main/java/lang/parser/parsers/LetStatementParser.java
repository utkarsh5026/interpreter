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

/**
 * 📦 LetStatementParser - The Variable Declaration Expert 📦
 * 
 * Specialized parser for handling let statements that create new variables.
 * Think of it as a skilled librarian who sets up new labeled storage boxes!
 * 📚🏷️
 * 
 * This parser handles statements like:
 * - let x = 5; 📝
 * - let name = "Alice"; 👤
 * - let result = calculate(a, b); 🔢
 * - let isActive = true; ✅
 * 
 * The parsing process follows these steps:
 * 1. 🔍 Recognize the 'let' keyword
 * 2. 📝 Extract the variable name
 * 3. 🎯 Expect an assignment operator '='
 * 4. 💡 Parse the value expression
 * 5. ✅ Expect a semicolon to end the statement
 * 
 * Like following a recipe: "To make a variable, take 'let', add a name,
 * mix with '=', blend in a value, and finish with ';'" 👨‍🍳📋
 */
public class LetStatementParser implements StatementParser<LetStatement> {

    private final StatementParse statementParser; // 🔧 The main statement parser for delegation

    /**
     * 🏗️ Creates a new let statement parser
     * 
     * @param statementParser The main statement parser for handling nested parsing
     *                        🔧
     */
    public LetStatementParser(StatementParse statementParser) {
        this.statementParser = statementParser;
    }

    /**
     * 🔍 Checks if this parser can handle the current token
     * 
     * @param context The parsing context containing current token information 🎯
     * @return True if current token is 'let', false otherwise ✅❌
     */
    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokens().isCurrentToken(TokenType.LET);
    }

    /**
     * 📝 Parses a complete let statement
     * 
     * Transforms a sequence of tokens into a LetStatement AST node.
     * Like translating a recipe into actual cooking steps! 👨‍🍳➡️🍽️
     * 
     * Parsing steps:
     * 1. 🎫 Capture the 'let' token for AST construction
     * 2. 🏷️ Extract the variable name (must be an identifier)
     * 3. 🎯 Consume the assignment operator '='
     * 4. 🧮 Parse the value expression (could be complex!)
     * 5. 🔚 Expect a semicolon to complete the statement
     * 
     * Error handling:
     * - Missing identifier: "Expected variable name after 'let'" 🚫
     * - Missing assignment: "Expected '=' after variable name" 🚫
     * - Invalid expression: "Expected expression after '='" 🚫
     * - Missing semicolon: "Expected ';' after expression" 🚫
     * 
     * Examples of successful parsing:
     * ```
     * let x = 5; // Simple assignment
     * let name = "Alice"; // String value
     * let sum = a + b; // Expression value
     * let result = func(); // Function call value
     * ```
     * 
     * @param context The parsing context with tokens and error reporting 🎯
     * @return A complete LetStatement AST node, or null if parsing fails 📦
     */
    @Override
    public LetStatement parse(ParsingContext context) {
        TokenStream tokens = context.getTokens();
        Token letToken = tokens.getCurrentToken(); // 🎫 Save the 'let' token

        // 🏷️ Get the variable name (must be an identifier)
        Token nameToken = tokens.consume(TokenType.IDENTIFIER);
        Identifier name = new Identifier(nameToken, nameToken.literal());

        // 🎯 Expect assignment operator
        tokens.consume(TokenType.ASSIGN);

        // 🧮 Parse the value expression
        ExpressionParser expressionParser = new ExpressionParser(statementParser);
        Expression value = expressionParser.parseExpression(context, PrecedenceTable.Precedence.LOWEST);

        // ❌ Check if expression parsing failed
        if (value == null) {
            context.addError("Expected expression after '='", tokens.getCurrentToken());
            return null;
        }

        // 🔚 Expect semicolon to end the statement
        if (!tokens.expect(TokenType.SEMICOLON)) {
            context.addError("Expected ';' after expression", tokens.getCurrentToken());
            return null;
        }

        // ✅ Successfully create the let statement
        return new LetStatement(letToken, name, value);
    }
}
