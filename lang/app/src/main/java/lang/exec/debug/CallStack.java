package lang.exec.debug;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.ArrayList;

/**
 * 📚 CallStack - Execution Stack Manager 📚
 * 
 * Manages the call stack during program execution, tracking which functions
 * are currently being executed and where they were called from.
 * 
 * From first principles, the call stack works like this:
 * 
 * 1. **Push**: When entering a function, push a new frame onto the stack
 * 2. **Pop**: When exiting a function, pop the top frame off the stack
 * 3. **Peek**: Look at the current (top) frame without removing it
 * 4. **Capture**: Take a snapshot of the entire stack for error reporting
 * 
 * The stack grows "upward" - newer calls are at the top, older calls at the
 * bottom:
 * 
 * ```
 * ┌─────────────────┐ <- Top (most recent call)
 * │ innerFunc() │
 * ├─────────────────┤
 * │ middleFunc() │
 * ├─────────────────┤
 * │ outerFunc() │
 * ├─────────────────┤
 * │ <global> │ <- Bottom (program start)
 * └─────────────────┘
 * ```
 * 
 * When an error occurs, we can traverse the entire stack to show exactly
 * how the program reached the error point.
 */
public class CallStack {
    private final Deque<StackFrame> frames;

    private final int maxStackDepth;

    private int maxDepthReached;
    private long totalPushes;
    private long totalPops;

    /**
     * 🏗️ Creates a new call stack with default configuration
     */
    public CallStack() {
        this(1000);
    }

    /**
     * 🏗️ Creates a new call stack with custom configuration
     */
    public CallStack(int maxStackDepth) {
        this.frames = new ArrayDeque<>();
        this.maxStackDepth = maxStackDepth;

        this.frames.push(StackFrame.createGlobalFrame());

        this.maxDepthReached = 1;
        this.totalPushes = 1;
        this.totalPops = 0;
    }

    /**
     * ⬆️ Pushes a new frame onto the stack (entering a function)
     */
    public void push(StackFrame frame) {
        if (frames.size() >= maxStackDepth) {
            throw new RuntimeException(String.format(
                    "Stack overflow: Maximum stack depth of %d exceeded. " +
                            "This usually indicates infinite recursion.",
                    maxStackDepth));
        }

        frames.push(frame);
        totalPushes++;

        if (frames.size() > maxDepthReached) {
            maxDepthReached = frames.size();
        }
    }

    /**
     * ⬇️ Pops the top frame from the stack (exiting a function)
     * 
     * This should be called whenever the interpreter finishes executing a function.
     * 
     * @return The popped frame, or null if stack is empty
     */
    public StackFrame pop() {
        if (frames.isEmpty()) {
            return null;
        }

        if (frames.size() == 1) {
            return null;
        }

        totalPops++;
        return frames.pop();
    }

    /**
     * 👀 Peeks at the current (top) frame without removing it
     * 
     * @return The current frame, or null if stack is empty
     */
    public StackFrame peek() {
        return frames.peek();
    }

    /**
     * 📊 Gets the current stack depth
     * 
     * @return Number of frames currently on the stack
     */
    public int depth() {
        return frames.size();
    }

    /**
     * ❓ Checks if the stack is empty (only global frame remains)
     */
    public boolean isEmpty() {
        return frames.size() <= 1; // Consider only global frame as "empty"
    }

    /**
     * 📸 Captures the current stack state for error reporting
     * 
     * Creates a snapshot of the entire call stack that can be used
     * to generate detailed error messages and stack traces.
     * 
     * @return A list of stack frames from top (most recent) to bottom (oldest)
     */
    public List<StackFrame> captureStackTrace() {
        return new ArrayList<>(frames); // Top to bottom order
    }

    /**
     * 📝 Formats the current stack trace as a readable string
     * 
     * Creates a formatted stack trace similar to what you'd see in other
     * programming languages. Perfect for error messages and debugging.
     * 
     * @return Formatted stack trace string
     */
    public String formatStackTrace() {
        if (frames.isEmpty()) {
            return "  <empty stack>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Stack trace (most recent call first):\n");

        int frameNumber = 0;
        for (StackFrame frame : frames) {
            if (frameNumber > 0) { // Skip numbering for global frame
                sb.append(String.format("  #%d ", frameNumber));
            }
            sb.append(frame.formatForStackTrace()).append("\n");
            frameNumber++;
        }

        return sb.toString().trim();
    }

    /**
     * 📝 Formats a detailed stack trace with local variables and source context
     * 
     * Provides much more detailed information than the basic stack trace.
     * Useful for deep debugging but can be verbose.
     * 
     * @return Detailed formatted stack trace string
     */
    public String formatDetailedStackTrace() {
        if (frames.isEmpty()) {
            return "  <empty stack>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Detailed stack trace (most recent call first):\n");

        int frameNumber = 0;
        for (StackFrame frame : frames) {
            sb.append(String.format("  #%d ", frameNumber));
            sb.append(frame.formatDetailed()).append("\n");
            frameNumber++;
        }

        return sb.toString().trim();
    }

    /**
     * 🔍 Finds the most recent user code frame
     * 
     * Skips built-in function calls to find where the error actually
     * occurred in user-written code.
     * 
     * @return The most recent user code frame, or null if none found
     */
    public StackFrame findMostRecentUserFrame() {
        return frames.stream()
                .filter(StackFrame::isUserCode)
                .findFirst()
                .orElse(null);
    }

    /**
     * 🧹 Clears the entire stack (except global frame)
     * 
     * Useful for error recovery or resetting execution state.
     */
    public void clear() {
        frames.clear();
        frames.push(StackFrame.createGlobalFrame());
        maxDepthReached = 1;
    }

    /**
     * 📊 Gets execution statistics for debugging and performance analysis
     * 
     * @return Statistics about stack usage
     */
    public StackStatistics getStatistics() {
        return new StackStatistics(
                frames.size(),
                maxDepthReached,
                maxStackDepth,
                totalPushes,
                totalPops);
    }

    /**
     * 📊 Container for stack usage statistics
     */
    public static record StackStatistics(
            int currentDepth,
            int maxDepthReached,
            int maxDepthAllowed,
            long totalPushes,
            long totalPops) {

        public String formatStats() {
            return String.format(
                    "Stack Stats: depth=%d, max=%d/%d, pushes=%d, pops=%d",
                    currentDepth, maxDepthReached, maxDepthAllowed, totalPushes, totalPops);
        }
    }
}