package lang.evaluator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import lang.lexer.Lexer;
import lang.parser.LanguageParser;
import lang.ast.statements.Program;
import lang.exec.evaluator.LanguageEvaluator;
import lang.exec.objects.*;
import lang.exec.base.BaseObject;
import lang.exec.validator.ObjectValidator;

/**
 * 🌍 Real-World Programming Scenarios Test Suite 🌍
 * 
 * This test suite focuses on real-world programming scenarios that combine
 * multiple language features. These tests ensure that the evaluator can handle
 * complex, realistic programs that developers would actually write.
 * 
 * Test Categories:
 * 1. 📊 Data Processing Algorithms
 * 2. 🎮 Game Logic Simulations
 * 3. 🧮 Mathematical Computations
 * 4. 📝 Text Processing
 * 5. 🏗️ Object-Oriented Patterns
 * 6. 🔄 State Management
 * 7. 🎯 Algorithm Implementations
 * 8. 🌐 Complex Data Transformations
 */
public class RealWorldScenariosTest {

    private LanguageEvaluator evaluator;
    private Environment globalEnvironment;

    @BeforeEach
    void setUp() {
        evaluator = new LanguageEvaluator();
        globalEnvironment = new Environment();
    }

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

    // ==========================================
    // DATA PROCESSING ALGORITHMS
    // ==========================================

    @Nested
    @DisplayName("📊 Data Processing Algorithms")
    class DataProcessingTests {

        @Test
        @DisplayName("Find maximum value in array with custom comparison")
        void testFindMaximumValue() {
            String code = """
                    let findMax = fn(arr) { # funtion to check the max value in an array
                        if (len(arr) == 0) {
                            return null;
                        }

                        let max = arr[0];
                        for (let i = 1; i < len(arr); i = i + 1) {
                            if (arr[i] > max) {
                                max = arr[i];
                            }
                        }
                        return max;
                    };

                    let numbers = [3, 7, 2, 9, 1, 8];
                    findMax(numbers);
                    """;

            BaseObject result = evaluateCode(code);
            assertEquals(9, ObjectValidator.asInteger(result).getValue());
        }

        @Test
        @DisplayName("Sort array using bubble sort algorithm")
        void testBubbleSort() {
            String code = """
                    let bubbleSort = fn(arr) {
                        let n = len(arr);
                        for (let i = 0; i < n - 1; i = i + 1) {
                            for (let j = 0; j < n - i - 1; j = j + 1) {
                                if (arr[j] > arr[j + 1]) {
                                    # Swap elements
                                    let temp = arr[j];
                                    arr[j] = arr[j + 1];
                                    arr[j + 1] = temp;
                                }
                            }
                        }
                        return arr;
                    };

                    let unsorted = [64, 34, 25, 12, 22, 11, 90];
                    bubbleSort(unsorted);
                    """;

            BaseObject result = evaluateCode(code);
            ArrayObject sortedArray = ObjectValidator.asArray(result);

            assertEquals(7, sortedArray.size());
            assertEquals(11, ObjectValidator.asInteger(sortedArray.get(0)).getValue());
            assertEquals(12, ObjectValidator.asInteger(sortedArray.get(1)).getValue());
            assertEquals(22, ObjectValidator.asInteger(sortedArray.get(2)).getValue());
            assertEquals(25, ObjectValidator.asInteger(sortedArray.get(3)).getValue());
            assertEquals(34, ObjectValidator.asInteger(sortedArray.get(4)).getValue());
            assertEquals(64, ObjectValidator.asInteger(sortedArray.get(5)).getValue());
            assertEquals(90, ObjectValidator.asInteger(sortedArray.get(6)).getValue());

        }

