package org.pakicek.compiler;

import org.junit.Test;
import org.pakicek.parser.Parser;
import org.pakicek.parser.ast.node.ProgramNode;
import org.pakicek.parser.lexer.Lexer;
import org.pakicek.parser.lexer.Token;
import org.pakicek.vm.ProgramImage;
import org.pakicek.vm.bytecode.Chunk;
import org.pakicek.vm.bytecode.OpCode;

import java.util.List;

import static org.junit.Assert.*;

public class CompilerFlowTest {

    private Chunk compileSource(String code) {
        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();
        BytecodeCompiler compiler = new BytecodeCompiler();
        ProgramImage image = compiler.compile(program);
        return image.mainChunk;
    }

    private int countOpCodes(Chunk chunk, OpCode op) {
        int count = 0;
        for (byte b : chunk.code) {
            if (b >= 0 && b < OpCode.values().length && OpCode.values()[b] == op) {
                count++;
            }
        }
        return count;
    }

    @Test
    public void testIfElse() {
        String code = """
            main (int argc, array string argv[]) {
                if (true) {
                    print("true");
                } else {
                    print("false");
                }
            }
        """;
        Chunk chunk = compileSource(code);

        // If-Else generates:
        // JMP_FALSE (jump to else)
        // ... then block ...
        // JMP (jump to end)
        // ... else block ...

        assertEquals(1, countOpCodes(chunk, OpCode.JMP_FALSE));
        assertEquals(1, countOpCodes(chunk, OpCode.JMP));
    }

    @Test
    public void testWhileLoop() {
        String code = """
            main (int argc, array string argv[]) {
                while (true) {
                    print("loop");
                }
            }
        """;
        Chunk chunk = compileSource(code);

        // While generates:
        // Condition check
        // JMP_FALSE (exit)
        // Body
        // JMP (loop back)

        assertEquals(1, countOpCodes(chunk, OpCode.JMP_FALSE));
        assertEquals(1, countOpCodes(chunk, OpCode.JMP));
    }

    @Test
    public void testShortCircuitLogic() {
        String code = """
            main (int argc, array string argv[]) {
                bool res = true && false;
                bool res2 = false || true;
            }
        """;
        Chunk chunk = compileSource(code);

        // && generates JMP_FALSE (skip if first is false)
        // || generates JMP_FALSE (to check second) and JMP (to skip second if first true)

        // Total JMP_FALSE: 1 (for &&) + 1 (for ||) = 2
        // Total JMP: 1 (for ||)

        assertTrue(countOpCodes(chunk, OpCode.JMP_FALSE) >= 2);
        assertTrue(countOpCodes(chunk, OpCode.JMP) >= 1);

        // Also check stack manipulation for logic
        assertTrue(countOpCodes(chunk, OpCode.DUP) >= 2);
        assertTrue(countOpCodes(chunk, OpCode.POP) >= 2); // To pop the first operand if consumed
    }
}