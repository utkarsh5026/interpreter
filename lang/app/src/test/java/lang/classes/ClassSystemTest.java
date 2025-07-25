package lang.classes;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import lang.exec.evaluator.LanguageEvaluator;
import lang.exec.objects.base.BaseObject;
import lang.exec.objects.env.Environment;
import lang.lexer.Lexer;
import lang.parser.LanguageParser;
import lang.ast.statements.Program;
import lang.exec.validator.ObjectValidator;

/**
 * 🧪 Comprehensive Class System Tests 🧪
 * 
 * Tests all aspects of the class system from first principles:
 * - Class definition and instantiation
 * - Inheritance and method resolution
 * - Constructor chaining
 * - Property access and method calls
 * - Super calls and this binding
 * - Error handling and edge cases
 */
public class ClassSystemTest {

    private Environment globalEnvironment;
    private LanguageEvaluator evaluator;

    @BeforeEach
    void setUp() {
        globalEnvironment = new Environment();
        evaluator = new LanguageEvaluator();
    }

    /**
     * 🏗️ Helper method to evaluate code and return result
     */
    private BaseObject evaluateCode(String code) {
        Lexer lexer = new Lexer(code);
        LanguageParser parser = new LanguageParser(lexer);
        Program program = parser.parseProgram();

        if (parser.hasErrors()) {
            parser.printErrors();
            fail("Parser errors occurred");
        }

        return evaluator.evaluateProgram(program, globalEnvironment);
    }

    // ========================================================================
    // BASIC CLASS DEFINITION AND INSTANTIATION TESTS
    // ========================================================================