        @Test
        @DisplayName("Filter and map operations on data")
        void testFilterAndMap() {
            String code = """
                    let filter = fn(arr, predicate) {
                        let result = [];
                        for (let i = 0; i < len(arr); i = i + 1) {
                            if (predicate(arr[i])) {
                                result = push(result, arr[i]);
                            }
                        }
                        return result;
                    };

                    let map = fn(arr, transform) {
                        let result = [];
                        for (let i = 0; i < len(arr); i = i + 1) {
                            result = push(result, transform(arr[i]));
                        }
                        return result;
                    };

                    let isEven = fn(x) { x % 2 == 0; };
                    let double = fn(x) { x * 2; };

                    let numbers = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
                    let evenNumbers = filter(numbers, isEven);
                    let doubledEvens = map(evenNumbers, double);
                    doubledEvens;
                    """;
            // Expected: [4, 8, 12, 16, 20]

            BaseObject result = evaluateCode(code);
            ArrayObject doubledEvens = ObjectValidator.asArray(result);

            // Expected: [4, 8, 12, 16, 20]
            assertEquals(5, doubledEvens.size());
            assertEquals(4, ObjectValidator.asInteger(doubledEvens.get(0)).getValue());
            assertEquals(8, ObjectValidator.asInteger(doubledEvens.get(1)).getValue());
            assertEquals(12, ObjectValidator.asInteger(doubledEvens.get(2)).getValue());
            assertEquals(16, ObjectValidator.asInteger(doubledEvens.get(3)).getValue());
            assertEquals(20, ObjectValidator.asInteger(doubledEvens.get(4)).getValue());
        }

        @Test
        @DisplayName("Group data by category")
        void testGroupBy() {
            String code = """
                    let groupBy = fn(arr, keyFunc) {
                        let groups = {};
                        for (let i = 0; i < len(arr); i = i + 1) {
                            let item = arr[i];
                            let key = keyFunc(item);

                            if (groups[key] == null) {
                                groups[key] = [];
                            }
                            groups[key] = push(groups[key], item);
                        }
                        return groups;
                    };

                    let getFirstLetter = fn(word) {
                        return substr(word, 0, 1);
                    };

                    let words = ["apple", "banana", "cherry", "apricot", "blueberry"];
                    let grouped = groupBy(words, getFirstLetter);
                    len(grouped["a"]);  # Should be 2
                    """;

            BaseObject result = evaluateCode(code);
            assertEquals(2, ObjectValidator.asInteger(result).getValue());
        }

        @Test
        @DisplayName("Calculate statistics from dataset")
        void testStatisticsCalculation() {
            String code = """
                    let calculateStats = fn(numbers) {
                        let sum = 0;
                        let count = len(numbers);

                        # Calculate sum
                        for (let j = 0; j < count; j = j + 1) {
                            sum = sum + numbers[j];
                        }

                        let average = sum // count;

                        # Find min and max
                        let min = numbers[0];
                        let max = numbers[0];

                        for (let i = 1; i < count; i = i + 1) {
                            if (numbers[i] < min) {
                                min = numbers[i];
                            }
                            if (numbers[i] > max) {
                                max = numbers[i];
                            }
                        }

                        return {
                            "sum": sum,
                            "average": average,
                            "min": min,
                            "max": max,
                            "count": count
                        };
                    };

                    let dataset = [15, 23, 8, 42, 16, 4, 35];
                    let stats = calculateStats(dataset);
                    stats;
                    """;
            BaseObject result = evaluateCode(code);
            HashObject stats = ObjectValidator.asHash(result);
            assertEquals(143, ObjectValidator.asInteger(stats.get("sum")).getValue());
            assertEquals(20, ObjectValidator.asInteger(stats.get("average")).getValue());
            assertEquals(4, ObjectValidator.asInteger(stats.get("min")).getValue());
            assertEquals(42, ObjectValidator.asInteger(stats.get("max")).getValue());
            assertEquals(7, ObjectValidator.asInteger(stats.get("count")).getValue());
        }
    }

    // ==========================================
    // GAME LOGIC SIMULATIONS
    // ==========================================

    @Nested
    @DisplayName("🎮 Game Logic Simulations")
    class GameLogicTests {

