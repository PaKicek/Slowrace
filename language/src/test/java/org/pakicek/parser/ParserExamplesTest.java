package org.pakicek.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import org.pakicek.parser.ast.node.*;
import org.pakicek.parser.ast.node.expression.*;
import org.pakicek.parser.ast.node.statement.*;
import org.pakicek.parser.lexer.Lexer;
import org.pakicek.parser.lexer.Token;

import java.util.List;

public class ParserExamplesTest {

    @Test
    public void testInsertionSortExample() {
        String code = """
                func array int insertion_sort (array int numbers) { // up to 10000 elements
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

        // Check insertion_sort function
        FunctionDeclarationNode sortFunc = program.getFunctions().get(0);
        assertEquals("insertion_sort", sortFunc.getName());
        assertEquals(1, sortFunc.getParameters().size());

        // Check main function
        MainNode main = program.getMainNode();
        assertNotNull(main.getBody());
        assertFalse(main.getBody().getStatements().isEmpty());
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

        // Check iterative factorial
        FunctionDeclarationNode iterative = program.getFunctions().get(0);
        assertEquals("factorial_iterative", iterative.getName());
        assertEquals(1, iterative.getParameters().size());

        // Check recursive factorial has proper structure
        FunctionDeclarationNode recursive = program.getFunctions().get(1);
        BlockStatementNode body = recursive.getBody();
        assertEquals(2, body.getStatements().size());
        assertTrue(body.getStatements().get(0) instanceof IfStatementNode);
        assertTrue(body.getStatements().get(1) instanceof ReturnStatementNode);
    }

    @Test
    public void testSieveOfEratosthenes() {
        String code = """
                func void sieve (int n) { // up to 100000
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

        // Check sieve function structure
        FunctionDeclarationNode sieve = program.getFunctions().get(0);
        assertEquals("sieve", sieve.getName());

        // Check complex condition: p * p <= n
        BlockStatementNode body = sieve.getBody();
        IfStatementNode ifStmt = (IfStatementNode) body.getStatements().get(0);
        assertTrue(ifStmt.getCondition() instanceof BinaryExpressionNode);
    }
}