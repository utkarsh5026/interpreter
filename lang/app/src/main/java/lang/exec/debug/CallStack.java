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
 * │ innerFunc()
 * ├─────────────────┤
 * │ middleFunc()
 * ├─────────────────┤
 * │ outerFunc()
 * ├─────────────────┤
 * │ <global> <- Bottom (program start)
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

    public CallStack() {
        this(1000);
    }

    public CallStack(int maxStackDepth) {
        this.frames = new ArrayDeque<>();
        this.maxStackDepth = maxStackDepth;

        this.frames.push(StackFrame.createGlobalFrame());

        this.maxDepthReached = 1;
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

        if (frames.size() > maxDepthReached) {
            maxDepthReached = frames.size();
        }
    }

    /**
     * ⬇️ Pops the top frame from the stack (exiting a function)
     */
    public StackFrame pop() {
        if (frames.isEmpty()) {
            return null;
        }

        if (frames.size() == 1) {
            return null;
        }

        return frames.pop();
    }

    /**
     * 📊 Gets the current stack depth
     */
    public int depth() {
        return frames.size();
    }

    /**
     * 📸 Captures the current stack state for error reporting from (oldest) to
     * (newest)
     */
    public List<StackFrame> captureStackTrace() {
        return new ArrayList<>(frames);
    }

    /**
     * 🧹 Clears the entire stack (except global frame)
     */
    public void clear() {
        frames.clear();
        frames.push(StackFrame.createGlobalFrame());
        maxDepthReached = 1;
    }
}