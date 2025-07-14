package lang.parser.core;

import lang.ast.base.Expression;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.precedence.Precedence;
import lang.parser.error.ParserException;
import lang.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 📋 ListParsingUtils - Common List Parsing Utilities 📋
 * 
 * Provides reusable utilities for parsing comma-separated lists in various
 * contexts:
 * - Function arguments: (expr1, expr2, expr3)
 * - Array elements: [expr1, expr2, expr3]
 * - Hash pairs: {key1: value1, key2: value2}
 * 
 * This eliminates code duplication and provides consistent parsing behavior.
 */
public class ListParsingUtils {

    /**
     * 📝 Parses a comma-separated list of expressions
     * 
     * @param context          The parsing context
     * @param expressionParser The expression parser to use
     * @param closingDelimiter The token that ends the list (e.g., RPAREN, RBRACKET,
     *                         RBRACE)
     * @param contextName      Description for error messages (e.g., "function
     *                         arguments", "array elements")
     * @return List of parsed expressions
     */
    public static List<Expression> parseExpressionList(
            ParsingContext context,
            ExpressionParser expressionParser,
            TokenType closingDelimiter,
            String contextName) {

        List<Expression> expressions = new ArrayList<>();
        TokenStream tokens = context.getTokenStream();

        if (tokens.isCurrentToken(closingDelimiter)) {
            return expressions;
        }

        while (!tokens.isCurrentToken(closingDelimiter)) {
            Expression expression = expressionParser.parseExpression(context, Precedence.LOWEST);
            expressions.add(expression);

            if (!tokens.isCurrentToken(TokenType.COMMA) && !tokens.isCurrentToken(closingDelimiter)) {
                String expectedDelimiter = getDelimiterName(closingDelimiter);
                throw new ParserException(
                        String.format("Expected ',' or '%s' after %s", expectedDelimiter, contextName),
                        tokens.getCurrentToken());
            }

            if (tokens.isCurrentToken(TokenType.COMMA)) {
                context.consumeCurrentToken(TokenType.COMMA);
            }
        }

        return expressions;
    }

    /**
     * 🗃️ Parses a comma-separated list with custom parsing logic
     * 
     * This is a more flexible version that allows custom parsing functions
     * for cases like hash key-value pairs where we need special parsing logic.
     * 
     * @param context          The parsing context
     * @param parser           Function that parses a single item and returns
     *                         whether to continue
     * @param closingDelimiter The token that ends the list
     * @param contextName      Description for error messages
     * @param <T>              The type of items being parsed
     * @return List of parsed items
     */
    public static <T> List<T> parseCustomList(
            ParsingContext context,
            Function<ParsingContext, T> parser,
            TokenType closingDelimiter,
            String contextName) {

        List<T> items = new ArrayList<>();
        TokenStream tokens = context.getTokenStream();

        if (tokens.isCurrentToken(closingDelimiter)) {
            return items;
        }

        while (!tokens.isCurrentToken(closingDelimiter)) {
            T item = parser.apply(context);
            items.add(item);

            if (!tokens.isCurrentToken(TokenType.COMMA) && !tokens.isCurrentToken(closingDelimiter)) {
                String expectedDelimiter = getDelimiterName(closingDelimiter);
                throw new ParserException(
                        String.format("Expected ',' or '%s' after %s", expectedDelimiter, contextName),
                        tokens.getCurrentToken());
            }

            if (tokens.isCurrentToken(TokenType.COMMA)) {
                context.consumeCurrentToken(TokenType.COMMA);
            }
        }

        return items;
    }

    /**
     * 🔤 Gets human-readable name for delimiter tokens
     */
    private static String getDelimiterName(TokenType delimiter) {
        switch (delimiter) {
            case RPAREN:
                return ")";
            case RBRACKET:
                return "]";
            case RBRACE:
                return "}";
            default:
                return delimiter.toString();
        }
    }
}