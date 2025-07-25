package lang.repl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import lang.exec.evaluator.LanguageEvaluator;
import lang.ast.statements.Program;
import lang.lexer.Lexer;
import lang.parser.LanguageParser;
import lang.parser.error.ParseError;
import lang.exec.base.BaseObject;
import lang.exec.objects.ErrorObject;
import lang.exec.objects.env.Environment;
import lang.exec.validator.ObjectValidator;
import lang.exec.builtins.BuiltinRegistry;

/**
 * 🚀 Interactive REPL for the Language 🚀
 * 
 * A visually stunning and feature-rich Read-Eval-Print Loop that provides:
 * - 🎨 Colorized output and syntax highlighting
 * - 📚 Command completion and help system
 * - 📜 Command history
 * - 🔍 Multi-line input support
 * - 🎯 Interactive debugging features
 * - ✨ Beautiful visual feedback
 */
public class LanguageREPL {

    // 🎨 ANSI Color Constants for beautiful output
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String DIM = "\u001B[2m";

    // Colors for different elements
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String BRIGHT_GREEN = "\u001B[92m";
    private static final String BRIGHT_BLUE = "\u001B[94m";
    private static final String BRIGHT_CYAN = "\u001B[96m";
    private static final String BRIGHT_YELLOW = "\u001B[93m";
    private static final String BRIGHT_MAGENTA = "\u001B[95m";

    private final Scanner scanner;
    private final LanguageEvaluator evaluator;
    private final Environment globalEnvironment;
    private final List<String> commandHistory;
    private boolean running;
    private int commandCount;

    public LanguageREPL() {
        this.scanner = new Scanner(System.in);
        this.evaluator = new LanguageEvaluator();
        this.globalEnvironment = createGlobalEnvironment();
        this.commandHistory = new ArrayList<>();
        this.running = true;
        this.commandCount = 0;
    }

    /**
     * 🚀 Starts the REPL main loop
     */
    public void start() {
        showWelcomeBanner();
        showQuickHelp();

        while (running) {
            try {
                String input = readInput();

                if (input == null || input.trim().isEmpty()) {
                    continue;
                }

                // Add to history
                commandHistory.add(input);

                // Handle REPL commands
                if (input.startsWith(":")) {
                    handleReplCommand(input);
                    continue;
                }

                // Evaluate language code
                evaluateCode(input);
                commandCount++;

            } catch (Exception e) {
                printError("REPL Error: " + e.getMessage());
            }
        }

        showGoodbyeMessage();
    }

    /**
     * 🎨 Shows the welcome banner with beautiful ASCII art
     */
    private void showWelcomeBanner() {
        clearScreen();

        String banner = String.format("""
                %s🚀 Welcome to the Interactive Language REPL! 🚀
                %s✨
                %sVersion: 1.0.0 | Built with ❤️  in Java
                """,
                BRIGHT_CYAN, RESET,
                BRIGHT_CYAN, RESET,
                BRIGHT_CYAN, BRIGHT_YELLOW, BRIGHT_CYAN, RESET,
                BRIGHT_CYAN, RESET,
                BRIGHT_CYAN, BRIGHT_GREEN, BRIGHT_CYAN, RESET,
                BRIGHT_CYAN, RESET,
                BRIGHT_CYAN, DIM, BRIGHT_CYAN, RESET,
                BRIGHT_CYAN, RESET,
                BRIGHT_CYAN, RESET);

        System.out.println(banner);
        System.out.println();
    }

    /**
     * 📚 Shows quick help information
     */
    private void showQuickHelp() {
        System.out.printf("%s💡 Quick Start:%s\n", BRIGHT_YELLOW, RESET);
        System.out.printf("  • Type %s:help%s for full command list\n", CYAN, RESET);
        System.out.printf("  • Type %s:examples%s to see language examples\n", CYAN, RESET);
        System.out.printf("  • Type %s:builtins%s to see available functions\n", CYAN, RESET);
        System.out.printf("  • Type %s:exit%s to quit\n", CYAN, RESET);
        System.out.println();
    }

