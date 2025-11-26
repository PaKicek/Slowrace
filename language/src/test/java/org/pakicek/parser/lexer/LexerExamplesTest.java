package org.pakicek.parser.lexer;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class LexerExamplesTest {

    @Test
    public void testInsertionSortExampleTokens() {
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

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        // Check that the lexer didn't throw exceptions and created tokens
        assertFalse(tokens.isEmpty());

        // Check for key tokens
        assertTrue(containsTokenType(tokens, TokenType.FUNC));
        assertTrue(containsTokenType(tokens, TokenType.ARRAY));
        assertTrue(containsTokenType(tokens, TokenType.INT));
        assertTrue(containsTokenType(tokens, TokenType.MAIN));
        assertTrue(containsTokenType(tokens, TokenType.FOR));
        assertTrue(containsTokenType(tokens, TokenType.WHILE));
        assertTrue(containsTokenType(tokens, TokenType.RETURN));

        // Check operators
        assertTrue(containsTokenType(tokens, TokenType.ASSIGN));
        assertTrue(containsTokenType(tokens, TokenType.LESS));
        assertTrue(containsTokenType(tokens, TokenType.GREATER));
        assertTrue(containsTokenType(tokens, TokenType.GREATER_EQUAL));
        assertTrue(containsTokenType(tokens, TokenType.AND));
        assertTrue(containsTokenType(tokens, TokenType.PLUS));
        assertTrue(containsTokenType(tokens, TokenType.MINUS));
        assertTrue(containsTokenType(tokens, TokenType.INCREMENT));

        // Check literals
        assertTrue(containsTokenType(tokens, TokenType.INTEGER_LITERAL));
        assertTrue(containsTokenType(tokens, TokenType.STRING_LITERAL));

        // Check specific values
        assertTrue(containsTokenWithLexeme(tokens, "insertion_sort"));
        assertTrue(containsTokenWithLexeme(tokens, "numbers"));
        assertTrue(containsTokenWithLexeme(tokens, "main"));
        assertTrue(containsTokenWithLexeme(tokens, "argc"));
        assertTrue(containsTokenWithLexeme(tokens, "argv"));

        // Check numeric literals
        assertTrue(containsTokenWithLiteral(tokens, 1L));
        assertTrue(containsTokenWithLiteral(tokens, 0L));
        assertTrue(containsTokenWithLiteral(tokens, 23L));

        // Check string literals
        assertTrue(containsTokenWithLiteral(tokens, "The original array is "));
        assertTrue(containsTokenWithLiteral(tokens, "The sorted array is "));
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

        // Check keywords
        assertTrue(containsTokenType(tokens, TokenType.FUNC));
        assertTrue(containsTokenType(tokens, TokenType.INT));
        assertTrue(containsTokenType(tokens, TokenType.IF));
        assertTrue(containsTokenType(tokens, TokenType.FOR));
        assertTrue(containsTokenType(tokens, TokenType.RETURN));
        assertTrue(containsTokenType(tokens, TokenType.MAIN));
        assertTrue(containsTokenType(tokens, TokenType.ARRAY));
        assertTrue(containsTokenType(tokens, TokenType.STRING));

        // Check operators
        assertTrue(containsTokenType(tokens, TokenType.ASSIGN));
        assertTrue(containsTokenType(tokens, TokenType.LESS_EQUAL));
        assertTrue(containsTokenType(tokens, TokenType.MULTIPLY));
        assertTrue(containsTokenType(tokens, TokenType.MINUS));
        assertTrue(containsTokenType(tokens, TokenType.PLUS));

        // Check function identifiers
        assertTrue(containsTokenWithLexeme(tokens, "factorial_iterative"));
        assertTrue(containsTokenWithLexeme(tokens, "factorial_recursive"));
        assertTrue(containsTokenWithLexeme(tokens, "println"));

        // Check numeric literals
        assertTrue(containsTokenWithLiteral(tokens, 1L));
        assertTrue(containsTokenWithLiteral(tokens, 2L));
        assertTrue(containsTokenWithLiteral(tokens, 5L));

        // Check parameters
        assertTrue(containsTokenWithLexeme(tokens, "n"));
        assertTrue(containsTokenWithLexeme(tokens, "result"));
        assertTrue(containsTokenWithLexeme(tokens, "i"));
        assertTrue(containsTokenWithLexeme(tokens, "result1"));
        assertTrue(containsTokenWithLexeme(tokens, "result2"));
    }

    @Test
    public void testSieveOfEratosthenesTokens() {
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

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        assertFalse(tokens.isEmpty());

        // Check keywords
        assertTrue(containsTokenType(tokens, TokenType.FUNC));
        assertTrue(containsTokenType(tokens, TokenType.VOID));
        assertTrue(containsTokenType(tokens, TokenType.INT));
        assertTrue(containsTokenType(tokens, TokenType.BOOL));
        assertTrue(containsTokenType(tokens, TokenType.ARRAY));
        assertTrue(containsTokenType(tokens, TokenType.IF));
        assertTrue(containsTokenType(tokens, TokenType.ELSE));
        assertTrue(containsTokenType(tokens, TokenType.FOR));
        assertTrue(containsTokenType(tokens, TokenType.WHILE));
        assertTrue(containsTokenType(tokens, TokenType.MAIN));
        assertTrue(containsTokenType(tokens, TokenType.STRING));

        // Check operators
        assertTrue(containsTokenType(tokens, TokenType.LESS));
        assertTrue(containsTokenType(tokens, TokenType.LESS_EQUAL));
        assertTrue(containsTokenType(tokens, TokenType.GREATER_EQUAL));
        assertTrue(containsTokenType(tokens, TokenType.EQUALS));
        assertTrue(containsTokenType(tokens, TokenType.MULTIPLY));
        assertTrue(containsTokenType(tokens, TokenType.PLUS));
        assertTrue(containsTokenType(tokens, TokenType.ASSIGN));
        assertTrue(containsTokenType(tokens, TokenType.INCREMENT));

        // Check identifiers
        assertTrue(containsTokenWithLexeme(tokens, "sieve"));
        assertTrue(containsTokenWithLexeme(tokens, "primes"));
        assertTrue(containsTokenWithLexeme(tokens, "p"));
        assertTrue(containsTokenWithLexeme(tokens, "to_int"));
        assertTrue(containsTokenWithLexeme(tokens, "print"));
        assertTrue(containsTokenWithLexeme(tokens, "println"));

        // Check boolean literals
        assertTrue(containsTokenWithLiteral(tokens, true));
        assertTrue(containsTokenWithLiteral(tokens, false));

        // Check numeric literals
        assertTrue(containsTokenWithLiteral(tokens, 0L));
        assertTrue(containsTokenWithLiteral(tokens, 1L));
        assertTrue(containsTokenWithLiteral(tokens, 2L));

        // Check string literals
        assertTrue(containsTokenWithLiteral(tokens, "There are no prime numbers less than 2"));
        assertTrue(containsTokenWithLiteral(tokens, "The prime numbers are "));
        assertTrue(containsTokenWithLiteral(tokens, " "));
        assertTrue(containsTokenWithLiteral(tokens, ""));
        assertTrue(containsTokenWithLiteral(tokens, "No number is passed"));
    }

    @Test
    public void testNoSyntaxErrorsInExamples() {
        String[] examples = {
                """
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
            """,
                """
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
            """,
                """
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
            """
        };

        for (String example : examples) {
            Lexer lexer = new Lexer(example);
            List<Token> tokens = lexer.scanTokens();

            // Check that there are no ERROR tokens
            assertFalse("Should not have ERROR tokens",
                    containsTokenType(tokens, TokenType.ERROR));

            // Check that there is at least one non-trivial token
            assertTrue("Should have non-EOF tokens",
                    tokens.size() > 1);
        }
    }

    // Helper methods
    private boolean containsTokenType(List<Token> tokens, TokenType type) {
        return tokens.stream().anyMatch(token -> token.getType() == type);
    }

    private boolean containsTokenWithLexeme(List<Token> tokens, String lexeme) {
        return tokens.stream().anyMatch(token -> token.getLexeme().equals(lexeme));
    }

    private boolean containsTokenWithLiteral(List<Token> tokens, Object literal) {
        return tokens.stream().anyMatch(token ->
                token.getLiteral() != null && token.getLiteral().equals(literal));
    }
}