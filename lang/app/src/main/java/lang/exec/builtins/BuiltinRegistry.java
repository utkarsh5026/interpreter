package lang.exec.builtins;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lang.exec.objects.BuiltinObject;

/**
 * 🗂️ BuiltinRegistry - The Function Library Manager 🗂️
 * 
 * This enhanced registry provides comprehensive management of builtin functions
 * with thread-safety, categorization, and advanced querying capabilities.
 * 
 * Key improvements over the basic registry:
 * - 🔒 Thread-safe operations for concurrent access
 * - 📂 Category-based organization and filtering
 * - 🔍 Advanced search and filtering capabilities
 * - 📊 Statistics and reporting functionality
 * - 🚀 Lazy initialization with automatic loading
 * - 🛡️ Validation and error handling
 * - 🔄 Hot-swapping and dynamic registration
 * 
 */
public class BuiltinRegistry {
    private static final Map<String, BuiltinObject> builtins = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> categories = new ConcurrentHashMap<>();
    private static volatile boolean initialized = false;

    private static final Object initLock = new Object();

    private BuiltinRegistry() {
        throw new UnsupportedOperationException("Utility class - cannot instantiate");
    }

    /**
     * 🚀 Ensures the registry is properly initialized
     * 
     * This method uses double-checked locking pattern for thread-safe lazy
     * initialization.
     * Only one thread will perform the initialization, and subsequent calls will
     * return immediately.
     */
    private static void ensureInitialized() {
        if (!initialized) {
            synchronized (initLock) {
                if (!initialized) {
                    initializeFromBuiltinFunctions();
                    initialized = true;
                }
            }
        }
    }

    /**
     * 🏗️ Initializes the registry from the BuiltinFunctions library
     */
    private static void initializeFromBuiltinFunctions() {
        builtins.clear();
        categories.clear();

        builtins.putAll(BuiltinFunctions.BUILTINS);

        BuiltinFunctions.getBuiltinsByCategory()
                .forEach((category, functionNames) -> {
                    Set<String> functionSet = new HashSet<>(functionNames);
                    categories.put(category, functionSet);
                });

        initialized = true;
    }

    /**
     * 🔍 Checks if a function name is a registered builtin
     */
    public static boolean isBuiltin(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        ensureInitialized();
        return builtins.containsKey(name.trim());
    }

    /**
     * 📖 Gets a builtin function by name
     */
    public static BuiltinObject getBuiltin(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        ensureInitialized();
        return builtins.get(name.trim());
    }

