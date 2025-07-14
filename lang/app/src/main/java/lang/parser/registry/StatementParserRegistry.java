package lang.parser.registry;

import java.util.*;

import lang.parser.core.ParsingContext;
import lang.parser.core.StatementParse;
import lang.parser.interfaces.TypedStatementParser;
import lang.parser.parsers.expressions.LanguageExpressionParser;
import lang.parser.parsers.statements.*;
import lang.ast.base.Statement;

/**
 * StatementParserRegistry manages all statement parsers and coordinates
 * parsing.
 * 
 * This implements a registry pattern where different statement parsers
 * can be registered and the registry will find the appropriate parser
 * for each statement type.
 */
public class StatementParserRegistry implements StatementParse {
    private final List<TypedStatementParser<? extends Statement>> parsers = new ArrayList<>();
    private final LanguageExpressionParser expressionParser;
    private final ExpressionParserRegistry expressionParserRegistry;

    public StatementParserRegistry() {
        this.expressionParser = new LanguageExpressionParser(this);
        this.expressionParserRegistry = new ExpressionParserRegistry(this.expressionParser, this);
        registerDefaultParsers();
    }

    public ExpressionParserRegistry getExpressionParserRegistry() {
        return expressionParserRegistry;
    }

    public LanguageExpressionParser getExpressionParser() {
        return expressionParser;
    }

    /**
     * Registers all default statement parsers.
     * Order matters - more specific parsers should come first.
     */
    private void registerDefaultParsers() {
        parsers.add(new LetStatementParser(expressionParser));
        parsers.add(new ConstStatementParser(expressionParser));
        parsers.add(new ReturnStatementParser(expressionParser));

        parsers.add(new WhileStatementParser(expressionParser, this));
        parsers.add(new ForStatementParser(expressionParser, this));
        parsers.add(new BreakStatementParser());
        parsers.add(new ContinueStatementParser());

        parsers.add(new BlockStatementParser(this));
        parsers.add(new ExpressionStatementParser(expressionParser));
    }

    /**
     * Finds the appropriate parser for the current token.
     */
    public TypedStatementParser<? extends Statement> findParser(ParsingContext context) {
        return parsers.stream()
                .filter(parser -> parser.canParse(context))
                .findFirst()
                .orElse(null);
    }

    /**
     * Parses a statement using the appropriate parser.
     */
    public Statement parseStatement(ParsingContext context) {
        TypedStatementParser<? extends Statement> parser = findParser(context);

        if (parser == null) {
            context.addError("No parser found for token: " +
                    context.getTokenStream().getCurrentToken().type(),
                    context.getTokenStream().getCurrentToken());
            return null;
        }
        System.out.println("\nParser: " + parser.getClass().getName());

        return parser.parse(context);
    }

    /**
     * Adds a custom statement parser.
     */
    public void addParser(TypedStatementParser<? extends Statement> parser) {
        // Insert before expression statement parser (which should be last)
        int insertIndex = Math.max(0, parsers.size() - 1);
        parsers.add(insertIndex, parser);
    }

    /**
     * Removes a statement parser by type.
     */
    public void removeParser(Class<? extends TypedStatementParser<? extends Statement>> parserType) {
        parsers.removeIf(parser -> parser.getClass().equals(parserType));
    }
}
