package lang.ast.statements;

import lang.ast.base.Statement;
import lang.token.Token;
import lang.ast.visitor.AstVisitor;

public class ContinueStatement extends Statement {

    public ContinueStatement(Token token) {
        super(token);
    }

    @Override
    public String toString() {
        return "continue;";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitContinueStatement(this);
    }
}