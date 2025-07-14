package lang.parser.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lang.token.Token;
import lang.token.TokenType;
import lang.lexer.debug.DebugColors;

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
 * ErrorReporter reporter = new ErrorReporter(inputLines, true);
 * reporter.addError("Missing semicolon", token);
 * reporter.addTokenError(TokenType.SEMICOLON, actualToken);
 * 
 * if (reporter.hasErrors()) {
 * reporter.printErrors(); // Show all problems found with colors! 🌈
 * }
 * ```
 */
public class ErrorReporter {
    private final List<ParseError> errors = new ArrayList<>();
    private final String[] inputLines;
    private final boolean useColors;

    public ErrorReporter(String[] inputLines) {
        this(inputLines, true); // Default to using colors
    }

    public ErrorReporter(String[] inputLines, boolean useColors) {
        this.inputLines = inputLines;
        this.useColors = useColors;
    }

    /**
     * 📝 Adds a custom error message with token context
     */
    public void addError(String message, Token token) {
        errors.add(new ParseError(message, token));
    }

    /**
     * 🎯 Adds a token expectation error
     * 
     * Records an error when we expected one type of token but got another.
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
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * 🧹 Clears all recorded errors
     */
    public void clear() {
        errors.clear();
    }

    /**
     * 🖨️ Prints all errors to standard error with beautiful colors! 🌈
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
     * Colors used:
     * - 🔍 Source line: Dimmed for context
     * - 👉 Error pointer (^): Bright red to highlight exact location
     * - 🚨 Error message: Red for clear error indication
     * - 📍 Line numbers: Yellow for easy reference
     * 
     * Perfect for debugging and user feedback!
     */
    public void printErrors() {
        System.err.println("\nErrors: ");

        for (ParseError error : errors) {
            Token token = error.getToken();
            int lineNum = token.position().line();
            int column = token.position().column();

            // Get the source line (with bounds checking)
            String sourceLine = "";
            if (lineNum > 0 && lineNum <= inputLines.length) {
                sourceLine = inputLines[lineNum - 1];
            }

            // Print line number and source line with subtle coloring
            String linePrefix = String.format("%s[Line %d]%s ",
                    DebugColors.colorize("", DebugColors.POSITION, useColors),
                    lineNum,
                    DebugColors.RESET);

            String dimmedSource = DebugColors.colorize(sourceLine, DebugColors.DIM, useColors);
            System.err.println(linePrefix + dimmedSource);

            String pointer = " ".repeat(Math.max(0, column)) + "^";
            String coloredPointer = DebugColors.colorize(pointer, DebugColors.ERROR, useColors);
            System.err.println(" ".repeat(
                    linePrefix.length() - (useColors ? DebugColors.POSITION.length() + DebugColors.RESET.length() : 0))
                    + coloredPointer);

            String errorMessage = "🚨 " + error.getMessage();
            String coloredMessage = DebugColors.colorize(errorMessage, DebugColors.ERROR, useColors);
            System.err.println(coloredMessage);

            System.err.println();
        }
    }
}
