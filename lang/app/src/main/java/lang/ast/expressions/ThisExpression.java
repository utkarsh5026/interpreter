package lang.ast.expressions;

import lang.ast.base.Expression;
import lang.ast.visitor.AstVisitor;
import lang.token.Token;

/**
 * 👆 ThisExpression - Current Instance Reference AST Node 👆
 * 
 * Represents the `this` keyword which refers to the current object instance.
 * 
 * From first principles, `this` provides:
 * - Reference to the current object instance
 * - Access to instance variables and methods
 * - Disambiguation when parameter names shadow instance variables
 * - Proper binding context for method calls
 * 
 * Examples:
 * ```
 * this.name = name; // Set instance variable
 * this.speak(); // Call instance method
 * return this; // Return current instance (method chaining)
 * ```
 */
public class ThisExpression extends Expression {

    public ThisExpression(Token token) {
        super(token);
    }

    @Override
    public String toString() {
        return "this";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitThisExpression(this);
    }
}