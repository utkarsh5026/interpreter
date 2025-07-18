package lang.exec.objects;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

import lang.exec.base.BaseObject;
import lang.exec.base.ObjectType;
import lang.exec.debug.StackFrame;
import lang.exec.debug.CallStack;
import lang.token.TokenPosition;

/**
 * Error object for representing runtime errors.
 */
public class ErrorObject implements BaseObject {
    private final String message;

    private final List<StackFrame> stackTrace;
    private final boolean hasStackTrace;
    private Optional<TokenPosition> position;

    public ErrorObject(String message, TokenPosition position, List<StackFrame> stackTrace, String suggestion) {
        this.message = message != null ? message : "Unknown error";
        this.position = Optional.ofNullable(position);
        this.stackTrace = stackTrace != null ? List.copyOf(stackTrace) : Collections.emptyList();
        this.hasStackTrace = !this.stackTrace.isEmpty();
    }

    public static ErrorObject withStackTrace(String message, CallStack callStack) {
        List<StackFrame> stackTrace = callStack != null ? callStack.captureStackTrace() : Collections.emptyList();
        return new ErrorObject(message, null, stackTrace, null);
    }

    public ErrorObject(String message) {
        this(message, null, null, null);
    }

    public Optional<TokenPosition> getPosition() {
        return position;
    }

    public String getMessage() {
        return message;
    }

    public List<StackFrame> getStackTrace() {
        return hasStackTrace ? stackTrace : Collections.emptyList();
    }

    /**
     * 📝 NEW: Gets detailed error message with stack trace
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append("🚨 ").append(message);

        if (position.isPresent()) {
            sb.append("\n   at ").append(position.get());
        }

        if (hasStackTrace) {
            sb.append("\n\n📚 ").append(formatStackTrace());
        }

        return sb.toString();
    }

    /**
     * 📚 NEW: Formats the stack trace for display
     */
    public String formatStackTrace() {
        if (!hasStackTrace) {
            return "No stack trace available";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Stack trace (most recent call first):");

        for (int i = 0; i < stackTrace.size(); i++) {
            StackFrame frame = stackTrace.get(i);
            sb.append("\n  ").append(frame.formatForStackTrace());
        }

        return sb.toString();
    }

    @Override
    public ObjectType type() {
        return ObjectType.ERROR;
    }

    @Override
    public String inspect() {
        return hasStackTrace ? getDetailedMessage() : "ERROR: " + message;
    }

    @Override
    public boolean isTruthy() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ErrorObject))
            return false;
        ErrorObject other = (ErrorObject) obj;
        return message.equals(other.message);
    }

    @Override
    public int hashCode() {
        return message.hashCode();
    }
}