    /**
     * Adds a new builtin function to the registry
     */
    public static void addBuiltin(String name, BuiltinObject builtin) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }

        if (builtin == null) {
            throw new IllegalArgumentException("BuiltinObject cannot be null");
        }

        ensureInitialized();

        String trimmedName = name.trim();
        if (builtins.containsKey(trimmedName)) {
            throw new IllegalStateException(String.format(
                    "Function '%s' is already registered", trimmedName));
        }

        builtins.put(trimmedName, builtin);
    }

    /**
     * Replaces an existing builtin function (hot-swapping)
     */
    public static BuiltinObject replaceBuiltin(String name, BuiltinObject builtin) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }

        if (builtin == null) {
            throw new IllegalArgumentException("BuiltinObject cannot be null");
        }

        ensureInitialized();
        return builtins.put(name.trim(), builtin);
    }

    /**
     * ➖ Removes a builtin function from the registry
     * 
     * @param name The function name to remove
     * @return The removed BuiltinObject, or null if none existed
     */
    public static BuiltinObject removeBuiltin(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        ensureInitialized();

        String trimmedName = name.trim();
        BuiltinObject removed = builtins.remove(trimmedName);
        categories.values().forEach(functionSet -> functionSet.remove(trimmedName));

        return removed;
    }

    /**
     * 📋 Gets all builtin function names
     */
    public static Set<String> getAllBuiltinNames() {
        ensureInitialized();
        return Collections.unmodifiableSet(builtins.keySet());
    }

    /**
     * 📋 Gets all builtin function names sorted alphabetically
     * 
     * @return A sorted list of all registered function names
     */
    public static List<String> getAllBuiltinNamesSorted() {
        ensureInitialized();
        return builtins.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 📂 Gets all registered builtin functions
     * 
     * @return An unmodifiable map of all registered functions
     */
    public static Map<String, BuiltinObject> getAllBuiltins() {
        ensureInitialized();
        return Collections.unmodifiableMap(builtins);
    }

    /**
     * 📂 Gets function names by category
     * 
     * @param categoryName The category to query
     * @return A set of function names in the category, or empty set if category
     *         doesn't exist
     */
    public static Set<String> getFunctionsByCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return Collections.emptySet();
        }

        ensureInitialized();
        return categories.getOrDefault(categoryName.trim(), Collections.emptySet());
    }

    /**
     * 📂 Gets all available categories
     * 
     * @return An unmodifiable set of all category names
     */
    public static Set<String> getAllCategories() {
        ensureInitialized();
        return Collections.unmodifiableSet(categories.keySet());
    }

    /**
     * 📂 Gets the complete category mapping
     * 
     * @return An unmodifiable map of categories to function names
     */
    public static Map<String, Set<String>> getCategoryMapping() {
        ensureInitialized();
        return Collections.unmodifiableMap(categories);
    }

    /**
     * 🔍 Searches for functions by name pattern
     */
    public static List<String> searchFunctions(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return Collections.emptyList();
        }

        ensureInitialized();

        String regex = pattern.trim()
                .toLowerCase()
                .replace("*", ".*")
                .replace("?", ".");

        return builtins.keySet().stream()
                .filter(name -> name.toLowerCase().matches(regex))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 🔍 Finds functions that contain a substring in their name or description
     */
    public static Map<String, String> searchFunctionsWithDescription(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Collections.emptyMap();
        }

        ensureInitialized();

        String lowerSearchTerm = searchTerm.trim().toLowerCase();

        return builtins.entrySet().stream()
                .filter(entry -> {
                    String name = entry.getKey().toLowerCase();
                    String description = entry.getValue().getDescription().toLowerCase();
                    return name.contains(lowerSearchTerm) || description.contains(lowerSearchTerm);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getDescription(),
                        (existing, _) -> existing,
                        LinkedHashMap::new));
    }

    /**
     * 📖 Gets detailed information about a specific function
     * 
     * @param functionName The name of the function
     * @return A formatted string with function details, or null if not found
     */
    public static String getFunctionInfo(String functionName) {
        if (functionName == null || functionName.trim().isEmpty()) {
            return null;
        }

        ensureInitialized();

        BuiltinObject builtin = builtins.get(functionName.trim());
        if (builtin == null) {
            return null;
        }

        StringBuilder info = new StringBuilder();
        info.append(String.format("🔧 Function: %s\n", builtin.getName()));
        info.append(String.format("📝 Description: %s\n", builtin.getDescription()));

        String category = categories.entrySet().stream()
                .filter(entry -> entry.getValue().contains(functionName.trim()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("Unknown");

        info.append(String.format("📂 Category: %s\n", category));

        return info.toString();
    }

    /**
     * 📋 Lists all functions in a category with their descriptions
     * 
     * @param categoryName The category to list
     * @return A formatted string listing all functions in the category
     */
    public static String listCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return "Invalid category name";
        }

        ensureInitialized();

        Set<String> functionNames = categories.get(categoryName.trim());
        if (functionNames == null || functionNames.isEmpty()) {
            return String.format("Category '%s' not found or empty", categoryName);
        }

        StringBuilder listing = new StringBuilder();
        listing.append(String.format("📂 %s (%d functions)\n", categoryName, functionNames.size()));
        listing.append("═".repeat(50)).append("\n");

        functionNames.stream()
                .sorted()
                .forEach(name -> {
                    BuiltinObject builtin = builtins.get(name);
                    if (builtin != null) {
                        listing.append(String.format("• %-15s - %s\n",
                                name, builtin.getDescription()));
                    }
                });

        return listing.toString();
    }

    /**
     * 🧹 Clears all registered functions (use with caution!)
     */
    public static void clear() {
        synchronized (initLock) {
            builtins.clear();
            categories.clear();
            initialized = false;
        }
    }

    /**
     * 🔄 Forces re-initialization of the registry
     * 
     * This method clears the current state and reloads all functions from
     * BuiltinFunctions.
     * Useful for hot-reloading or resetting to a clean state.
     */
    public static void reinitialize() {
        synchronized (initLock) {
            initialized = false;
            ensureInitialized();
        }
    }

    /**
     * ✅ Validates the registry state and reports any issues
     * 
     * @return A list of validation issues, empty if all is well
     */
    public static List<String> validateRegistry() {
        ensureInitialized();

        List<String> issues = new ArrayList<>();

        // Check for null values
        builtins.entrySet().stream()
                .filter(entry -> entry.getValue() == null)
                .forEach(entry -> issues.add(String.format("Null builtin for name: %s", entry.getKey())));

        // Check for inconsistent category mappings
        categories.forEach((category, functionNames) -> {
            functionNames.stream()
                    .filter(name -> !builtins.containsKey(name))
                    .forEach(name -> issues.add(String.format(
                            "Category '%s' references non-existent function: %s", category, name)));
        });

        // Check for functions not in any category
        Set<String> allCategorizedFunctions = categories.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        builtins.keySet().stream()
                .filter(name -> !allCategorizedFunctions.contains(name))
                .forEach(name -> issues.add(String.format("Function '%s' is not in any category", name)));

        return issues;
    }

    /**
     * 📊 Gets memory usage information about the registry
     * 
     * @return A string with memory usage details
     */
    public static String getMemoryInfo() {
        ensureInitialized();

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return String.format(
                "🧠 Registry Memory Info\n" +
                        "Functions: %d\n" +
                        "Categories: %d\n" +
                        "Used Memory: %.2f MB\n" +
                        "Total Memory: %.2f MB",
                builtins.size(),
                categories.size(),
                usedMemory / (1024.0 * 1024.0),
                totalMemory / (1024.0 * 1024.0));
    }
}