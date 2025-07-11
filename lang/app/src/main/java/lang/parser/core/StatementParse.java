package lang.parser.core;

import lang.ast.base.Statement;

/**
 * Interface for statement parsers.
 * 
 * @param <T> The type of statement to parse
 */
@FunctionalInterface
public interface StatementParse {
    /**
     * Parses a statement from the current position.
     * 
     * @param context The parsing context containing the current state
     * @return The parsed statement
     */
    Statement parseStatement(ParsingContext context);
}
