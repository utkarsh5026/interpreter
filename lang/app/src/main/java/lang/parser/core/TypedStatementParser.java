package lang.parser.core;

import lang.ast.base.Statement;

/**
 * Parser interface for statement parsing.
 * Each statement type has its own parser implementation.
 */
public interface TypedStatementParser<T extends Statement> {
    /**
     * Checks if this parser can handle the current token.
     */
    boolean canParse(ParsingContext context);

    /**
     * Parses the statement from the current position.
     */
    T parse(ParsingContext context) throws ParserException;
}