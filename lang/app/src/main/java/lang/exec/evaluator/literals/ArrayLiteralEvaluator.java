package lang.exec.evaluator.literals;

import java.util.List;

import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.base.BaseObject;

import lang.exec.validator.ObjectValidator;
import lang.exec.evaluator.base.EvaluationContext;
import lang.ast.literals.ArrayLiteral;
import lang.exec.objects.ArrayObject;
import lang.exec.objects.env.Environment;

public class ArrayLiteralEvaluator implements NodeEvaluator<ArrayLiteral> {

    @Override
    public BaseObject evaluate(ArrayLiteral node, Environment env, EvaluationContext context) {
        List<BaseObject> elements = context.evaluateExpressions(node.getElements(), env);

        for (BaseObject element : elements) {
            if (ObjectValidator.isError(element)) {
                return element;
            }
        }
        return new ArrayObject(elements);
    }
}
