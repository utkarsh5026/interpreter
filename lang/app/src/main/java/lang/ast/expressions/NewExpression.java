package lang.ast.expressions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lang.ast.base.Expression;
import lang.ast.visitor.AstVisitor;
import lang.token.Token;

/**
 * 🆕 NewExpression - Object Instantiation AST Node 🆕
 * 
 * Represents creating a new instance of a class using the `new` keyword.
 * 
 * From first principles, object instantiation involves:
 * - The class to instantiate
 * - Arguments to pass to the constructor
 * - Creation of a new object instance
 * - Calling the constructor with the provided arguments
 * 
 * Examples:
 * ```
 * new Animal("Lion")
 * new Dog("Rex", "German Shepherd")
 * new Vehicle() // No arguments
 * ```
 */
public class NewExpression extends Expression {
    private final Expression className; // 🏷️ Class to instantiate
    private final List<Expression> arguments; // 📋 Constructor arguments

    public NewExpression(Token token, Expression className, List<Expression> arguments) {
        super(token);
        this.className = className;
        this.arguments = new ArrayList<>(arguments);
    }

    /**
     * 🏷️ Gets the class expression (usually an Identifier)
     */
    public Expression getClassName() {
        return className;
    }

    /**
     * 📋 Gets the constructor arguments
     */
    public List<Expression> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    /**
     * 🔢 Gets the number of arguments
     */
    public int getArgumentCount() {
        return arguments.size();
    }

    /**
     * ✅ Checks if any arguments were provided
     */
    public boolean hasArguments() {
        return !arguments.isEmpty();
    }

    @Override
    public String toString() {
        String args = arguments.stream()
                .map(Expression::toString)
                .collect(Collectors.joining(", "));

        return String.format("new %s(%s)", className.toString(), args);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitNewExpression(this);
    }
}