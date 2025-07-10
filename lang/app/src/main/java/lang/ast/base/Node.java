package lang.ast.base;

import lang.token.TokenPosition;

/**
 * 🌳 Node - The Building Block of Code Trees 🌳
 * 
 * This is the foundation interface for all parts of an Abstract Syntax Tree
 * (AST).
 * Think of it as the blueprint for every piece of your code structure! 🏗️📋
 * 
 * An AST is like a family tree for your code - it shows how different parts
 * relate to each other in a hierarchical structure. 👨‍👩‍👧‍👦🌲
 * 
 * Examples of nodes:
 * - Variables like "x" or "userName" 📝
 * - Numbers like "42" or "3.14" 🔢
 * - Operations like "a + b" ➕
 * - Statements like "if (condition)" 🔀
 */
public interface Node {

    /**
     * 🏷️ Gets the original text that created this node
     * 
     * Returns the actual text from your source code that this node represents.
     * Like getting the original ingredient label from a recipe! 📋✨
     * 
     * Examples:
     * - For a number node: "42"
     * - For a variable: "userName"
     * - For a keyword: "if"
     * 
     * @return The literal text from the source code 📝
     */
    String tokenLiteral();

    /**
     * 📄 Converts this node into readable text
     * 
     * Creates a human-readable string representation of the node.
     * Like getting a summary description of what this piece of code does! 📖🔍
     * 
     * This is super useful for debugging and understanding your code structure.
     * 
     * @return A string description of this node 📝
     */
    String toString();

    /**
     * 📍 Gets the exact location of this node in your source code
     * 
     * Returns the line and column numbers where this node appears.
     * Like GPS coordinates for your code - tells you exactly where to find it!
     * 🗺️📌
     * 
     * This is invaluable for:
     * - Error reporting 🚨
     * - Debugging 🐛
     * - Code navigation 🧭
     * 
     * @return Position information (line and column numbers) 📍
     */
    TokenPosition position();
}
