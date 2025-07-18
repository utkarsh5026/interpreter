package lang.exec.debug;

import lang.token.TokenPosition;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * 📞 StackFrame - Single Call Stack Entry 📞
 * 
 * Represents a single function call frame in the execution stack.
 * 
 * This is like a snapshot of "where we are" in the program execution.
 * When an error occurs, we can look at all the stack frames to see
 * the complete path that led to the error.
 * 
 * Example stack frame chain:
 * ```
 * main() at line 10 <- Top of stack (most recent call)
 * └─ calculateTotal() at line 5
 * └─ processItem() at line 2 <- Bottom of stack (oldest call)
 * ```
 */
public class StackFrame {

    private final String functionName;
    private final TokenPosition position;
    private final FrameType frameType;

    private final Map<String, String> localVariables;
    private final String sourceContext;

    /**
     * 🏷️ Types of stack frames in our interpreter
     */
    public enum FrameType {
        GLOBAL("Global"), // Top-level program execution
        USER_FUNCTION("Function"), // User-defined function call
        BUILTIN("Built-in"), // Built-in function call
        EXPRESSION("Expression"), // Expression evaluation (for very detailed traces)
        UNKNOWN("Unknown"); // Fallback for edge cases

        private final String displayName;

        FrameType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 🏗️ Creates a new stack frame with full context
     */
    public StackFrame(String functionName, TokenPosition position, FrameType frameType,
            Map<String, String> localVariables, String sourceContext) {
        this.functionName = functionName != null ? functionName : "<anonymous>";
        this.position = position;
        this.frameType = frameType != null ? frameType : FrameType.UNKNOWN;
        this.localVariables = new HashMap<>(localVariables != null ? localVariables : Collections.emptyMap());
        this.sourceContext = sourceContext != null ? sourceContext : "";
    }

    /**
     * 🏗️ Simplified constructor for basic frames
     */
    public StackFrame(String functionName, TokenPosition position, FrameType frameType) {
        this(functionName, position, frameType, null, null);
    }

    /**
     * 🏗️ Creates a global scope frame
     */
    public static StackFrame createGlobalFrame() {
        return new StackFrame("<global>", new TokenPosition(1, 1), FrameType.GLOBAL);
    }

    /**
     * 🏗️ Creates a user function frame
     */
    public static StackFrame createFunctionFrame(String functionName, TokenPosition position) {
        return new StackFrame(functionName, position, FrameType.USER_FUNCTION);
    }

    /**
     * 🏗️ Creates a built-in function frame
     */
    public static StackFrame createBuiltinFrame(String functionName) {
        TokenPosition builtinPosition = new TokenPosition(0, 0); // Built-ins don't have source positions
        return new StackFrame(functionName, builtinPosition, FrameType.BUILTIN);
    }

    // 🔍 Getters for frame information
    public String getFunctionName() {
        return functionName;
    }

    public TokenPosition getPosition() {
        return position;
    }

    public FrameType getFrameType() {
        return frameType;
    }

    public Map<String, String> getLocalVariables() {
        return Collections.unmodifiableMap(localVariables);
    }

    public String getSourceContext() {
        return sourceContext;
    }

    /**
     * 📝 Formats this frame for display in stack traces
     * 
     * Creates a readable representation like:
     * "at calculateTotal() [Function] (line 15, column 8)"
     * "at len() [Built-in]"
     * "at <global> [Global] (line 1, column 1)"
     */
    public String formatForStackTrace() {
        StringBuilder sb = new StringBuilder();

        sb.append("  at ").append(functionName)
                .append(" [").append(frameType.getDisplayName()).append("]");

        if (frameType != FrameType.BUILTIN && position != null) {
            sb.append(" (").append(position).append(")");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return formatForStackTrace();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof StackFrame))
            return false;

        StackFrame other = (StackFrame) obj;
        return functionName.equals(other.functionName) &&
                frameType == other.frameType &&
                (position != null ? position.equals(other.position) : other.position == null);
    }

    @Override
    public int hashCode() {
        int result = functionName.hashCode();
        result = 31 * result + frameType.hashCode();
        result = 31 * result + (position != null ? position.hashCode() : 0);
        return result;
    }
}