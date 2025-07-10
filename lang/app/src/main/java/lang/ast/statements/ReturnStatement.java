package lang.ast.statements;

import lang.ast.base.Statement;
import lang.ast.base.Expression;
import lang.token.Token;

public class ReturnStatement extends Statement {
    private final Expression returnValue;

    public ReturnStatement(Token token, Expression returnValue) {
        super(token);
        this.returnValue = returnValue;
    }

    public Expression getReturnValue() {
        return returnValue;
    }

    @Override
    public String toString() {
        return String.format("%s %s;",
                tokenLiteral(),
                returnValue.toString());
    }

}
