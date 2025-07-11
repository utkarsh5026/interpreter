package lang.examples;

import lang.ast.visualization.*;
import lang.lexer.Lexer;
import lang.parser.LanguageParser;
import lang.ast.statements.*;

/**
 * Comprehensive examples showing how to use the AST Tree Visualizer
 */
public class AstVisualizerExamples {

    public static void show() {
        System.out.println("🌳 AST Tree Visualizer Examples\n");
        System.out.println("=".repeat(60));

        // Example programs to visualize
        demonstrateBasicUsage();
        demonstrateStyleVariations();
        demonstrateComplexProgram();
        demonstrateConfigurationOptions();
    }

    /**
     * Basic usage with default settings
     */
    private static void demonstrateBasicUsage() {
        System.out.println("\n📋 BASIC USAGE");
        System.out.println("-".repeat(40));

        String code = """
                let x = 5 + 3;
                let message = "Hello, World!";
                return x * 2;
                """;

        Program ast = parseCode(code);
        AstTreeVisualizer visualizer = new AstTreeVisualizer();

        System.out.println("Source Code:");
        System.out.println(code);
        System.out.println("AST Tree:");
        System.out.println(visualizer.visualize(ast));
    }

    /**
     * Different visual styles
     */
    private static void demonstrateStyleVariations() {
        System.out.println("\n🎨 STYLE VARIATIONS");
        System.out.println("-".repeat(40));

        String code = "let result = func(x + 1, arr[0]);";
        Program ast = parseCode(code);

        // Unicode tree style (default)
        System.out.println("📐 Unicode Tree Style:");
        VisualizationConfig unicodeConfig = VisualizationConfig.builder()
                .style(AstTreeVisualizer.Style.UNICODE_TREE)
                .colorScheme(AstTreeVisualizer.ColorScheme.TERMINAL)
                .build();
        System.out.println(new AstTreeVisualizer(unicodeConfig).visualize(ast));

        // ASCII tree style
        System.out.println("📝 ASCII Tree Style:");
        VisualizationConfig asciiConfig = VisualizationConfig.builder()
                .style(AstTreeVisualizer.Style.ASCII_TREE)
                .colorScheme(AstTreeVisualizer.ColorScheme.NONE)
                .build();
        System.out.println(new AstTreeVisualizer(asciiConfig).visualize(ast));

        // Compact style
        System.out.println("📦 Compact Style:");
        VisualizationConfig compactConfig = VisualizationConfig.builder()
                .style(AstTreeVisualizer.Style.COMPACT)
                .compactMode(true)
                .showMetadata(false)
                .showPositions(false)
                .build();
        System.out.println(new AstTreeVisualizer(compactConfig).visualize(ast));
    }

    /**
     * Complex program with control flow
     */
    private static void demonstrateComplexProgram() {
        System.out.println("\n🏗️ COMPLEX PROGRAM");
        System.out.println("-".repeat(40));

        String code = """
                fn fibonacci(n) {
                    if (n <= 1) {
                        return n;
                    } elif (n == 2) {
                        return 1;
                    } else {
                        return fibonacci(n - 1) + fibonacci(n - 2);
                    }
                }

                let numbers = [1, 2, 3, 4, 5];
                let result = 0;

                for (let i = 0; i < len(numbers); i = i + 1) {
                    if (numbers[i] % 2 == 0) {
                        result = result + fibonacci(numbers[i]);
                    }
                }

                return result;
                """;

        Program ast = parseCode(code);

        VisualizationConfig config = VisualizationConfig.builder()
                .style(AstTreeVisualizer.Style.UNICODE_TREE)
                .colorScheme(AstTreeVisualizer.ColorScheme.TERMINAL)
                .showMetadata(true)
                .showPositions(false)
                .maxDepth(6) // Limit depth for readability
                .build();

        AstTreeVisualizer visualizer = new AstTreeVisualizer(config);
        System.out.println("Source Code:");
        System.out.println(code);
        System.out.println("AST Tree (depth limited to 6):");
        System.out.println(visualizer.visualize(ast));
    }

