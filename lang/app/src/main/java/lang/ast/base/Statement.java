package lang.ast.base;

import lang.token.Token;
import lang.token.TokenPosition;

/**
 * ⚡ Statement - Code That Performs Actions ⚡
 * 
 * Base class for all statement nodes in the Abstract Syntax Tree.
 * Statements are pieces of code that DO things rather than produce values! 🎬🎯
 * 
 * Think of statements as "commands" or "instructions":
 * - "Store 5 in variable x" 📦➡️
 * - "If condition is true, do this" 🔀
 * - "Return this value" ↩️
 * - "Print hello world" 🖨️
 * 
 * Key difference from expressions:
 * - Expressions ASK questions and get answers 🤔💡
 * - Statements GIVE commands and cause actions ⚡🎬
 * 
 * Examples of statements:
 * - Variable declarations: let x = 5 📝
 * - Assignments: x = 10 ➡️📦
 * - Control flow: if, while, for 🔄🔀
 * - Function returns: return result ↩️
 * - Function calls (when used as statements): print("hello") 📞
 */
public abstract class Statement implements Node {
    protected final Token token; // 🎫 The original token that created this statement

    /**
     * 🏗️ Creates a new statement from a token
     * 
     * Every statement begins with a token from the lexer.
     * Like starting a recipe with the first ingredient! 👨‍🍳📋
     * 
     * @param token The token that represents this statement 🎫
     */
    protected Statement(Token token) {
        this.token = token;
    }

    /**
     * 🏷️ Gets the literal text of this statement
     * 
     * Returns the original keyword or symbol that started this statement.
     * Like reading the action word that tells you what this statement does! 📖⚡
     * 
     * Examples:
     * - For let statement: "let"
     * - For if statement: "if"
     * - For return statement: "return"
     * - For function statement: "fn"
     * 
     * @return The literal text from the source code 📝
     */
    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    /**
     * 📍 Gets the source code position of this statement
     * 
     * Returns exactly where this statement begins in your code.
     * Essential for pinpointing issues and understanding code flow! 🗺️🎯
     * 
     * When you see "Error on line 23", this is what provides that info!
     * 
     * @return Position information (line and column) 📍
     */
    @Override
    public TokenPosition position() {
        return token.position();
    }
}
