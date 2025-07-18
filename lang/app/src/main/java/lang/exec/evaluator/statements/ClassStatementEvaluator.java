package lang.exec.evaluator.statements;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lang.ast.literals.FunctionLiteral;
import lang.ast.statements.ClassStatement;
import lang.ast.statements.ClassStatement.MethodDefinition;
import lang.exec.base.BaseObject;
import lang.exec.evaluator.base.EvaluationContext;
import lang.exec.evaluator.base.NodeEvaluator;
import lang.exec.objects.*;
import lang.exec.validator.ObjectValidator;

/**
 * 🏛️ ClassStatementEvaluator - Class Definition Evaluator 🏛️
 * 
 * Evaluates class definitions to create ClassObject instances.
 * 
 * From first principles, class evaluation involves:
 * 1. Create class environment for lexical scoping
 * 2. Resolve parent class if inheritance is used
 * 3. Create constructor function object
 * 4. Create method function objects
 * 5. Create ClassObject with all components
 * 6. Register class in current environment
 */
public class ClassStatementEvaluator implements NodeEvaluator<ClassStatement> {

    private static record ParentClassResolution(Optional<ClassObject> parentClass, Optional<ErrorObject> error) {
    }

    @Override
    public BaseObject evaluate(ClassStatement node, Environment env, EvaluationContext context) {
        String className = node.getName().getValue();

        if (env.containsVariableLocally(className)) {
            return new ErrorObject("Class '" + className + "' already defined in this scope");
        }

        var parentClassResolution = resolveParentClass(node, env);
        if (parentClassResolution.error().isPresent()) {
            return parentClassResolution.error().get();
        }

        Environment classEnv = new Environment(env, false);

        Optional<FunctionObject> constructor = Optional.empty();
        if (node.hasConstructor()) {
            constructor = Optional.of(createFunctionObject(node.getConstructor().get(), classEnv));
        }

        Map<String, FunctionObject> methods = createMethods(node, classEnv);
        ClassObject classObj = new ClassObject(className,
                parentClassResolution.parentClass(),
                constructor,
                methods,
                classEnv);

        env.defineVariable(className, classObj);

        return classObj;
    }

    /**
     * 🔄 Checks for circular inheritance
     */
    private boolean wouldCreateCircularInheritance(String newClassName, ClassObject parentClass) {
        ClassObject current = parentClass;
        while (current != null) {
            if (current.getName().equals(newClassName)) {
                return true;
            }
            current = current.getParentClass().orElse(null);
        }
        return false;
    }

    /**
     * 🔄 Resolves the parent class for a class definition
     */
    private ParentClassResolution resolveParentClass(ClassStatement node, Environment env) {
        String className = node.getName().getValue();
        Optional<ClassObject> parentClass = Optional.empty();
        Optional<ErrorObject> error = Optional.empty();
        if (node.hasParentClass()) {
            Optional<BaseObject> parentObj = env.resolveVariable(node.getParentClass().get().getValue());
            if (parentObj.isEmpty()) {
                error = Optional
                        .of(new ErrorObject("Parent class '" + node.getParentClass().get().getValue() + "' not found"));
            }
            if (!ObjectValidator.isClass(parentObj.get())) {
                error = Optional.of(new ErrorObject("'" + node.getParentClass().get().getValue() + "' is not a class"));
            }
            parentClass = Optional.of((ClassObject) parentObj.get());

            // Check for circular inheritance
            if (wouldCreateCircularInheritance(className, (ClassObject) parentObj.get())) {
                error = Optional.of(new ErrorObject("Circular inheritance detected: " + className +
                        " cannot extend " + node.getParentClass().get().getValue()));
            }
        }

        return new ParentClassResolution(parentClass, error);
    }

    /**
     * 🔧 Creates a FunctionObject from a FunctionLiteral
     */
    private FunctionObject createFunctionObject(FunctionLiteral funcLiteral, Environment classEnv) {
        return new FunctionObject(classEnv, funcLiteral.getParameters(), funcLiteral.getBody());
    }

    /**
     * 🔧 Creates a map of method function objects from a class definition
     */
    private Map<String, FunctionObject> createMethods(ClassStatement node, Environment classEnv) {
        Map<String, FunctionObject> methods = new HashMap<>();
        for (MethodDefinition methodDef : node.getMethods()) {
            String methodName = methodDef.name().getValue();
            FunctionObject methodObj = createFunctionObject(methodDef.function(), classEnv);
            methods.put(methodName, methodObj);
        }
        return methods;
    }
}