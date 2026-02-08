package org.pakicek.runtime.bytecode;

public enum OpCode {
    LOAD_CONST,
    LOAD_TRUE,
    LOAD_FALSE,

    LOAD_LOCAL,
    STORE_LOCAL,
    POP, DUP, ROT,

    ADD, SUB, MUL, DIV, MOD,

    BIT_AND, BIT_OR, LOGIC_AND, LOGIC_OR, NOT,

    EQ, NEQ, GT, LT, GTE, LTE,

    JMP, JMP_FALSE,

    CALL, RETURN,

    NEW_STRUCT,
    GET_FIELD,
    SET_FIELD,

    NEW_ARRAY,
    GET_ARRAY,
    SET_ARRAY,
    LEN,

    PRINT,
    PRINTLN,
    SQRT,
    TO_INT,
    RANDOM,
    HALT
}