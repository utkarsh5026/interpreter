package lang.ast.literals;

import lang.ast.visitor.AstVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lang.ast.base.Expression;
import lang.token.Token;

/**
 * Represents an array literal: [1, 2, 3], ["a", "b", "c"]
 */
public class ArrayLiteral extends Expression {
    private final List<Expression> elements;

    public ArrayLiteral(Token token, List<Expression> elements) {
        super(token);
        this.elements = new ArrayList<>(elements);
    }

    public List<Expression> getElements() {
        return Collections.unmodifiableList(elements);
    }

    @Override
    public String toString() {
        String elementsStr = elements.stream()
                .map(Expression::toString)
                .collect(Collectors.joining(", "));

        return String.format("[%s]", elementsStr);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitArrayLiteral(this);
    }
}
