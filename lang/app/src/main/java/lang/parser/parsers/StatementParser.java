package lang.parser.parsers;

import lang.ast.base.Statement;
import lang.parser.core.ParsingContext;

/**
 * Parser interface for statement parsing.
 * Each statement type has its own parser implementation.
 */
public interface StatementParser<T extends Statement> {
    /**
     * Checks if this parser can handle the current token.
     */
    boolean canParse(ParsingContext context);

    /**
     * Parses the statement from the current position.
     */
    T parse(ParsingContext context);
}