package lang.ast.literals;

import lang.ast.base.Expression;
import lang.token.Token;
import lang.ast.visitor.AstVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 🎯 FStringLiteral - Formatted String with Embedded Expressions 🎯
 * 
 * Represents an f-string literal like: f"Hello {name}, you are {age} years
 * old!"
 * 
 * F-strings contain alternating static text and dynamic expressions:
 * - Static parts: ["Hello ", ", you are ", " years old!"]
 * - Expressions: [name, age]
 * 
 * The structure is: static[0] + expr[0] + static[1] + expr[1] + ... + static[n]
 * There's always one more static part than expressions (can be empty strings).
 * 
 * Examples:
 * - f"Hello {name}!" → static=["Hello ", "!"], expressions=[name]
 * - f"{x} + {y} = {x + y}" → static=["", " + ", " = ", ""], expressions=[x, y,
 * x+y]
 * - f"Just text" → static=["Just text"], expressions=[]
 */
public class FStringLiteral extends Expression {
    private final List<String> actualStrings;
    private final List<Expression> expressions;

    public FStringLiteral(Token token, List<String> actualStrings, List<Expression> expressions) {
        super(token);
        this.actualStrings = new ArrayList<>(actualStrings);
        this.expressions = new ArrayList<>(expressions);

        // Validate structure: static parts should be expressions.size() + 1
        if (actualStrings.size() != expressions.size() + 1) {
            throw new IllegalArgumentException(
                    String.format("Invalid f-string structure: %d static parts for %d expressions",
                            actualStrings.size(), expressions.size()));
        }
    }

    /**
     * 📋 Gets the static text parts of the f-string
     * 
     * These are the literal text segments that don't change.
     * 
     * @return Unmodifiable list of static text segments
     */
    public List<String> getActualStrings() {
        return Collections.unmodifiableList(actualStrings);
    }

    /**
     * ⚡ Gets the dynamic expressions to be interpolated
     * 
     * These expressions are evaluated and their results inserted into the string.
     * 
     * @return Unmodifiable list of expressions
     */
    public List<Expression> getExpressions() {
        return Collections.unmodifiableList(expressions);
    }

    /**
     * 🔢 Gets the number of expressions in this f-string
     * 
     * @return Number of dynamic expressions
     */
    public int getExpressionCount() {
        return expressions.size();
    }

    /**
     * ❓ Checks if this f-string has any dynamic expressions
     * 
     * @return True if there are expressions to interpolate, false if just static
     *         text
     */
    public boolean hasExpressions() {
        return !expressions.isEmpty();
    }

    /**
     * 🔍 Gets a specific static part by index
     * 
     * @param index Index of the static part (0 to actualStrings.size() - 1)
     * @return The static text at the given index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public String getStaticPart(int index) {
        return actualStrings.get(index);
    }

    /**
     * ⚡ Gets a specific expression by index
     * 
     * @param index Index of the expression (0 to expressions.size() - 1)
     * @return The expression at the given index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public Expression getExpression(int index) {
        return expressions.get(index);
    }

    @Override
    public String toString() {
        if (expressions.isEmpty()) {
            return String.format("f\"%s\"", actualStrings.get(0));
        }

        StringBuilder sb = new StringBuilder("f\"");

        for (int i = 0; i < expressions.size(); i++) {
            sb.append(actualStrings.get(i));
            sb.append("{").append(expressions.get(i).toString()).append("}");
        }

        // Add the final static part
        sb.append(actualStrings.get(actualStrings.size() - 1));
        sb.append("\"");

        return sb.toString();
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFStringLiteral(this);
    }

    /**
     * 📊 Gets debug information about the f-string structure
     * 
     * @return Formatted string showing the internal structure
     */
    public String getDebugInfo() {
        return String.format("FStringLiteral{actualStrings=%s, expressions=%s}",
                actualStrings,
                expressions.stream().map(Object::toString).collect(Collectors.toList()));
    }
}