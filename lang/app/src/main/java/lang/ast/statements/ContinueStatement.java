package lang.ast.statements;

import lang.ast.base.Statement;
import lang.token.Token;

public class ContinueStatement extends Statement {

    public ContinueStatement(Token token) {
        super(token);
    }

    @Override
    public String toString() {
        return "continue;";
    }

}