package org.pakicek.parser.utils;

public class RuntimeError extends RuntimeException {
    private final int line;
    
    public RuntimeError(int line, String message) {
        super(message);
        this.line = line;
    }
    
    public RuntimeError(String message) {
        super(message);
        this.line = -1;
    }
    
    public int getLine() {
        return line;
    }
}