        @Test
        @DisplayName("Tic-tac-toe game logic")
        void testTicTacToeGame() {
            String code = """
                    let createBoard = fn() {
                        return [
                            [" ", " ", " "],
                            [" ", " ", " "],
                            [" ", " ", " "]
                        ];
                    };

                    let makeMove = fn(board, row, col, player) {
                        if (board[row][col] == " ") {
                            board[row][col] = player;
                            return true;
                        }
                        return false;
                    };

                    let checkWin = fn(board, player) {
                        # Check rows
                        for (let i = 0; i < 3; i = i + 1) {
                            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) {
                                return true;
                            }
                        }

                        # Check columns
                        for (let j = 0; j < 3; j = j + 1) {
                            if (board[0][j] == player && board[1][j] == player && board[2][j] == player) {
                                return true;
                            }
                        }

                        # Check diagonals
                        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) {
                            return true;
                        }
                        if (board[0][2] == player && board[1][1] == player && board[2][0] == player) {
                            return true;
                        }

                        return false;
                    };

                    let board = createBoard();
                    makeMove(board, 0, 0, "X");
                    makeMove(board, 0, 1, "X");
                    makeMove(board, 0, 2, "X");
                    checkWin(board, "X");
                    """;
            // Expected: true (X wins in top row)
            BaseObject result = evaluateCode(code);
            assertTrue(ObjectValidator.asBoolean(result).getValue());
        }

        @Test
        @DisplayName("Simple RPG character system")
        void testRPGCharacterSystem() {
            String code = """
                    let createCharacter = fn(name, className) {
                        let baseStats = {
                            "warrior": {"hp": 100, "attack": 15, "defense": 10},
                            "mage": {"hp": 60, "attack": 20, "defense": 5},
                            "rogue": {"hp": 80, "attack": 18, "defense": 7}
                        };

                        let stats = baseStats[className];

                        return {
                            "name": name,
                            "class": className,
                            "hp": stats["hp"],
                            "maxHp": stats["hp"],
                            "attack": stats["attack"],
                            "defense": stats["defense"],
                            "level": 1,
                            "xp": 0
                        };
                    };

                    let calculateDamage = fn(attacker, defender) {
                        let baseDamage = attacker["attack"];
                        let defense = defender["defense"];
                        let damage = baseDamage - defense;
                        if (damage > 0) {
                            return damage;
                        } else {
                            return 1;
                        }
                    };

                    let attack = fn(attacker, defender) {
                        let damage = calculateDamage(attacker, defender);
                        defender["hp"] = defender["hp"] - damage;
                        return damage;
                    };

                    let warrior = createCharacter("Conan", "warrior");
                    let mage = createCharacter("Gandalf", "mage");

                    let damageDealt = attack(warrior, mage);
                    mage["hp"];
                    """;
            // Expected: 60 - (15 - 5) = 50
            BaseObject result = evaluateCode(code);
            assertEquals(50, ObjectValidator.asInteger(result).getValue());
        }

        @Test
        @DisplayName("Card deck shuffling and dealing")
        void testCardDeckSystem() {
            String code = """
                    let createDeck = fn() {
                        let suits = ["Hearts", "Diamonds", "Clubs", "Spades"];
                        let ranks = ["A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"];
                        let deck = [];

                        for (let i = 0; i < len(suits); i = i + 1) {
                            for (let j = 0; j < len(ranks); j = j + 1) {
                                let card = {"suit": suits[i], "rank": ranks[j]};
                                deck = push(deck, card);
                            }
                        }

                        return deck;
                    };

                    let shuffleDeck = fn(deck) {
                        # Simple shuffle - swap each card with a random position
                        for (let i = 0; i < len(deck); i = i + 1) {
                            let j = i + (random() * (len(deck) - i));  # Assumes random() built-in
                            # Swap cards at positions i and j
                            let temp = deck[i];
                            deck[i] = deck[j];
                            deck[j] = temp;
                        }
                        return deck;
                    };

                    let dealHand = fn(deck, handSize) {
                        let hand = [];
                        for (let i = 0; i < handSize; i = i + 1) {
                            if (len(deck) > 0) {
                                hand = push(hand, deck[0]);
                                deck = rest(deck);  # Assumes rest() built-in to remove first element
                            }
                        }
                        return hand;
                    };

                    let deck = createDeck();
                    len(deck);  # Should be 52
                    """;

            BaseObject result = evaluateCode(code);
            assertEquals(52, ObjectValidator.asInteger(result).getValue());
        }
    }

    // ==========================================
    // MATHEMATICAL COMPUTATIONS
    // ==========================================

    @Nested
    @DisplayName("🧮 Mathematical Computations")
    class MathematicalComputationTests {

