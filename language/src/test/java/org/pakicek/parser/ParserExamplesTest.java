package org.pakicek.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.pakicek.parser.ast.node.*;
import org.pakicek.parser.ast.node.expression.*;
import org.pakicek.parser.ast.node.statement.*;
import org.pakicek.parser.ast.node.type.BasicTypeNode;
import org.pakicek.parser.lexer.Lexer;
import org.pakicek.parser.lexer.Token;

import java.util.List;

public class ParserExamplesTest {

    @Test
    public void testInsertionSortExample() {
        String code = """
                func array int insertion_sort (array int numbers) {
                     for (int i = 1; i < len(numbers); i++) {
                         int key = numbers[i];
                         int j = i - 1;
                         while (j >= 0 && numbers[j] > key) {
                             numbers[j + 1] = numbers[j];
                             j = j - 1;
                         }
                         numbers[j + 1] = key;
                     }
                     return numbers;
                 }
            
                 main (int argc, array string argv[]) {
                     array int numbers[10] = [3, 4, 2, 6, 7, 4, 0, -4, -9, 23];
                     print("The original array is ");
                     for (int i = 0; i < len(numbers); i++) {
                         print(numbers[i]);
                         print(" ");
                     }
                     println("");
                     array int result[10] = insertion_sort(numbers);
                     print("The sorted array is ");
                     for (int i = 0; i < len(result); i++) {
                         print(result[i]);
                         print(" ");
                     }
                     println("");
                 }
            """;

        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        assertNotNull(program);
        assertEquals(1, program.getFunctions().size());
        assertNotNull(program.getMainNode());

        FunctionDeclarationNode sortFunc = program.getFunctions().getFirst();
        assertEquals("insertion_sort", sortFunc.getName());
        assertEquals(1, sortFunc.getParameters().size());

        MainNode main = program.getMainNode();
        assertNotNull(main.getBody());
        assertFalse(main.getBody().getStatements().isEmpty());
    }

    @Test
    public void testQuickSortExample() {
        String code = """
            func int partition (array int arr, int low, int high) {
                int pivot = arr[high];
                int i = low - 1;
                for (int j = low; j < high; j++) {
                    if (arr[j] < pivot) {
                        i++;
                        int temp = arr[i];
                        arr[i] = arr[j];
                        arr[j] = temp;
                    }
                }
                int temp = arr[i + 1];
                arr[i + 1] = arr[high];
                arr[high] = temp;
                return i + 1;
            }
        
            func void quick_sort_recursive (array int arr, int low, int high) {
                if (low < high) {
                    int pi = partition(arr, low, high);
                    quick_sort_recursive(arr, low, pi - 1);
                    quick_sort_recursive(arr, pi + 1, high);
                }
            }
        
            func array int quick_sort (array int arr) {
                quick_sort_recursive(arr, 0, len(arr) - 1);
                return arr;
            }
        
            main (int argc, array string argv[]) {
                // Main body simplified for parsing test
                quick_sort(numbers);
            }
        """;

        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        assertNotNull(program);
        assertEquals(3, program.getFunctions().size());

        assertEquals("partition", program.getFunctions().get(0).getName());
        assertEquals("quick_sort_recursive", program.getFunctions().get(1).getName());
        assertEquals("quick_sort", program.getFunctions().get(2).getName());

        BlockStatementNode body = program.getFunctions().get(1).getBody();
        IfStatementNode ifStmt = (IfStatementNode) body.getStatements().getFirst();
        BlockStatementNode thenBlock = ifStmt.getThenBlock();
        assertEquals(3, thenBlock.getStatements().size());
        assertInstanceOf(FunctionCallNode.class, ((ExpressionStatementNode) thenBlock.getStatements().get(1)).getExpression());
    }

    @Test
    public void testFibonacciExample() {
        String code = """
            func int fib_iterative(int n) {
                if (n <= 1) { return n; }
                int a = 0;
                int b = 1;
                for (int i = 2; i <= n; i++) {
                    int temp = a + b;
                    a = b;
                    b = temp;
                }
                return b;
            }
        
            main (int argc, array string argv[]) {
                int res = fib_iterative(10);
            }
        """;

        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        assertNotNull(program);
        assertEquals(1, program.getFunctions().size());

        FunctionDeclarationNode fibFunc = program.getFunctions().getFirst();
        assertEquals("fib_iterative", fibFunc.getName());
        assertNotEquals("void", ((BasicTypeNode) fibFunc.getReturnType()).getTypeName());

        boolean hasLoop = false;
        for(StatementNode stmt : fibFunc.getBody().getStatements()) {
            if (stmt instanceof ForLoopNode) {
                hasLoop = true;
                break;
            }
        }
        assertTrue(hasLoop, "Fibonacci function should have a loop");
    }

