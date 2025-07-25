package lang.exec.evaluator.expressions;

import java.util.List;

import lang.exec.objects.base.BaseObject;
import lang.exec.objects.classes.InstanceBoundMethod;
import lang.exec.objects.env.Environment;
import lang.exec.objects.error.ErrorObject;
import lang.exec.objects.functions.*;
import lang.exec.validator.ObjectValidator;
import lang.token.TokenPosition;
import lang.ast.expressions.CallExpression;
import lang.ast.utils.*;
import lang.exec.debug.StackFrame;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;

public class CallExpressionEvaluator implements NodeEvaluator<CallExpression> {

    /**
     * Evaluates a call expression and returns the result.
     */
    @Override
    public BaseObject evaluate(CallExpression node, Environment env, EvaluationContext context) {

        BaseObject function = context.evaluate(node.getFunction(), env);
        if (ObjectValidator.isError(function)) {
            return function;
        }

        List<BaseObject> args = context.evaluateExpressions(node.getArguments(), env);
        return applyFunction(function, args, context, node);
    }

    /**
     * Applies a function to the given arguments and returns the result.
     */
    private BaseObject applyFunction(BaseObject function,
            List<BaseObject> args,
            EvaluationContext context,
            CallExpression caller) {
        var functionPos = caller.getFunction().position();

        if (ObjectValidator.isBuiltin(function)) {
            return applyBuiltinFunction(function, args, context, functionPos);
        }

        if (ObjectValidator.isFunction(function)) {
            return applyUserFunction(function, args, context, caller);
        }

        if (ObjectValidator.isInstanceBoundMethod(function)) {
            InstanceBoundMethod instanceBoundMethod = ObjectValidator.asInstanceBoundMethod(function);
            return applyClassMethod(instanceBoundMethod, args, context, caller);
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
            EvaluationContext context,
            CallExpression caller) {
        FunctionObject functionObject = ObjectValidator.asFunction(function);
        String functionName = determineFunctionName(caller);
        context.enterFunction(functionName, caller.getFunction().position(), StackFrame.FrameType.USER_FUNCTION);

        try {
            return functionObject.execute(args.toArray(new BaseObject[0]), context);
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

    /**
     * Applies a class method to the given arguments and returns the result.
     */
    private BaseObject applyClassMethod(InstanceBoundMethod method, List<BaseObject> args, EvaluationContext context,
            CallExpression caller) {
        BaseObject result = method.call(args.toArray(new BaseObject[0]), context);

        if (ObjectValidator.isError(result)) {
            return context.createError(ObjectValidator.asError(result).getMessage(), caller.getFunction().position());
        }

        if (ObjectValidator.isReturnValue(result)) {
            return ObjectValidator.asReturnValue(result).getValue();
        }
        return result;
    }
}