        @Test
        @DisplayName("Calculate prime numbers using Sieve of Eratosthenes")
        void testSieveOfEratosthenes() {
            String code = """
                    let sieveOfEratosthenes = fn(limit) {
                        let isPrime = [];

                        # Initialize array
                        for (let i = 0; i <= limit; i = i + 1) {
                            isPrime = push(isPrime, true);
                        }

                        isPrime[0] = false;
                        isPrime[1] = false;

                        for (let i = 2; i * i <= limit; i = i + 1) {
                            if (isPrime[i]) {
                                for (let j = i * i; j <= limit; j = j + i) {
                                    isPrime[j] = false;
                                }
                            }
                        }

                        let primes = [];
                        for (let i = 2; i <= limit; i = i + 1) {
                            if (isPrime[i]) {
                                primes = push(primes, i);
                            }
                        }

                        return primes;
                    };

                    let primesUpTo20 = sieveOfEratosthenes(20);
                    len(primesUpTo20);  # Should be 8 primes: [2,3,5,7,11,13,17,19]
                    """;
            BaseObject result = evaluateCode(code);
            assertEquals(8, ObjectValidator.asInteger(result).getValue());
        }

        @Test
        @DisplayName("Calculate greatest common divisor (GCD)")
        void testGCDCalculation() {
            String code = """
                    let gcd = fn(a, b) {
                        while (b != 0) {
                            let temp = b;
                            b = a % b;
                            a = temp;
                        }
                        return a;
                    };

                    let lcm = fn(a, b) {
                        return (a * b) / gcd(a, b);
                    };

                    # Test with 48 and 18
                    let result = gcd(48, 18);
                    result;  # Should be 6
                    """;

            BaseObject result = evaluateCode(code);
            assertEquals(6, ObjectValidator.asInteger(result).getValue());
        }

        @Test
        @DisplayName("Matrix multiplication")
        void testMatrixMultiplication() {
            String code = """
                    let multiplyMatrices = fn(a, b) {
                        let rowsA = len(a);
                        let colsA = len(a[0]);
                        let colsB = len(b[0]);

                        let result = [];

                        for (let i = 0; i < rowsA; i = i + 1) {
                            let row = [];
                            for (let j = 0; j < colsB; j = j + 1) {
                                let sum = 0;
                                for (let k = 0; k < colsA; k = k + 1) {
                                    sum = sum + (a[i][k] * b[k][j]);
                                }
                                row = push(row, sum);
                            }
                            result = push(result, row);
                        }

                        return result;
                    };

                    let matrixA = [[1, 2], [3, 4]];
                    let matrixB = [[5, 6], [7, 8]];
                    let product = multiplyMatrices(matrixA, matrixB);
                    product[0][0];  # Should be 19 (1*5 + 2*7)
                    """;

            BaseObject result = evaluateCode(code);
            assertEquals(19, ObjectValidator.asInteger(result).getValue());
        }

        @Test
        @DisplayName("Calculate factorial with memoization")
        void testMemoizedFactorial() {
            String code = """
                    let createMemoizedFactorial = fn() {
                        let cache = {};

                        let factorial = fn(n) {
                            if (cache[n] != null) {
                                return cache[n];
                            }

                            let result = 0;
                            if (n <= 1) {
                                result = 1;
                            } else {
                                result = n * factorial(n - 1);
                            }

                            cache[n] = result;
                            return result;
                        };

                        return factorial;
                    };

                    let factorial = createMemoizedFactorial();
                    let result10 = factorial(10);  # Should be 3628800
                    let result5 = factorial(5);    # Should use cached values
                    result5;
                    """;

            BaseObject result = evaluateCode(code);
            assertEquals(120, ObjectValidator.asInteger(result).getValue()); // 5! = 120
        }
    }

    // ==========================================
    // TEXT PROCESSING
    // ==========================================

    @Nested
    @DisplayName("📝 Text Processing")
    class TextProcessingTests {

