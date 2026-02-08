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
            i = obj.field;    // field access
        }
        """;

        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        BlockStatementNode body = program.getFunctions().get(0).getBody();

        Class<?>[] expectedTypes = {
                IntegerLiteralNode.class,
                FloatLiteralNode.class,
                StringLiteralNode.class,
                BooleanLiteralNode.class,
                BinaryExpressionNode.class,
                UnaryExpressionNode.class,
                FunctionCallNode.class,
                ArrayAccessNode.class,
                FieldAccessNode.class
        };

        for (int i = 0; i < expectedTypes.length; i++) {
            ExpressionStatementNode exprStmt = (ExpressionStatementNode) body.getStatements().get(i);
            AssignmentNode assignment = (AssignmentNode) exprStmt.getExpression();
            assertTrue("Expression " + i + " should be " + expectedTypes[i].getSimpleName(), expectedTypes[i].isInstance(assignment.getValue()));
        }
    }

    @Test
    public void testSimpleArrayParameter() {
        String code = "func void test(array int arr[]) {}";
        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();
        assertNotNull(program);
    }

    @Test
    public void testTypeNodes() {
        String code = """
            func void test(int a, float b, string c, bool d, array int e[], array bool f[10], Point p) {
                array string dynamic[size];
            }
            """;

        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        FunctionDeclarationNode function = program.getFunctions().get(0);

        assertEquals("int", ((BasicTypeNode) function.getParameters().get(0).getType()).getTypeName());
        assertEquals("float", ((BasicTypeNode) function.getParameters().get(1).getType()).getTypeName());
        assertEquals("string", ((BasicTypeNode) function.getParameters().get(2).getType()).getTypeName());
        assertEquals("bool", ((BasicTypeNode) function.getParameters().get(3).getType()).getTypeName());

        assertTrue(function.getParameters().get(4).getType() instanceof ArrayTypeNode);
        assertTrue(function.getParameters().get(5).getType() instanceof ArrayTypeNode);
        assertTrue(function.getParameters().get(6).getType() instanceof StructTypeNode);
        assertEquals("Point", ((StructTypeNode) function.getParameters().get(6).getType()).getStructName());
    }

    @Test
    public void testStructsAndFieldAccess() {
        String code = """
            struct Point {
                float x;
                float y;
            }

            func void move(Point p) {
                p.x = p.x + 1.0;
                p.y = p.y + 1.0;
            }
            """;

        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        assertEquals(1, program.getStructs().size());
        StructDeclarationNode struct = program.getStructs().get(0);
        assertEquals("Point", struct.getName());
        assertEquals(2, struct.getFields().size());
        assertEquals("x", struct.getFields().get(0).getName());
        assertEquals("float", ((BasicTypeNode) struct.getFields().get(0).getType()).getTypeName());

        assertEquals(1, program.getFunctions().size());
        FunctionDeclarationNode func = program.getFunctions().get(0);
        assertEquals("move", func.getName());
        assertTrue(func.getParameters().get(0).getType() instanceof StructTypeNode);
        assertEquals("Point", ((StructTypeNode) func.getParameters().get(0).getType()).getStructName());

        ExpressionStatementNode stmt = (ExpressionStatementNode) func.getBody().getStatements().get(0);
        AssignmentNode assign = (AssignmentNode) stmt.getExpression();

        assertTrue(assign.getTarget() instanceof FieldAccessNode);
        FieldAccessNode fieldAccess = (FieldAccessNode) assign.getTarget();
        assertEquals("x", fieldAccess.getFieldName());
        assertTrue(fieldAccess.getObject() instanceof VariableNode);
        assertEquals("p", ((VariableNode) fieldAccess.getObject()).getName());
    }
}