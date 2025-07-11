package lang.ast.statements;

import lang.ast.base.Statement;
import lang.ast.base.Expression;
import lang.token.Token;
import lang.ast.visitor.AstVisitor;

public class WhileStatement extends Statement {
    private final Expression condition;
    private final BlockStatement body;

    public WhileStatement(Token token, Expression condition, BlockStatement body) {
        super(token);
        this.condition = condition;
        this.body = body;
    }

    public Expression getCondition() {
        return condition;
    }

    public BlockStatement getBody() {
        return body;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("while (").append(condition.toString()).append(") {\n");

        for (Statement stmt : body.getStatements()) {
            sb.append("\t").append(stmt.toString()).append(";\n");
        }

        sb.append("\n}");
        return sb.toString();
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitWhileStatement(this);
    }
}
