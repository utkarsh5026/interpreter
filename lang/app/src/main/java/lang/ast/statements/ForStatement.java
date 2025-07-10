package lang.ast.statements;

import lang.ast.base.Statement;
import lang.ast.base.Expression;
import lang.token.Token;

/**
 * Represents a for statement: for (init; condition; update) { body }
 */
public class ForStatement extends Statement {
    private final Statement initializer;
    private final Expression condition;
    private final Expression increment;
    private final BlockStatement body;

    public ForStatement(Token token, Statement initializer, Expression condition,
            Expression increment, BlockStatement body) {
        super(token);
        this.initializer = initializer;
        this.condition = condition;
        this.increment = increment;
        this.body = body;
    }

    public Statement getInitializer() {
        return initializer;
    }

    public Expression getCondition() {
        return condition;
    }

    public Expression getIncrement() {
        return increment;
    }

    public BlockStatement getBody() {
        return body;
    }

    @Override
    public String toString() {
        return String.format("for (%s; %s; %s) {\n%s\n}",
                initializer.toString(),
                condition.toString(),
                increment.toString(),
                body.toString());
    }

}