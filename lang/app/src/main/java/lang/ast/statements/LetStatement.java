package lang.ast.statements;

import lang.ast.base.Statement;
import lang.ast.base.Expression;
import lang.ast.base.Identifier;
import lang.token.Token;
import lang.ast.visitor.AstVisitor;

/**
 * Represents a let statement: let x = 5;
 */
public class LetStatement extends Statement {
    private final Identifier name;
    private final Expression value;

    public LetStatement(Token token, Identifier name, Expression value) {
        super(token);
        this.name = name;
        this.value = value;
    }

    public Identifier getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s %s = %s;",
                tokenLiteral(),
                name.toString(),
                value.toString());
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitLetStatement(this);
    }
}
