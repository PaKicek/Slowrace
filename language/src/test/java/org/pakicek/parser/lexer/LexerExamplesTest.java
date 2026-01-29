package org.pakicek.parser.lexer;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class LexerExamplesTest {

    @Test
    public void testInsertionSortExampleTokens() {
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

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        assertFalse(tokens.isEmpty());

        assertTrue(containsTokenType(tokens, TokenType.FUNC));
        assertTrue(containsTokenType(tokens, TokenType.ARRAY));
        assertTrue(containsTokenType(tokens, TokenType.INT));
        assertTrue(containsTokenType(tokens, TokenType.MAIN));
        assertTrue(containsTokenType(tokens, TokenType.FOR));
        assertTrue(containsTokenType(tokens, TokenType.WHILE));
        assertTrue(containsTokenType(tokens, TokenType.RETURN));

        assertTrue(containsTokenType(tokens, TokenType.ASSIGN));
        assertTrue(containsTokenType(tokens, TokenType.LESS));
        assertTrue(containsTokenType(tokens, TokenType.GREATER));
        assertTrue(containsTokenType(tokens, TokenType.GREATER_EQUAL));
        assertTrue(containsTokenType(tokens, TokenType.AND));
        assertTrue(containsTokenType(tokens, TokenType.PLUS));
        assertTrue(containsTokenType(tokens, TokenType.MINUS));
        assertTrue(containsTokenType(tokens, TokenType.INCREMENT));

        assertTrue(containsTokenType(tokens, TokenType.INTEGER_LITERAL));
        assertTrue(containsTokenType(tokens, TokenType.STRING_LITERAL));
    }

    @Test
    public void testQuickSortExampleTokens() {
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
                if (argc >= 1) {
                    int count = to_int(argv[0]);
                    array int numbers[count];
                    for (int i = 0; i < count; i++) {
                        numbers[i] = random(0, 100);
                    }
                    quick_sort(numbers);
                }
            }
        """;

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        assertFalse(tokens.isEmpty());
        assertTrue(containsTokenType(tokens, TokenType.FUNC));
        assertTrue(containsTokenType(tokens, TokenType.INT));
        assertTrue(containsTokenType(tokens, TokenType.IF));
        assertTrue(containsTokenType(tokens, TokenType.FOR));

        assertTrue(containsTokenWithLexeme(tokens, "partition"));
        assertTrue(containsTokenWithLexeme(tokens, "quick_sort"));
        assertTrue(containsTokenWithLexeme(tokens, "random"));

        assertFalse(containsTokenType(tokens, TokenType.ERROR));
    }

    @Test
    public void testFibonacciExampleTokens() {
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
                if (argc >= 1) {
                    int n = to_int(argv[0]);
                    println(fib_iterative(n));
                }
            }
        """;

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        assertFalse(tokens.isEmpty());
        assertTrue(containsTokenType(tokens, TokenType.FUNC));
        assertTrue(containsTokenType(tokens, TokenType.IF));
        assertTrue(containsTokenType(tokens, TokenType.FOR));
        assertTrue(containsTokenType(tokens, TokenType.RETURN));
        assertTrue(containsTokenWithLexeme(tokens, "fib_iterative"));
        assertTrue(containsTokenType(tokens, TokenType.LESS_EQUAL));


        assertFalse(containsTokenType(tokens, TokenType.ERROR));
    }

    @Test
    public void testFactorialExamplesTokens() {
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

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        assertFalse(tokens.isEmpty());
        assertTrue(containsTokenType(tokens, TokenType.FUNC));
        assertTrue(containsTokenType(tokens, TokenType.INT));
        assertTrue(containsTokenType(tokens, TokenType.IF));
    }

    @Test
    public void testSieveOfEratosthenesTokens() {
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
                     }
                 }
                 main (int argc, array string argv[]) {
                     if (argc >= 2) {
                         int n = to_int(argv[1]);
                         sieve(n);
                     }
                 }
            """;

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();
        assertFalse(tokens.isEmpty());
        assertTrue(containsTokenType(tokens, TokenType.BOOL));
    }

    @Test
    public void testNBodySimulationTokens() {
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
                b.y = y;
                b.z = z;
                b.vx = vx;
                b.vy = vy;
                b.vz = vz;
                b.mass = mass;
                return b;
            }
        
            func void offset_momentum(array Body bodies) {
                float px = 0.0;
                for (int i = 0; i < len(bodies); i++) {
                    px = px + bodies[i].vx * bodies[i].mass;
                }
                bodies[0].vx = -px / 4.0;
            }

            main (int argc, array string argv[]) {
                array Body bodies[5];
                bodies[0] = create_body(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
                offset_momentum(bodies);
                float energy_val = 0.0;
                // sqrt usage
                float dist = sqrt(bodies[0].x * bodies[0].x);
                println(dist);
            }
        """;

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        assertFalse(tokens.isEmpty());

        assertTrue("Tokens should contain STRUCT", containsTokenType(tokens, TokenType.STRUCT));
        assertTrue("Tokens should contain DOT", containsTokenType(tokens, TokenType.DOT));
        assertTrue("Tokens should contain Body identifier", containsTokenWithLexeme(tokens, "Body"));
        assertTrue("Tokens should contain mass identifier", containsTokenWithLexeme(tokens, "mass"));

        boolean fieldAccessFound = false;
        for (int i = 0; i < tokens.size() - 2; i++) {
            if (tokens.get(i).getType() == TokenType.IDENTIFIER &&
                    tokens.get(i+1).getType() == TokenType.DOT &&
                    tokens.get(i+2).getType() == TokenType.IDENTIFIER) {
                fieldAccessFound = true;
                break;
            }
        }
        assertTrue("Should contain field access pattern (obj.prop)", fieldAccessFound);
        assertTrue("Should contain sqrt function", containsTokenWithLexeme(tokens, "sqrt"));

        assertFalse(containsTokenType(tokens, TokenType.ERROR));
    }

    private boolean containsTokenType(List<Token> tokens, TokenType type) {
        return tokens.stream().anyMatch(token -> token.getType() == type);
    }

    private boolean containsTokenWithLexeme(List<Token> tokens, String lexeme) {
        return tokens.stream().anyMatch(token -> token.getLexeme().equals(lexeme));
    }
}