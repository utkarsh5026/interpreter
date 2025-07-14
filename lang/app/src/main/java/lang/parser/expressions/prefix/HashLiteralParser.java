package lang.parser.expressions.prefix;

import java.util.Set;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import lang.parser.interfaces.PrefixExpressionParser;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.core.ParsingContext;
import lang.ast.base.Expression;
import lang.ast.literals.HashLiteral;
import lang.ast.utils.*;
import lang.token.*;
import lang.parser.precedence.Precedence;
import lang.parser.error.ParserException;

/**
 * 🗃️ HashLiteralParser - Object/Map Construction Specialist 🗃️
 * 
 * Handles hash literal expressions that create key-value mappings.
 * Hashes (also called objects, maps, or dictionaries) store data as key-value
 * pairs.
 * 
 * Examples:
 * - {"name": "Alice", "age": 30} (person object)
 * - {"x": 10, "y": 20} (coordinate object)
 * - {1: "one", 2: "two"} (integer keys)
 * - {} (empty hash)
 * - {"nested": {"inner": "value"}} (nested hashes)
 * - {"func": getValue(), "calc": 2 + 3} (expression values)
 * 
 * Parsing process:
 * 1. Current token is LBRACE {
 * 2. Parse key-value pairs separated by commas
 * 3. Each pair: key : value
 * 4. Keys must be literals (strings or integers)
 * 5. Values can be any expression
 * 6. Expect RBRACE } to close
 */
public class HashLiteralParser implements PrefixExpressionParser {

    private final ExpressionParser expressionParser;

    public HashLiteralParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public Expression parsePrefix(ParsingContext context) {
        Token leftBraceToken = context.consumeCurrentToken(TokenType.LBRACE);
        Map<String, Expression> pairs = new LinkedHashMap<>();

        if (context.getTokenStream().isCurrentToken(TokenType.RBRACE)) {
            context.consumeCurrentToken(TokenType.RBRACE);
            return new HashLiteral(leftBraceToken, pairs);
        }

        while (!context.getTokenStream().isCurrentToken(TokenType.RBRACE)) {
            if (context.getTokenStream().isCurrentToken(TokenType.COMMA)) {
                context.consumeCurrentToken(TokenType.COMMA);
            }

            Expression key = expressionParser.parseExpression(context, Precedence.LOWEST);
            String keyString = getKeyString(key).orElseThrow(
                    () -> new ParserException(
                            "Invalid key type key must be a string or integer like '1' or '\"name\"'"));

            context.consumeCurrentToken(TokenType.COLON);

            Expression value = expressionParser.parseExpression(context, Precedence.LOWEST);
            pairs.put(keyString, value);

            if (!context.getTokenStream().isCurrentToken(TokenType.COMMA)
                    && !context.getTokenStream().isCurrentToken(TokenType.RBRACE)) {
                throw new ParserException("Expected ',' or '}'");
            }
        }

        context.consumeCurrentToken(TokenType.RBRACE);
        return new HashLiteral(leftBraceToken, pairs);
    }

    /**
     * 🔑 Converts key expression to string representation
     */
    private Optional<String> getKeyString(Expression key) {
        if (AstValidator.isStringLiteral(key)) {
            return Optional.of(AstCaster.asStringLiteral(key).getValue());
        } else if (AstValidator.isIntegerLiteral(key)) {
            return Optional.of(String.valueOf(AstCaster.asIntegerLiteral(key).getValue()));
        }
        return Optional.empty();
    }

    @Override
    public Set<TokenType> getHandledTokenTypes() {
        return Set.of(TokenType.LBRACE);
    }
}
