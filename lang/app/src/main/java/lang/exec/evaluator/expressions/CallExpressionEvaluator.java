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
            return applyFunction(ObjectValidator.asFunction(function), args, env, context);
        }

        return new ErrorObject("Not a function: " + function.type());

    }

    private BaseObject applyFunction(FunctionObject function, List<BaseObject> args, Environment env,
            EvaluationContext context) {

        if (!ObjectValidator.isFunction(function)) {
            return new ErrorObject("Not a function: " + function.type());
        }

        Environment extendedEnv = new Environment(function.getEnvironment(), false);
        List<Identifier> parameters = function.getParameters();

        IntStream.range(0, parameters.size())
                .forEach(i -> extendedEnv.set(
                        parameters.get(i).getValue(),
                        args.get(i)));

        BaseObject result = context.evaluate(function.getBody(), extendedEnv);

        if (ObjectValidator.isError(result)) {
            return result;
        }

        if (ObjectValidator.isReturnValue(result)) {
            return ObjectValidator.asReturnValue(result).getValue();
        }

        return result;
    }
}
