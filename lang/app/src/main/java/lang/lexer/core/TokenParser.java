package lang.lexer.core;

import lang.token.Token;

/**
 * 🎯 TokenParser - Contract for all token parsers 🎯
 * 
 * From first principles, every parser needs to:
 * 1. Determine if it can handle the current character
 * 2. Parse and return the appropriate token
 * 
 * This interface allows the lexer to delegate parsing to
 * specialized components without knowing their implementation details.
 */
public interface TokenParser {

    /**
     * 🔍 Checks if this parser can handle the current character
     */
    boolean canParse(char ch);

    /**
     * 🎯 Parses and returns a token
     * 
     * Precondition: canParse(stream.current()) returns true
     * Postcondition: stream is positioned after the parsed token
     */
    Token parse();
}