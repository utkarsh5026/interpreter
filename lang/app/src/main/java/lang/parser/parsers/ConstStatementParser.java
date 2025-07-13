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

/**
 * 🔒 ConstStatementParser - The Immutable Value Declaration Expert 🔒
 * 
 * Specialized parser for handling const statements that create unchangeable
 * values.
 * Think of it as a skilled jeweler who creates permanent, tamper-proof
 * settings! 💎🔐
 * 
 * This parser handles statements like:
 * - const PI = 3.14159; 🔢
 * - const MAX_USERS = 100; 📊
 * - const API_URL = "https://api.example.com"; 🌐
 * - const COMPANY_NAME = "TechCorp"; 🏢
 * 
 * The parsing process follows these steps:
 * 1. 🔍 Recognize the 'const' keyword
 * 2. 📝 Extract the constant name
 * 3. 🎯 Expect an assignment operator '='
 * 4. 💎 Parse the value expression
 * 5. 🔒 Expect a semicolon to seal the constant
 * 
 * Like creating a time capsule: "To make a constant, take 'const', add a name,
 * bind with '=', lock in a value, and seal with ';'" 🏺🔐
 */
public class ConstStatementParser implements StatementParser<ConstStatement> {

    private final StatementParse statementParser; // 🔧 The main statement parser for delegation

    /**
     * 🏗️ Creates a new const statement parser
     * 
     * Sets up the parser with access to the main statement parser.
     * Like equipping a specialist jeweler with access to the main workshop! 💎🔧
     * 
     * @param statementParser The main statement parser for handling nested parsing
     *                        🔧
     */
    public ConstStatementParser(StatementParse statementParser) {
        this.statementParser = statementParser;
    }

    /**
     * 🔍 Checks if this parser can handle the current token
     * 
     * Determines whether the current token starts a const statement.
     * Like a specialist saying "Yes, this is my domain of expertise!" 🎯👨‍🔬
     * 
     * This method is called by the parser dispatcher to find the right
     * parser for each statement type.
     * 
     * @param context The parsing context containing current token information 🎯
     * @return True if current token is 'const', false otherwise ✅❌
     */
    @Override
    public boolean canParse(ParsingContext context) {
        return context.getTokenStream().isCurrentToken(TokenType.CONST);
    }

    /**
     * 🔒 Parses a complete const statement
     * 
     * Transforms a sequence of tokens into a ConstStatement AST node.
     * Like forging a permanent inscription in stone! 🗿✍️
     * 
     * Parsing steps:
     * 1. 🎫 Capture the 'const' token for AST construction
     * 2. 🏷️ Extract the constant name (must be an identifier)
     * 3. 🎯 Consume the assignment operator '='
     * 4. 💎 Parse the value expression (locked forever!)
     * 5. 🔐 Expect a semicolon to seal the constant
     * 
     * Error handling:
     * - Wrong keyword: "Expected 'const' keyword" 🚫
     * - Missing identifier: "Expected constant name after 'const'" 🚫
     * - Missing assignment: "Expected '=' after constant name" 🚫
     * - Invalid expression: "Expected expression after '='" 🚫
     * - Missing semicolon: "Expected ';' after expression" 🚫
     * 
     * Examples of successful parsing:
     * ```
     * const PI = 3.14159; // Mathematical constant
     * const MAX_SIZE = 1000; // Configuration limit
     * const GREETING = "Hello!"; // String constant
     * const VERSION = getVersion(); // Function result constant
     * ```
     * 
     * @param context The parsing context with tokens and error reporting 🎯
     * @return A complete ConstStatement AST node, or null if parsing fails 💎
     */
    @Override
    public ConstStatement parse(ParsingContext context) {
        TokenStream tokens = context.getTokenStream();
        Token constToken = tokens.getCurrentToken(); // 🎫 Save the 'const' token

        // 🔍 Double-check that we have the right token (safety measure)
        if (constToken.type() != TokenType.CONST) {
            context.addError("Expected 'const' keyword", constToken);
            return null;
        }

        // 🏷️ Get the constant name (must be an identifier)
        Token nameToken = tokens.consume(TokenType.IDENTIFIER);
        Identifier name = new Identifier(nameToken, nameToken.literal());

        // 🎯 Expect assignment operator
        tokens.consume(TokenType.ASSIGN);

        // 💎 Parse the value expression (this value will be immutable!)
        ExpressionParser expressionParser = new ExpressionParser(statementParser);
        Expression value = expressionParser.parseExpression(context, PrecedenceTable.Precedence.LOWEST);

        // ❌ Check if expression parsing failed
        if (value == null) {
            context.addError("Expected expression after '='", tokens.getCurrentToken());
            return null;
        }

        // 🔐 Expect semicolon to seal the constant
        if (!tokens.expect(TokenType.SEMICOLON)) {
            context.addError("Expected ';' after expression", tokens.getCurrentToken());
            return null;
        }

        // ✅ Successfully create the const statement
        return new ConstStatement(constToken, name, value);
    }
}
