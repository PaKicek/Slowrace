package org.pakicek.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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

        ExpressionStatementNode stmt = (ExpressionStatementNode) program.getMainNode().getBody().getStatements().getFirst();
        assertInstanceOf(FunctionCallNode.class, stmt.getExpression());
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
        FunctionDeclarationNode function = program.getFunctions().getFirst();
        assertEquals("factorial", function.getName());
        assertEquals("int", ((BasicTypeNode) function.getReturnType()).getTypeName());
        assertEquals(1, function.getParameters().size());
        assertEquals("n", function.getParameters().getFirst().getName());
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

        FunctionDeclarationNode function = program.getFunctions().getFirst();
        BlockStatementNode body = function.getBody();
        assertEquals(4, body.getStatements().size());

        assertInstanceOf(VariableDeclarationNode.class, body.getStatements().getFirst());
        VariableDeclarationNode varA = (VariableDeclarationNode) body.getStatements().getFirst();
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

        FunctionDeclarationNode function = program.getFunctions().getFirst();
        BlockStatementNode body = function.getBody();

        VariableDeclarationNode fixedArray = (VariableDeclarationNode) body.getStatements().getFirst();
        assertInstanceOf(ArrayTypeNode.class, fixedArray.getType());
        ArrayTypeNode fixedType = (ArrayTypeNode) fixedArray.getType();
        assertTrue(fixedType.isFixedSize(), "Should be fixed size");
        assertEquals(5, fixedType.getFixedSize().intValue());
        assertNull(fixedType.getSizeExpression(), "Should not have size expression");

        VariableDeclarationNode dynamicArray = (VariableDeclarationNode) body.getStatements().get(1);
        ArrayTypeNode dynamicType = (ArrayTypeNode) dynamicArray.getType();
        assertTrue(dynamicType.isDynamicSize(), "Should be dynamic size");
        assertNotNull(dynamicType.getSizeExpression(), "Should have size expression");
        assertNull(dynamicType.getFixedSize(), "Should not have fixed size");

        VariableDeclarationNode noSizeArray = (VariableDeclarationNode) body.getStatements().get(2);
        ArrayTypeNode noSizeType = (ArrayTypeNode) noSizeArray.getType();
        assertFalse(noSizeType.isFixedSize(), "Should not be fixed size");
        assertFalse(noSizeType.isDynamicSize(), "Should not be dynamic size");
        assertNull(noSizeType.getFixedSize(),"Should not have fixed size");
        assertNull(noSizeType.getSizeExpression(), "Should not have size expression");
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

        FunctionDeclarationNode function = program.getFunctions().getFirst();
        BlockStatementNode body = function.getBody();

        assertInstanceOf(IfStatementNode.class, body.getStatements().getFirst());
        IfStatementNode ifStmt = (IfStatementNode) body.getStatements().getFirst();
        assertNotNull(ifStmt.getCondition());
        assertNotNull(ifStmt.getThenBlock());
        assertEquals(1, ifStmt.getElifBranches().size());
        assertNotNull(ifStmt.getElseBlock());

        assertInstanceOf(ForLoopNode.class, body.getStatements().get(1));
        ForLoopNode forLoop = (ForLoopNode) body.getStatements().get(1);
        assertNotNull(forLoop.getInitialization());
        assertNotNull(forLoop.getCondition());
        assertNotNull(forLoop.getUpdate());

        assertInstanceOf(WhileLoopNode.class, body.getStatements().get(2));
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

        FunctionDeclarationNode function = program.getFunctions().getFirst();
        ReturnStatementNode returnStmt = (ReturnStatementNode) function.getBody().getStatements().getFirst();
        assertInstanceOf(BinaryExpressionNode.class, returnStmt.getValue());
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

        FunctionDeclarationNode function = program.getFunctions().getFirst();
        BlockStatementNode body = function.getBody();

        assertInstanceOf(ExpressionStatementNode.class, body.getStatements().getFirst());
        ExpressionStatementNode exprStmt1 = (ExpressionStatementNode) body.getStatements().getFirst();
        assertInstanceOf(AssignmentNode.class, exprStmt1.getExpression());

        for (int i = 1; i < 3; i++) {
            assertInstanceOf(ExpressionStatementNode.class, body.getStatements().get(i));
            ExpressionStatementNode exprStmt = (ExpressionStatementNode) body.getStatements().get(i);
            assertInstanceOf(FunctionCallNode.class, exprStmt.getExpression());
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

        FunctionDeclarationNode function = program.getFunctions().getFirst();
        BlockStatementNode body = function.getBody();

        assertInstanceOf(VariableDeclarationNode.class, body.getStatements().get(0));

        assertInstanceOf(ExpressionStatementNode.class, body.getStatements().get(1));
        ExpressionStatementNode assignStmt = (ExpressionStatementNode) body.getStatements().get(1);
        assertInstanceOf(AssignmentNode.class, assignStmt.getExpression());
        AssignmentNode assignment = (AssignmentNode) assignStmt.getExpression();
        assertInstanceOf(ArrayAccessNode.class, assignment.getTarget());

        assertInstanceOf(ExpressionStatementNode.class, body.getStatements().get(2));
        ExpressionStatementNode accessStmt = (ExpressionStatementNode) body.getStatements().get(2);
        assertInstanceOf(AssignmentNode.class, accessStmt.getExpression());
        AssignmentNode access = (AssignmentNode) accessStmt.getExpression();
        assertInstanceOf(ArrayAccessNode.class, access.getValue());
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

        FunctionDeclarationNode function = program.getFunctions().getFirst();
        BlockStatementNode body = function.getBody();

        for (int i = 0; i < 4; i++) {
            assertInstanceOf(ExpressionStatementNode.class, body.getStatements().get(i), "Statement " + i + " should be ExpressionStatement");
            ExpressionStatementNode stmt = (ExpressionStatementNode) body.getStatements().get(i);
            assertInstanceOf(AssignmentNode.class, stmt.getExpression(), "Expression in statement " + i + " should be AssignmentNode");
        }

        ExpressionStatementNode chainedStmt = (ExpressionStatementNode) body.getStatements().get(3);
        AssignmentNode chained = (AssignmentNode) chainedStmt.getExpression();
        assertInstanceOf(AssignmentNode.class, chained.getValue());
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
        assertInstanceOf(IfStatementNode.class, main.getBody().getStatements().getFirst());
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

        FunctionDeclarationNode function = program.getFunctions().getFirst();
        ReturnStatementNode returnStmt = (ReturnStatementNode) function.getBody().getStatements().getFirst();

        assertInstanceOf(BinaryExpressionNode.class, returnStmt.getValue());
        BinaryExpressionNode binaryExpr = (BinaryExpressionNode) returnStmt.getValue();
        assertInstanceOf(AssignmentNode.class, binaryExpr.getLeft());
    }
}