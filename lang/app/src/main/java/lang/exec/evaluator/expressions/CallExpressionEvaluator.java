package lang.exec.evaluator.expressions;

import java.util.List;
import java.util.stream.IntStream;

import lang.ast.base.Identifier;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;
import lang.exec.objects.Environment;
import lang.exec.objects.FunctionObject;
import lang.exec.validator.ObjectValidator;

import lang.ast.expressions.CallExpression;
import lang.exec.evaluator.base.EvaluationContext;

import lang.exec.objects.ErrorObject;

public class CallExpressionEvaluator implements NodeEvaluator<CallExpression> {

    @Override
    public BaseObject evaluate(CallExpression node, Environment env, EvaluationContext context) {

        BaseObject function = context.evaluate(node.getFunction(), env);
        if (ObjectValidator.isError(function)) {
            return function;
        }

        List<BaseObject> args = context.evaluateExpressions(node.getArguments(), env);

        boolean isFunction = ObjectValidator.isFunction(function);
        boolean isBuiltin = ObjectValidator.isBuiltin(function);

        if (isFunction || isBuiltin) {
            if (isFunction && args.size() != ObjectValidator.asFunction(function).getParameters().size()) {
                return new ErrorObject("Wrong number of arguments. Expected "
                        + ObjectValidator.asFunction(function).getParameters().size() + ", got " + args.size());
            }
            return applyFunction(function, args, env, context);
        }

        return new ErrorObject("Not a function: " + function.type());

    }

    private BaseObject applyFunction(BaseObject function, List<BaseObject> args, Environment env,
            EvaluationContext context) {

        if (ObjectValidator.isBuiltin(function)) {
            return ObjectValidator.asBuiltin(function).getFunction().apply(args.toArray(new BaseObject[0]));
        }

        if (!ObjectValidator.isFunction(function)) {
            return new ErrorObject("Not a function: " + function.type());
        }

        FunctionObject functionObject = ObjectValidator.asFunction(function);
        Environment extendedEnv = new Environment(functionObject.getEnvironment(), false);
        List<Identifier> parameters = functionObject.getParameters();

        IntStream.range(0, parameters.size())
                .forEach(i -> extendedEnv.defineVariable(
                        parameters.get(i).getValue(),
                        args.get(i)));

        BaseObject result = context.evaluate(functionObject.getBody(), extendedEnv);

        if (ObjectValidator.isError(result)) {
            return result;
        }

        if (ObjectValidator.isReturnValue(result)) {
            return ObjectValidator.asReturnValue(result).getValue();
        }

        return result;
    }
}
