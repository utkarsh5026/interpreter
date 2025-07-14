package lang.parser.expressions.prefix;

import lang.token.*;
import java.util.Set;
import java.util.List;

import lang.parser.interfaces.PrefixExpressionParser;
import lang.parser.parsers.BlockStatementParser;
import lang.parser.core.ParsingContext;
import lang.parser.core.StatementParse;
import lang.parser.core.ListParsingUtils; // Add this import

import lang.ast.base.Expression;
import lang.ast.base.Identifier;
import lang.ast.literals.FunctionLiteral;
import lang.ast.statements.BlockStatement;

/**
 * 🔧 FunctionLiteralParser - Function Definition Specialist 🔧
 * 
 * Handles function literal expressions that define anonymous functions.
 * Function literals create callable objects with parameters and a body.
 * 
 * Examples:
 * - fn(x) { return x * 2; } (single parameter)
 * - fn(a, b) { return a + b; } (multiple parameters)
 * - fn() { print("hello"); } (no parameters)
 * - fn(x, y) { let sum = x + y; return sum * 2; } (complex body)
 * 
 * Parsing process:
 * 1. Current token is FUNCTION (fn)
 * 2. Expect LPAREN (
 * 3. Parse parameter list (comma-separated identifiers)
 * 4. Expect RPAREN )
 * 5. Expect LBRACE {
 * 6. Parse block statement body
 * 7. Create FunctionLiteral AST node
 */
public class FunctionalLiteralParser implements PrefixExpressionParser {

    private final StatementParse statementParser;

    public FunctionalLiteralParser(StatementParse statementParser) {
        this.statementParser = statementParser;
    }

    @Override
    public Expression parsePrefix(ParsingContext context) {
        Token functionToken = context.consumeCurrentToken(TokenType.FUNCTION,
                "Expected 'fn' keyword");

        context.consumeCurrentToken(TokenType.LPAREN, "Expected '(' after 'fn' keyword");

        List<Identifier> parameters = parseParameters(context);
        context.consumeCurrentToken(TokenType.RPAREN, "Expected ')' after parameters");

        BlockStatementParser blockParser = new BlockStatementParser(statementParser);
        BlockStatement block = blockParser.parse(context);

        return new FunctionLiteral(functionToken, parameters, block);
    }

    /**
     * 📋 Parses function parameters using the common list parsing utility
     */
    private List<Identifier> parseParameters(ParsingContext context) {
        return ListParsingUtils.parseCustomList(
                context,
                this::parseParameter,
                TokenType.RPAREN,
                "function parameter");
    }

    /**
     * 🏷️ Parses a single function parameter (identifier)
     */
    private Identifier parseParameter(ParsingContext context) {
        Token identifierToken = context.consumeCurrentToken(TokenType.IDENTIFIER,
                "Expected parameter name");
        return new Identifier(identifierToken, identifierToken.literal());
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.FUNCTION);
    }
}
