package lang.exec.evaluator.expressions;

import java.util.Optional;
import lang.exec.objects.classes.BaseObjectClass;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.ast.expressions.PrefixExpression;
import lang.exec.validator.ObjectValidator;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.classes.InstanceObject;
import lang.exec.objects.classes.MethodObject;
import lang.exec.objects.env.Environment;

/**
 * 🔄 PrefixExpressionEvaluator - Unary Operation Specialist (Enhanced with
 * Float Support) 🔄
 * 
 * Handles prefix (unary) operators with comprehensive support for both integers
 * and floats.
 * operations:
 * - Negation (-): Works on both int and float
 * - Logical NOT (!): Works on all types (uses truthiness)
 * 
 * Type preservation rules:
 * - -int → int
 * - -float → float
 * - !anything → boolean
 */
public class PrefixExpressionEvaluator implements NodeEvaluator<PrefixExpression> {

    @Override
    public BaseObject evaluate(PrefixExpression node, Environment env, EvaluationContext context) {
        BaseObject right = context.evaluate(node.getRight(), env);
        if (ObjectValidator.isError(right)) {
            return right;
        }

        Optional<BaseObject> result = tryUnaryDunderMethod(node.getOperator(), right, context);
        if (result.isPresent()) {
            return result.get();
        }

        return context.createError("Unknown operator: " + node.getOperator(), node.position());
    }

    /**
     * 🎯 Attempts to call unary dunder method on operand
     * 
     * From first principles, unary dunder method resolution:
     * 1. Check if operand is an instance object
     * 2. Look up the appropriate dunder method name for the operator
     * 3. Search for the method in the instance's class hierarchy
     * 4. If found, call the method with no arguments (unary operation)
     * 5. Return the result, or empty if no dunder method available
     */
    private Optional<BaseObject> tryUnaryDunderMethod(String operator, BaseObject operand,
            EvaluationContext context) {
        if (!ObjectValidator.isInstance(operand)) {
            return Optional.empty();
        }

        Optional<String> dunderMethodName = BaseObjectClass.getDunderMethodForUnaryOperator(operator);
        if (dunderMethodName.isEmpty()) {
            return Optional.empty();
        }

        InstanceObject instance = ObjectValidator.asInstance(operand);

        Optional<MethodObject> dunderMethod = instance.findMethod(dunderMethodName.get());
        if (dunderMethod.isEmpty()) {
            return Optional.empty();
        }

        BaseObject result = dunderMethod.get().call(
                instance,
                new BaseObject[] {},
                context,
                env -> env);

        if (ObjectValidator.isReturnValue(result)) {
            return Optional.of(ObjectValidator.asReturnValue(result).getValue());
        }

        return Optional.of(result);
    }
}
