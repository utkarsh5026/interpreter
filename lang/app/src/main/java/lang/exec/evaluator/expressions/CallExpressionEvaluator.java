package lang.exec.evaluator.expressions;

import java.util.List;
import java.util.stream.IntStream;

import lang.ast.base.Identifier;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;
import lang.exec.objects.*;
import lang.exec.objects.env.Environment;
import lang.exec.objects.error.ErrorObject;
import lang.exec.objects.functions.BuiltinObject;
import lang.exec.objects.functions.FunctionObject;
import lang.exec.validator.ObjectValidator;
import lang.token.TokenPosition;
import lang.ast.expressions.CallExpression;
import lang.ast.utils.*;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.debug.StackFrame;

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
                var functionObject = ObjectValidator.asFunction(function);
                String message = String.format("Wrong number of arguments. Expected %d, got %d",
                        functionObject.getParameters().size(), args.size());

                return context.createError(message, node.getFunction().position());
            }
            return applyFunction(function, args, env, context, node);
        }

        return new ErrorObject("Not a function: " + function.type());

    }

    private BaseObject applyFunction(BaseObject function, List<BaseObject> args, Environment env,
            EvaluationContext context, CallExpression caller) {
        TokenPosition functionPos = caller.getFunction().position();
        if (ObjectValidator.isBuiltin(function)) {
            return applyBuiltinFunction(function, args, context, functionPos);
        }

        if (ObjectValidator.isFunction(function)) {
            return applyUserFunction(function, args, env, context, caller);
        }

        return context.createError("Not a function: " + function.type(), functionPos);
    }

    /**
     * Applies a builtin function to the given arguments and returns the result.
     */
    private BaseObject applyBuiltinFunction(
            BaseObject function,
            List<BaseObject> args,
            EvaluationContext context,
            TokenPosition functionPos) {
        BuiltinObject builtin = ObjectValidator.asBuiltin(function);
        String functionName = builtin.getName();
        context.enterFunction(functionName, functionPos, StackFrame.FrameType.BUILTIN);

        try {
            var result = builtin.getFunction().apply(args.toArray(new BaseObject[0]));
            if (ObjectValidator.isError(result)) {
                ErrorObject error = ObjectValidator.asError(result);
                String message = String.format("Error in evaluation of the builtin function %s: %s",
                        functionName, error.getMessage());
                return context.createError(message, error.getPosition().orElse(functionPos));
            }
            return result;
        } finally {
            context.exitFunction();
        }
    }

    /**
     * Applies a user function to the given arguments and returns the result.
     */
    private BaseObject applyUserFunction(
            BaseObject function,
            List<BaseObject> args,
            Environment env,
            EvaluationContext context,
            CallExpression caller) {
        FunctionObject functionObject = ObjectValidator.asFunction(function);

        String functionName = determineFunctionName(caller);
        context.enterFunction(functionName, caller.getFunction().position(), StackFrame.FrameType.USER_FUNCTION);

        try {
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

        } finally {
            context.exitFunction();
        }
    }

    /**
     * Determines the name of the function to be used in the stack trace.
     */
    private String determineFunctionName(CallExpression node) {
        if (AstValidator.isIdentifier(node.getFunction())) {
            return AstCaster.asIdentifier(node.getFunction()).getValue();
        }
        return "<anonymous>";
    }
}
