package lang.exec.objects.classes;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.objects.error.ErrorObject;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.base.ObjectType;
import lang.exec.objects.env.Environment;
import lang.exec.objects.env.EnvironmentFactory;
import lang.ast.base.Identifier;
import lang.ast.statements.BlockStatement;

/**
 * 👨‍💻 UserDefinedMethod - Methods Defined in Source Code 👨‍💻
 * 
 * These methods come from parsed AST and have actual parameter lists and body
 * statements.
 * This is what we had before with FunctionObject, but now it's specifically for
 * user code.
 */
public class UserDefinedMethod extends MethodObject {
    private final List<Identifier> parameters;
    private final BlockStatement body;

    public UserDefinedMethod(String name, List<Identifier> parameters, BlockStatement body, Environment environment) {
        super(name, "user-defined method", environment);
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public BaseObject call(InstanceObject instance, BaseObject[] arguments, EvaluationContext context,
            Function<Environment, Environment> extendEnv) {
        var error = validateArgumentCount(arguments, parameters.size());
        if (error.isPresent())
            return context.createError(error.get().getMessage(), null);

        Environment methodEnv = EnvironmentFactory.createFunctionScope(environment);
        methodEnv = extendEnv.apply(methodEnv); // Apply the environment extension function
        methodEnv.defineVariable("this", instance);

        for (int i = 0; i < parameters.size(); i++) {
            String paramName = parameters.get(i).getValue();
            methodEnv.defineVariable(paramName, arguments[i]);
        }

        return context.evaluate(body, methodEnv);
    }

    @Override
    public List<String> getParameterNames() {
        return parameters.stream().map(Identifier::getValue).toList();
    }

    @Override
    public int getParameterCount() {
        return parameters.size();
    }

    public List<Identifier> getParameters() {
        return parameters;
    }

    public BlockStatement getBody() {
        return body;
    }
}