    /**
     * 📝 Reads input from the user with a beautiful prompt
     */
    private String readInput() {
        System.out.printf("%s[%d]%s %s❯%s ",
                DIM, commandCount + 1, RESET,
                BRIGHT_MAGENTA, RESET);

        try {
            return scanner.nextLine();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 🎯 Handles REPL-specific commands
     */
    private void handleReplCommand(String command) {
        String[] parts = command.split("\\s+");
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case ":help" -> showHelp();
            case ":exit", ":quit", ":q" -> {
                running = false;
            }
            case ":clear", ":cls" -> clearScreen();
            case ":history" -> showHistory();
            case ":env", ":environment" -> showEnvironment();
            case ":builtins" -> showBuiltins();
            case ":examples" -> showExamples();
            case ":reset" -> resetEnvironment();
            case ":version" -> showVersion();
            case ":colors" -> showColorTest();
            default -> printError("Unknown command: " + cmd + ". Type :help for available commands.");
        }
    }

    /**
     * 🔍 Evaluates language code
     */
    private void evaluateCode(String input) {
        try {
            // Tokenize
            Lexer lexer = new Lexer(input);

            // Parse
            LanguageParser parser = new LanguageParser(lexer);
            Program program = parser.parseProgram();

            // Check for parsing errors
            if (parser.hasErrors()) {
                printParsingErrors(parser.getErrors());
                return;
            }

            // Evaluate
            BaseObject result = evaluator.evaluateProgram(program, globalEnvironment);

            // Display result
            displayResult(result);

        } catch (Exception e) {
            printError("Evaluation failed: " + e.getMessage());
        }
    }

    /**
     * 🎨 Displays the evaluation result with beautiful formatting
     */
    private void displayResult(BaseObject result) {
        if (ObjectValidator.isError(result)) {
            ErrorObject error = ObjectValidator.asError(result);
            printError(error.getMessage());
        } else {
            String output = formatOutput(result);
            System.out.printf("%s⟹%s %s\n", BRIGHT_GREEN, RESET, output);
        }
        System.out.println();
    }

    /**
     * 🎨 Formats output with appropriate colors based on type
     */
    private String formatOutput(BaseObject obj) {
        if (obj == null) {
            return colorize("null", DIM);
        }

        switch (obj.type()) {
            case INTEGER -> {
                return colorize(obj.inspect(), BRIGHT_BLUE);
            }
            case STRING -> {
                return colorize("\"" + obj.inspect() + "\"", BRIGHT_GREEN);
            }
            case BOOLEAN -> {
                String value = obj.inspect();
                String color = "true".equals(value) ? BRIGHT_GREEN : BRIGHT_YELLOW;
                return colorize(value, color);
            }
            case ARRAY -> {
                return colorize(obj.inspect(), MAGENTA);
            }
            case HASH -> {
                return colorize(obj.inspect(), CYAN);
            }
            case FUNCTION -> {
                return colorize("fn(...) { ... }", BRIGHT_MAGENTA);
            }
            case NULL -> {
                return colorize("null", DIM);
            }
            default -> {
                return colorize(obj.inspect(), WHITE);
            }
        }
    }

    /**
     * 🚨 Prints parsing errors with beautiful formatting
     */
    private void printParsingErrors(List<ParseError> errors) {
        System.out.printf("%s🚨 Parsing Errors:%s\n", RED, RESET);
        System.out.println();

        for (ParseError error : errors) {
            System.out.printf("%s  ❌ %s%s\n", RED, error.getMessage(), RESET);
            System.out.printf("%s     at %s%s\n", DIM, error.getPosition(), RESET);
        }
        System.out.println();
    }

    /**
     * 📚 Shows comprehensive help information
     */
    private void showHelp() {
        System.out.printf("\n%s╔══════════════════════════════════════════════════════════════════════╗%s\n",
                BRIGHT_CYAN, RESET);
        System.out.printf("%s║                              📚 HELP GUIDE 📚                        ║%s\n", BRIGHT_CYAN,
                RESET);
        System.out.printf("%s╚══════════════════════════════════════════════════════════════════════╝%s\n", BRIGHT_CYAN,
                RESET);

        System.out.printf("\n%s🎯 REPL Commands:%s\n", BRIGHT_YELLOW, RESET);
        printHelpItem(":help", "Show this help message");
        printHelpItem(":exit, :quit, :q", "Exit the REPL");
        printHelpItem(":clear, :cls", "Clear the screen");
        printHelpItem(":history", "Show command history");
        printHelpItem(":env", "Show current environment variables");
        printHelpItem(":builtins", "Show available built-in functions");
        printHelpItem(":examples", "Show language usage examples");
        printHelpItem(":reset", "Reset the environment");
        printHelpItem(":version", "Show version information");
        printHelpItem(":colors", "Test color output");

        System.out.printf("\n%s🔤 Language Features:%s\n", BRIGHT_YELLOW, RESET);
        System.out.printf("  • %sVariables:%s let x = 5; const y = 10;\n", BRIGHT_CYAN, RESET);
        System.out.printf("  • %sFunctions:%s fn add(a, b) { return a + b; }\n", BRIGHT_CYAN, RESET);
        System.out.printf("  • %sArrays:%s [1, 2, 3, \"hello\"]\n", BRIGHT_CYAN, RESET);
        System.out.printf("  • %sHashes:%s {\"name\": \"Alice\", \"age\": 30}\n", BRIGHT_CYAN, RESET);
        System.out.printf("  • %sControl Flow:%s if, elif, else, while, for\n", BRIGHT_CYAN, RESET);
        System.out.printf("  • %sOperators:%s +, -, *, /, %, ==, !=, <, >, &&, ||\n", BRIGHT_CYAN, RESET);

        System.out.println();
    }

    private void printHelpItem(String command, String description) {
        System.out.printf("  %s%-20s%s %s%s\n", CYAN, command, RESET, description, RESET);
    }

    /**
     * 📜 Shows command history
     */
    private void showHistory() {
        if (commandHistory.isEmpty()) {
            System.out.printf("%sNo command history yet.%s\n\n", DIM, RESET);
            return;
        }

        System.out.printf("\n%s📜 Command History:%s\n", BRIGHT_YELLOW, RESET);
        for (int i = 0; i < commandHistory.size(); i++) {
            String cmd = commandHistory.get(i);
            System.out.printf("%s[%d]%s %s\n", DIM, i + 1, RESET, cmd);
        }
        System.out.println();
    }

    /**
     * 🌍 Shows current environment variables
     */
    private void showEnvironment() {
        System.out.printf("\n%s🌍 Current Environment:%s\n", BRIGHT_YELLOW, RESET);

        Map<String, BaseObject> env = globalEnvironment.getLocalVariableBindings();
        if (env.isEmpty()) {
            System.out.printf("%sNo user-defined variables.%s\n", DIM, RESET);
        } else {
            env.entrySet().stream()
                    .filter(entry -> !BuiltinRegistry.isBuiltin(entry.getKey()))
                    .forEach(entry -> {
                        String name = entry.getKey();
                        BaseObject value = entry.getValue();
                        String formattedValue = formatOutput(value);
                        System.out.printf("  %s%s%s = %s\n", BRIGHT_CYAN, name, RESET, formattedValue);
                    });
        }
        System.out.println();
    }

    /**
     * 🔧 Shows available built-in functions
     */
    private void showBuiltins() {
        System.out.printf("\n%s🔧 Built-in Functions:%s\n", BRIGHT_YELLOW, RESET);

        Map<String, Set<String>> categories = BuiltinRegistry.getCategoryMapping();

        categories.forEach((category, functions) -> {
            System.out.printf("\n%s📂 %s:%s\n", BRIGHT_MAGENTA, category, RESET);
            functions.forEach(func -> {
                BaseObject builtin = BuiltinRegistry.getBuiltin(func);
                if (builtin instanceof lang.exec.objects.functions.BuiltinObject) {
                    String desc = ((lang.exec.objects.functions.BuiltinObject) builtin).getDescription();
                    System.out.printf("  %s%-15s%s %s%s\n", CYAN, func, RESET, DIM, desc, RESET);
                }
            });
        });
        System.out.println();
    }

    /**
     * 💡 Shows language usage examples
     */
    private void showExamples() {
        System.out.printf("\n%s💡 Language Examples:%s\n", BRIGHT_YELLOW, RESET);

        Example[] examples = {
                new Example("Variables",
                        "let name = \"Alice\";\nlet age = 25;\nprint(name, \"is\", age, \"years old\");"),
                new Example("Functions",
                        "fn greet(name) {\n  return \"Hello, \" + name + \"!\";\n}\ngreet(\"World\");"),
                new Example("Arrays", "let numbers = [1, 2, 3, 4, 5];\nlen(numbers);\nfirst(numbers);\nlast(numbers);"),
                new Example("Hash Maps",
                        "let person = {\"name\": \"Bob\", \"age\": 30};\nperson[\"name\"];\nkeys(person);"),
                new Example("Control Flow",
                        "let x = 10;\nif (x > 5) {\n  print(\"x is greater than 5\");\n} else {\n  print(\"x is 5 or less\");\n}"),
                new Example("Loops", "for (let i = 0; i < 5; i = i + 1) {\n  print(\"Count:\", i);\n}")
        };

        for (Example example : examples) {
            System.out.printf("\n%s🔹 %s:%s\n", BRIGHT_GREEN, example.title, RESET);
            String[] lines = example.code.split("\n");
            for (String line : lines) {
                System.out.printf("   %s%s%s\n", CYAN, line, RESET);
            }
        }
        System.out.println();
    }

    /**
     * 🔄 Resets the environment
     */
    private void resetEnvironment() {
        Map<String, BaseObject> store = globalEnvironment.getLocalVariableBindings();

        // Remove all user-defined variables, keep builtins
        store.entrySet().removeIf(entry -> !BuiltinRegistry.isBuiltin(entry.getKey()));

        commandCount = 0;
        System.out.printf("%s✨ Environment reset successfully!%s\n\n", BRIGHT_GREEN, RESET);
    }

    /**
     * 📋 Shows version information
     */
    private void showVersion() {
        System.out.printf("\n%s📋 Version Information:%s\n", BRIGHT_YELLOW, RESET);
        System.out.printf("  %sLanguage Version:%s 1.0.0\n", BRIGHT_CYAN, RESET);
        System.out.printf("  %sREPL Version:%s 1.0.0\n", BRIGHT_CYAN, RESET);
        System.out.printf("  %sJava Version:%s %s\n", BRIGHT_CYAN, RESET, System.getProperty("java.version"));
        System.out.printf("  %sBuilt-in Functions:%s %d\n", BRIGHT_CYAN, RESET,
                BuiltinRegistry.getAllBuiltinNames().size());
        System.out.println();
    }

    /**
     * 🎨 Shows color test
     */
    private void showColorTest() {
        System.out.printf("\n%s🎨 Color Test:%s\n\n", BRIGHT_YELLOW, RESET);

        System.out.printf("%sRed%s ", RED, RESET);
        System.out.printf("%sGreen%s ", GREEN, RESET);
        System.out.printf("%sYellow%s ", YELLOW, RESET);
        System.out.printf("%sBlue%s ", BLUE, RESET);
        System.out.printf("%sMagenta%s ", MAGENTA, RESET);
        System.out.printf("%sCyan%s\n", CYAN, RESET);

        System.out.printf("%sBright Red%s ", "\u001B[91m", RESET);
        System.out.printf("%sBright Green%s ", BRIGHT_GREEN, RESET);
        System.out.printf("%sBright Yellow%s ", BRIGHT_YELLOW, RESET);
        System.out.printf("%sBright Blue%s ", BRIGHT_BLUE, RESET);
        System.out.printf("%sBright Magenta%s ", BRIGHT_MAGENTA, RESET);
        System.out.printf("%sBright Cyan%s\n", BRIGHT_CYAN, RESET);

        System.out.printf("%s%sBold%s ", BOLD, "Bold Text", RESET);
        System.out.printf("%s%sDim%s\n", DIM, "Dim Text", RESET);
        System.out.println();
    }

    /**
     * 🧹 Clears the screen
     */
    private void clearScreen() {
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }

    /**
     * 👋 Shows goodbye message
     */
    private void showGoodbyeMessage() {
        System.out.printf("\n%s👋 Thank you for using the Language REPL!%s\n", BRIGHT_GREEN, RESET);
        System.out.printf("%sCommands executed: %d%s\n", DIM, commandCount, RESET);
        System.out.printf("%sGoodbye! 🚀%s\n\n", BRIGHT_CYAN, RESET);
    }

    /**
     * 🚨 Prints error messages with formatting
     */
    private void printError(String message) {
        System.out.printf("%s❌ Error:%s %s\n\n", RED, RESET, message);
    }

    /**
     * 🎨 Helper method to colorize text
     */
    private String colorize(String text, String color) {
        return color + text + RESET;
    }

    /**
     * 🏗️ Creates the global environment with built-ins
     */
    private Environment createGlobalEnvironment() {
        Environment env = new Environment();

        // Add all built-in functions
        BuiltinRegistry.getAllBuiltins().forEach((name, builtin) -> {
            env.defineVariable(name, builtin);
        });

        return env;
    }

    /**
     * 📚 Helper class for examples
     */
    private static record Example(String title, String code) {
    }
}