package lang.exec.objects.classes;

import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.base.ObjectType;

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
        return method.call(instance, arguments, context);
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
