package lang.exec.objects.functions;

import java.util.List;

import lang.ast.statements.BlockStatement;
import lang.ast.base.Identifier;
import lang.exec.objects.base.*;
import lang.exec.objects.env.Environment;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.validator.ObjectValidator;

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

    /**
     * Executes the function with the given arguments and returns the result.
     */
    public BaseObject execute(BaseObject[] arguments, EvaluationContext context) {
        Environment extendedEnv = new Environment(environment, false);

        if (parameters.size() != arguments.length) {
            return context.createError(String.format("Wrong number of arguments. Expected %d, got %d",
                    parameters.size(), arguments.length), body.position());
        }

        for (int i = 0; i < parameters.size(); i++) {
            String paramName = parameters.get(i).getValue();
            extendedEnv.defineVariable(paramName, arguments[i]);
        }

        BaseObject result = context.evaluate(body, extendedEnv);

        if (ObjectValidator.isError(result)) {
            return result;
        }

        if (ObjectValidator.isReturnValue(result)) {
            return ObjectValidator.asReturnValue(result).getValue();
        }

        return result;
    }

}
