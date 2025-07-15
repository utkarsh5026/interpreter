# Lang - A Custom Programming Language Implementation

A complete interpreter for a dynamic programming language built from scratch in Java, featuring lexical analysis, parsing, and evaluation with comprehensive built-in functions and debugging tools.

## Table of Contents

- [Overview](#overview)
- [Language Features](#language-features)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [Language Syntax](#language-syntax)
- [Built-in Functions](#built-in-functions)
- [Development Tools](#development-tools)
- [Project Structure](#project-structure)
- [Building and Running](#building-and-running)
- [Examples](#examples)
- [Contributing](#contributing)

## Overview

This project implements a complete programming language interpreter from first principles, showcasing fundamental computer science concepts:

- **Lexical Analysis**: Converting source code text into meaningful tokens
- **Parsing**: Building an Abstract Syntax Tree (AST) using Pratt parsing
- **Evaluation**: Executing the AST using a tree-walking interpreter
- **Environment Management**: Lexical scoping and variable resolution
- **Type System**: Dynamic typing with runtime type checking

### Why Build a Language from Scratch?

Understanding how programming languages work requires building one. This project demonstrates:

1. **Tokenization**: How raw text becomes structured tokens
2. **Grammar Processing**: How syntax rules create meaningful structures
3. **Semantic Analysis**: How meaning is extracted from syntax
4. **Runtime Execution**: How code actually runs and produces results
5. **Memory Management**: How variables and scopes are managed

## Language Features

### Core Programming Constructs

- **Variables**: Mutable (`let`) and immutable (`const`) bindings
- **Functions**: First-class functions with closures
- **Control Flow**: Conditional statements and loops
- **Data Structures**: Arrays and hash maps
- **Expressions**: Mathematical, logical, and comparison operations
- **String Processing**: Comprehensive string manipulation

### Advanced Features

- **Lexical Scoping**: Variables resolved based on definition location
- **Closures**: Functions that capture their surrounding environment
- **Error Handling**: Comprehensive error reporting with positions
- **Built-in Library**: 40+ pre-defined functions for common operations
- **Interactive Debugging**: Tools for analyzing lexer and parser behavior

## Architecture

The interpreter follows a traditional three-phase design:

```
Source Code → [Lexer] → Tokens → [Parser] → AST → [Evaluator] → Result
```

### 1. Lexical Analysis (Tokenization)

The **Lexer** converts raw source code into a stream of tokens:

```java
// Input: "let x = 42;"
// Output: [LET, IDENTIFIER("x"), ASSIGN, INT("42"), SEMICOLON]
```

**Key Components:**
- `Lexer.java`: Main tokenization engine
- `Token.java`: Token representation with type and position
- `Keywords.java`: Reserved word management
- Debug tools for token analysis

### 2. Syntax Analysis (Parsing)

The **Parser** builds an Abstract Syntax Tree using Pratt parsing for proper operator precedence:

```java
// Tokens: [IDENTIFIER("x"), PLUS, INT("5"), MULTIPLY, INT("3")]
// AST: InfixExpression(x, "+", InfixExpression(5, "*", 3))
```

**Key Features:**
- **Modular Design**: Separate parsers for each construct type
- **Pratt Parsing**: Elegant handling of operator precedence
- **Error Recovery**: Continues parsing after syntax errors
- **Registry Pattern**: Extensible parser architecture

### 3. Semantic Analysis & Execution

The **Evaluator** walks the AST and executes code:

```java
// AST: LetStatement("x", IntegerLiteral(42))
// Action: Create variable "x" with value 42 in current environment
```

**Key Components:**
- **Environment**: Scope-based variable storage
- **Object System**: Runtime type representation
- **Builtin Registry**: Pre-defined function library
- **Error Handling**: Runtime error management

## Quick Start

### Prerequisites

- Java 24+ (uses preview features)
- Gradle 8.8+

### Installation

```bash
# Clone the repository
git clone <repository-url>
cd lang

# Build the project
./gradlew build

# Run the interpreter
./gradlew run
```

### Your First Program

Create a file `hello.lang`:

```javascript
// Variables and basic operations
let name = "World";
let greeting = "Hello, " + name + "!";
println(greeting);

// Functions
fn factorial(n) {
    if (n <= 1) {
        return 1;
    } else {
        return n * factorial(n - 1);
    }
}

// Arrays and iteration
let numbers = [1, 2, 3, 4, 5];
for (let i = 0; i < len(numbers); i = i + 1) {
    println("factorial(" + str(numbers[i]) + ") = " + str(factorial(numbers[i])));
}
```

## Language Syntax

### Variables

```javascript
// Mutable variables
let x = 42;
let name = "Alice";
x = x + 10;  // x is now 52

// Immutable constants
const PI = 3.14159;
const message = "Hello, World!";
// PI = 3.14; // Error: cannot reassign constant
```

### Functions

```javascript
// Function declaration
fn add(a, b) {
    return a + b;
}

// Functions are first-class values
let operation = add;
let result = operation(5, 3);  // result = 8

// Closures capture environment
fn makeCounter() {
    let count = 0;
    return fn() {
        count = count + 1;
        return count;
    };
}

let counter = makeCounter();
println(counter());  // 1
println(counter());  // 2
```

### Control Flow

```javascript
// Conditional statements
if (x > 0) {
    println("positive");
} elif (x < 0) {
    println("negative");
} else {
    println("zero");
}

// While loops
let i = 0;
while (i < 5) {
    println(i);
    i = i + 1;
}

// For loops
for (let j = 0; j < 10; j = j + 2) {
    println("Even number: " + str(j));
}
```

### Data Structures

```javascript
// Arrays
let fruits = ["apple", "banana", "orange"];
fruits[1] = "grape";  // Arrays are mutable
let length = len(fruits);

// Hash maps (objects)
let person = {
    "name": "Alice",
    "age": 30,
    "city": "New York"
};
person["occupation"] = "Engineer";  // Add new key
```

### Expressions and Operators

```javascript
// Arithmetic
let math = 2 + 3 * 4 - 1;  // Respects precedence: 2 + 12 - 1 = 13

// Comparison
let comparison = (5 > 3) && (2 < 4);  // true

// String operations
let text = "Hello" + " " + "World";
let upper = upper(text);  // "HELLO WORLD"
```

## Built-in Functions

The language includes 40+ built-in functions organized by category:

### Core Data Operations
- `len(obj)` - Get length of arrays, strings, or hashes
- `type(obj)` - Get object type as string
- `str(obj)` - Convert to string representation
- `int(str)` - Convert string to integer
- `bool(obj)` - Convert to boolean using truthiness

### Array Operations
- `first(array)` - Get first element
- `last(array)` - Get last element
- `push(array, element)` - Add element to end
- `pop(array)` - Remove last element
- `slice(array, start, end)` - Extract portion
- `concat(array1, array2)` - Join arrays
- `reverse(array)` - Reverse order
- `join(array, separator)` - Join to string

### String Operations
- `split(string, delimiter)` - Split into array
- `replace(string, search, replace)` - Replace text
- `trim(string)` - Remove whitespace
- `upper(string)` / `lower(string)` - Case conversion
- `substr(string, start, length)` - Extract substring
- `indexOf(string, substring)` - Find position
- `contains(string, substring)` - Check existence

### Mathematical Operations
- `abs(number)` - Absolute value
- `max(...numbers)` / `min(...numbers)` - Extremes
- `pow(base, exponent)` - Power function
- `sqrt(number)` - Square root
- `random(max?)` - Random number generation

### I/O Operations
- `print(...args)` - Output without newline
- `println(...args)` - Output with newline

### Utility Functions
- `range(start, end, step?)` - Generate number sequences
- `keys(hash)` - Get hash keys
- `values(hash)` - Get hash values

## Development Tools

### AST Visualization

The project includes comprehensive AST visualization tools:

```java
// Generate visual AST representation
VisualizationConfig config = VisualizationConfig.builder()
    .style(AstTreeVisualizer.Style.UNICODE_TREE)
    .colorScheme(AstTreeVisualizer.ColorScheme.TERMINAL)
    .showTypes(true)
    .showValues(true)
    .build();

AstTreeVisualizer visualizer = new AstTreeVisualizer(config);
System.out.println(visualizer.visualize(program));
```

### Lexer Debugging

Debug tokenization with detailed output:

```java
DebugConfig debugConfig = DebugConfig.builder()
    .liveTokenOutput(true)
    .sourceContext(true)
    .useColors(true)
    .statistics(true)
    .build();

Lexer lexer = new Lexer(sourceCode, new LexerDebugger(debugConfig));
```

### Error Reporting

Comprehensive error messages with source context:

```
Parse Error at line 5, column 12: Expected SEMICOLON, got COMMA
    let x = 5, y = 10;
            ^
🚨 Expected ';' after variable declaration
```

## Project Structure

```
lang/
├── app/src/main/java/lang/
│   ├── lexer/              # Tokenization
│   │   ├── Lexer.java     # Main lexer implementation
│   │   └── debug/         # Debugging tools
│   ├── parser/            # Syntax analysis
│   │   ├── LanguageParser.java
│   │   ├── core/          # Core parsing utilities
│   │   ├── interfaces/    # Parser contracts
│   │   ├── parsers/       # Specific parsers
│   │   └── registry/      # Parser management
│   ├── ast/               # Abstract Syntax Tree
│   │   ├── base/          # Base AST classes
│   │   ├── expressions/   # Expression nodes
│   │   ├── statements/    # Statement nodes
│   │   ├── literals/      # Literal nodes
│   │   └── visitor/       # Visitor pattern
│   ├── exec/              # Execution engine
│   │   ├── evaluator/     # AST evaluation
│   │   ├── objects/       # Runtime objects
│   │   └── builtins/      # Built-in functions
│   ├── token/             # Token definitions
│   └── examples/          # Usage examples
├── build.gradle           # Build configuration
└── settings.gradle        # Project settings
```

## Building and Running

### Development Build

```bash
# Compile and run tests
./gradlew build

# Run the main application
./gradlew run

# Run with debug output
./gradlew run --args="--debug"

# Run tests with detailed output
./gradlew test --info
```

### IDE Setup

The project uses Java 24 preview features. Configure your IDE:

**IntelliJ IDEA:**
1. Set Project SDK to Java 24
2. Enable preview features in compiler settings
3. Add `--enable-preview` to VM options

**VS Code:**
1. Install Java Extension Pack
2. Configure `java.compile.nullAnalysis.mode` to "automatic"
3. Set `java.configuration.runtimes` to Java 24

## Examples

### Example 1: Fibonacci Sequence

```javascript
fn fibonacci(n) {
    if (n <= 1) {
        return n;
    }
    return fibonacci(n - 1) + fibonacci(n - 2);
}

let numbers = range(0, 10);
for (let i = 0; i < len(numbers); i = i + 1) {
    let fib = fibonacci(numbers[i]);
    println("fibonacci(" + str(numbers[i]) + ") = " + str(fib));
}
```

### Example 2: Data Processing

```javascript
// Process user data
let users = [
    {"name": "Alice", "age": 30, "city": "New York"},
    {"name": "Bob", "age": 25, "city": "London"},
    {"name": "Charlie", "age": 35, "city": "Tokyo"}
];

fn processUsers(userList) {
    println("Processing " + str(len(userList)) + " users:");
    
    for (let i = 0; i < len(userList); i = i + 1) {
        let user = userList[i];
        let name = user["name"];
        let age = user["age"];
        let city = user["city"];
        
        println("- " + name + " (" + str(age) + ") from " + city);
    }
}

processUsers(users);
```

### Example 3: String Processing

```javascript
fn analyzeText(text) {
    let words = split(text, " ");
    let wordCount = len(words);
    let charCount = len(text);
    
    println("Text analysis:");
    println("- Characters: " + str(charCount));
    println("- Words: " + str(wordCount));
    println("- Average word length: " + str(charCount / wordCount));
    
    // Find longest word
    let longest = "";
    for (let i = 0; i < len(words); i = i + 1) {
        if (len(words[i]) > len(longest)) {
            longest = words[i];
        }
    }
    println("- Longest word: " + longest);
}

analyzeText("The quick brown fox jumps over the lazy dog");
```

## Contributing

### Development Workflow

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/amazing-feature`
3. **Write** tests for new functionality
4. **Implement** your changes
5. **Test** thoroughly: `./gradlew test`
6. **Commit** with clear messages: `git commit -m 'Add amazing feature'`
7. **Push** to your branch: `git push origin feature/amazing-feature`
8. **Create** a Pull Request

### Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add comprehensive JavaDoc comments
- Include unit tests for new features
- Maintain consistent indentation (4 spaces)

### Architecture Guidelines

- **Separation of Concerns**: Keep lexer, parser, and evaluator independent
- **Visitor Pattern**: Use for AST operations
- **Registry Pattern**: For extensible parser/evaluator components
- **Immutable AST**: AST nodes should be immutable after creation
- **Error Handling**: Provide clear, actionable error messages

### Testing

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "LexerTest"

# Run with coverage
./gradlew test jacocoTestReport
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Inspired by "Writing An Interpreter In Go" by Thorsten Ball
- Uses Pratt parsing techniques pioneered by Vaughan Pratt
- Built with modern Java features and best practices

---

**Happy coding!** 🚀

For questions, issues, or contributions, please visit our [GitHub repository](https://github.com/utkarsh5026/interpreter).