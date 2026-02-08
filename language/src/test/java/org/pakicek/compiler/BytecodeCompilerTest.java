package org.pakicek.compiler;

import org.junit.Test;
import org.pakicek.parser.Parser;
import org.pakicek.parser.ast.node.*;
import org.pakicek.parser.lexer.Lexer;
import org.pakicek.parser.lexer.Token;
import org.pakicek.runtime.ProgramImage;
import org.pakicek.runtime.bytecode.Chunk;
import org.pakicek.runtime.bytecode.OpCode;

import java.util.List;

import static org.junit.Assert.*;

public class BytecodeCompilerTest {

    private Chunk compileSource(String code) {
        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();
        BytecodeCompiler compiler = new BytecodeCompiler();
        ProgramImage image = compiler.compile(program);
        return image.mainChunk;
    }

    private boolean hasOpCode(Chunk chunk, OpCode op) {
        for (byte b : chunk.code) {
            if (b >= 0 && b < OpCode.values().length && OpCode.values()[b] == op) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testVariableDeclaration() {
        String code = """
            main (int argc, array string argv[]) {
                int a = 42;
                int b = a;
            }
        """;
        Chunk chunk = compileSource(code);
        assertTrue(hasOpCode(chunk, OpCode.LOAD_CONST));
        assertTrue(hasOpCode(chunk, OpCode.STORE_LOCAL));
        assertTrue(hasOpCode(chunk, OpCode.LOAD_LOCAL));
    }

    @Test
    public void testUnaryOperations() {
        String code = """
            main (int argc, array string argv[]) {
                bool a = true;
                bool b = !a;
                int c = -5;
            }
        """;
        Chunk chunk = compileSource(code);
        assertTrue(hasOpCode(chunk, OpCode.LOAD_TRUE));
        assertTrue(hasOpCode(chunk, OpCode.NOT));
        assertTrue(hasOpCode(chunk, OpCode.MUL));
    }

    @Test
    public void testStructOperations() {
        String code = """
            struct Point { float x; float y; }
            main (int argc, array string argv[]) {
                Point p;
                p.x = 10.0;
                float val = p.x;
            }
        """;
        Chunk chunk = compileSource(code);
        assertTrue(hasOpCode(chunk, OpCode.NEW_STRUCT));
        assertTrue(hasOpCode(chunk, OpCode.SET_FIELD));
        assertTrue(hasOpCode(chunk, OpCode.GET_FIELD));
    }

    @Test
    public void testArrayOperations() {
        String code = """
            main (int argc, array string argv[]) {
                array int arr[5];
                arr[0] = 1;
                int val = arr[0];
            }
        """;
        Chunk chunk = compileSource(code);
        assertTrue(hasOpCode(chunk, OpCode.NEW_ARRAY));
        assertTrue(hasOpCode(chunk, OpCode.SET_ARRAY));
        assertTrue(hasOpCode(chunk, OpCode.GET_ARRAY));
    }
}