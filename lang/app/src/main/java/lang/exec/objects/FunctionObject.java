package lang.exec.objects;

import java.util.List;

import lang.ast.statements.BlockStatement;
import lang.ast.base.Identifier;
import lang.exec.base.BaseObject;
import lang.exec.base.ObjectType;

/**
 * FunctionObject represents a function in the language.
 * 
 * This class is used to store functions and provide a way to inspect them.
 */
public class FunctionObject implements BaseObject {

    private final Environment environment;
    private final List<Identifier> parameters;
    private final BlockStatement body;

    public FunctionObject(Environment environment, List<Identifier> parameters, BlockStatement body) {
        this.environment = environment;
        this.parameters = parameters;
        this.body = body;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public List<Identifier> getParameters() {
        return parameters;
    }

    public BlockStatement getBody() {
        return body;
    }

    @Override
    public String inspect() {
        List<String> parameterStrings = parameters.stream().map(Identifier::toString).toList();
        return "fn(" + String.join(", ", parameterStrings) + ") {\n" + body.toString() + "\n}";
    }

    @Override
    public ObjectType type() {
        return ObjectType.FUNCTION;
    }

}
