package lang.ast.literals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lang.ast.base.Expression;
import lang.ast.base.Statement;
import lang.ast.base.Identifier;
import lang.ast.statements.BlockStatement;
import lang.token.Token;
import lang.ast.visitor.AstVisitor;

/**
 * Represents a function literal: fn(param1, param2) { body }
 */
public class FunctionLiteral extends Expression {
    private final List<Identifier> parameters;
    private final BlockStatement body;

    public FunctionLiteral(Token token, List<Identifier> parameters, BlockStatement body) {
        super(token);
        this.parameters = new ArrayList<>(parameters);
        this.body = body;
    }

    public List<Identifier> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public BlockStatement getBody() {
        return body;
    }

    public String getFunctionSignature() {
        String params = parameters.stream()
                .map(Identifier::toString)
                .collect(Collectors.joining(", "));

        return String.format("%s(%s)", tokenLiteral(), params);
    }

    @Override
    public String toString() {
        String statements = body.getStatements().stream()
                .map(Statement::toString)
                .collect(Collectors.joining("\n"));

        return String.format("%s { \n%s\n}", getFunctionSignature(), statements);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFunctionLiteral(this);
    }

}