    @Test
    public void testFactorialExamples() {
        String code = """
                func int factorial_iterative(int n) {
                    int result = 1;
                    for (int i = 2; i <= n; i = i + 1) {
                        result = result * i;
                    }
                    return result;
                }
    
                func int factorial_recursive(int n) {
                    if (n <= 1) {
                        return 1;
                    }
                    return n * factorial_recursive(n - 1);
                }
    
                main (int argc, array string argv[]) {
                    int result1 = factorial_iterative(5);
                    int result2 = factorial_recursive(5);
                    println(result1);
                    println(result2);
                }
            """;

        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        assertNotNull(program);
        assertEquals(2, program.getFunctions().size());
        assertNotNull(program.getMainNode());

        FunctionDeclarationNode iterative = program.getFunctions().getFirst();
        assertEquals("factorial_iterative", iterative.getName());
        assertEquals(1, iterative.getParameters().size());

        FunctionDeclarationNode recursive = program.getFunctions().get(1);
        BlockStatementNode body = recursive.getBody();
        assertEquals(2, body.getStatements().size());
        assertInstanceOf(IfStatementNode.class, body.getStatements().get(0));
        assertInstanceOf(ReturnStatementNode.class, body.getStatements().get(1));
    }

    @Test
    public void testSieveOfEratosthenes() {
        String code = """
                func void sieve (int n) {
                     if (n < 2) {
                         println("There are no prime numbers less than 2");
                     } else {
                         array bool primes[n + 1];
                         for (int i = 0; i <= n; i++) {
                             primes[i] = true;
                         }
                         primes[0] = false;
                         primes[1] = false;
                         int p = 2;
                         while (p * p <= n) {
                             if (primes[p] == true) {
                                 for (int i = p * p; i <= n; i = i + p) {
                                     primes[i] = false;
                                 }
                             }
                             p = p + 1;
                         }
                         print("The prime numbers are ");
                         for (int i = 2; i <= n; i++) {
                             if (primes[i] == true) {
                                 print(i);
                                 print(" ");
                             }
                         }
                         println("");
                     }
                 }
            
                 main (int argc, array string argv[]) {
                     if (argc >= 2) {
                         int n = to_int(argv[1]);
                         sieve(n);
                     } else {
                         println("No number is passed");
                     }
                 }
            """;

        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        assertNotNull(program);
        assertEquals(1, program.getFunctions().size());
        assertNotNull(program.getMainNode());

        FunctionDeclarationNode sieve = program.getFunctions().getFirst();
        assertEquals("sieve", sieve.getName());

        BlockStatementNode body = sieve.getBody();
        IfStatementNode ifStatement = (IfStatementNode) body.getStatements().getFirst();
        assertInstanceOf(BinaryExpressionNode.class, ifStatement.getCondition());
    }

    @Test
    public void testNBodyExample() {
        String code = """
            struct Body {
                float x;
                float y;
                float z;
                float vx;
                float vy;
                float vz;
                float mass;
            }

            func Body create_body(float x, float y, float z, float vx, float vy, float vz, float mass) {
                Body b;
                b.x = x;
                // ... rest of initialization
                return b;
            }
        
            main (int argc, array string argv[]) {
                array Body bodies[5];
                bodies[0].x = 0.0;
            }
        """;

        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        assertNotNull(program);
        assertEquals(1, program.getStructs().size());
        assertEquals(1, program.getFunctions().size());
        assertNotNull(program.getMainNode());

        StructDeclarationNode bodyStruct = program.getStructs().getFirst();
        assertEquals("Body", bodyStruct.getName());
        assertEquals(7, bodyStruct.getFields().size());

        for(VariableDeclarationNode field : bodyStruct.getFields()) {
            assertInstanceOf(BasicTypeNode.class, field.getType());
            assertEquals("float", ((org.pakicek.parser.ast.node.type.BasicTypeNode)field.getType()).getTypeName());
        }
    }
}