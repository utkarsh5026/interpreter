package lang.ast.statements;

import lang.ast.base.Statement;
import lang.ast.base.Expression;
import lang.ast.base.Identifier;
import lang.token.Token;

/**
 * Represents a const statement: const x = 5;
 */
public class ConstStatement extends Statement {
    private final Identifier name;
    private final Expression value;

    public ConstStatement(Token token, Identifier name, Expression value) {
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
        return String.format("const %s = %s;",
                name.toString(),
                value.toString());
    }
}