package lang.ast.base;

import lang.token.Token;
import lang.token.TokenPosition;

/**
 * 🧮 Expression - Code That Produces Values 🧮
 * 
 * Base class for all expression nodes in the Abstract Syntax Tree.
 * Expressions are pieces of code that calculate or produce values! 💡✨
 * 
 * Think of expressions as "questions that have answers":
 * - "What's 2 + 3?" → Answer: 5 🔢
 * - "What's in variable x?" → Answer: the value of x 📦
 * - "What does getName() return?" → Answer: a name string 📝
 * 
 * Examples of expressions:
 * - Numbers: 42, 3.14 🔢
 * - Variables: userName, age 📝
 * - Math: 2 + 3 * 4 ➕✖️
 * - Function calls: getName() 📞
 * - Comparisons: x > 10 ⚖️
 */
public abstract class Expression implements Node {
    protected final Token token; // 🎫 The original token that created this expression

    /**
     * 🏗️ Creates a new expression from a token
     * 
     * Every expression starts with a token from the lexer.
     * Like turning raw ingredients into a dish! 🍳🥘
     * 
     * @param token The token that represents this expression 🎫
     */
    protected Expression(Token token) {
        this.token = token;
    }

    /**
     * 🏷️ Gets the literal text of this expression
     * 
     * Returns the original text from your source code that created this expression.
     * Like reading the label on a jar to see what's inside! 🏺📋
     * 
     * Examples:
     * - For number expression: "42"
     * - For variable expression: "userName"
     * - For operator expression: "+"
     * 
     * @return The literal text from the source code 📝
     */
    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    /**
     * 📍 Gets the source code position of this expression
     * 
     * Returns exactly where this expression appears in your code.
     * Perfect for error messages and debugging! 🎯🔍
     * 
     * Example: "Error on line 15, column 8"
     * 
     * @return Position information (line and column) 📍
     */
    @Override
    public TokenPosition position() {
        return token.position();
    }
}