package org.pakicek.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.pakicek.parser.ast.node.*;
import org.pakicek.parser.ast.node.expression.*;
import org.pakicek.parser.ast.node.statement.*;
import org.pakicek.parser.ast.node.type.*;
import org.pakicek.parser.lexer.Lexer;
import org.pakicek.parser.lexer.Token;

import java.util.List;

public class ParserTest {
    private Parser parser;

    @Test
    public void testEmptyProgram() {
        String code = "";
        List<Token> tokens = new Lexer(code).scanTokens();
        parser = new Parser(tokens);

        ProgramNode program = parser.parse();
        assertNotNull(program);
        assertTrue(program.getFunctions().isEmpty());
        assertNull(program.getMainNode());
    }

    @Test
    public void testHelloWorld() {
        String code = """
            main (int argc, array string argv[]) {
                println("Hello World!");
            }
            """;

        List<Token> tokens = new Lexer(code).scanTokens();
        parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        assertNotNull(program);
        assertNotNull(program.getMainNode());
        assertEquals(1, program.getMainNode().getBody().getStatements().size());

        ExpressionStatementNode stmt = (ExpressionStatementNode) program.getMainNode().getBody().getStatements().get(0);
        assertTrue(stmt.getExpression() instanceof FunctionCallNode);
    }

    @Test
    public void testFunctionDeclaration() {
        String code = """
            func int factorial(int n) {
                return n * factorial(n - 1);
            }
            """;

        List<Token> tokens = new Lexer(code).scanTokens();
        parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        assertEquals(1, program.getFunctions().size());
        FunctionDeclarationNode function = program.getFunctions().get(0);
        assertEquals("factorial", function.getName());
        assertEquals("int", ((BasicTypeNode) function.getReturnType()).getTypeName());
        assertEquals(1, function.getParameters().size());
        assertEquals("n", function.getParameters().get(0).getName());
    }

    @Test
    public void testVariableDeclaration() {
        String code = """
            func void test() {
                int a;
                float b = 3.14;
                string s = "hello";
                bool flag = true;
            }
            """;

        List<Token> tokens = new Lexer(code).scanTokens();
        parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        FunctionDeclarationNode function = program.getFunctions().get(0);
        BlockStatementNode body = function.getBody();
        assertEquals(4, body.getStatements().size());

        assertTrue(body.getStatements().get(0) instanceof VariableDeclarationNode);
        VariableDeclarationNode varA = (VariableDeclarationNode) body.getStatements().get(0);
        assertEquals("a", varA.getName());
        assertNull(varA.getInitialValue());

        VariableDeclarationNode varB = (VariableDeclarationNode) body.getStatements().get(1);
        assertEquals("b", varB.getName());
        assertNotNull(varB.getInitialValue());
    }

    @Test
    public void testArrayDeclarations() {
        String code = """
        func void test() {
            array int fixed[5];
            array bool dynamic[n + 1];
            array string strings[] = ["a", "b", "c"];
        }
        """;

        List<Token> tokens = new Lexer(code).scanTokens();
        parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        FunctionDeclarationNode function = program.getFunctions().get(0);
        BlockStatementNode body = function.getBody();

        VariableDeclarationNode fixedArray = (VariableDeclarationNode) body.getStatements().get(0);
        assertTrue(fixedArray.getType() instanceof ArrayTypeNode);
        ArrayTypeNode fixedType = (ArrayTypeNode) fixedArray.getType();
        assertTrue("Should be fixed size", fixedType.isFixedSize());
        assertEquals(5, fixedType.getFixedSize().intValue());
        assertNull("Should not have size expression", fixedType.getSizeExpression());

        VariableDeclarationNode dynamicArray = (VariableDeclarationNode) body.getStatements().get(1);
        ArrayTypeNode dynamicType = (ArrayTypeNode) dynamicArray.getType();
        assertTrue("Should be dynamic size", dynamicType.isDynamicSize());
        assertNotNull("Should have size expression", dynamicType.getSizeExpression());
        assertNull("Should not have fixed size", dynamicType.getFixedSize());

        VariableDeclarationNode noSizeArray = (VariableDeclarationNode) body.getStatements().get(2);
        ArrayTypeNode noSizeType = (ArrayTypeNode) noSizeArray.getType();
        assertFalse("Should not be fixed size", noSizeType.isFixedSize());
        assertFalse("Should not be dynamic size", noSizeType.isDynamicSize());
        assertNull("Should not have fixed size", noSizeType.getFixedSize());
        assertNull("Should not have size expression", noSizeType.getSizeExpression());
    }

    @Test
    public void testControlStructures() {
        String code = """
            func void test(int x) {
                if (x > 0) {
                    print("positive");
                } elif (x < 0) {
                    print("negative");
                } else {
                    print("zero");
                }
            
                for (int i = 0; i < 10; i++) {
                    println(i);
                }
            
                while (x > 0) {
                    x = x - 1;
                }
            }
            """;

        List<Token> tokens = new Lexer(code).scanTokens();
        parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        FunctionDeclarationNode function = program.getFunctions().get(0);
        BlockStatementNode body = function.getBody();

        assertTrue(body.getStatements().get(0) instanceof IfStatementNode);
        IfStatementNode ifStmt = (IfStatementNode) body.getStatements().get(0);
        assertNotNull(ifStmt.getCondition());
        assertNotNull(ifStmt.getThenBlock());
        assertEquals(1, ifStmt.getElifBranches().size());
        assertNotNull(ifStmt.getElseBlock());

        assertTrue(body.getStatements().get(1) instanceof ForLoopNode);
        ForLoopNode forLoop = (ForLoopNode) body.getStatements().get(1);
        assertNotNull(forLoop.getInitialization());
        assertNotNull(forLoop.getCondition());
        assertNotNull(forLoop.getUpdate());

        assertTrue(body.getStatements().get(2) instanceof WhileLoopNode);
        WhileLoopNode whileLoop = (WhileLoopNode) body.getStatements().get(2);
        assertNotNull(whileLoop.getCondition());
    }

