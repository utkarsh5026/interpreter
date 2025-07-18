package lang.ast.expressions;

import lang.ast.base.Expression;
import lang.ast.visitor.AstVisitor;
import lang.token.Token;

/**
 * 🔗 PropertyExpression - Property Access AST Node 🔗
 * 
 * Represents accessing a property or method of an object using dot notation.
 * 
 * From first principles, property access involves:
 * - An object expression (what we're accessing on)
 * - A property name (what we're accessing)
 * - Resolution of the property in the object's class hierarchy
 * 
 * Examples:
 * ```
 * dog.name // Access instance variable
 * dog.speak() // Method call (parsed as property access + call)
 * this.energy // Access property on current instance
 * super.speak() // Access parent class method
 * ```
 */
public class PropertyExpression extends Expression {
    private final Expression object; // 🎯 Object being accessed
    private final Expression property; // 🏷️ Property name

    public PropertyExpression(Token token, Expression object, Expression property) {
        super(token);
        this.object = object;
        this.property = property;
    }

    /**
     * 🎯 Gets the object expression
     */
    public Expression getObject() {
        return object;
    }

    /**
     * 🏷️ Gets the property expression
     */
    public Expression getProperty() {
        return property;
    }

    @Override
    public String toString() {
        return String.format("%s.%s", object.toString(), property.toString());
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitPropertyExpression(this);
    }
}