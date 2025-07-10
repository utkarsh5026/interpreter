package lang.parser.core;

import lang.lexer.Lexer;
import lang.token.Token;
import lang.token.TokenType;

/**
 * 🎯 ParsingContext - The Parser's Control Center 🎯
 * 
 * The central coordination hub that holds all shared state for parsing
 * operations.
 * Think of it as mission control for parsing - everything flows through here!
 * 🚀🏢
 * 
 * This context manages:
 * - 🌊 Token stream navigation (what tokens we're looking at)
 * - 🚨 Error collection and reporting (what problems we've found)
 * - 📊 Operator precedence rules (math order of operations)
 * - 🔄 Loop depth tracking (for break/continue validation)
 * 
 * Like a skilled conductor orchestrating a symphony:
 * - Keeps track of where we are in the music (tokens) 🎼
 * - Notes any mistakes (errors) 📝
 * - Knows the rules of harmony (precedence) 🎵
 * - Manages nested sections (loops) 🔄
 * 
 * Example usage:
 * ```
 * ParsingContext context = new ParsingContext(lexer);
 * 
 * // Parse tokens
 * if (context.getTokens().isCurrentToken(TokenType.WHILE)) {
 * context.enterLoop(); // Track that we're in a loop
 * parseWhileStatement();
 * context.exitLoop();
 * }
 * 
 * // Check for problems
 * if (context.getErrors().hasErrors()) {
 * context.getErrors().printErrors();
 * }
 * ```
 */
public class ParsingContext {
    private final TokenStream tokens; // 🌊 Token navigation manager
    private final ErrorReporter errors; // 🚨 Error collection service
    private final PrecedenceTable precedenceTable; // 📊 Operator precedence rules
    private int loopDepth = 0; // 🔄 Current nesting level of loops

    /**
     * 🏗️ Creates a new parsing context from a lexer
     * 
     * Sets up the complete parsing environment with all necessary components.
     * Like setting up a fully equipped workshop before starting a project! 🛠️🏭
     * 
     * @param lexer The lexer that will provide tokens for parsing 🔍
     */
    public ParsingContext(Lexer lexer) {
        this.tokens = new TokenStream(lexer);
        this.errors = new ErrorReporter();
        this.precedenceTable = new PrecedenceTable();
    }

    /**
     * 🌊 Gets the token stream manager
     * 
     * Returns the TokenStream that handles token navigation and consumption.
     * Like getting access to the conveyor belt that feeds tokens to the parser!
     * 🏭➡️
     * 
     * Use this to:
     * - Check current and upcoming tokens 👀
     * - Advance through the token sequence ➡️
     * - Consume expected tokens 🎯
     * 
     * @return The TokenStream for token navigation 🌊
     */
    public TokenStream getTokens() {
        return tokens;
    }

    /**
     * 🚨 Gets the error reporter
     * 
     * Returns the ErrorReporter that collects all parsing problems.
     * Like getting access to the complaint department! 📋🚨
     * 
     * Use this to:
     * - Check if any errors occurred ❓
     * - Get detailed error information 📋
     * - Print error reports 🖨️
     * 
     * @return The ErrorReporter for error management 🚨
     */
    public ErrorReporter getErrors() {
        return errors;
    }

    /**
     * 📊 Gets the precedence table
     * 
     * Returns the PrecedenceTable that determines operator order of operations.
     * Like getting access to the math rulebook! 📐📚
     * 
     * Use this to:
     * - Determine operator precedence levels 📈
     * - Resolve expression parsing ambiguities 🤔
     * - Ensure correct evaluation order ✅
     * 
     * @return The PrecedenceTable for operator precedence 📊
     */
    public PrecedenceTable getPrecedenceTable() {
        return precedenceTable;
    }

    /**
     * 🔄 Enters a new loop level
     * 
     * Increments the loop depth counter when parsing enters a loop structure.
     * Like going down one level in a nested loop maze! 🏗️⬇️
     * 
     * Call this when starting to parse:
     * - While loops 🔄
     * - For loops 🔁
     * - Do-while loops 🔃
     * 
     * This is essential for validating break/continue statements!
     */
    public void enterLoop() {
        loopDepth++;
    }

    /**
     * 🔄 Exits the current loop level
     * 
     * Decrements the loop depth counter when parsing leaves a loop structure.
     * Like climbing back up one level in a nested loop maze! 🏗️⬆️
     * 
     * Call this when finishing parsing of any loop construct.
     * Must be balanced with enterLoop() calls!
     */
    public void exitLoop() {
        loopDepth--;
    }

    /**
     * ❓ Checks if currently inside any loop
     * 
     * Returns true if we're currently parsing inside one or more loops.
     * Like asking "Are we in a loop right now?" 🔄❓
     * 
     * This is crucial for validating:
     * - Break statements (only valid in loops) 🚪
     * - Continue statements (only valid in loops) ⏭️
     * 
     * Example usage:
     * ```
     * if (context.isInLoop()) {
     * // Break/continue are valid here
     * } else {
     * // Error: break/continue outside loop
     * }
     * ```
     * 
     * @return True if inside a loop, false otherwise ✅❌
     */
    public boolean isInLoop() {
        return loopDepth > 0;
    }

    /**
     * 🚨 Adds a custom error message
     * 
     * Convenience method to report parsing errors with context.
     * Like filing a complaint with the problem department! 📋🚨
     * 
     * This is a shortcut for `getErrors().addError(message, token)`.
     * 
     * @param message A clear description of what went wrong 💬
     * @param token   The token where the error occurred 🎫
     */
    public void addError(String message, Token token) {
        errors.addError(message, token);
    }

    /**
     * 🎯 Adds a token expectation error
     * 
     * Convenience method to report token mismatch errors.
     * Like saying "I expected a hammer but got a screwdriver!" 🔨🪛
     * 
     * This is a shortcut for `getErrors().addTokenError(expected, actual)`.
     * 
     * @param expected The token type we were expecting 🎯
     * @param actual   The token we actually found 🎫
     */
    public void addTokenError(TokenType expected, Token actual) {
        errors.addTokenError(expected, actual);
    }
}