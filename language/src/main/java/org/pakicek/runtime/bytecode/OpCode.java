package org.pakicek.runtime.bytecode;

public enum OpCode {
    // Constants
    LOAD_CONST,    // Load constant from pool
    LOAD_TRUE,
    LOAD_FALSE,

    // Variables (local)
    LOAD_LOCAL,    // Load local variable onto stack
    STORE_LOCAL,   // Store top of stack into local variable
    POP, DUP, ROT,

    // Arithmetic (polymorphic: int/float)
    ADD, SUB, MUL, DIV, MOD,

    // Bitwise and Logic
    BIT_AND, BIT_OR, LOGIC_AND, LOGIC_OR, NOT,

    // Comparison
    EQ, NEQ, GT, LT, GTE, LTE,

    // Control flow
    JMP,           // Unconditional jump
    JMP_FALSE,     // Jump if false is on stack

    // Functions
    CALL,          // Call function
    RETURN,        // Return from function

    // Structs and Arrays (Heap)
    NEW_STRUCT,    // Create struct
    GET_FIELD,     // Access field (p.x)
    SET_FIELD,     // Assign to field (p.x = ...)

    NEW_ARRAY,     // Create array (size on stack)
    GET_ARRAY,     // arr[i]
    SET_ARRAY,     // arr[i] = val
    LEN,           // Get length

    // Built-ins
    PRINT,
    PRINTLN,
    SQRT,
    TO_INT,
    RANDOM,

    // System
    HALT
}