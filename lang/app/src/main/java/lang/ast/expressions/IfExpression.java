package lang.ast.expressions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lang.ast.base.Expression;
import lang.ast.statements.BlockStatement;
import lang.token.Token;

public class IfExpression extends Expression {
    private final List<Expression> conditions;
    private final List<BlockStatement> consequences;
    private final BlockStatement alternative;

    public IfExpression(Token token, List<Expression> conditions,
            List<BlockStatement> consequences, BlockStatement alternative) {
        super(token);
        this.conditions = new ArrayList<>(conditions);
        this.consequences = new ArrayList<>(consequences);
        this.alternative = alternative;
    }

    public List<Expression> getConditions() {
        return Collections.unmodifiableList(conditions);
    }

    public List<BlockStatement> getConsequences() {
        return Collections.unmodifiableList(consequences);
    }

    public BlockStatement getAlternative() {
        return alternative;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // First if condition
        sb.append("if ")
                .append(conditions.get(0).toString())
                .append(" ")
                .append(consequences.get(0).toString());

        // elif conditions
        for (int i = 1; i < conditions.size(); i++) {
            sb.append("elif ").append(conditions.get(i).toString())
                    .append(" ").append(consequences.get(i).toString());
        }

        // else
        if (alternative != null) {
            sb.append("else ").append(alternative.toString());
        }

        return sb.toString();
    }

}
