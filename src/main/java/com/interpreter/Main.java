package com.interpreter;

/**
 * Main entry point for the interpreter
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to the Interpreter!");
        
        if (args.length > 0) {
            System.out.println("Input file: " + args[0]);
            // TODO: Add file processing logic here
        } else {
            System.out.println("No input file provided. Starting interactive mode...");
            // TODO: Add interactive mode logic here
        }
    }
} 