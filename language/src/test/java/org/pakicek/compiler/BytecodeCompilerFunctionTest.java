package org.pakicek.compiler;

import org.junit.Test;
import org.pakicek.parser.Parser;
import org.pakicek.parser.ast.node.ProgramNode;
import org.pakicek.parser.lexer.Lexer;
import org.pakicek.parser.lexer.Token;
import org.pakicek.runtime.ProgramImage;
import org.pakicek.runtime.bytecode.Chunk;
import org.pakicek.runtime.bytecode.OpCode;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class BytecodeCompilerFunctionTest {

    @Test
    public void testFunctionCompilation() {
        String code = """
            func int add(int a, int b) {
                return a + b;
            }
        
            main (int argc, array string argv[]) {
                add(1, 2);
            }
        """;

        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();
        BytecodeCompiler compiler = new BytecodeCompiler();
        ProgramImage image = compiler.compile(program);
        Chunk mainChunk = image.mainChunk;

        // 1. Check Main calls function
        boolean hasCall = false;
        for (byte b : mainChunk.code) {
            if (OpCode.values()[b] == OpCode.CALL) {
                hasCall = true;
                break;
            }
        }
        assertTrue("Main should contain CALL opcode", hasCall);

        // 2. Check Function exists in registry
        Map<String, Chunk> functions = compiler.getFunctions();
        assertTrue(functions.containsKey("add"));

        Chunk addChunk = functions.get("add");
        assertNotNull(addChunk);

        // 3. Check Function body
        // Should have LOAD_LOCAL (a), LOAD_LOCAL (b), ADD, RETURN
        // a and b are locals 0 and 1
        boolean hasLoadLocal = false;
        boolean hasAdd = false;
        boolean hasReturn = false;

        for (byte b : addChunk.code) {
            OpCode op = OpCode.values()[b];
            if (op == OpCode.LOAD_LOCAL) hasLoadLocal = true;
            if (op == OpCode.ADD) hasAdd = true;
            if (op == OpCode.RETURN) hasReturn = true;
        }

        assertTrue(hasLoadLocal);
        assertTrue(hasAdd);
        assertTrue(hasReturn);
    }
}