package org.pakicek.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.pakicek.parser.ast.node.ProgramNode;
import org.pakicek.parser.lexer.Lexer;
import org.pakicek.parser.lexer.Token;

import java.time.Duration;
import java.util.List;

public class ParserPerformanceTest {

    @Test
    public void testLargeProgramParsing() {
        assertTimeout(Duration.ofMillis(1000), () -> {
            StringBuilder largeCode = new StringBuilder();
            largeCode.append("main (int argc, array string argv[]) {\n");
            for (int i = 0; i < 100; i++) {
                largeCode.append("    int var").append(i).append(" = ").append(i).append(";\n");
            }
            largeCode.append("}\n");

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
        });
    }

    @Test
    public void testDeeplyNestedExpressions() {
        assertTimeout(Duration.ofMillis(1000), () -> {
            String deepCode = "func int deep_expression(int x) {\n    return " + "(".repeat(100) + "x" + " + x)".repeat(100) + ";\n}\n";

            List<Token> tokens = new Lexer(deepCode).scanTokens();
            Parser parser = new Parser(tokens);
            ProgramNode program = parser.parse();

            assertNotNull(program);
        });
    }
}