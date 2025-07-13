package lang.exec.evaluator.base;

public class LoopContext {
    public static final int MAX_ITERATIONS = 100000; // Prevent infinite loops
    private int loopDepth = 0;

    public void enterLoop() {
        loopDepth++;
    }

    public void exitLoop() {
        if (loopDepth <= 0) {
            throw new RuntimeException("Loop depth is already 0");
        }

        loopDepth--;
    }

    public boolean isInLoop() {
        return loopDepth > 0;
    }

    public boolean isMaxIterationsReached() {
        return loopDepth >= MAX_ITERATIONS;
    }

}
