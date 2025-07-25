package lang.classes;

// =============================================================================
// CLASS RUNTIME BEHAVIOR TESTS
// =============================================================================
// These tests focus on the runtime execution behavior of classes, including
// instance creation, method calls, inheritance behavior, and complex scenarios
// that require full evaluation of class-related expressions and statements.
// =============================================================================

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.provider.Arguments;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import lang.exec.evaluator.LanguageEvaluator;
import lang.exec.objects.*;
import lang.exec.objects.literals.BooleanObject;
import lang.exec.objects.literals.FloatObject;
import lang.exec.objects.literals.IntegerObject;
import lang.exec.objects.literals.StringObject;
import lang.exec.base.BaseObject;
import lang.parser.LanguageParser;
import lang.lexer.Lexer;

/**
 * 🚀 CLASS RUNTIME BEHAVIOR TESTS 🚀
 * 
 * From first principles, runtime testing validates:
 * 1. Class instantiation and object creation
 * 2. Method invocation and parameter passing
 * 3. Property access and modification
 * 4. Inheritance and method resolution
 * 5. Constructor behavior and initialization
 * 6. Super calls and parent method access
 * 7. Error handling during execution
 * 8. Performance characteristics
 */
@DisplayName("🚀 Class Runtime Behavior Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClassRuntimeBehaviorTests {

    private LanguageEvaluator evaluator;
    private Environment globalEnvironment;

    @BeforeEach
    void setUp() {
        evaluator = new LanguageEvaluator();
        globalEnvironment = new Environment(null, true);
    }

    @AfterEach
    void tearDown() {
        evaluator = null;
        globalEnvironment = null;
    }

    // =============================================================================
    // 1. BASIC RUNTIME EXECUTION TESTS
    // =============================================================================

    @Nested
    @DisplayName("⚡ Basic Runtime Execution")
    @Order(1)
    class BasicRuntimeTests {

        @Test
        @DisplayName("Should execute simple class definition")
        void shouldExecuteSimpleClassDefinition() {
            String program = """
                    class Person {
                        constructor(name) {
                            this.name = name;
                        }

                        getName() {
                            return this.name;
                        }
                    }

                    let person = new Person("Alice");
                    person.getName();
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof StringObject);
            assertEquals("Alice", ((StringObject) result).getValue());
        }

        @Test
        @DisplayName("Should handle class without constructor")
        void shouldHandleClassWithoutConstructor() {
            String program = """
                    class Utility {
                        staticMethod() {
                            return "utility function";
                        }
                    }

                    let util = new Utility();
                    util.staticMethod();
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof StringObject);
            assertEquals("utility function", ((StringObject) result).getValue());
        }

        @Test
        @DisplayName("Should manage instance properties correctly")
        void shouldManageInstancePropertiesCorrectly() {
            String program = """
                    class Counter {
                        constructor() {
                            this.count = 0;
                        }

                        increment() {
                            this.count = this.count + 1;
                            return this.count;
                        }

                        getCount() {
                            return this.count;
                        }
                    }

                    let counter = new Counter();
                    counter.increment();
                    counter.increment();
                    counter.getCount();
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof IntegerObject);
            assertEquals(2, ((IntegerObject) result).getValue());
        }

        @Test
        @DisplayName("Should handle multiple instances independently")
        void shouldHandleMultipleInstancesIndependently() {
            String program = """
                    class Account {
                        constructor(balance) {
                            this.balance = balance;
                        }

                        withdraw(amount) {
                            this.balance = this.balance - amount;
                            return this.balance;
                        }
                    }

                    let account1 = new Account(100);
                    let account2 = new Account(200);

                    account1.withdraw(30);
                    account2.withdraw(50);

                    # Return array with both balances
                    [account1.balance, account2.balance];
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof ArrayObject);
            ArrayObject array = (ArrayObject) result;
            assertEquals(2, array.getElements().size());
            assertEquals(70, ((IntegerObject) array.getElements().get(0)).getValue());
            assertEquals(150, ((IntegerObject) array.getElements().get(1)).getValue());
        }
    }

    // =============================================================================
    // 2. INHERITANCE RUNTIME TESTS
    // =============================================================================

    @Nested
    @DisplayName("🔗 Inheritance Runtime Behavior")
    @Order(2)
    class InheritanceRuntimeTests {

        @Test
        @DisplayName("Should execute inheritance with method overriding")
        void shouldExecuteInheritanceWithMethodOverriding() {
            String code = """
                    class Animal {
                        constructor(name) {
                            this.name = name;
                        }

                        speak() {
                            return this.name + " makes a sound";
                        }
                    }

                    class Dog extends Animal {
                        constructor(name) {
                            super(name);
                        }

                        speak() {
                            return this.name + " barks";
                        }
                    }

                    let dog = new Dog("Buddy");
                    dog.speak();
                    """;

            BaseObject result = executeProgram(code);

            assertTrue(result instanceof StringObject);
            assertEquals("Buddy barks", ((StringObject) result).getValue());
        }

        @Test
        @DisplayName("Should access inherited methods")
        void shouldAccessInheritedMethods() {
            String program = """
                    class Vehicle {
                        constructor(type) {
                            this.type = type;
                        }

                        getType() {
                            return this.type;
                        }

                        start() {
                            return "Starting " + this.type;
                        }
                    }

                    class Car extends Vehicle {
                        constructor(brand) {
                            super("car");
                            this.brand = brand;
                        }

                        getBrand() {
                            return this.brand;
                        }
                    }

                    let car = new Car("Toyota");
                    car.getType() + " - " + car.getBrand();
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof StringObject);
            assertEquals("car - Toyota", ((StringObject) result).getValue());
        }

        @Test
        @DisplayName("Should handle super constructor calls")
        void shouldHandleSuperConstructorCalls() {
            String program = """
                    class Person {
                        constructor(name, age) {
                            this.name = name;
                            this.age = age;
                        }

                        getInfo() {
                            return this.name + " is " + this.age + " years old";
                        }
                    }

                    class Student extends Person {
                        constructor(name, age, studentId) {
                            super(name, age);
                            this.studentId = studentId;
                        }

                        getStudentInfo() {
                            return this.getInfo() + " (ID: " + this.studentId + ")";
                        }
                    }

                    let student = new Student("John", 20, "S123");
                    student.getStudentInfo();
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof StringObject);
            assertEquals("John is 20 years old (ID: S123)", ((StringObject) result).getValue());
        }

        @Test
        @DisplayName("Should handle deep inheritance chains")
        void shouldHandleDeepInheritanceChains() {
            String program = """
                    class Level1 {
                        method1() { return "level1"; }
                    }

                    class Level2 extends Level1 {
                        method2() { return "level2"; }
                    }

                    class Level3 extends Level2 {
                        method3() { return "level3"; }
                    }

                    let obj = new Level3();
                    obj.method1() + "-" + obj.method2() + "-" + obj.method3();
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof StringObject);
            assertEquals("level1-level2-level3", ((StringObject) result).getValue());
        }

        @Test
        @DisplayName("Should handle method resolution in complex hierarchy")
        void shouldHandleMethodResolutionInComplexHierarchy() {
            String program = """
                    class A {
                        method() { return "A"; }
                    }

                    class B extends A {
                        method() { return "B"; }
                    }

                    class C extends B {
                        # Inherits B's version of method
                    }

                    class D extends C {
                        method() { return "D"; }
                    }

                    let d = new D();
                    d.method();
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof StringObject);
            assertEquals("D", ((StringObject) result).getValue());
        }
    }

    // =============================================================================
    // 3. CONSTRUCTOR AND INITIALIZATION TESTS
    // =============================================================================

    @Nested
    @DisplayName("🏗️ Constructor and Initialization")
    @Order(3)
    class ConstructorRuntimeTests {

        @Test
        @DisplayName("Should execute constructor with complex initialization")
        void shouldExecuteConstructorWithComplexInitialization() {
            String program = """
                    class Rectangle {
                        constructor(width, height) {
                            this.width = width;
                            this.height = height;
                            this.area = width * height;
                            this.perimeter = 2 * (width + height);
                        }

                        getArea() {
                            return this.area;
                        }

                        getPerimeter() {
                            return this.perimeter;
                        }
                    }

                    let rect = new Rectangle(5, 3);
                    rect.getArea();
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof IntegerObject);
            assertEquals(15, ((IntegerObject) result).getValue());
        }

        @Test
        @DisplayName("Should handle constructor parameter validation")
        void shouldHandleConstructorParameterValidation() {
            String program = """
                    class PositiveNumber {
                        constructor(value) {
                            if (value <= 0) {
                                this.value = 1;
                                this.valid = false;
                            } else {
                                this.value = value;
                                this.valid = true;
                            }
                        }

                        isValid() {
                            return this.valid;
                        }
                    }

                    let negative = new PositiveNumber(-5);
                    let positive = new PositiveNumber(10);

                    [negative.isValid(), positive.isValid()];
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof ArrayObject);
            ArrayObject array = (ArrayObject) result;
            assertEquals(2, array.getElements().size());
            assertFalse(((BooleanObject) array.getElements().get(0)).getValue());
            assertTrue(((BooleanObject) array.getElements().get(1)).getValue());
        }

        @Test
        @DisplayName("Should handle constructor side effects")
        void shouldHandleConstructorSideEffects() {
            String program = """
                    let constructorCallCount = 0;

                    class CountedClass {
                        constructor() {
                            constructorCallCount = constructorCallCount + 1;
                            this.instanceNumber = constructorCallCount;
                        }

                        getInstanceNumber() {
                            return this.instanceNumber;
                        }
                    }

                    let obj1 = new CountedClass();
                    let obj2 = new CountedClass();
                    let obj3 = new CountedClass();

                    [obj1.getInstanceNumber(), obj2.getInstanceNumber(), obj3.getInstanceNumber()];
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof ArrayObject);
            ArrayObject array = (ArrayObject) result;
            assertEquals(3, array.getElements().size());
            assertEquals(1, ((IntegerObject) array.getElements().get(0)).getValue());
            assertEquals(2, ((IntegerObject) array.getElements().get(1)).getValue());
            assertEquals(3, ((IntegerObject) array.getElements().get(2)).getValue());
        }

        @Test
        @DisplayName("Should handle constructor with default values")
        void shouldHandleConstructorWithDefaultValues() {
            String program = """
                    class Configuration {
                        constructor(name, timeout) {
                            this.name = name;
                            if (timeout) {
                                this.timeout = timeout;
                            } else {
                                this.timeout = 5000;  # default
                            }
                        }

                        getTimeout() {
                            return this.timeout;
                        }
                    }

                    let config1 = new Configuration("test", 3000);
                    let config2 = new Configuration("default", null);

                    [config1.getTimeout(), config2.getTimeout()];
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof ArrayObject);
            ArrayObject array = (ArrayObject) result;
            assertEquals(2, array.getElements().size());
            assertEquals(3000, ((IntegerObject) array.getElements().get(0)).getValue());
            assertEquals(5000, ((IntegerObject) array.getElements().get(1)).getValue());
        }
    }

    // =============================================================================
    // 4. METHOD INVOCATION AND PARAMETER TESTS
    // =============================================================================

    @Nested
    @DisplayName("📞 Method Invocation and Parameters")
    @Order(4)
    class MethodInvocationTests {

        @Test
        @DisplayName("Should handle methods with multiple parameters")
        void shouldHandleMethodsWithMultipleParameters() {
            String program = """
                    class Calculator {
                        add(a, b) {
                            return a + b;
                        }

                        multiply(x, y, z) {
                            return x * y * z;
                        }

                        average(numbers) {
                            let sum = 0;
                            let count = len(numbers);

                            for (let i = 0; i < count; i = i + 1) {
                                sum = sum + numbers[i];
                            }

                            return sum / count;
                        }
                    }

                    let calc = new Calculator();
                    calc.average([1, 2, 3, 4, 5]);
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof FloatObject);
            assertEquals(3.0, ((FloatObject) result).getValue());
        }

        @Test
        @DisplayName("Should handle method chaining")
        void shouldHandleMethodChaining() {
            String program = """
                    class StringBuilder {
                        constructor() {
                            this.value = "";
                        }

                        append(str) {
                            this.value = this.value + str;
                            return this;  # Return self for chaining
                        }

                        prepend(str) {
                            this.value = str + this.value;
                            return this;
                        }

                        toString() {
                            return this.value;
                        }
                    }

                    let sb = new StringBuilder();
                    sb.append("Hello").append(" ").append("World").toString();
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof StringObject);
            assertEquals("Hello World", ((StringObject) result).getValue());
        }

        @Test
        @DisplayName("Should handle recursive method calls")
        void shouldHandleRecursiveMethodCalls() {
            String program = """
                    class MathUtils {
                        factorial(n) {
                            if (n <= 1) {
                                return 1;
                            }
                            return n * this.factorial(n - 1);
                        }

                        fibonacci(n) {
                            if (n <= 1) {
                                return n;
                            }
                            return this.fibonacci(n - 1) + this.fibonacci(n - 2);
                        }
                    }

                    let math = new MathUtils();
                    math.factorial(5);
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof IntegerObject);
            assertEquals(120, ((IntegerObject) result).getValue());
        }

        @Test
        @DisplayName("Should handle methods that call other methods")
        void shouldHandleMethodsThatCallOtherMethods() {
            String program = """
                    class NumberProcessor {
                        isEven(n) {
                            return n % 2 == 0;
                        }

                        isOdd(n) {
                            return !this.isEven(n);
                        }

                        classifyNumbers(numbers) {
                            let even = [];
                            let odd = [];

                            for (let i = 0; i < len(numbers); i = i + 1) {
                                let num = numbers[i];
                                if (this.isEven(num)) {
                                    even = push(even, num);
                                } else {
                                    odd = push(odd, num);
                                }
                            }

                            return [even, odd];
                        }
                    }

                    let processor = new NumberProcessor();
                    let result = processor.classifyNumbers([1, 2, 3, 4, 5, 6]);
                    len(result[0]); // Count of even numbers
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof IntegerObject);
            assertEquals(3, ((IntegerObject) result).getValue()); // [2, 4, 6]
        }
    }

    // =============================================================================
    // 5. ERROR HANDLING RUNTIME TESTS
    // =============================================================================

    @Nested
    @DisplayName("🚨 Runtime Error Handling")
    @Order(5)
    class ErrorHandlingRuntimeTests {

        @Test
        @DisplayName("Should handle constructor argument mismatch")
        void shouldHandleConstructorArgumentMismatch() {
            String program = """
                    class Person {
                        constructor(name, age) {
                            this.name = name;
                            this.age = age;
                        }
                    }

                    new Person("Alice"); // Missing age argument
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof ErrorObject);
            ErrorObject error = (ErrorObject) result;
            assertTrue(error.getMessage().contains("argument") ||
                    error.getMessage().contains("parameter"));
        }

        @Test
        @DisplayName("Should handle method not found errors")
        void shouldHandleMethodNotFoundErrors() {
            String program = """
                    class TestClass {
                        existingMethod() {
                            return "exists";
                        }
                    }

                    let obj = new TestClass();
                    obj.nonExistentMethod();
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof ErrorObject);
            ErrorObject error = (ErrorObject) result;
            assertTrue(error.getMessage().contains("not found") ||
                    error.getMessage().contains("undefined"));
        }

        @Test
        @DisplayName("Should handle property access on null/undefined")
        void shouldHandlePropertyAccessOnNullUndefined() {
            String program = """
                    class TestClass {
                        getProperty() {
                            return this.undefinedProperty;
                        }
                    }

                    let obj = new TestClass();
                    obj.getProperty();
                    """;

            BaseObject result = executeProgram(program);

            // Should return null or handle gracefully
            assertTrue(result instanceof NullObject || result instanceof ErrorObject);
        }

        @Test
        @DisplayName("Should handle super calls on non-inherited classes")
        void shouldHandleSuperCallsOnNonInheritedClasses() {
            String program = """
                    class BaseClass {
                        constructor() {
                            super(); // Invalid - no parent class
                            this.value = 1;
                        }
                    }

                    new BaseClass();
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof ErrorObject);
            ErrorObject error = (ErrorObject) result;
            assertTrue(error.getMessage().contains("super") ||
                    error.getMessage().contains("parent"));
        }
    }

    // =============================================================================
    // 6. COMPLEX INTEGRATION SCENARIOS
    // =============================================================================

    @Nested
    @DisplayName("🔄 Complex Integration Scenarios")
    @Order(6)
    class ComplexIntegrationTests {

        @Test
        @DisplayName("Should handle class hierarchies with mixed features")
        void shouldHandleClassHierarchiesWithMixedFeatures() {
            String code = """
                    class EventEmitter {
                        constructor() {
                            this.listeners = [];
                        }

                        on(event, callback) {
                            this.listeners = push(this.listeners, [event, callback]);
                        }

                        emit(event, data) {
                            for (let i = 0; i < len(this.listeners); i = i + 1) {
                                let listener = this.listeners[i];
                                if (listener[0] == event) {
                                    # In real implementation, would call callback with data
                                    return data;
                                }
                            }
                            return data;
                        }
                    }

                    class Button extends EventEmitter {
                        constructor(label) {
                            super();
                            this.label = label;
                            this.clicked = false;
                        }

                        click() {
                            this.clicked = true;
                            return this.emit("click", this.label + " clicked");
                        }

                        getLabel() {
                            return this.label;
                        }
                    }

                    let button = new Button("Submit");
                    button.click();
                    """;

            BaseObject result = executeProgram(code);

            assertTrue(result instanceof StringObject);
            assertEquals("Submit clicked", ((StringObject) result).getValue());
        }

        @Test
        @DisplayName("Should handle polymorphic behavior")
        void shouldHandlePolymorphicBehavior() {
            String program = """
                    class Shape {
                        constructor(name) {
                            this.name = name;
                        }

                        area() {
                            return 0; # Default implementation
                        }

                        describe() {
                            return this.name + " has area " + this.area();
                        }
                    }

                    class Circle extends Shape {
                        constructor(radius) {
                            super("Circle");
                            this.radius = radius;
                        }

                        area() {
                            return 3.14 * this.radius * this.radius;
                        }
                    }

                    class Rectangle extends Shape {
                        constructor(width, height) {
                            super("Rectangle");
                            this.width = width;
                            this.height = height;
                        }

                        area() {
                            return this.width * this.height;
                        }
                    }

                    let shapes = [new Circle(5), new Rectangle(4, 6)];
                    shapes[1].describe();
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof StringObject);
            assertTrue(((StringObject) result).getValue().contains("Rectangle"));
            assertTrue(((StringObject) result).getValue().contains("24"));
        }

        @Test
        @DisplayName("Should handle factory patterns with classes")
        void shouldHandleFactoryPatternsWithClasses() {
            String program = """
                    class Animal {
                        constructor(name, sound) {
                            this.name = name;
                            this.sound = sound;
                        }

                        speak() {
                            return this.name + " says " + this.sound;
                        }
                    }

                    class AnimalFactory {
                        createDog(name) {
                            return new Animal(name, "woof");
                        }

                        createCat(name) {
                            return new Animal(name, "meow");
                        }

                        createCow(name) {
                            return new Animal(name, "moo");
                        }
                    }

                    let factory = new AnimalFactory();
                    let dog = factory.createDog("Buddy");
                    let cat = factory.createCat("Whiskers");

                    dog.speak() + " and " + cat.speak();
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof StringObject);
            assertEquals("Buddy says woof and Whiskers says meow",
                    ((StringObject) result).getValue());
        }

        @Test
        @DisplayName("Should handle nested class instantiation")
        void shouldHandleNestedClassInstantiation() {
            String program = """
                    class Container {
                        constructor() {
                            this.items = [];
                        }

                        addItem(item) {
                            this.items = push(this.items, item);
                            return this;
                        }

                        getCount() {
                            return len(this.items);
                        }
                    }

                    class ItemFactory {
                        createContainer() {
                            return new Container();
                        }

                        fillContainer(container, count) {
                            for (let i = 0; i < count; i = i + 1) {
                                container.addItem("item" + i);
                            }
                            return container;
                        }
                    }

                    let factory = new ItemFactory();
                    let container = factory.createContainer();
                    factory.fillContainer(container, 5);
                    container.getCount();
                    """;

            BaseObject result = executeProgram(program);

            assertTrue(result instanceof IntegerObject);
            assertEquals(5, ((IntegerObject) result).getValue());
        }
    }

    // =============================================================================
    // 7. PERFORMANCE AND STRESS RUNTIME TESTS
    // =============================================================================

    @Nested
    @DisplayName("⚡ Performance Runtime Tests")
    @Order(7)
    class PerformanceRuntimeTests {

        @Test
        @DisplayName("Should handle many instance creations efficiently")
        void shouldHandleManyInstanceCreationsEfficiently() {
            String program = """
                    class SimpleClass {
                        constructor(value) {
                            this.value = value;
                        }

                        getValue() {
                            return this.value;
                        }
                    }

                    let instances = [];
                    for (let i = 0; i < 1000; i = i + 1) {
                        let instance = new SimpleClass(i);
                        instances = push(instances, instance);
                    }

                    len(instances);
                    """;

            long startTime = System.currentTimeMillis();
            BaseObject result = executeProgram(program);
            long duration = System.currentTimeMillis() - startTime;

            assertTrue(result instanceof IntegerObject);
            assertEquals(1000, ((IntegerObject) result).getValue());
            assertTrue(duration < 10000, "Creating 1000 instances took too long: " + duration + "ms");
        }

        @Test
        @DisplayName("Should handle deep method call chains efficiently")
        void shouldHandleDeepMethodCallChainsEfficiently() {
            String program = """
                    class ChainClass {
                        constructor(value) {
                            this.value = value;
                        }

                        increment() {
                            this.value = this.value + 1;
                            return this;
                        }

                        getValue() {
                            return this.value;
                        }
                    }

                    let obj = new ChainClass(0);
                    for (let i = 0; i < 100; i = i + 1) {
                        obj.increment();
                    }
                    obj.getValue();
                    """;

            long startTime = System.currentTimeMillis();
            BaseObject result = executeProgram(program);
            long duration = System.currentTimeMillis() - startTime;

            assertTrue(result instanceof IntegerObject);
            assertEquals(100, ((IntegerObject) result).getValue());
            assertTrue(duration < 5000, "100 method calls took too long: " + duration + "ms");
        }
    }

    // =============================================================================
    // UTILITY METHODS
    // =============================================================================

    /**
     * Executes a complete program and returns the result of the last expression
     */
    private BaseObject executeProgram(String program) {
        try {
            Lexer lexer = new Lexer(program);
            LanguageParser parser = new LanguageParser(lexer);
            var ast = parser.parseProgram();

            BaseObject result = null;
            for (var statement : ast.getStatements()) {
                result = evaluator.evaluate(statement, globalEnvironment);
                if (result instanceof ErrorObject) {
                    return result; // Return error immediately
                }
            }

            return result != null ? result : NullObject.INSTANCE;
        } catch (Exception e) {
            return new ErrorObject("Execution failed: " + e.getMessage());
        }
    }

    /**
     * Data provider for parameterized tests
     */
    static Stream<Arguments> provideClassScenarios() {
        return Stream.of(
                Arguments.of("Simple class", "class Test { method() { return 42; } }"),
                Arguments.of("Inheritance", "class Child extends Parent { method() { return super.method(); } }"),
                Arguments.of("Constructor", "class Test { constructor(x) { this.x = x; } }"));
    }

    /**
     * Provides sample programs for stress testing
     */
    static Stream<String> provideStressTestPrograms() {
        return Stream.of(
                "class A {} for (let i = 0; i < 100; i = i + 1) { new A(); }",
                "class B { method() { return 1; } } let b = new B(); for (let i = 0; i < 100; i = i + 1) { b.method(); }",
                "class C extends B {} class D extends C {} new D();");
    }
}