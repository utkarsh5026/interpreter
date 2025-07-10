package lang.ast.statements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lang.ast.base.Statement;
import lang.token.Token;

public class BlockStatement extends Statement {
    private final List<Statement> statements;

    public BlockStatement(Token token, List<Statement> statements) {
        super(token);
        this.statements = new ArrayList<>(statements);
    }

    public List<Statement> getStatements() {
        return Collections.unmodifiableList(statements);
    }

    @Override
    public String toString() {
        return statements.stream()
                .map(Statement::toString)
                .collect(Collectors.joining(""));
    }
}
