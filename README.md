# Interpreter

A custom programming language interpreter built in Java.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── interpreter/
│   │           ├── Main.java          # Entry point
│   │           └── Interpreter.java   # Core interpreter logic
│   └── resources/                     # Resource files
└── test/
    └── java/
        └── com/
            └── interpreter/
                └── InterpreterTest.java  # Unit tests
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Building the Project

```bash
# Compile the project
mvn compile

# Run tests
mvn test

# Package into JAR
mvn package
```

### Running the Interpreter

```bash
# Run directly with Maven
mvn exec:java -Dexec.mainClass="com.interpreter.Main"

# Or run the packaged JAR
java -jar target/interpreter-1.0.0.jar

# Run with input file
java -jar target/interpreter-1.0.0.jar input.txt
```

## Development

### Adding Dependencies

Add dependencies to the `pom.xml` file under the `<dependencies>` section.

### Running Tests

```bash
mvn test
```

### IDE Setup

This project follows the standard Maven directory layout and can be imported into any Java IDE:

- **IntelliJ IDEA**: File → Open → Select the project directory
- **Eclipse**: File → Import → Existing Maven Projects
- **VS Code**: Open the folder with Java Extension Pack installed

## Features (To Be Implemented)

- [ ] Lexical analysis (tokenization)
- [ ] Syntax analysis (parsing)
- [ ] Semantic analysis
- [ ] Code execution
- [ ] Interactive REPL mode
- [ ] Error handling and reporting
- [ ] Standard library functions

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Run the test suite
6. Submit a pull request