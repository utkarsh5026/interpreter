package lang.ast.expressions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lang.ast.base.Expression;
import lang.ast.visitor.AstVisitor;
import lang.token.Token;

/**
 * ⬆️ SuperExpression - Parent Class Access AST Node ⬆️
 * 
 * Represents accessing methods or properties from a parent class using `super`.
 * 
 * From first principles, super access involves:
 * - Reference to the parent class
 * - Method name to call on parent
 * - Arguments for the parent method
 * - Proper `this` binding (current instance, parent method)
 * 
 * Examples:
 * ```
 * super(name) // Call parent constructor
 * super.speak() // Call parent method
 * super.move(distance) // Call parent method with arguments
 * ```
 */
public class SuperExpression extends Expression {
    private final Expression method; // 🏷️ Method to call on parent
    private final List<Expression> arguments; // 📋 Arguments for parent method

    public SuperExpression(Token token, Expression method, List<Expression> arguments) {
        super(token);
        this.method = method;
        this.arguments = new ArrayList<>(arguments);
    }

    /**
     * 🏷️ Gets the method expression
     */
    public Expression getMethod() {
        return method;
    }

    /**
     * 📋 Gets the arguments
     */
    public List<Expression> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    /**
     * ✅ Checks if this is a constructor call (super() with no method)
     */
    public boolean isConstructorCall() {
        return method == null;
    }

    @Override
    public String toString() {
        String args = arguments.stream()
                .map(Expression::toString)
                .collect(Collectors.joining(", "));

        if (isConstructorCall()) {
            return String.format("super(%s)", args);
        } else {
            return String.format("super.%s(%s)", method.toString(), args);
        }
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitSuperExpression(this);
    }
}