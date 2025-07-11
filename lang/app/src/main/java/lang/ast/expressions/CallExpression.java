package lang.ast.expressions;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import lang.ast.base.Expression;
import lang.token.Token;
import lang.ast.visitor.AstVisitor;

public class CallExpression extends Expression {

    private final Expression function;
    private final List<Expression> arguments;

    public CallExpression(Token token, Expression function, List<Expression> arguments) {
        super(token);
        this.function = function;
        this.arguments = new ArrayList<>(arguments);
    }

    public Expression getFunction() {
        return function;
    }

    public List<Expression> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    @Override
    public String toString() {
        String args = arguments.stream()
                .map(Expression::toString)
                .collect(Collectors.joining(", "));

        return String.format("%s (%s)", function.toString(), args);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitCallExpression(this);
    }

}
