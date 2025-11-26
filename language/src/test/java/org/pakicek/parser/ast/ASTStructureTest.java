package org.pakicek.parser.ast;

import org.junit.Test;
import static org.junit.Assert.*;

import org.pakicek.parser.Parser;
import org.pakicek.parser.ast.node.*;
import org.pakicek.parser.ast.node.expression.*;
import org.pakicek.parser.ast.node.expression.literal.*;
import org.pakicek.parser.ast.node.statement.*;
import org.pakicek.parser.ast.node.type.*;
import org.pakicek.parser.lexer.Lexer;
import org.pakicek.parser.lexer.Token;

import java.util.List;

public class ASTStructureTest {

    @Test
    public void testExpressionTypes() {
        String code = """
        func void test() {
            a = 42;           // integer literal
            b = 3.14;         // float literal
            c = "hello";      // string literal
            d = true;         // boolean literal
            e = a + b;        // binary expression
            f = -a;           // unary expression
            g = test();       // function call
            h = arr[0];       // array access
        }
        """;

        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        BlockStatementNode body = program.getFunctions().get(0).getBody();

        Class<?>[] expectedTypes = {
                IntegerLiteralNode.class,   // 42
                FloatLiteralNode.class,     // 3.14
                StringLiteralNode.class,    // "hello"
                BooleanLiteralNode.class,   // true
                BinaryExpressionNode.class, // a + b
                UnaryExpressionNode.class,  // -a
                FunctionCallNode.class,     // test()
                ArrayAccessNode.class       // arr[0]
        };

        for (int i = 0; i < expectedTypes.length; i++) {
            ExpressionStatementNode exprStmt = (ExpressionStatementNode) body.getStatements().get(i);
            AssignmentNode assignment = (AssignmentNode) exprStmt.getExpression();
            assertTrue("Expression " + i + " should be " + expectedTypes[i].getSimpleName(),
                    expectedTypes[i].isInstance(assignment.getValue()));
        }

        // Print using detailed printer
        ASTPrinter detailedPrinter = new ASTPrinter();
        System.out.println("=== Detailed AST ===");
        System.out.println(detailedPrinter.print(program));
    }

    @Test
    public void testSimpleArrayParameter() {
        String code = "func void test(array int arr[]) {}";

        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        assertNotNull(program);

        // Print using detailed printer
        ASTPrinter detailedPrinter = new ASTPrinter();
        System.out.println("=== Detailed AST ===");
        System.out.println(detailedPrinter.print(program));
    }

    @Test
    public void testTypeNodes() {
        String code = """
            func void test(int a, float b, string c, bool d, array int e[], array bool f[10]) {
                array string dynamic[size];
            }
            """;

        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        FunctionDeclarationNode function = program.getFunctions().get(0);

        // Check parameter types
        assertEquals("int", ((BasicTypeNode) function.getParameters().get(0).getType()).getTypeName());
        assertEquals("float", ((BasicTypeNode) function.getParameters().get(1).getType()).getTypeName());
        assertEquals("string", ((BasicTypeNode) function.getParameters().get(2).getType()).getTypeName());
        assertEquals("bool", ((BasicTypeNode) function.getParameters().get(3).getType()).getTypeName());

        // Check array types
        assertTrue(function.getParameters().get(4).getType() instanceof ArrayTypeNode);
        assertTrue(function.getParameters().get(5).getType() instanceof ArrayTypeNode);

        // Print using detailed printer
        ASTPrinter detailedPrinter = new ASTPrinter();
        System.out.println("=== Detailed AST ===");
        System.out.println(detailedPrinter.print(program));
    }
}