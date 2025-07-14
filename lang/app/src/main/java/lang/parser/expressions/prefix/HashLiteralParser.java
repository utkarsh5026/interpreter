package lang.parser.expressions.prefix;

import java.util.Set;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

import lang.parser.interfaces.PrefixExpressionParser;
import lang.parser.interfaces.ExpressionParser;
import lang.parser.core.ParsingContext;
import lang.ast.base.Expression;
import lang.ast.literals.HashLiteral;
import lang.ast.utils.*;
import lang.token.*;
import lang.parser.precedence.Precedence;
import lang.parser.error.ParserException;
import lang.parser.core.ListParsingUtils;

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
        System.out.println("Parsing hash literal: " + context.getTokenStream().getCurrentToken());
        Token leftBraceToken = context.consumeCurrentToken(TokenType.LBRACE, "Expected '{' at start of hash literal");
        Map<String, Expression> pairs = new LinkedHashMap<>();

        List<KeyValuePair> keyValuePairs = ListParsingUtils.parseCustomList(
                context,
                this::parseKeyValuePair,
                TokenType.RBRACE,
                "hash key-value pair");

        keyValuePairs.stream()
                .forEach(pair -> pairs.put(pair.key, pair.value));

        context.consumeCurrentToken(TokenType.RBRACE, "Expected '}' at end of hash literal");
        return new HashLiteral(leftBraceToken, pairs);
    }

    /**
     * 🔑 Parses a single key-value pair
     */
    private KeyValuePair parseKeyValuePair(ParsingContext context) {
        System.out.println("Parsing key-value pair: " + context.getTokenStream().getCurrentToken());
        Expression key = expressionParser.parseExpression(context, Precedence.LOWEST);
        String keyString = getKeyString(key).orElseThrow(
                () -> new ParserException(
                        "Invalid key type key must be a string or integer like '1' or '\"name\"'"));

        System.out.println("Key string: " + keyString + " " + context.getTokenStream().getCurrentToken());

        context.consumeCurrentToken(TokenType.COLON, "Expected ':' after key");
        Expression value = expressionParser.parseExpression(context, Precedence.LOWEST);

        return new KeyValuePair(keyString, value);
    }

    /**
     * 📦 Simple container for key-value pairs during parsing
     */
    private static class KeyValuePair {
        final String key;
        final Expression value;

        KeyValuePair(String key, Expression value) {
            this.key = key;
            this.value = value;
        }
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