    /**
     * Different configuration options
     */
    private static void demonstrateConfigurationOptions() {
        System.out.println("\n⚙️ CONFIGURATION OPTIONS");
        System.out.println("-".repeat(40));

        String code = "let data = [1, 2, {\"key\": \"value\"}];";
        Program ast = parseCode(code);

        System.out.println("🔍 Detailed View (all information):");
        VisualizationConfig detailedConfig = VisualizationConfig.builder()
                .style(AstTreeVisualizer.Style.DETAILED)
                .colorScheme(AstTreeVisualizer.ColorScheme.TERMINAL)
                .showMetadata(true)
                .showPositions(true)
                .showIndices(true)
                .showTypes(true)
                .showValues(true)
                .build();
        System.out.println(new AstTreeVisualizer(detailedConfig).visualize(ast));

        System.out.println("\n🎯 Minimal View (types only):");
        VisualizationConfig minimalConfig = VisualizationConfig.builder()
                .style(AstTreeVisualizer.Style.INDENTED)
                .colorScheme(AstTreeVisualizer.ColorScheme.NONE)
                .showMetadata(false)
                .showPositions(false)
                .showIndices(false)
                .showTypes(true)
                .showValues(false)
                .build();
        System.out.println(new AstTreeVisualizer(minimalConfig).visualize(ast));

        System.out.println("\n📊 Values Focus (emphasize values):");
        VisualizationConfig valuesConfig = VisualizationConfig.builder()
                .style(AstTreeVisualizer.Style.UNICODE_TREE)
                .colorScheme(AstTreeVisualizer.ColorScheme.TERMINAL)
                .showMetadata(false)
                .showPositions(false)
                .showTypes(false)
                .showValues(true)
                .build();
        System.out.println(new AstTreeVisualizer(valuesConfig).visualize(ast));
    }

    /**
     * Helper method to parse code
     */
    private static Program parseCode(String code) {
        Lexer lexer = new Lexer(code);
        LanguageParser parser = new LanguageParser(lexer);
        Program program = parser.parseProgram();

        return program;
    }

    public static VisualizationConfig debug() {
        return VisualizationConfig.builder()
                .style(AstTreeVisualizer.Style.DETAILED)
                .colorScheme(AstTreeVisualizer.ColorScheme.TERMINAL)
                .showMetadata(true)
                .showPositions(true)
                .showIndices(true)
                .showTypes(true)
                .showValues(true)
                .build();
    }

    public static VisualizationConfig presentation() {
        return VisualizationConfig.builder()
                .style(AstTreeVisualizer.Style.UNICODE_TREE)
                .colorScheme(AstTreeVisualizer.ColorScheme.TERMINAL)
                .showMetadata(false)
                .showPositions(false)
                .showTypes(true)
                .showValues(true)
                .compactMode(false)
                .build();
    }

    public static VisualizationConfig minimal() {
        return VisualizationConfig.builder()
                .style(AstTreeVisualizer.Style.INDENTED)
                .colorScheme(AstTreeVisualizer.ColorScheme.NONE)
                .showMetadata(false)
                .showPositions(false)
                .showTypes(true)
                .showValues(false)
                .compactMode(true)
                .build();
    }

    public static VisualizationConfig html() {
        return VisualizationConfig.builder()
                .style(AstTreeVisualizer.Style.UNICODE_TREE)
                .colorScheme(AstTreeVisualizer.ColorScheme.HTML)
                .showMetadata(true)
                .showPositions(true)
                .showTypes(true)
                .showValues(true)
                .build();
    }

    public static VisualizationConfig markdown() {
        return VisualizationConfig.builder()
                .style(AstTreeVisualizer.Style.UNICODE_TREE)
                .colorScheme(AstTreeVisualizer.ColorScheme.MARKDOWN)
                .showMetadata(true)
                .showPositions(false)
                .showTypes(true)
                .showValues(true)
                .build();
    }
}