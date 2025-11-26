package org.pakicek.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.pakicek.parser.ast.node.ProgramNode;
import org.pakicek.parser.lexer.Lexer;
import org.pakicek.parser.lexer.Token;

import java.util.List;

public class ParserPerformanceTest {

    @Test(timeout = 1000) // 1 second timeout
    public void testLargeProgramParsing() {
        StringBuilder largeCode = new StringBuilder();

        // Generate a large program with many functions
        largeCode.append("main (int argc, array string argv[]) {\n");
        for (int i = 0; i < 100; i++) {
            largeCode.append("    int var").append(i).append(" = ").append(i).append(";\n");
        }
        largeCode.append("}\n");

        // Add multiple functions
        for (int i = 0; i < 50; i++) {
            largeCode.append("func void function_").append(i).append("(int param) {\n");
            largeCode.append("    if (param > 0) {\n");
            largeCode.append("        print(\"Positive: \" + param);\n");
            largeCode.append("    } else {\n");
            largeCode.append("        print(\"Non-positive: \" + param);\n");
            largeCode.append("    }\n");
            largeCode.append("}\n");
        }

        List<Token> tokens = new Lexer(largeCode.toString()).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        assertNotNull(program);
        assertEquals(50, program.getFunctions().size());
        assertNotNull(program.getMainNode());
    }

    @Test(timeout = 1000) // 1 second timeout
    public void testDeeplyNestedExpressions() {

        // Create a very deep expression tree
        String deepCode = "func int deep_expression(int x) {\n    return " + "(".repeat(100) +
                "x" +
                " + x)".repeat(100) +
                ";\n}\n";

        List<Token> tokens = new Lexer(deepCode).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        assertNotNull(program);
        // Should parse without stack overflow
    }
}