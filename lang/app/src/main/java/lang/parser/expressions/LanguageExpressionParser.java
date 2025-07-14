package lang.parser.expressions;

import lang.token.TokenType;
import java.util.Optional;
import lang.ast.base.Expression;
import java.util.Set;

import lang.parser.interfaces.InfixExpressionParser;
import lang.parser.interfaces.PrefixExpressionParser;

import lang.parser.core.ParsingContext;
import lang.parser.precedence.Precedence;
import lang.parser.registry.ExpressionParserRegistry;
import lang.parser.core.*;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.core.StatementParse;
import lang.parser.error.ParserException;

import lang.token.Token;

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

        System.out.println("Should continue parsing: " + tokens.getPeekToken() + " " + tokens.getCurrentToken()
                + " " + tokens.isCurrentToken(TokenType.SEMICOLON) + " " + tokens.isCurrentToken(TokenType.COMMA)
                + " " + tokens.isCurrentToken(TokenType.RPAREN));

        Set<TokenType> stopTokens = Set.of(TokenType.SEMICOLON, TokenType.COMMA, TokenType.COLON);
        if (stopTokens.contains(tokens.getCurrentToken().type())) {
            return false;
        }

        TokenType currentTokenType = tokens.getCurrentToken().type();
        Precedence nextPrecedence = context.getPrecedenceTable()
                .getPrecedence(currentTokenType);

        if (minPrecedence.getLevel() >= nextPrecedence.getLevel()) {
            return false;
        }

        return registry.hasInfixParser(currentTokenType);
    }

    private Expression parseInfix(ParsingContext context, Expression left) {
        TokenStream tokens = context.getTokenStream();
        Token currentToken = tokens.getCurrentToken();

        System.out.println("Parsing infix: " + currentToken);

        Optional<InfixExpressionParser> parser = registry.getInfixParser(currentToken.type());
        if (parser.isEmpty()) {
            return left;
        }

        return parser.get().parseInfix(context, left);
    }

    private Expression parsePrefix(ParsingContext context) {
        TokenStream tokens = context.getTokenStream();
        TokenType currentTokenType = tokens.getCurrentToken().type();

        Optional<PrefixExpressionParser> parser = registry.getPrefixParser(currentTokenType);

        if (parser.isEmpty()) {
            throw new ParserException("No prefix parser found for token type: " + currentTokenType,
                    tokens.getCurrentToken());
        }

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
        Expression leftExpression = parsePrefix(context);

        while (shouldContinueParsing(context, minPrecedence)) {
            leftExpression = parseInfix(context, leftExpression);
        }

        return leftExpression;
    }
}
