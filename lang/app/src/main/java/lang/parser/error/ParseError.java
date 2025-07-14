package lang.parser.error;

import lang.token.Token;
import lang.token.TokenPosition;

/**
 * 🐛 ParseError - The Detailed Problem Report 🐛
 * 
 * Represents a single parsing error with complete context information.
 * Like a detailed bug report that tells you exactly what went wrong and where!
 * 📋🎯
 * 
 * Each ParseError contains:
 * - 💬 A descriptive message explaining the problem
 * - 📍 The exact position in the source code
 * - 🎫 The token that caused the issue
 * 
 * Example scenarios:
 * - "Expected semicolon at line 5, column 12" 🎯
 * - "Unexpected token 'else' at line 10, column 3" 😱
 * - "Missing closing brace at line 15, column 8" 🚫
 * 
 * This makes debugging much easier because you know exactly where to look! 🔍✨
 */
public class ParseError {
    private final String message; // 💬 What went wrong
    private final TokenPosition position; // 📍 Where it happened
    private final Token token; // 🎫 The problematic token

    /**
     * 🏗️ Creates a new parse error
     * 
     * Packages up all the information about a parsing problem.
     * Like creating a detailed incident report! 📋🎯
     * 
     * @param message A clear description of what went wrong
     * @param token   The token that caused the problem (provides position info)
     */
    public ParseError(String message, Token token) {
        this.message = message;
        this.token = token;
        this.position = token.position();
    }

    /**
     * 💬 Gets the error message
     * 
     * Returns a human-readable description of what went wrong.
     * Like reading the summary of a bug report! 📖💬
     * 
     * Examples:
     * - "Expected SEMICOLON, got COMMA"
     * - "No prefix parser for RBRACE"
     * - "Unexpected end of input"
     * 
     * @return The error message string 💬
     */
    public String getMessage() {
        return message;
    }

    /**
     * 📍 Gets the error position
     * 
     * Returns the exact line and column where the error occurred.
     * 
     * @return The position information (line and column) 📍
     */
    public TokenPosition getPosition() {
        return position;
    }

    /**
     * 🎫 Gets the problematic token
     * 
     * Returns the token that caused this parsing error.
     * Like getting the evidence from a crime scene! 🔍🎫
     * 
     * The token contains:
     * - Its type (SEMICOLON, IDENTIFIER, etc.)
     * - Its literal value ("hello", "42", etc.)
     * - Its position in the source code
     * 
     * @return The token that caused the error 🎫
     */
    public Token getToken() {
        return token;
    }

    /**
     * 📄 Converts the error to a readable string
     * Format: "Parse Error at line X, column Y: MESSAGE"
     * 
     * Examples:
     * - "Parse Error at line 5, column 12: Expected SEMICOLON, got COMMA"
     * - "Parse Error at line 8, column 3: No prefix parser for RBRACE"
     * 
     * Perfect for console output, log files, and user feedback!
     * 
     * @return A formatted error string with position and message 📝
     */
    @Override
    public String toString() {
        return String.format("Parse Error at %s: %s", position, message);
    }
}
