package lang.exec.objects.classes;

import java.util.function.Function;

import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.base.ObjectType;
import lang.exec.objects.env.Environment;

public class InstanceBoundMethod implements BaseObject {

    private final MethodObject method;
    private final InstanceObject instance;

    public InstanceBoundMethod(MethodObject method, InstanceObject instance) {
        this.method = method;
        this.instance = instance;
    }

    public MethodObject getMethod() {
        return method;
    }

    public InstanceObject getInstance() {
        return instance;
    }

    public BaseObject call(BaseObject[] arguments, EvaluationContext context) {
        return method.call(instance, arguments, context, Function.identity()); // Use identity function as default
    }

    public BaseObject call(BaseObject[] arguments, EvaluationContext context,
            Function<Environment, Environment> extendEnv) {
        return method.call(instance, arguments, context, extendEnv);
    }

    @Override
    public ObjectType type() {
        return ObjectType.FUNCTION;
    }

    @Override
    public String inspect() {
        return String.format("<bound method %s of %s>", method.getName(), instance.getClassObject().getName());
    }

}
