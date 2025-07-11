package lang.ast.statements;

import lang.ast.base.Statement;
import lang.token.Token;
import lang.ast.visitor.AstVisitor;

public class BreakStatement extends Statement {

    public BreakStatement(Token token) {
        super(token);
    }

    @Override
    public String toString() {
        return "break;";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitBreakStatement(this);
    }
}