        @Test
        @DisplayName("Word frequency counter")
        void testWordFrequencyCounter() {
            String code = """
                    let countWords = fn(text) {
                        let words = split(text, " ");  # Assumes split() built-in
                        let counts = {};

                        for (let i = 0; i < len(words); i = i + 1) {
                            let word = lower(words[i]);

                            if (counts[word] == null) {
                                counts[word] = 0;
                            }
                            counts[word] = counts[word] + 1;
                        }

                        return counts;
                    };

                    let text = "the quick brown fox jumps over the lazy dog the fox";
                    let wordCounts = countWords(text);
                    wordCounts;  # Should be 3
                    """;

            BaseObject result = evaluateCode(code);
            HashObject wordCounts = ObjectValidator.asHash(result);
            assertEquals(3, ObjectValidator.asInteger(wordCounts.get("the")).getValue());
            assertEquals(1, ObjectValidator.asInteger(wordCounts.get("quick")).getValue());
            assertEquals(1, ObjectValidator.asInteger(wordCounts.get("brown")).getValue());
            assertEquals(2, ObjectValidator.asInteger(wordCounts.get("fox")).getValue());
            assertEquals(1, ObjectValidator.asInteger(wordCounts.get("jumps")).getValue());
            assertEquals(1, ObjectValidator.asInteger(wordCounts.get("over")).getValue());
            assertEquals(1, ObjectValidator.asInteger(wordCounts.get("lazy")).getValue());
            assertEquals(1, ObjectValidator.asInteger(wordCounts.get("dog")).getValue());
        }

        @Test
        @DisplayName("Palindrome checker")
        void testPalindromeChecker() {
            String code = """
                    let isPalindrome = fn(text) {
                        let cleaned = lower(text);
                        let length = len(cleaned);

                        for (let i = 0; i < length / 2; i = i + 1) {
                            if (charAt(cleaned, i) != charAt(cleaned, length - 1 - i)) {  # Assumes charAt() built-in
                                return false;
                            }
                        }

                        return true;
                    };

                    isPalindrome("racecar");  # Should be true
                    """;

            BaseObject result = evaluateCode(code);
            assertEquals(true, ObjectValidator.asBoolean(result).getValue());
        }
    }

    // ==========================================
    // OBJECT-ORIENTED PATTERNS
    // ==========================================

    @Nested
    @DisplayName("🏗️ Object-Oriented Patterns")
    class ObjectOrientedPatternTests {

        @Test
        @DisplayName("Constructor pattern with methods")
        void testConstructorPattern() {
            String code = """
                    let createBankAccount = fn(initialBalance) {
                        let balance = initialBalance;

                        return {
                            "getBalance": fn() { balance; },
                            "deposit": fn(amount) {
                                if (amount > 0) {
                                    balance = balance + amount;
                                    return true;
                                }
                                return false;
                            },
                            "withdraw": fn(amount) {
                                if (amount > 0 && amount <= balance) {
                                    balance = balance - amount;
                                    return true;
                                }
                                return false;
                            }
                        };
                    };

                    let account = createBankAccount(100);
                    account["deposit"](50);
                    account["withdraw"](30);
                    account["getBalance"]();  # Should be 120
                    """;

            BaseObject result = evaluateCode(code);
            assertEquals(120, ObjectValidator.asInteger(result).getValue());
        }

        @Test
        @DisplayName("Inheritance-like pattern")
        void testInheritancePattern() {
            String code = """
                    let createAnimal = fn(name) {
                        return {
                            "name": name,
                            "speak": fn() { name + " makes a sound"; }
                        };
                    };

                    let createDog = fn(name, breed) {
                        let animal = createAnimal(name);

                        return {
                            "name": animal["name"],
                            "breed": breed,
                            "speak": fn() { animal["name"] + " barks"; },
                            "wagTail": fn() { animal["name"] + " wags tail"; }
                        };
                    };

                    let dog = createDog("Buddy", "Golden Retriever");
                    dog["speak"]();  # Should be "Buddy barks"
                    """;

            BaseObject result = evaluateCode(code);
            assertEquals("Buddy barks", ObjectValidator.asString(result).getValue());
        }
    }

    // ==========================================
    // STATE MANAGEMENT
    // ==========================================

    @Nested
    @DisplayName("🔄 State Management")
    class StateManagementTests {

