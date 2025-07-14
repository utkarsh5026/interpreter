package lang.parser.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lang.token.Token;
import lang.token.TokenType;

/**
 * 🚨 ErrorReporter - The Code Problem Detective 🚨
 * 
 * Collects and manages parsing errors that occur when analyzing code.
 * Think of it as a detective that keeps track of all the problems found!
 * 🕵️‍♂️📝
 * 
 * When parsing code, many things can go wrong:
 * - Missing semicolons: let x = 5 (oops, no semicolon!) 😱
 * - Unexpected tokens: if x = 5 (should be if x == 5) 🤔
 * - Malformed expressions: 2 + + 3 (extra plus!) ➕❌
 * 
 * Instead of stopping at the first error, this reporter collects them all
 * so you can fix multiple issues at once! 🛠️📋
 * 
 * Example usage:
 * ```
 * ErrorReporter reporter = new ErrorReporter();
 * reporter.addError("Missing semicolon", token);
 * reporter.addTokenError(TokenType.SEMICOLON, actualToken);
 * 
 * if (reporter.hasErrors()) {
 * reporter.printErrors(); // Show all problems found
 * }
 * ```
 */
public class ErrorReporter {
    private final List<ParseError> errors = new ArrayList<>(); // 📋 List of all errors found

    /**
     * 📝 Adds a custom error message with token context
     * 
     * Records a parsing error with a custom message and the problematic token.
     * Like writing down what went wrong and where it happened! 📝🎯
     * 
     * @param message A descriptive error message explaining the problem 💬
     * @param token   The token where the error occurred 🎫
     */
    public void addError(String message, Token token) {
        errors.add(new ParseError(message, token));
    }

    /**
     * 🎯 Adds a token expectation error
     * 
     * Records an error when we expected one type of token but got another.
     * Like expecting a comma but finding a semicolon! 🤷‍♂️
     * 
     * Example: Expected SEMICOLON, got COMMA
     * 
     * @param expected The token type we were expecting 🎯
     * @param actual   The token we actually found 🎫
     */
    public void addTokenError(TokenType expected, Token actual) {
        String message = String.format("Expected %s, got %s", expected, actual.type());
        addError(message, actual);
    }

    /**
     * 🚫 Adds a prefix parser error
     * 
     * Records an error when we can't find a way to parse a token at the start of an
     * expression.
     * Like trying to start a sentence with a word that doesn't make sense! 🤯
     * 
     * Example: No prefix parser for SEMICOLON (you can't start an expression with
     * ;)
     * 
     * @param tokenType The token type that can't be used as a prefix 🚫
     * @param token     The problematic token 🎫
     */
    public void addPrefixError(TokenType tokenType, Token token) {
        String message = String.format("No prefix parser for %s", tokenType);
        addError(message, token);
    }

    /**
     * 📋 Gets all collected errors
     * 
     * Returns a read-only list of all parsing errors found so far.
     * Like getting a complete report of all the problems! 📊📋
     * 
     * The list is immutable to prevent accidental modifications.
     * 
     * @return An unmodifiable list of all parse errors 📋
     */
    public List<ParseError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * ❓ Checks if any errors have been recorded
     * 
     * Returns true if there are any parsing errors, false if everything is clean.
     * Like asking "Did we find any problems?" 🤔✅❌
     * 
     * Perfect for conditional logic:
     * ```
     * if (reporter.hasErrors()) {
     * System.out.println("Found problems!");
     * } else {
     * System.out.println("All good!");
     * }
     * ```
     * 
     * @return True if errors exist, false if no errors 🎯
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * 🧹 Clears all recorded errors
     * 
     * Removes all errors from the list, starting fresh.
     * Like erasing the whiteboard to start over! 🧹📋
     * 
     * Useful when you want to reuse the same reporter for multiple parsing
     * sessions.
     */
    public void clear() {
        errors.clear();
    }

    /**
     * 🖨️ Prints all errors to standard error
     * 
     * Outputs all collected errors to the console for debugging.
     * Like showing a complete error report to the user! 📄🖨️
     * 
     * Each error is printed with its position and message:
     * ```
     * Parse Error at line 5, column 10: Expected SEMICOLON, got COMMA
     * Parse Error at line 8, column 3: No prefix parser for RBRACE
     * ```
     * 
     * Perfect for debugging and user feedback!
     */
    public void printErrors() {
        for (ParseError error : errors) {
            System.err.println(error);
        }
    }
}