    @Test
    public void testExpressions() {
        String code = """
            func int test(int a, int b) {
                return a + b * (c - d) / e % f & g | h && i || j;
            }
            """;

        List<Token> tokens = new Lexer(code).scanTokens();
        parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        FunctionDeclarationNode function = program.getFunctions().get(0);
        ReturnStatementNode returnStmt = (ReturnStatementNode) function.getBody().getStatements().get(0);
        assertTrue(returnStmt.getValue() instanceof BinaryExpressionNode);
    }

    @Test
    public void testFunctionCalls() {
        String code = """
            func void test() {
                result = factorial(5);
                print("Result: " + result);
                len(arr);
                to_int("123");
            }
            """;

        List<Token> tokens = new Lexer(code).scanTokens();
        parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        FunctionDeclarationNode function = program.getFunctions().get(0);
        BlockStatementNode body = function.getBody();

        assertTrue(body.getStatements().get(0) instanceof ExpressionStatementNode);
        ExpressionStatementNode exprStmt1 = (ExpressionStatementNode) body.getStatements().get(0);
        assertTrue(exprStmt1.getExpression() instanceof AssignmentNode);

        for (int i = 1; i < 3; i++) {
            assertTrue(body.getStatements().get(i) instanceof ExpressionStatementNode);
            ExpressionStatementNode exprStmt = (ExpressionStatementNode) body.getStatements().get(i);
            assertTrue(exprStmt.getExpression() instanceof FunctionCallNode);
        }
    }

    @Test
    public void testArrayOperations() {
        String code = """
            func void test() {
                array int arr[10];
                arr[0] = 1;
                value = arr[index];
            }
            """;

        List<Token> tokens = new Lexer(code).scanTokens();
        parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        FunctionDeclarationNode function = program.getFunctions().get(0);
        BlockStatementNode body = function.getBody();

        assertTrue(body.getStatements().get(0) instanceof VariableDeclarationNode);

        assertTrue(body.getStatements().get(1) instanceof ExpressionStatementNode);
        ExpressionStatementNode assignStmt = (ExpressionStatementNode) body.getStatements().get(1);
        assertTrue(assignStmt.getExpression() instanceof AssignmentNode);
        AssignmentNode assignment = (AssignmentNode) assignStmt.getExpression();
        assertTrue(assignment.getTarget() instanceof ArrayAccessNode);

        assertTrue(body.getStatements().get(2) instanceof ExpressionStatementNode);
        ExpressionStatementNode accessStmt = (ExpressionStatementNode) body.getStatements().get(2);
        assertTrue(accessStmt.getExpression() instanceof AssignmentNode);
        AssignmentNode access = (AssignmentNode) accessStmt.getExpression();
        assertTrue(access.getValue() instanceof ArrayAccessNode);
    }

    @Test
    public void testAssignmentAsExpression() {
        String code = """
            func void test() {
                a = 5;
                b = a + 10;
                arr[0] = 1;
                x = y = z;  // chained assignment
            }
            """;

        List<Token> tokens = new Lexer(code).scanTokens();
        parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        FunctionDeclarationNode function = program.getFunctions().get(0);
        BlockStatementNode body = function.getBody();

        for (int i = 0; i < 4; i++) {
            assertTrue("Statement " + i + " should be ExpressionStatement", body.getStatements().get(i) instanceof ExpressionStatementNode);
            ExpressionStatementNode stmt = (ExpressionStatementNode) body.getStatements().get(i);
            assertTrue("Expression in statement " + i + " should be AssignmentNode", stmt.getExpression() instanceof AssignmentNode);
        }

        ExpressionStatementNode chainedStmt = (ExpressionStatementNode) body.getStatements().get(3);
        AssignmentNode chained = (AssignmentNode) chainedStmt.getExpression();
        assertTrue(chained.getValue() instanceof AssignmentNode);
    }

    @Test
    public void testMainFunction() {
        String code = """
            main (int argc, array string argv[]) {
                if (argc > 1) {
                    println(argv[1]);
                }
            }
            """;

        List<Token> tokens = new Lexer(code).scanTokens();
        parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        assertNotNull(program);
        assertNotNull(program.getMainNode());
        assertTrue(program.getFunctions().isEmpty());

        MainNode main = program.getMainNode();
        assertEquals(1, main.getBody().getStatements().size());
        assertTrue(main.getBody().getStatements().get(0) instanceof IfStatementNode);
    }

    @Test
    public void testComplexExpressionWithAssignment() {
        String code = """
            func int test() {
                return (a = b + c) * 2;
            }
            """;

        List<Token> tokens = new Lexer(code).scanTokens();
        parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        FunctionDeclarationNode function = program.getFunctions().get(0);
        ReturnStatementNode returnStmt = (ReturnStatementNode) function.getBody().getStatements().get(0);

        assertTrue(returnStmt.getValue() instanceof BinaryExpressionNode);
        BinaryExpressionNode binaryExpr = (BinaryExpressionNode) returnStmt.getValue();
        assertTrue(binaryExpr.getLeft() instanceof AssignmentNode);
    }
}