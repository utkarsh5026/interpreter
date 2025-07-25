package lang.exec.evaluator.literals;

import java.util.List;

import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.validator.ObjectValidator;
import lang.ast.literals.ArrayLiteral;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.exec.objects.structures.ArrayObject;

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
