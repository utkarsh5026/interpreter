package com.interpreter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Interpreter class
 */
public class InterpreterTest {
    
    private Interpreter interpreter;
    
    @BeforeEach
    public void setUp() {
        interpreter = new Interpreter();
    }
    
    @Test
    public void testParseMethod() {
        // Test that parse method doesn't throw exceptions
        assertDoesNotThrow(() -> {
            interpreter.parse("test input");
        });
    }
    
    @Test
    public void testExecuteMethod() {
        // Test that execute method doesn't throw exceptions
        assertDoesNotThrow(() -> {
            interpreter.execute();
        });
    }
    
    @Test
    public void testInteractiveMode() {
        // Test that interactive mode starts without exceptions
        assertDoesNotThrow(() -> {
            interpreter.startInteractiveMode();
        });
    }
} 