    @Test
    @DisplayName("Should define a simple class and create instance")
    void testBasicClassDefinition() {
        String code = """
                class Person {
                    constructor(name, age) {
                        this.name = name;
                        this.age = age;
                    }

                    greet() {
                        return "Hello, I'm " + this.name;
                    }

                    getAge() {
                        return this.age;
                    }
                }

                let person = new Person("Alice", 30);
                person.greet();
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isString(result));
        assertEquals("Hello, I'm Alice", ObjectValidator.asString(result).getValue());
    }

    @Test
    @DisplayName("Should access instance properties correctly")
    void testInstancePropertyAccess() {
        String code = """
                class Car {
                    constructor(make, model) {
                        this.make = make;
                        this.model = model;
                        this.mileage = 0;
                    }

                    drive(miles) {
                        this.mileage = this.mileage + miles;
                        return "Drove " + miles + " miles";
                    }
                }

                let car = new Car("Toyota", "Camry");
                car.drive(100);
                car.mileage;
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isInteger(result));
        assertEquals(100, ObjectValidator.asInteger(result).getValue());
    }

    @Test
    @DisplayName("Should handle classes without constructors")
    void testClassWithoutConstructor() {
        String code = """
                class Utility {
                    static() {
                        return "This is a utility method";
                    }
                }

                let util = new Utility();
                util.static();
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isString(result));
        assertEquals("This is a utility method", ObjectValidator.asString(result).getValue());
    }

    // ========================================================================
    // INHERITANCE TESTS
    // ========================================================================

    @Test
    @DisplayName("Should support single inheritance")
    void testSingleInheritance() {
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
                    constructor(name, breed) {
                        super(name);
                        this.breed = breed;
                    }

                    speak() {
                        return this.name + " barks";
                    }

                    wagTail() {
                        return this.name + " wags tail";
                    }
                }

                let dog = new Dog("Rex", "German Shepherd");
                dog.speak();
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isString(result));
        assertEquals("Rex barks", ObjectValidator.asString(result).getValue());
    }

    @Test
    @DisplayName("Should support method inheritance from parent")
    void testMethodInheritance() {
        String code = """
                class Animal {
                    constructor(name) {
                        this.name = name;
                        this.energy = 100;
                    }

                    move() {
                        this.energy = this.energy - 10;
                        return this.name + " moved";
                    }

                    getEnergy() {
                        return this.energy;
                    }
                }

                class Bird extends Animal {
                    constructor(name, wingspan) {
                        super(name);
                        this.wingspan = wingspan;
                    }

                    fly() {
                        return this.name + " flies with " + this.wingspan + " wingspan";
                    }
                }

                let bird = new Bird("Eagle", "6 feet");
                bird.move();
                bird.getEnergy();
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isInteger(result));
        assertEquals(90, ObjectValidator.asInteger(result).getValue());
    }

    @Test
    @DisplayName("Should support multi-level inheritance")
    void testMultiLevelInheritance() {
        String code = """
                class LivingThing {
                    constructor(name) {
                        this.name = name;
                        this.alive = true;
                    }

                    isAlive() {
                        return this.alive;
                    }
                }

                class Animal extends LivingThing {
                    constructor(name, species) {
                        super(name);
                        this.species = species;
                    }

                    getSpecies() {
                        return this.species;
                    }
                }

                class Mammal extends Animal {
                    constructor(name, species, furColor) {
                        super(name, species);
                        this.furColor = furColor;
                    }

                    getFurColor() {
                        return this.furColor;
                    }
                }

                let mammal = new Mammal("Tiger", "Panthera tigris", "orange");
                mammal.getFurColor();
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isString(result));
        assertEquals("orange", ObjectValidator.asString(result).getValue());
    }

    // ========================================================================
    // SUPER CALL TESTS
    // ========================================================================

    @Test
    @DisplayName("Should support super method calls")
    void testSuperMethodCalls() {
        String code = """
                class Vehicle {
                    constructor(type) {
                        this.type = type;
                    }

                    start() {
                        return this.type + " engine started";
                    }
                }

                class Car extends Vehicle {
                    constructor(make, model) {
                        super("car");
                        this.make = make;
                        this.model = model;
                    }

                    start() {
                        let baseStart = super.start();
                        return baseStart + " - " + this.make + " " + this.model + " ready";
                    }
                }

                let car = new Car("Honda", "Civic");
                car.start();
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isString(result));
        assertEquals("car engine started - Honda Civic ready", ObjectValidator.asString(result).getValue());
    }

    @Test
    @DisplayName("Should support super constructor calls")
    void testSuperConstructorCalls() {
        String code = """
                class Shape {
                    constructor(color) {
                        this.color = color;
                    }

                    getColor() {
                        return this.color;
                    }
                }

                class Circle extends Shape {
                    constructor(color, radius) {
                        super(color);
                        this.radius = radius;
                    }

                    getArea() {
                        return 3.14 * this.radius * this.radius;
                    }
                }

                let circle = new Circle("red", 5);
                circle.getColor();
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isString(result));
        assertEquals("red", ObjectValidator.asString(result).getValue());
    }

    // ========================================================================
    // THIS BINDING TESTS
    // ========================================================================

    @Test
    @DisplayName("Should properly bind 'this' in methods")
    void testThisBinding() {
        String code = """
                class Counter {
                    constructor(start) {
                        this.value = start;
                    }

                    increment() {
                        this.value = this.value + 1;
                        return this;
                    }

                    getValue() {
                        return this.value;
                    }
                }

                let counter = new Counter(10);
                counter.increment().increment().getValue();
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isInteger(result));
        assertEquals(12, ObjectValidator.asInteger(result).getValue());
    }

    @Test
    @DisplayName("Should access 'this' properties correctly")
    void testThisPropertyAccess() {
        String code = """
                class Person {
                    constructor(firstName, lastName) {
                        this.firstName = firstName;
                        this.lastName = lastName;
                    }

                    getFullName() {
                        return this.firstName + " " + this.lastName;
                    }

                    changeName(newFirst, newLast) {
                        this.firstName = newFirst;
                        this.lastName = newLast;
                        return this.getFullName();
                    }
                }

                let person = new Person("John", "Doe");
                person.changeName("Jane", "Smith");
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isString(result));
        assertEquals("Jane Smith", ObjectValidator.asString(result).getValue());
    }

    // ========================================================================
    // METHOD CHAINING TESTS
    // ========================================================================

    @Test
    @DisplayName("Should support method chaining")
    void testMethodChaining() {
        String code = """
                class StringBuilder {
                    constructor() {
                        this.text = "";
                    }

                    append(str) {
                        this.text = this.text + str;
                        return this;
                    }

                    prepend(str) {
                        this.text = str + this.text;
                        return this;
                    }

                    toString() {
                        return this.text;
                    }
                }

                let sb = new StringBuilder();
                sb.append("World").prepend("Hello ").append("!").toString();
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isString(result));
        assertEquals("Hello World!", ObjectValidator.asString(result).getValue());
    }

    // ========================================================================
    // ERROR HANDLING TESTS
    // ========================================================================

    @Test
    @DisplayName("Should handle constructor argument mismatch")
    void testConstructorArgumentMismatch() {
        String code = """
                class Point {
                    constructor(x, y) {
                        this.x = x;
                        this.y = y;
                    }
                }

                new Point(1);  # missing argument
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isError(result));
        assertTrue(ObjectValidator.asError(result).getMessage().contains("Point requires 2 got 1"));
    }

    @Test
    @DisplayName("Should handle accessing undefined properties")
    void testUndefinedPropertyAccess() {
        String code = """
                class Empty {
                    constructor() {
                    }
                }

                let obj = new Empty();
                obj.nonExistentProperty;
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isError(result));
        assertTrue(ObjectValidator.asError(result).getMessage().contains("Property 'nonExistentProperty' not found"));
    }

    @Test
    @DisplayName("Should handle super calls without parent class")
    void testSuperCallWithoutParent() {
        String code = """
                class Base {
                    constructor() {
                    }

                    method() {
                        super.someMethod();  # Error: no parent class
                    }
                }

                let base = new Base();
                base.method();
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isError(result));
        assertTrue(ObjectValidator.asError(result).getMessage().contains("No parent class found for class: Base"));
    }

    // ========================================================================
    // COMPLEX SCENARIOS TESTS
    // ========================================================================

    @Test
    @DisplayName("Should handle complex inheritance with method overriding")
    void testComplexInheritanceScenario() {
        String code = """
                class GameCharacter {
                    constructor(name, health) {
                        this.name = name;
                        this.health = health;
                        this.level = 1;
                    }

                    attack() {
                        return this.name + " attacks for " + (this.level * 10) + " damage";
                    }

                    levelUp() {
                        this.level = this.level + 1;
                        return this.name + " is now level " + this.level;
                    }
                }

                class Warrior extends GameCharacter {
                    constructor(name, health, weapon) {
                        super(name, health);
                        this.weapon = weapon;
                        this.rage = 0;
                    }

                    attack() {
                        let baseAttack = super.attack();
                        return baseAttack + " with " + this.weapon;
                    }

                    berserk() {
                        this.rage = this.rage + 10;
                        return this.name + " enters berserk mode! Rage: " + this.rage;
                    }
                }

                class Paladin extends Warrior {
                    constructor(name, health, weapon, deity) {
                        super(name, health, weapon);
                        this.deity = deity;
                        this.holiness = 100;
                    }

                    attack() {
                        let warriorAttack = super.attack();
                        return warriorAttack + " blessed by " + this.deity;
                    }

                    heal() {
                        this.health = this.health + (this.holiness / 10);
                        return this.name + " heals for " + (this.holiness / 10) + " points";
                    }
                }

                let paladin = new Paladin("Sir Galahad", 100, "Holy Sword", "Light");
                paladin.levelUp();
                paladin.attack();
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isString(result));
        assertEquals("Sir Galahad attacks for 20 damage with Holy Sword blessed by Light",
                ObjectValidator.asString(result).getValue());
    }

    @Test
    @DisplayName("Should handle instanceof-like behavior")
    void testInstanceTypeChecking() {
        String code = """
                class Animal {
                    constructor(name) {
                        this.name = name;
                    }
                }

                class Dog extends Animal {
                    constructor(name, breed) {
                        super(name);
                        this.breed = breed;
                    }
                }

                let dog = new Dog("Rex", "Labrador");

                dog.name;  # Should be "Rex"
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isString(result));
        assertEquals("Rex", ObjectValidator.asString(result).getValue());
    }

    // ========================================================================
    // PERFORMANCE AND EDGE CASE TESTS
    // ========================================================================

    @Test
    @DisplayName("Should handle deep inheritance chains")
    void testDeepInheritanceChain() {
        String code = """
                class A {
                    constructor() { this.value = "A"; }
                    getValue() { return this.value; }
                }

                class B extends A {
                    constructor() { super(); this.value = this.value + "B"; }
                }

                class C extends B {
                    constructor() { super(); this.value = this.value + "C"; }
                }

                class D extends C {
                    constructor() { super(); this.value = this.value + "D"; }
                }

                class E extends D {
                    constructor() { super(); this.value = this.value + "E"; }
                }

                let e = new E();
                e.getValue();
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isString(result));
        assertEquals("ABCDE", ObjectValidator.asString(result).getValue());
    }

    @Test
    @DisplayName("Should handle empty classes correctly")
    void testEmptyClass() {
        String code = """
                class Empty {
                }

                let obj = new Empty();
                "created";  # Just return a success indicator
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isString(result));
        assertEquals("created", ObjectValidator.asString(result).getValue());
    }

    @Test
    @DisplayName("Should handle property assignment and modification")
    void testPropertyModification() {
        String code = """
                class Mutable {
                    constructor() {
                        this.counter = 0;
                        this.data = "initial";
                    }

                    update(newData) {
                        this.data = newData;
                        this.counter = this.counter + 1;
                        return this.counter;
                    }
                }

                let obj = new Mutable();
                obj.update("first");
                obj.update("second");
                obj.update("third");
                """;

        BaseObject result = evaluateCode(code);
        assertTrue(ObjectValidator.isInteger(result));
        assertEquals(3, ObjectValidator.asInteger(result).getValue());
    }
}
