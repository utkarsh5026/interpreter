package lang.ast.statements;

import lang.ast.base.Node;
import lang.ast.base.Statement;
import lang.token.TokenPosition;
import lang.ast.visitor.AstVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 📋 Program - The Complete Code Document 📋
 * 
 * This class represents an entire program or script in the Abstract Syntax
 * Tree.
 * Think of it as a folder containing all your code statements in order! 📂✨
 * 
 * A Program is like:
 * - A recipe with multiple steps 👨‍🍳📝
 * - A to-do list with tasks 📋✅
 * - A book with chapters (statements) 📚📖
 * 
 * Every piece of code you write gets parsed into a Program containing
 * all the statements that make up your code. It's the top-level container! 🏠🌟
 * 
 * Example program structure:
 * ```
 * let x = 5; // Statement 1
 * if (x > 0) { // Statement 2
 * print("positive");
 * }
 * return x; // Statement 3
 * ```
 */
public class Program implements Node {

    private final List<Statement> statements; // 📋 The list of all statements in this program

    /**
     * 🆕 Creates a new empty program
     * 
     * Like starting with a blank document - ready to add statements!
     * Perfect for building programs step by step. 📄✨
     */
    public Program() {
        this.statements = new ArrayList<>();
    }

    /**
     * 🆕 Creates a program with existing statements
     * 
     * Like importing a list of instructions into your program.
     * Takes a pre-built list of statements and makes them part of this program!
     * 📥📋
     * 
     * @param statements The list of statements to include in this program 📝
     */
    public Program(List<Statement> statements) {
        this.statements = new ArrayList<>(statements);
    }

    /**
     * 📋 Gets all statements in this program
     * 
     * Returns a read-only view of all the statements that make up this program.
     * Like getting a table of contents for your code! 📚🔍
     * 
     * The returned list is immutable - you can't accidentally modify it.
     * This keeps your program structure safe! 🔒✅
     * 
     * @return An unmodifiable list of all statements 📋
     */
    public List<Statement> getStatements() {
        return Collections.unmodifiableList(statements);
    }

    /**
     * ➕ Adds a new statement to this program
     * 
     * Like adding a new line of code to your program.
     * The statement gets appended to the end of the current list! 📝➕
     * 
     * Examples of what you might add:
     * - Variable declarations: let x = 5 📦
     * - If statements: if (condition) {...} 🔀
     * - Function calls: print("hello") 📞
     * - Return statements: return result ↩️
     * 
     * @param statement The statement to add to this program ⚡
     */
    public void addStatement(Statement statement) {
        this.statements.add(statement);
    }

    /**
     * 🏷️ Gets the literal text of the first statement
     * 
     * Returns the token literal of the very first statement in the program.
     * Like reading the first word of a document to get context! 📖👀
     * 
     * If the program is empty, returns an empty string.
     * This helps identify what kind of program this is at a glance.
     * 
     * Examples:
     * - If first statement is "let x = 5": returns "let"
     * - If first statement is "if (x > 0)": returns "if"
     * - If program is empty: returns ""
     * 
     * @return The literal text of the first statement, or empty string 📝
     */
    @Override
    public String tokenLiteral() {
        if (!statements.isEmpty()) {
            return statements.get(0).tokenLiteral();
        }
        return "";
    }

    /**
     * 📄 Converts the entire program to readable text
     * 
     * Creates a string representation of the whole program by joining
     * all statements with newlines. Like printing out your entire code! 🖨️📋
     * 
     * Each statement gets its own line, making it easy to read and debug.
     * Perfect for:
     * - Debugging 🐛
     * - Code generation 🏭
     * - Pretty printing 🎨
     * - Understanding program structure 🔍
     * 
     * Example output:
     * ```
     * let x = 5
     * if (x > 0) {
     * print("positive")
     * }
     * return x
     * ```
     * 
     * @return A formatted string representation of the entire program 📝
     */
    @Override
    public String toString() {
        return statements.stream()
                .map(Statement::toString)
                .collect(Collectors.joining("\n"));
    }

    /**
     * 📍 Gets the position of the first statement
     * 
     * Returns the line and column where this program begins in the source code.
     * Like finding the starting coordinates of your code! 🗺️📌
     * 
     * If the program has statements, returns the position of the first one.
     * If the program is empty, returns position (1, 1) as a default.
     * 
     * This is super useful for:
     * - Error reporting: "Error in program starting at line 1" 🚨
     * - Code navigation: Jump to program start 🧭
     * - Debugging: Know where your program begins 🔍
     * 
     * @return Position information of the program start 📍
     */
    @Override
    public TokenPosition position() {
        if (!statements.isEmpty()) {
            return statements.get(0).position();
        }
        return new TokenPosition(1, 1);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitProgram(this);
    }
}