        @Test
        @DisplayName("Simple state machine")
        void testStateMachine() {
            String code = """
                    let createStateMachine = fn(initialState, transitions) {
                        let currentState = initialState;

                        return {
                            "getState": fn() { currentState; },
                            "transition": fn(event) {
                                let stateTransitions = transitions[currentState];
                                if (stateTransitions != null && stateTransitions[event] != null) {
                                    currentState = stateTransitions[event];
                                    return true;
                                }
                                return false;
                            }
                        };
                    };

                    let doorTransitions = {
                        "closed": {"open": "open"},
                        "open": {"close": "closed", "lock": "closed"}
                    };

                    let door = createStateMachine("closed", doorTransitions);
                    door["transition"]("open");
                    door["getState"]();  # Should be "open"
                    """;

            BaseObject result = evaluateCode(code);
            assertEquals("open", ObjectValidator.asString(result).getValue());
        }

    }

    // ==========================================
    // ALGORITHM IMPLEMENTATIONS
    // ==========================================

    @Nested
    @DisplayName("🎯 Classic Algorithm Implementations")
    class AlgorithmImplementationTests {

        @Test
        @DisplayName("Binary search implementation")
        void testBinarySearch() {
            String code = """
                    let binarySearch = fn(arr, target) {
                        let left = 0;
                        let right = len(arr) - 1;

                        while (left <= right) {
                            let mid = left + ((right - left) // 2);

                            if (arr[mid] == target) {
                                return mid;
                            } elif (arr[mid] < target) {
                                left = mid + 1;
                            } else {
                                right = mid - 1;
                            }
                        }

                        return -1;  # Not found
                    };

                    let sortedArray = [1, 3, 5, 7, 9, 11, 13, 15, 17, 19];
                    let index = binarySearch(sortedArray, 7);
                    index;  # Should be 3
                    """;

            BaseObject result = evaluateCode(code);
            assertEquals(3, ObjectValidator.asInteger(result).getValue());
        }

        @Test
        @DisplayName("Depth-first search on tree structure")
        void testDepthFirstSearch() {
            String code = """
                    let createNode = fn(value, children) {
                        return {"value": value, "children": children};
                    };

                    let dfs = fn(node, target, visited) {
                        if (visited == null) {
                            visited = [];
                        }

                        visited = push(visited, node["value"]);

                        if (node["value"] == target) {
                            return visited;
                        }

                        let children = node["children"];
                        for (let i = 0; i < len(children); i = i + 1) {
                            let result = dfs(children[i], target, visited);
                            if (result != null) {
                                return result;
                            }
                        }

                        return null;
                    };

                    let leaf1 = createNode(4, []);
                    let leaf2 = createNode(5, []);
                    let branch = createNode(2, [leaf1, leaf2]);
                    let root = createNode(1, [branch, createNode(3, [])]);

                    let path = dfs(root, 5, []);
                    path;  # Should include path to target
                    """;

            BaseObject result = evaluateCode(code);
            ArrayObject path = ObjectValidator.asArray(result);
            assertEquals(3, path.size());
            assertEquals(1, ObjectValidator.asInteger(path.get(0)).getValue());
            assertEquals(2, ObjectValidator.asInteger(path.get(1)).getValue());
            assertEquals(5, ObjectValidator.asInteger(path.get(2)).getValue());
        }

        @Test
        @DisplayName("Quicksort implementation")
        void testQuickSort() {
            String code = """
                    let partition = fn(arr, low, high) {
                        let pivot = arr[high];
                        let i = low - 1;

                        for (let j = low; j < high; j = j + 1) {
                            if (arr[j] < pivot) {
                                i = i + 1;
                                # Swap arr[i] and arr[j]
                                let temp = arr[i];
                                arr[i] = arr[j];
                                arr[j] = temp;
                            }
                        }

                        # Swap arr[i+1] and arr[high]
                        let temp = arr[i + 1];
                        arr[i + 1] = arr[high];
                        arr[high] = temp;

                        return i + 1;
                    };

                    let quickSort = fn(arr, low, high) {
                        if (low < high) {
                            let pi = partition(arr, low, high);
                            quickSort(arr, low, pi - 1);
                            quickSort(arr, pi + 1, high);
                        }
                        return arr;
                    };

                    let unsorted = [10, 7, 8, 9, 1, 5];
                    let sorted = quickSort(unsorted, 0, len(unsorted) - 1);
                    sorted[0];  # Should be 1 (smallest element)
                    """;

            BaseObject result = evaluateCode(code);
            assertEquals(1, ObjectValidator.asInteger(result).getValue());
        }
    }
}