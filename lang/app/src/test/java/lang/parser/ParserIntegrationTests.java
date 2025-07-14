package lang.parser;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import lang.lexer.Lexer;
import lang.ast.statements.*;
import lang.ast.base.*;
import lang.ast.expressions.*;
import lang.ast.literals.*;

/**
 * 🌟 Comprehensive Parser Integration Tests 🌟
 * 
 * This test suite validates complete programs and complex scenarios:
 * 1. Real-world program structures
 * 2. Nested constructs and complex expressions
 * 3. Integration between all language features
 * 4. Performance with larger programs
 * 5. Complete workflows from source to AST
 * 
 * Testing philosophy:
 * - Test complete, realistic programs
 * - Verify that all components work together
 * - Test complex nesting and interactions
 * - Validate end-to-end parsing workflows
 */
@DisplayName("Parser Integration Tests")
public class ParserIntegrationTests {

    private LanguageParser parser;

    // ═══════════════════════════════════════════════════════════════════════════
    // 🧮 FIBONACCI PROGRAM TEST
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🧮 Parse complete Fibonacci function")
    void testCompleteFibonacciProgram() {
        String input = """
                let fibonacci = fn(n) {
                    if (n <= 1) {
                        return n;
                    } elif (n == 2) {
                        return 1;
                    } else {
                        return fibonacci(n - 1) + fibonacci(n - 2);
                    }
                };

                let result = fibonacci(10);
                return result;
                """;

        parser = new LanguageParser(new Lexer(input));
        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Fibonacci program should parse without errors");
        assertEquals(3, program.getStatements().size(), "Should have 3 statements");

        // Verify first statement is expression statement containing function literal
        Statement firstStmt = program.getStatements().get(0);
        assertTrue(firstStmt instanceof LetStatement);
        LetStatement letStmt = (LetStatement) firstStmt;
        assertTrue(letStmt.getValue() instanceof FunctionLiteral);

        FunctionLiteral fibFunc = (FunctionLiteral) letStmt.getValue();
        assertEquals(1, fibFunc.getParameters().size());
        assertEquals("n", fibFunc.getParameters().get(0).getValue());

        // Verify function body contains if expression
        BlockStatement body = fibFunc.getBody();
        assertEquals(1, body.getStatements().size());
        assertTrue(body.getStatements().get(0) instanceof ExpressionStatement);

        ExpressionStatement bodyExpr = (ExpressionStatement) body.getStatements().get(0);
        assertTrue(bodyExpr.getExpression() instanceof IfExpression);

        IfExpression ifExpr = (IfExpression) bodyExpr.getExpression();
        assertEquals(2, ifExpr.getConditions().size()); // if and elif
        assertEquals(2, ifExpr.getConsequences().size());
        assertNotNull(ifExpr.getAlternative()); // else block

        // Verify second statement is let statement
        Statement secondStmt = program.getStatements().get(1);
        assertTrue(secondStmt instanceof LetStatement);
        LetStatement resultStmt = (LetStatement) secondStmt;
        assertEquals("result", resultStmt.getName().getValue());
        assertTrue(resultStmt.getValue() instanceof CallExpression);

        // Verify third statement is return statement
        Statement thirdStmt = program.getStatements().get(2);
        assertTrue(thirdStmt instanceof ReturnStatement);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔢 CALCULATOR PROGRAM TEST
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🔢 Parse calculator with operators and functions")
    void testCalculatorProgram() {
        String input = """
                const PI = 3;
                const E = 2;

                let add = fn(x, y) {
                    return x + y;
                };

                let multiply = fn(x, y) {
                    return x * y;
                };

                let circleArea = fn(radius) {
                    return PI * radius * radius;
                };

                let numbers = [1, 2, 3, 4, 5];
                let operations = {"add": add, "multiply": multiply};

                let result = add(multiply(2, 3), circleArea(5));

                for (let i = 0; i < len(numbers); i = i + 1) {
                    if (numbers[i] % 2 == 0) {
                        result = result + numbers[i];
                    } else {
                        result = result - numbers[i];
                    }
                }

                return result;
                """;

        parser = new LanguageParser(new Lexer(input));
        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Calculator program should parse without errors");
        assertEquals(10, program.getStatements().size(), "Should have 8 statements");

        // Verify constants
        assertTrue(program.getStatements().get(0) instanceof ConstStatement);
        assertTrue(program.getStatements().get(1) instanceof ConstStatement);

        ConstStatement piConst = (ConstStatement) program.getStatements().get(0);
        assertEquals("PI", piConst.getName().getValue());

        // Verify function definitions
        assertTrue(program.getStatements().get(2) instanceof LetStatement);
        assertTrue(program.getStatements().get(3) instanceof LetStatement);
        assertTrue(program.getStatements().get(4) instanceof LetStatement);

        // Verify array literal
        assertTrue(program.getStatements().get(5) instanceof LetStatement);
        LetStatement arrayStmt = (LetStatement) program.getStatements().get(5);
        assertEquals("numbers", arrayStmt.getName().getValue());
        assertTrue(arrayStmt.getValue() instanceof ArrayLiteral);

        ArrayLiteral arrayLit = (ArrayLiteral) arrayStmt.getValue();
        assertEquals(5, arrayLit.getElements().size());

        // Verify hash literal
        assertTrue(program.getStatements().get(6) instanceof LetStatement);
        LetStatement hashStmt = (LetStatement) program.getStatements().get(6);
        assertEquals("operations", hashStmt.getName().getValue());
        assertTrue(hashStmt.getValue() instanceof HashLiteral);

        // Verify for loop
        assertTrue(program.getStatements().get(8) instanceof ForStatement);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🎮 SIMPLE GAME LOGIC TEST
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🎮 Parse game logic with state management")
    void testGameLogicProgram() {
        String input = """
                let player = {
                    "name": "Hero",
                    "health": 100,
                    "level": 1,
                    "inventory": ["sword", "potion"]
                };

                let takeDamage = fn(amount) {
                    player["health"] = player["health"] - amount;
                    if (player["health"] <= 0) {
                        player["health"] = 0;
                        return "Game Over";
                    }
                    return "Still alive";
                };

                let usePotion = fn() {
                    let hasPotion = false;
                    let inventory = player["inventory"];

                    for (let i = 0; i < len(inventory); i = i + 1) {
                        if (inventory[i] == "potion") {
                            hasPotion = true;
                            break;
                        }
                    }

                    if (hasPotion) {
                        player["health"] = player["health"] + 50;
                        if (player["health"] > 100) {
                            player["health"] = 100;
                        }
                        return "Health restored";
                    } else {
                        return "No potion available";
                    }
                };

                let gameLoop = fn() {
                    while (player["health"] > 0) {
                        let action = getInput();

                        if (action == "attack") {
                            let damage = random(10, 20);
                            takeDamage(damage);
                        } elif (action == "heal") {
                            usePotion();
                        } elif (action == "quit") {
                            break;
                        } else {
                            continue;
                        }
                    }

                    return "Game ended";
                };
                """;

        parser = new LanguageParser(new Lexer(input));
        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Game logic should parse without errors");
        assertEquals(4, program.getStatements().size(), "Should have 4 statements");

        // Verify player object (let statement with hash literal)
        assertTrue(program.getStatements().get(0) instanceof LetStatement);
        LetStatement playerStmt = (LetStatement) program.getStatements().get(0);
        assertEquals("player", playerStmt.getName().getValue());
        assertTrue(playerStmt.getValue() instanceof HashLiteral);

        HashLiteral playerObj = (HashLiteral) playerStmt.getValue();
        assertTrue(playerObj.getPairs().containsKey("name"));
        assertTrue(playerObj.getPairs().containsKey("health"));
        assertTrue(playerObj.getPairs().containsKey("level"));
        assertTrue(playerObj.getPairs().containsKey("inventory"));

        // Verify functions are expression statements with function literals
        for (int i = 1; i < 4; i++) {
            assertTrue(program.getStatements().get(i) instanceof LetStatement);
            LetStatement funcStmt = (LetStatement) program.getStatements().get(i);
            assertTrue(funcStmt.getValue() instanceof FunctionLiteral);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 📊 DATA PROCESSING PROGRAM TEST
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("📊 Parse data processing with complex operations")
    void testDataProcessingProgram() {
        String input = """
                let data = [
                    {"name": "Alice", "age": 30, "score": 85},
                    {"name": "Bob", "age": 25, "score": 92},
                    {"name": "Charlie", "age": 35, "score": 78}
                ];

                let filterByAge = fn(records, minAge) {
                    let filtered = [];

                    for (let i = 0; i < len(records); i = i + 1) {
                        let record = records[i];
                        if (record["age"] >= minAge) {
                            filtered = append(filtered, record);
                        }
                    }

                    return filtered;
                };

                let calculateAverage = fn(records, field) {
                    let sum = 0;
                    let count = 0;

                    for (let i = 0; i < len(records); i = i + 1) {
                        let record = records[i];
                        sum = sum + record[field];
                        count = count + 1;
                    }

                    if (count > 0) {
                        return sum / count;
                    } else {
                        return 0;
                    }
                };

                let adults = filterByAge(data, 30);
                let averageScore = calculateAverage(adults, "score");

                if (averageScore > 80) {
                    return "High performance group";
                } else {
                    return "Average performance group";
                }
                """;

        parser = new LanguageParser(new Lexer(input));
        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Data processing program should parse without errors");
        assertEquals(6, program.getStatements().size(), "Should have 6 statements");

        // Verify data array with hash literals
        assertTrue(program.getStatements().get(0) instanceof LetStatement);
        LetStatement dataStmt = (LetStatement) program.getStatements().get(0);
        assertEquals("data", dataStmt.getName().getValue());
        assertTrue(dataStmt.getValue() instanceof ArrayLiteral);

        ArrayLiteral dataArray = (ArrayLiteral) dataStmt.getValue();
        assertEquals(3, dataArray.getElements().size());

        // Each element should be a hash literal
        for (Expression element : dataArray.getElements()) {
            assertTrue(element instanceof HashLiteral);
        }

        // Verify function definitions
        assertTrue(program.getStatements().get(1) instanceof LetStatement);
        assertTrue(program.getStatements().get(2) instanceof LetStatement);

        // Verify final if statement
        assertTrue(program.getStatements().get(5) instanceof ExpressionStatement);
        ExpressionStatement finalStmt = (ExpressionStatement) program.getStatements().get(5);
        assertTrue(finalStmt.getExpression() instanceof IfExpression);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔄 NESTED LOOPS AND COMPLEX CONTROL FLOW TEST
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🔄 Parse nested loops with complex control flow")
    void testNestedLoopsProgram() {
        String input = """
                let printMatrix = fn(matrix) {
                    let rows = len(matrix);

                    for (let i = 0; i < rows; i = i + 1) {
                        let row = matrix[i];
                        let cols = len(row);

                        for (let j = 0; j < cols; j = j + 1) {
                            let value = row[j];

                            if (value == 0) {
                                continue;
                            }

                            if (value < 0) {
                                print("Negative: " + value);
                            } elif (value > 100) {
                                print("Large: " + value);
                                break;
                            } else {
                                print("Normal: " + value);
                            }
                        }
                    }
                };

                let findElement = fn(matrix, target) {
                    let rows = len(matrix);

                    for (let i = 0; i < rows; i = i + 1) {
                        let row = matrix[i];
                        let cols = len(row);

                        for (let j = 0; j < cols; j = j + 1) {
                            if (row[j] == target) {
                                return {"found": true, "row": i, "col": j};
                            }
                        }
                    }

                    return {"found": false, "row": -1, "col": -1};
                };

                let testMatrix = [
                    [1, 2, 3],
                    [4, 5, 6],
                    [7, 8, 9]
                ];

                printMatrix(testMatrix);
                let result = findElement(testMatrix, 5);

                while (result["found"]) {
                    print("Found at: " + result["row"] + ", " + result["col"]);
                    break;
                }
                """;

        parser = new LanguageParser(new Lexer(input));
        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Nested loops program should parse without errors");
        assertEquals(6, program.getStatements().size(), "Should have 6 statements");

        // Verify first function (printMatrix)
        assertTrue(program.getStatements().get(0) instanceof LetStatement);
        LetStatement printMatrixStmt = (LetStatement) program.getStatements().get(0);
        assertTrue(printMatrixStmt.getValue() instanceof FunctionLiteral);

        FunctionLiteral printMatrix = (FunctionLiteral) printMatrixStmt.getValue();
        assertEquals(1, printMatrix.getParameters().size());
        assertEquals("matrix", printMatrix.getParameters().get(0).getValue());

        // Verify second function (findElement)
        assertTrue(program.getStatements().get(1) instanceof LetStatement);
        LetStatement findElementStmt = (LetStatement) program.getStatements().get(1);
        assertTrue(findElementStmt.getValue() instanceof FunctionLiteral);

        // Verify matrix definition
        assertTrue(program.getStatements().get(2) instanceof LetStatement);
        LetStatement matrixStmt = (LetStatement) program.getStatements().get(2);
        assertEquals("testMatrix", matrixStmt.getName().getValue());
        assertTrue(matrixStmt.getValue() instanceof ArrayLiteral);

        ArrayLiteral matrixArray = (ArrayLiteral) matrixStmt.getValue();
        assertEquals(3, matrixArray.getElements().size());

        // Each row should also be an array literal
        for (Expression row : matrixArray.getElements()) {
            assertTrue(row instanceof ArrayLiteral);
            ArrayLiteral rowArray = (ArrayLiteral) row;
            assertEquals(3, rowArray.getElements().size());
        }

        // Verify while loop at the end
        assertTrue(program.getStatements().get(5) instanceof WhileStatement);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🧪 COMPLEX EXPRESSION EVALUATION TEST
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🧪 Parse complex mathematical expressions")
    void testComplexMathematicalExpressions() {
        String input = """
                let result = (2 + 3) * (4 - 1) / ((5 + 2) % 3);
                let condition = (x > 0 && y < 10) || (z == 5 && w != 3);
                let assignment = b + c * d - e / f;
                let functionCall = max(min(a, b), abs(c - d));
                let arrayAccess = matrix[i + 1][j * 2] + vector[k];
                let stringOp = "Hello" + " " + "World" + "!";
                let comparison = (a + b) == (c * d) && (e - f) != (g / h);
                """;

        parser = new LanguageParser(new Lexer(input));
        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Complex expressions should parse without errors");
        assertEquals(7, program.getStatements().size(), "Should have 7 statements");

        // Verify each statement is a let statement with complex expression
        for (Statement stmt : program.getStatements()) {
            assertTrue(stmt instanceof LetStatement, "Each statement should be a let statement");
            LetStatement letStmt = (LetStatement) stmt;
            assertNotNull(letStmt.getValue(), "Each let statement should have a value");
        }

        // Verify specific expression types
        LetStatement resultStmt = (LetStatement) program.getStatements().get(0);
        assertEquals("result", resultStmt.getName().getValue());
        assertTrue(resultStmt.getValue() instanceof InfixExpression);

        LetStatement conditionStmt = (LetStatement) program.getStatements().get(1);
        assertEquals("condition", conditionStmt.getName().getValue());
        assertTrue(conditionStmt.getValue() instanceof InfixExpression);

        LetStatement assignmentStmt = (LetStatement) program.getStatements().get(2);
        assertEquals("assignment", assignmentStmt.getName().getValue());
        assertTrue(assignmentStmt.getValue() instanceof InfixExpression);

        LetStatement functionCallStmt = (LetStatement) program.getStatements().get(3);
        assertEquals("functionCall", functionCallStmt.getName().getValue());
        assertTrue(functionCallStmt.getValue() instanceof CallExpression);

        LetStatement arrayAccessStmt = (LetStatement) program.getStatements().get(4);
        assertEquals("arrayAccess", arrayAccessStmt.getName().getValue());
        assertTrue(arrayAccessStmt.getValue() instanceof InfixExpression);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🌐 COMPREHENSIVE LANGUAGE FEATURE TEST
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🌐 Parse program using all language features")
    void testComprehensiveLanguageFeatures() {
        String input = """
                // Constants and variables
                const VERSION = "1";
                const MAX_RETRIES = 3;
                let globalState = {"initialized": false};

                // Function with all control structures
                let processData = fn(input, options) {
                    if (!input) {
                        return {"error": "No input provided"};
                    }

                    let results = [];
                    let retries = 0;

                    while (retries < MAX_RETRIES) {
                        let success = false;

                        for (let i = 0; i < len(input); i = i + 1) {
                            let item = input[i];

                            if (item["type"] == "skip") {
                                continue;
                            }

                            if (item["value"] < 0) {
                                print("Negative value encountered");
                                break;
                            }

                            let processed = {
                                "original": item,
                                "processed": item["value"] * 2,
                                "timestamp": getTime()
                            };

                            results = append(results, processed);
                            success = true;
                        }

                        if (success) {
                            break;
                        }

                        retries = retries + 1;
                    }

                    return {
                        "results": results,
                        "retries": retries,
                        "success": len(results) > 0
                    };
                };

                // Test data and execution
                let testData = [
                    {"type": "process", "value": 10},
                    {"type": "skip", "value": 5},
                    {"type": "process", "value": 20}
                ];

                let config = {"strict": true, "verbose": false};
                let output = processData(testData, config);

                // Final result processing
                if (output["success"]) {
                    globalState["initialized"] = true;
                    return "Processing completed successfully";
                } else {
                    return "Processing failed after " + output["retries"] + " retries";
                }
                """;

        parser = new LanguageParser(new Lexer(input));
        Program program = parser.parseProgram();

        assertFalse(parser.hasErrors(), "Comprehensive program should parse without errors");
        assertEquals(8, program.getStatements().size(), "Should have 8 statements");

        // Verify constants
        assertTrue(program.getStatements().get(0) instanceof ConstStatement);
        assertTrue(program.getStatements().get(1) instanceof ConstStatement);

        // Verify global state variable
        assertTrue(program.getStatements().get(2) instanceof LetStatement);
        LetStatement globalStateStmt = (LetStatement) program.getStatements().get(2);
        assertEquals("globalState", globalStateStmt.getName().getValue());
        assertTrue(globalStateStmt.getValue() instanceof HashLiteral);

        // Verify main function
        assertTrue(program.getStatements().get(3) instanceof LetStatement);
        LetStatement funcStmt = (LetStatement) program.getStatements().get(3);
        assertTrue(funcStmt.getValue() instanceof FunctionLiteral);

        FunctionLiteral mainFunc = (FunctionLiteral) funcStmt.getValue();
        assertEquals(2, mainFunc.getParameters().size());
        assertEquals("input", mainFunc.getParameters().get(0).getValue());
        assertEquals("options", mainFunc.getParameters().get(1).getValue());

        // Verify test data
        assertTrue(program.getStatements().get(4) instanceof LetStatement);
        LetStatement testDataStmt = (LetStatement) program.getStatements().get(4);
        assertEquals("testData", testDataStmt.getName().getValue());
        assertTrue(testDataStmt.getValue() instanceof ArrayLiteral);

        // Verify final if statement
        assertTrue(program.getStatements().get(7) instanceof ExpressionStatement);
        ExpressionStatement finalStmt = (ExpressionStatement) program.getStatements().get(7);
        assertTrue(finalStmt.getExpression() instanceof IfExpression);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 📊 PERFORMANCE AND SCALE TEST
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("📊 Parse large program with many statements")
    void testLargeProgramParsing() {
        StringBuilder inputBuilder = new StringBuilder();

        // Generate a large program with 100 statements
        for (int i = 0; i < 100; i++) {
            inputBuilder.append("let var").append(i).append(" = ").append(i).append(";\n");
        }

        String input = inputBuilder.toString();
        parser = new LanguageParser(new Lexer(input));

        // Measure parsing time (basic performance check)
        long startTime = System.currentTimeMillis();
        Program program = parser.parseProgram();
        long endTime = System.currentTimeMillis();

        assertFalse(parser.hasErrors(), "Large program should parse without errors");
        assertEquals(100, program.getStatements().size(), "Should have 100 statements");

        // Basic performance assertion (should parse quickly)
        long parseTime = endTime - startTime;
        assertTrue(parseTime < 1000,
                "Parsing 100 statements should take less than 1 second, took: " + parseTime + "ms");

        // Verify some statements are correctly parsed
        for (int i = 0; i < 10; i++) {
            Statement stmt = program.getStatements().get(i);
            assertTrue(stmt instanceof LetStatement);
            LetStatement letStmt = (LetStatement) stmt;
            assertEquals("var" + i, letStmt.getName().getValue());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 🔄 PARSING WORKFLOW TEST
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🔄 Test complete parsing workflow")
    void testCompleteParsingWorkflow() {
        String input = """
                let program = "Parser Test";
                print("Starting: " + program);
                """;

        // Test the complete workflow: String -> Lexer -> Parser -> AST
        Lexer lexer = new Lexer(input);
        parser = new LanguageParser(lexer);
        Program program = parser.parseProgram();

        // Verify workflow completed successfully
        assertNotNull(lexer, "Lexer should be created");
        assertNotNull(parser, "Parser should be created");
        assertNotNull(program, "Program should be created");
        assertFalse(parser.hasErrors(), "Parsing should complete without errors");

        // Verify AST structure
        assertEquals(2, program.getStatements().size());
        assertNotNull(program.toString(), "Program should have string representation");
        assertNotNull(program.position(), "Program should have position information");

        // Verify each statement has position information
        for (Statement stmt : program.getStatements()) {
            assertNotNull(stmt.position(), "Each statement should have position");
            assertNotNull(stmt.tokenLiteral(), "Each statement should have token literal");
            assertNotNull(stmt.toString(), "Each statement should have string representation");
        }
    }
}