package lang.parser.registry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import lang.parser.interfaces.PrefixExpressionParser;
import lang.parser.interfaces.InfixExpressionParser;
import lang.parser.interfaces.ExpressionParser;
import lang.token.TokenType;
import lang.parser.expressions.prefix.*;
import lang.parser.expressions.infix.*;
import lang.parser.core.StatementParse;

public class ExpressionParserRegistry {

    private final Map<TokenType, PrefixExpressionParser> prefixParsers = new ConcurrentHashMap<>();
    private final Map<TokenType, InfixExpressionParser> infixParsers = new ConcurrentHashMap<>();
    private final ExpressionParser expressionParser;
    private final StatementParse statementParser;

    public ExpressionParserRegistry(ExpressionParser expressionParser, StatementParse statementParser) {
        this.expressionParser = expressionParser;
        this.statementParser = statementParser;
        registerPrefixParsers();
        registerInfixParsers();
    }

    // 📋 Lists of registered parsers for management
    private final List<PrefixExpressionParser> registeredPrefixParsers = new ArrayList<>();
    private final List<InfixExpressionParser> registeredInfixParsers = new ArrayList<>();

    public void registerPrefixParser(PrefixExpressionParser parser) {
        registeredPrefixParsers.add(parser);

        for (TokenType tokenType : parser.getHandledTokenTypes()) {
            if (prefixParsers.containsKey(tokenType)) {
                throw new IllegalArgumentException(
                        String.format("Prefix parser for token type %s is already registered", tokenType));
            }
            prefixParsers.put(tokenType, parser);
        }
    }

    private void registerPrefixParsers() {
        registerPrefixParser(new IdentifierExpressionParser());
        registerPrefixParser(new IntegerLiteralParser());
        registerPrefixParser(new StringLiteralParser());
        registerPrefixParser(new BooleanLiteralParser());
        registerPrefixParser(new NullLiteralParser());
        registerPrefixParser(new PrefixOperatorParser(expressionParser));
        registerPrefixParser(new FunctionalLiteralParser(statementParser));
        registerPrefixParser(new HashLiteralParser(expressionParser));
        registerPrefixParser(new GroupedExpressionParser(expressionParser));
        registerPrefixParser(new ArrayLiteralParser(expressionParser));
        registerPrefixParser(new IfExpressionParser(expressionParser, statementParser));
    }

    private void registerInfixParsers() {
        registerInfixParser(new ArithmeticOperatorParser(expressionParser));
        registerInfixParser(new ComparisonOperatorParser(expressionParser));
        registerInfixParser(new LogicalOperatorParser(expressionParser));
        registerInfixParser(new AssignmentExpressionParser(expressionParser));
        registerInfixParser(new CallExpressionParser(expressionParser));
        registerInfixParser(new IndexExpressionParser(expressionParser));
    }

    public void registerInfixParser(InfixExpressionParser parser) {
        registeredInfixParsers.add(parser);

        for (TokenType tokenType : parser.getHandledTokenTypes()) {
            if (infixParsers.containsKey(tokenType)) {
                throw new IllegalArgumentException(
                        String.format("Infix parser for token type %s is already registered", tokenType));
            }
            infixParsers.put(tokenType, parser);
        }
    }

    public Optional<PrefixExpressionParser> getPrefixParser(TokenType tokenType) {
        return Optional.ofNullable(prefixParsers.get(tokenType));
    }

    public Optional<InfixExpressionParser> getInfixParser(TokenType tokenType) {
        return Optional.ofNullable(infixParsers.get(tokenType));
    }

    public boolean removePrefixParser(PrefixExpressionParser parser) {
        if (!registeredPrefixParsers.remove(parser)) {
            return false;
        }

        prefixParsers.entrySet().removeIf(entry -> entry.getValue() == parser);
        return true;
    }

    public boolean removeInfixParser(InfixExpressionParser parser) {
        if (!registeredInfixParsers.remove(parser)) {
            return false;
        }

        // Remove all token mappings for this parser
        infixParsers.entrySet().removeIf(entry -> entry.getValue() == parser);
        return true;
    }

    public void clear() {
        prefixParsers.clear();
        infixParsers.clear();
        registeredPrefixParsers.clear();
        registeredInfixParsers.clear();
    }

    /**
     * 🔍 Checks if a token type has a registered prefix parser
     */
    public boolean hasPrefixParser(TokenType tokenType) {
        return prefixParsers.containsKey(tokenType);
    }

    /**
     * ⚡ Checks if a token type has a registered infix parser
     */
    public boolean hasInfixParser(TokenType tokenType) {
        return infixParsers.containsKey(tokenType);
    }

    /**
     * 📋 Gets all supported prefix token types
     */
    public Set<TokenType> getSupportedPrefixTokens() {
        return Set.copyOf(prefixParsers.keySet());
    }

    /**
     * 📋 Gets all supported infix token types
     */
    public Set<TokenType> getSupportedInfixTokens() {
        return Set.copyOf(infixParsers.keySet());
    }
}
