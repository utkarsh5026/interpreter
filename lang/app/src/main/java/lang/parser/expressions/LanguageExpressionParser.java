package lang.parser.expressions;

import lang.token.TokenType;
import java.util.Optional;
import lang.ast.base.Expression;

import lang.parser.interfaces.InfixExpressionParser;
import lang.parser.interfaces.PrefixExpressionParser;

import lang.parser.core.ParsingContext;
import lang.parser.precedence.Precedence;
import lang.parser.registry.ExpressionParserRegistry;
import lang.parser.core.TokenStream;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.core.StatementParse;

public class LanguageExpressionParser implements ExpressionParser {

    private final ExpressionParserRegistry registry;

    public LanguageExpressionParser(StatementParse statementParser) {
        this.registry = new ExpressionParserRegistry(this, statementParser);
    }

    /**
     * 🔍 Checks if a token type can start an expression
     */
    public boolean canStartExpression(TokenType tokenType) {
        return registry.hasPrefixParser(tokenType);
    }

    /**
     * ⚡ Checks if a token type can be used as an infix operator
     */
    public boolean canBeInfixOperator(TokenType tokenType) {
        return registry.hasInfixParser(tokenType);
    }

    public ExpressionParserRegistry getRegistry() {
        return registry;
    }

    private boolean shouldContinueParsing(ParsingContext context, Precedence minPrecedence) {
        TokenStream tokens = context.getTokenStream();

        if (tokens.isPeekToken(TokenType.SEMICOLON)) {
            return false;
        }

        TokenType peekTokenType = tokens.getPeekToken().type();
        Precedence nextPrecedence = context.getPrecedenceTable()
                .getPrecedence(peekTokenType);

        if (minPrecedence.getLevel() >= nextPrecedence.getLevel()) {
            return false;
        }

        return registry.hasInfixParser(peekTokenType);
    }

    private Expression parseInfix(ParsingContext context, Expression left) {
        TokenStream tokens = context.getTokenStream();
        TokenType peekTokenType = tokens.getPeekToken().type();

        Optional<InfixExpressionParser> parser = registry.getInfixParser(peekTokenType);

        if (parser.isEmpty()) {
            return left;
        }

        tokens.advance();
        return parser.get().parseInfix(context, left);
    }

    private Expression parsePrefix(ParsingContext context) {
        TokenStream tokens = context.getTokenStream();
        TokenType currentTokenType = tokens.getCurrentToken().type();

        // 🔍 Find a prefix parser for the current token
        Optional<PrefixExpressionParser> parser = registry.getPrefixParser(currentTokenType);

        if (parser.isEmpty()) {
            // 🚫 No prefix parser found - this token can't start an expression
            context.getErrors().addPrefixError(currentTokenType, tokens.getCurrentToken());
            return null;
        }

        // ✅ Delegate to the specialized prefix parser
        return parser.get().parsePrefix(context);
    }

    /**
     * 🎯 Main expression parsing method implementing Pratt parsing
     * 
     * This is the heart of the expression parsing system. It uses the
     * Pratt parsing algorithm to correctly handle operator precedence
     * and associativity while delegating to specialized parsers.
     * 
     * The algorithm works in two phases:
     * 1. **Prefix Phase**: Parse the initial expression using a prefix parser
     * 2. **Infix Phase**: Repeatedly combine with higher-precedence operators
     * 
     * @param context       The parsing context with tokens and error reporting
     * @param minPrecedence The minimum precedence level for this parsing pass
     * @return The parsed expression, or null if parsing fails
     */
    public Expression parseExpression(ParsingContext context, Precedence minPrecedence) {
        TokenStream tokens = context.getTokenStream();

        Expression leftExpression = parsePrefix(context);
        if (leftExpression == null) {
            return null;
        }

        while (shouldContinueParsing(context, minPrecedence)) {
            leftExpression = parseInfix(context, leftExpression);
            if (leftExpression == null) {
                return null; // Infix parsing failed
            }
        }

        tokens.advanceIfPeek(TokenType.SEMICOLON);
        return leftExpression;
    }
}
