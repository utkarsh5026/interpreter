package lang.ast.statements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lang.ast.base.Identifier;
import lang.ast.base.Statement;
import lang.ast.literals.FunctionLiteral;
import lang.ast.visitor.AstVisitor;
import lang.token.Token;

/**
 * 🏛️ ClassStatement - Class Definition AST Node 🏛️
 * 
 * Represents a class definition in the Abstract Syntax Tree.
 * 
 * From first principles, a class definition contains:
 * - Name of the class
 * - Optional parent class (for inheritance)
 * - Constructor method (special initialization function)
 * - Instance methods (functions that operate on instances)
 * 
 * Examples:
 * ```
 * class Animal {
 * constructor(name) {
 * this.name = name;
 * }
 * 
 * speak() {
 * return this.name + " makes a sound";
 * }
 * }
 * 
 * class Dog extends Animal {
 * constructor(name, breed) {
 * super(name);
 * this.breed = breed;
 * }
 * 
 * speak() {
 * return this.name + " barks";
 * }
 * }
 * ```
 */
public class ClassStatement extends Statement {
    private final Identifier name; // 🏷️ Class name
    private final Optional<Identifier> parentClass; // 🔗 Parent class for inheritance
    private final Optional<FunctionLiteral> constructor; // 🏗️ Constructor method
    private final List<MethodDefinition> methods; // 📋 Instance methods

    /**
     * 📦 Method definition container
     * 
     * Represents a method within a class definition.
     * Contains the method name and its function implementation.
     */
    public static record MethodDefinition(Identifier name, FunctionLiteral function) {
        @Override
        public String toString() {
            return String.format("%s%s", name.toString(), function.toString());
        }
    }

    public ClassStatement(Token token, Identifier name, Optional<Identifier> parentClass,
            Optional<FunctionLiteral> constructor, List<MethodDefinition> methods) {
        super(token);
        this.name = name;
        this.parentClass = parentClass;
        this.constructor = constructor;
        this.methods = new ArrayList<>(methods);
    }

    /**
     * 🏷️ Gets the class name
     */
    public Identifier getName() {
        return name;
    }

    /**
     * 🔗 Gets the parent class (if this class extends another)
     */
    public Optional<Identifier> getParentClass() {
        return parentClass;
    }

    /**
     * 🏗️ Gets the constructor method
     */
    public Optional<FunctionLiteral> getConstructor() {
        return constructor;
    }

    /**
     * 📋 Gets all instance methods
     */
    public List<MethodDefinition> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    /**
     * 🔍 Finds a specific method by name
     */
    public Optional<MethodDefinition> getMethod(String methodName) {
        return methods.stream()
                .filter(method -> method.name().getValue().equals(methodName))
                .findFirst();
    }

    /**
     * ✅ Checks if this class has inheritance
     */
    public boolean hasParentClass() {
        return parentClass.isPresent();
    }

    /**
     * ✅ Checks if this class has a constructor
     */
    public boolean hasConstructor() {
        return constructor.isPresent();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("class ").append(name.toString());

        if (parentClass.isPresent()) {
            sb.append(" extends ").append(parentClass.get().toString());
        }

        sb.append(" {\n");

        if (constructor.isPresent()) {
            sb.append("    constructor").append(constructor.get().toString()).append("\n");
        }

        for (MethodDefinition method : methods) {
            sb.append("    ").append(method.toString()).append("\n");
        }

        sb.append("}");

        return sb.toString();
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitClassStatement(this);
    }
}