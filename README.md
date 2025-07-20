# 🚀 Modern Programming Language Implementation

A comprehensive, object-oriented programming language built from scratch in Java, featuring a complete lexer, parser, evaluator, and interactive REPL environment.

## 📋 Table of Contents

- [🌟 Overview](#-overview)
- [🏗️ Architecture](#️-architecture)
- [🎯 Language Features](#-language-features)
- [🔧 Getting Started](#-getting-started)
- [📖 Language Reference](#-language-reference)
- [🎮 Interactive REPL](#-interactive-repl)
- [🏛️ Object-Oriented Programming](#️-object-oriented-programming)
- [🔍 Advanced Features](#-advanced-features)
- [🛠️ Development](#️-development)
- [📚 Examples](#-examples)

## 🌟 Overview

This project implements a complete programming language from first principles, demonstrating fundamental computer science concepts including:

- **Lexical Analysis**: Converting source code into meaningful tokens
- **Syntax Parsing**: Building Abstract Syntax Trees (AST) using Pratt parsing
- **Semantic Analysis**: Type checking and scope resolution
- **Code Evaluation**: Tree-walking interpreter with environment-based scoping
- **Object-Oriented Programming**: Classes, inheritance, polymorphism
- **Interactive Development**: Feature-rich REPL with debugging capabilities

### Why From First Principles?

Understanding how programming languages work requires building one yourself. This implementation covers:

1. **Tokenization**: How source code becomes structured data
2. **Parsing**: How syntax rules create tree structures
3. **Evaluation**: How abstract trees become executable programs
4. **Type Systems**: How different data types interact
5. **Scoping**: How variables are resolved in different contexts
6. **Object Models**: How classes and inheritance work under the hood

## 🏗️ Architecture

### Core Components

```
┌─────────────────────────────────────────────────────────────┐
│                    Source Code                              │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    LEXER                                    │
│  • Tokenization    • Comments      • String/Number Parsing  │
│  • Keywords        • Operators     • Position Tracking      │
└─────────────────────┬───────────────────────────────────────┘
                      │ Token Stream
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    PARSER                                   │
│  • Pratt Parsing   • Precedence    • AST Construction       │
│  • Error Recovery  • Expressions   • Statement Parsing      │
└─────────────────────┬───────────────────────────────────────┘
                      │ Abstract Syntax Tree
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                   EVALUATOR                                 │
│  • Tree Walking    • Environments  • Type System            │
│  • Built-ins       • Error Handling• Stack Traces           │
└─────────────────────┬───────────────────────────────────────┘
                      │ Program Result
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                     REPL                                    │
│  • Interactive     • Debugging     • Command History        │
│  • Syntax Colors   • Help System   • Error Display          │
└─────────────────────────────────────────────────────────────┘
```

### Design Patterns Used

- **Visitor Pattern**: AST traversal and operations
- **Registry Pattern**: Modular parser and evaluator components
- **Strategy Pattern**: Different parsing strategies for expressions/statements
- **Factory Pattern**: Object creation and type conversion
- **Observer Pattern**: Debug event system

## 🎯 Language Features

### Data Types

```javascript
// Numbers (integers and floats)
let age = 25;           // Integer
let price = 19.99;      // Float
let scientific = 1e6;   // Scientific notation

// Strings with escape sequences
let name = "Alice";
let message = "Hello\nWorld";

// Booleans
let isActive = true;
let isComplete = false;

// Arrays (heterogeneous)
let numbers = [1, 2, 3, 4, 5];
let mixed = [1, "hello", true, [1, 2]];

// Hash maps (objects)
let person = {
    "name": "Bob",
    "age": 30,
    "active": true
};

// Null values
let empty = null;
```

### Variables and Constants

```javascript
// Mutable variables
let counter = 0;
counter = counter + 1;  // ✅ Allowed

// Immutable constants
const PI = 3.14159;
PI = 3.14;              // ❌ Error: Cannot reassign constant

// Block scoping
{
    let local = "inside block";
    const BLOCK_CONST = 42;
}
// local is not accessible here
```

### Functions

```javascript
// Function definition
fn greet(name) {
    return "Hello, " + name + "!";
}

// Function calls
let message = greet("World");

// Functions are first-class values
let operation = fn(x, y) { return x + y; };
let result = operation(5, 3);

// Closures and lexical scoping
fn createCounter() {
    let count = 0;
    return fn() {
        count = count + 1;
        return count;
    };
}

let counter = createCounter();
counter(); // Returns 1
counter(); // Returns 2
```

### Control Flow

```javascript
// Conditional statements
if (age >= 18) {
    print("Adult");
} elif (age >= 13) {
    print("Teenager");
} else {
    print("Child");
}

// While loops
let i = 0;
while (i < 5) {
    print("Count:", i);
    i = i + 1;
}

// For loops
for (let j = 0; j < 10; j = j + 1) {
    if (j == 5) {
        break;
    }
    if (j % 2 == 0) {
        continue;
    }
    print("Odd number:", j);
}
```

### String Interpolation (F-Strings)

```javascript
let name = "Alice";
let age = 25;

// F-string with embedded expressions
let intro = f"My name is {name} and I'm {age} years old";
let calculation = f"2 + 3 = {2 + 3}";
let nested = f"Hello {getUser().name}!";
```

## 🏛️ Object-Oriented Programming

### Classes and Inheritance

```javascript
// Base class definition
class Animal {
    constructor(name, species) {
        this.name = name;
        this.species = species;
        this.energy = 100;
    }
    
    speak() {
        return f"{this.name} makes a sound";
    }
    
    move(distance) {
        this.energy = this.energy - distance;
        return f"{this.name} moved {distance} units";
    }
}

// Inheritance with method overriding
class Dog extends Animal {
    constructor(name, breed) {
        super(name, "Canine");  // Call parent constructor
        this.breed = breed;
    }
    
    speak() {
        return f"{this.name} barks loudly!";
    }
    
    fetch() {
        return f"{this.name} fetches the ball";
    }
}

// Creating instances
let buddy = new Dog("Buddy", "Golden Retriever");
print(buddy.speak());    // "Buddy barks loudly!"
print(buddy.move(10));   // "Buddy moved 10 units"
print(buddy.fetch());    // "Buddy fetches the ball"
```

### Advanced OOP Features

```javascript
class Vehicle {
    constructor(brand, model) {
        this.brand = brand;
        this.model = model;
        this.speed = 0;
    }
    
    accelerate(amount) {
        this.speed = this.speed + amount;
        return this.getStatus();
    }
    
    getStatus() {
        return f"{this.brand} {this.model} traveling at {this.speed} mph";
    }
}

class Car extends Vehicle {
    constructor(brand, model, doors) {
        super(brand, model);    // Initialize parent
        this.doors = doors;
    }
    
    // Override parent method
    accelerate(amount) {
        // Call parent method and add car-specific behavior
        super.accelerate(amount);
        if (this.speed > 80) {
            return this.getStatus() + " - Warning: High speed!";
        }
        return this.getStatus();
    }
    
    honk() {
        return f"{this.brand} {this.model} goes BEEP BEEP!";
    }
}

// Method chaining and polymorphism
let myCar = new Car("Toyota", "Camry", 4);
print(myCar.accelerate(30));  // Uses overridden method
print(myCar.honk());          // Car-specific method
```

### This and Super Keywords

```javascript
class Counter {
    constructor(start) {
        this.value = start;     // 'this' refers to current instance
    }
    
    increment() {
        this.value = this.value + 1;
        return this;            // Return this for chaining
    }
    
    getValue() {
        return this.value;
    }
}

class AdvancedCounter extends Counter {
    constructor(start, step) {
        super(start);           // Call parent constructor
        this.step = step;
    }
    
    increment() {
        this.value = this.value + this.step;
        return super.getValue(); // Call parent method
    }
}
```

## 🔍 Advanced Features

### Built-in Functions

```javascript
// Array operations
let arr = [1, 2, 3, 4, 5];
print(len(arr));        // 5
print(first(arr));      // 1
print(last(arr));       // 5
print(rest(arr));       // [2, 3, 4, 5]

// Hash operations
let person = {"name": "Alice", "age": 30};
print(keys(person));    // ["name", "age"]
print(values(person));  // ["Alice", 30]

// String operations
print(len("hello"));    // 5

// Type checking
print(type(42));        // "INTEGER"
print(type(3.14));      // "FLOAT"
print(type("hello"));   // "STRING"

// I/O operations
print("Hello", "World", 123);  // Multiple arguments
```

### Array and Hash Manipulation

```javascript
// Array indexing and assignment
let numbers = [10, 20, 30];
numbers[1] = 25;        // Modify element
print(numbers[1]);      // 25

// Hash key access and assignment
let config = {"debug": true, "port": 8080};
config["timeout"] = 30;  // Add new key
config.debug = false;    // Property-style access
print(config["port"]);   // 8080
```

### Compound Assignment Operators

```javascript
let x = 10;
x += 5;     // Equivalent to: x = x + 5
x -= 3;     // Equivalent to: x = x - 3
x *= 2;     // Equivalent to: x = x * 2
x /= 4;     // Equivalent to: x = x / 4
x %= 3;     // Equivalent to: x = x % 3
```

### Comments

```javascript
# Single-line comment

/*
 * Multi-line comment
 * Can span multiple lines
 */

let value = 42;  # End-of-line comment

/*
 * Nested comments are supported
 * /* This is nested */
 * Still inside the outer comment
 */
```

## 🎮 Interactive REPL

### Features

- **🎨 Syntax Highlighting**: Color-coded output for different data types
- **📜 Command History**: Navigate through previous commands
- **🔍 Debug Information**: Stack traces and error context
- **📚 Built-in Help**: Comprehensive help system
- **⚡ Live Evaluation**: Immediate feedback on expressions

### REPL Commands

```bash
:help           # Show all available commands
:examples       # Display language examples
:builtins       # List all built-in functions
:env            # Show current variables
:history        # Display command history
:clear          # Clear the screen
:reset          # Reset environment
:exit           # Exit REPL
```

### REPL Session Example

```
🚀 Welcome to the Interactive Language REPL! 🚀

[1] ❯ let greeting = "Hello, World!"
⟹ "Hello, World!"

[2] ❯ class Person {
    constructor(name) {
        this.name = name;
    }
    greet() {
        return f"Hi, I'm {this.name}";
    }
}
⟹ class Person { constructor, 1 methods }

[3] ❯ let alice = new Person("Alice")
⟹ instance of Person {"name": "Alice"}

[4] ❯ alice.greet()
⟹ "Hi, I'm Alice"
```

## 🔧 Getting Started

### Prerequisites

- Java 24+ with preview features enabled
- Gradle 8.8+

### Building and Running

```bash
# Clone the repository
git clone <repository-url>
cd lang

# Build the project
./gradlew build

# Run the REPL
./gradlew run

# Run tests
./gradlew test
```

### Usage Examples

```bash
# Run a source file
echo 'print("Hello from file!")' > hello.lang
java -jar build/libs/lang.jar hello.lang

# Interactive mode
java -jar build/libs/lang.jar
```

## 📚 Examples

### Fibonacci Calculator

```javascript
fn fibonacci(n) {
    if (n <= 1) {
        return n;
    }
    return fibonacci(n - 1) + fibonacci(n - 2);
}

for (let i = 0; i < 10; i = i + 1) {
    print(f"fib({i}) = {fibonacci(i)}");
}
```

### Data Processing

```javascript
let students = [
    {"name": "Alice", "grade": 85},
    {"name": "Bob", "grade": 92},
    {"name": "Charlie", "grade": 78}
];

let total = 0;
let count = len(students);

for (let i = 0; i < count; i = i + 1) {
    let student = students[i];
    total = total + student["grade"];
    print(f"{student['name']}: {student['grade']}");
}

let average = total / count;
print(f"Class average: {average}");
```

### Object-Oriented Banking System

```javascript
class BankAccount {
    constructor(owner, initialBalance) {
        this.owner = owner;
        this.balance = initialBalance;
        this.transactions = [];
    }
    
    deposit(amount) {
        if (amount > 0) {
            this.balance = this.balance + amount;
            this.addTransaction("deposit", amount);
            return f"Deposited ${amount}. New balance: ${this.balance}";
        }
        return "Invalid deposit amount";
    }
    
    withdraw(amount) {
        if (amount > 0 && amount <= this.balance) {
            this.balance = this.balance - amount;
            this.addTransaction("withdrawal", amount);
            return f"Withdrew ${amount}. New balance: ${this.balance}";
        }
        return "Invalid withdrawal amount or insufficient funds";
    }
    
    addTransaction(type, amount) {
        let transaction = {
            "type": type,
            "amount": amount,
            "balance": this.balance
        };
        this.transactions = this.transactions + [transaction];
    }
    
    getStatement() {
        print(f"Account Statement for {this.owner}");
        print(f"Current Balance: ${this.balance}");
        print("Recent Transactions:");
        
        let txCount = len(this.transactions);
        for (let i = 0; i < txCount; i = i + 1) {
            let tx = this.transactions[i];
            print(f"  {tx['type']}: ${tx['amount']} (Balance: ${tx['balance']})");
        }
    }
}

class SavingsAccount extends BankAccount {
    constructor(owner, initialBalance, interestRate) {
        super(owner, initialBalance);
        this.interestRate = interestRate;
    }
    
    addInterest() {
        let interest = this.balance * this.interestRate;
        this.balance = this.balance + interest;
        this.addTransaction("interest", interest);
        return f"Added ${interest} in interest. New balance: ${this.balance}";
    }
}

# Usage
let checking = new BankAccount("Alice", 1000);
print(checking.deposit(500));
print(checking.withdraw(200));

let savings = new SavingsAccount("Bob", 5000, 0.05);
print(savings.addInterest());
savings.getStatement();
```

## 🛠️ Development

### Architecture Deep Dive

#### Lexer (Tokenization)

The lexer converts raw source code into a stream of tokens using finite state automata:

```java
// Key components:
- Character stream processing
- Keyword recognition
- Operator tokenization  
- String/number parsing with escape sequences
- Position tracking for error reporting
- Comment handling (single and multi-line)
```

#### Parser (Syntax Analysis)

Uses Pratt parsing (Top-Down Operator Precedence) for expression parsing:

```java
// Parsing strategy:
- Recursive descent for statements
- Pratt parsing for expressions with precedence
- Error recovery mechanisms
- AST node construction
- Registry pattern for extensible parsers
```

#### Evaluator (Execution)

Tree-walking interpreter with environment-based scoping:

```java
// Evaluation features:
- Visitor pattern for AST traversal
- Lexical scoping with environment chains
- Dynamic dispatch for method calls
- Stack trace generation
- Built-in function registry
- Type system with automatic promotion
```

### Key Design Decisions

1. **Immutable AST Nodes**: Thread-safe and easier to reason about
2. **Environment Chains**: Efficient lexical scoping implementation
3. **Type Promotion**: Automatic numeric type conversion (int → float)
4. **Error Recovery**: Parser continues after errors to find multiple issues
5. **Position Tracking**: Every token tracks source location for debugging

### Testing Strategy

```bash
# Run all tests
./gradlew test

# Run specific test categories
./gradlew test --tests "*LexerTest*"
./gradlew test --tests "*ParserTest*" 
./gradlew test --tests "*EvaluatorTest*"

# Generate coverage report
./gradlew jacocoTestReport
```

### Debugging Features

- **Stack Traces**: Full call stack with source positions
- **Error Context**: Source code snippets around errors
- **REPL Debug Commands**: Environment inspection and debugging
- **Lexer Debugging**: Token stream visualization
- **Parser Debug Mode**: AST structure visualization

## 🤝 Contributing

This project demonstrates fundamental programming language implementation concepts. Areas for extension:

1. **Standard Library**: More built-in functions and modules
2. **Optimization**: Bytecode compilation, JIT compilation
3. **Type System**: Static typing, type inference
4. **Memory Management**: Garbage collection, reference counting
5. **Concurrency**: Threads, async/await, actors
6. **Package System**: Modules, imports, namespaces

## 📖 References

- **Dragon Book**: Compilers: Principles, Techniques, and Tools
- **Crafting Interpreters**: Robert Nystrom's excellent guide
- **Pratt Parsing**: Top-Down Operator Precedence parsing
- **Tree Walking**: Simple interpretation technique
- **Environment Chains**: Lexical scoping implementation

---

*This implementation serves as a comprehensive example of programming language design and implementation, covering everything from lexical analysis to object-oriented programming features.*