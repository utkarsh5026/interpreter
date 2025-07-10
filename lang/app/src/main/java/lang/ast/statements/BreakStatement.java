package lang.ast.statements;

import lang.ast.base.Statement;
import lang.token.Token;

public class BreakStatement extends Statement {

    public BreakStatement(Token token) {
        super(token);
    }

    @Override
    public String toString() {
        return "break;";
    }

}