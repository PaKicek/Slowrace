package org.pakicek.parser.utils;

import org.pakicek.parser.lexer.Token;
import org.pakicek.parser.lexer.TokenType;

public class ErrorHandler {
    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;
    
    public static void error(int line, String message) {
        report(line, "", message);
    }
    
    public static void error(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            report(token.line(), " at end", message);
        } else {
            report(token.line(), " at '" + token.lexeme() + "'", message);
        }
    }
    
    public static void runtimeError(RuntimeError error) {
        System.err.println("Runtime error: " + error.getMessage());
        hadRuntimeError = true;
    }
    
    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
    
    public static boolean hasError() {
        return hadError;
    }
    
    public static boolean hasRuntimeError() {
        return hadRuntimeError;
    }
    
    public static void reset() {
        hadError = false;
        hadRuntimeError = false;
    }
    
    public static void warning(int line, String message) {
        System.err.println("[line " + line + "] Warning: " + message);
    }
    
    public static void warning(Token token, String message) {
        System.err.println("[line " + token.line() + "] Warning at '" + 
                          token.lexeme() + "': " + message);
    }
    
    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }
    
    public static void debug(String message) {
        System.out.println("[DEBUG] " + message);
    }
}