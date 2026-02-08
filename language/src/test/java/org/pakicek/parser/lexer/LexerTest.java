package org.pakicek.parser.lexer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class LexerTest {
    private Lexer lexer;

    @Test
    public void testEmptySource() {
        lexer = new Lexer("");
        List<Token> tokens = lexer.scanTokens();

        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.getFirst().getType());
    }

    @Test
    public void testHelloWorld() {
        String code = """
            main (int argc, array string argv[]) {
                println("Hello World!");
            }
            """;
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        TokenType[] expectedTypes = {
                TokenType.MAIN, TokenType.LEFT_PAREN, TokenType.INT, TokenType.IDENTIFIER,
                TokenType.COMMA, TokenType.ARRAY, TokenType.STRING, TokenType.IDENTIFIER,
                TokenType.LEFT_BRACKET, TokenType.RIGHT_BRACKET, TokenType.RIGHT_PAREN,
                TokenType.LEFT_BRACE, TokenType.IDENTIFIER, TokenType.LEFT_PAREN,
                TokenType.STRING_LITERAL, TokenType.RIGHT_PAREN, TokenType.SEMICOLON,
                TokenType.RIGHT_BRACE, TokenType.EOF
        };

        assertTokens(expectedTypes, tokens);

        Token stringLiteral = tokens.get(14);
        assertEquals("Hello World!", stringLiteral.getLiteral());
    }

    @Test
    public void testDataTypes() {
        String code = "int a = 42; float b = 3.14; string s = \"test\"; bool flag = true;";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        TokenType[] expectedTypes = {
                TokenType.INT, TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.INTEGER_LITERAL, TokenType.SEMICOLON,
                TokenType.FLOAT, TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.FLOAT_LITERAL, TokenType.SEMICOLON,
                TokenType.STRING, TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.STRING_LITERAL, TokenType.SEMICOLON,
                TokenType.BOOL, TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.BOOLEAN_LITERAL, TokenType.SEMICOLON,
                TokenType.EOF
        };

        assertTokens(expectedTypes, tokens);

        assertEquals(42L, tokens.get(3).getLiteral());
        assertEquals(3.14, tokens.get(8).getLiteral());
        assertEquals("test", tokens.get(13).getLiteral());
        assertEquals(true, tokens.get(18).getLiteral());
    }

    @Test
    public void testStructDefinition() {
        String code = """
            struct Point {
                float x;
                float y;
            }
            """;
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        TokenType[] expectedTypes = {
                TokenType.STRUCT, TokenType.IDENTIFIER, TokenType.LEFT_BRACE,
                TokenType.FLOAT, TokenType.IDENTIFIER, TokenType.SEMICOLON,
                TokenType.FLOAT, TokenType.IDENTIFIER, TokenType.SEMICOLON,
                TokenType.RIGHT_BRACE, TokenType.EOF
        };

        assertTokens(expectedTypes, tokens);
        assertEquals("Point", tokens.get(1).getLexeme());
    }

    @Test
    public void testFieldAccess() {
        String code = "p.x = 10.5; user.age = 25;";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        TokenType[] expectedTypes = {
                TokenType.IDENTIFIER, TokenType.DOT, TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.FLOAT_LITERAL, TokenType.SEMICOLON,
                TokenType.IDENTIFIER, TokenType.DOT, TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.INTEGER_LITERAL, TokenType.SEMICOLON,
                TokenType.EOF
        };

        assertTokens(expectedTypes, tokens);
    }

    @Test
    public void testDotVsFloat() {
        String code = "3.14 3.x 3.";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        TokenType[] expectedTypes = {
                TokenType.FLOAT_LITERAL,
                TokenType.INTEGER_LITERAL, TokenType.DOT, TokenType.IDENTIFIER,
                TokenType.INTEGER_LITERAL, TokenType.DOT,
                TokenType.EOF
        };

        assertTokens(expectedTypes, tokens);
        assertEquals(3.14, tokens.get(0).getLiteral());
        assertEquals(3L, tokens.get(1).getLiteral()); // 3
        assertEquals("x", tokens.get(3).getLexeme());
        assertEquals(3L, tokens.get(4).getLiteral()); // 3
    }

    @Test
    public void testOperators() {
        String code = "a + b - c * d / e % f & g | h && i || j == k != l > m < n >= o <= p = q . r;";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        TokenType[] expectedTypes = {
                TokenType.IDENTIFIER, TokenType.PLUS, TokenType.IDENTIFIER, TokenType.MINUS,
                TokenType.IDENTIFIER, TokenType.MULTIPLY, TokenType.IDENTIFIER, TokenType.DIVIDE,
                TokenType.IDENTIFIER, TokenType.MODULO, TokenType.IDENTIFIER, TokenType.BITWISE_AND,
                TokenType.IDENTIFIER, TokenType.BITWISE_OR, TokenType.IDENTIFIER, TokenType.AND,
                TokenType.IDENTIFIER, TokenType.OR, TokenType.IDENTIFIER, TokenType.EQUALS,
                TokenType.IDENTIFIER, TokenType.NOT_EQUALS, TokenType.IDENTIFIER, TokenType.GREATER,
                TokenType.IDENTIFIER, TokenType.LESS, TokenType.IDENTIFIER, TokenType.GREATER_EQUAL,
                TokenType.IDENTIFIER, TokenType.LESS_EQUAL, TokenType.IDENTIFIER, TokenType.ASSIGN,
                TokenType.IDENTIFIER, TokenType.DOT, TokenType.IDENTIFIER, TokenType.SEMICOLON, TokenType.EOF
        };

        assertTokens(expectedTypes, tokens);
    }

    @Test
    public void testIncrementDecrement() {
        String code = "a++; b--; ++c; --d;";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        TokenType[] expectedTypes = {
                TokenType.IDENTIFIER, TokenType.INCREMENT, TokenType.SEMICOLON,
                TokenType.IDENTIFIER, TokenType.DECREMENT, TokenType.SEMICOLON,
                TokenType.INCREMENT, TokenType.IDENTIFIER, TokenType.SEMICOLON,
                TokenType.DECREMENT, TokenType.IDENTIFIER, TokenType.SEMICOLON,
                TokenType.EOF
        };

        assertTokens(expectedTypes, tokens);
    }

    @Test
    public void testArrays() {
        String code = "array int numbers[5] = [1, 2, 3, 4, 5];";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        TokenType[] expectedTypes = {
                TokenType.ARRAY, TokenType.INT, TokenType.IDENTIFIER, TokenType.LEFT_BRACKET,
                TokenType.INTEGER_LITERAL, TokenType.RIGHT_BRACKET, TokenType.ASSIGN,
                TokenType.LEFT_BRACKET, TokenType.INTEGER_LITERAL, TokenType.COMMA,
                TokenType.INTEGER_LITERAL, TokenType.COMMA, TokenType.INTEGER_LITERAL,
                TokenType.COMMA, TokenType.INTEGER_LITERAL, TokenType.COMMA,
                TokenType.INTEGER_LITERAL, TokenType.RIGHT_BRACKET, TokenType.SEMICOLON,
                TokenType.EOF
        };

        assertTokens(expectedTypes, tokens);
    }

    @Test
    public void testFunctions() {
        String code = "func int sum(int a, int b) { return a + b; }";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        TokenType[] expectedTypes = {
                TokenType.FUNC, TokenType.INT, TokenType.IDENTIFIER, TokenType.LEFT_PAREN,
                TokenType.INT, TokenType.IDENTIFIER, TokenType.COMMA, TokenType.INT,
                TokenType.IDENTIFIER, TokenType.RIGHT_PAREN, TokenType.LEFT_BRACE,
                TokenType.RETURN, TokenType.IDENTIFIER, TokenType.PLUS, TokenType.IDENTIFIER,
                TokenType.SEMICOLON, TokenType.RIGHT_BRACE, TokenType.EOF
        };

        assertTokens(expectedTypes, tokens);
    }

    @Test
    public void testControlStructures() {
        String code = """
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
                
                while (condition) {
                    doSomething();
                }""";

        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();
        assertTrue(containsTokenType(tokens, TokenType.IF));
        assertTrue(containsTokenType(tokens, TokenType.ELIF));
        assertTrue(containsTokenType(tokens, TokenType.ELSE));
        assertTrue(containsTokenType(tokens, TokenType.FOR));
        assertTrue(containsTokenType(tokens, TokenType.WHILE));
    }

    @Test
    public void testComments() {
        String code = """
                int a = 5; // This is a comment
                string s = "test"; // Another comment
                // Fully commented string
                float b = 3.14;""";

        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();
        TokenType[] expectedTypes = {
                TokenType.INT, TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.INTEGER_LITERAL, TokenType.SEMICOLON,
                TokenType.STRING, TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.STRING_LITERAL, TokenType.SEMICOLON,
                TokenType.FLOAT, TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.FLOAT_LITERAL, TokenType.SEMICOLON,
                TokenType.EOF
        };
        assertTokens(expectedTypes, tokens);
    }

    @Test
    public void testStringLiterals() {
        String code = """
                "Hello World"
                "Multi
                line
                string"
                "String"
                """;

        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        assertEquals(TokenType.STRING_LITERAL, tokens.get(0).getType());
        assertEquals("Hello World", tokens.get(0).getLiteral());
        assertEquals(TokenType.STRING_LITERAL, tokens.get(1).getType());
        assertEquals("Multi\nline\nstring", tokens.get(1).getLiteral());
        assertEquals(TokenType.STRING_LITERAL, tokens.get(2).getType());
        assertEquals("String", tokens.get(2).getLiteral());
    }

    @Test
    public void testNumbers() {
        String code = "int a = 42; float b = 3.14; int c = -100; float d = 0.001;";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        assertEquals(42L, tokens.get(3).getLiteral());
        assertEquals(3.14, tokens.get(8).getLiteral());
        assertEquals(TokenType.MINUS, tokens.get(13).getType());
        assertEquals(100L, tokens.get(14).getLiteral());
        assertEquals(0.001, tokens.get(19).getLiteral());
    }

    @Test
    public void testBooleanLiterals() {
        String code = "bool a = true; bool b = false;";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();
        assertEquals(true, tokens.get(3).getLiteral());
        assertEquals(false, tokens.get(8).getLiteral());
    }

    @Test
    public void testComplexExpression() {
        String code = "result = (a + b) * c / d % e & f | g && h || i;";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        TokenType[] expectedTypes = {
                TokenType.IDENTIFIER, TokenType.ASSIGN, TokenType.LEFT_PAREN, TokenType.IDENTIFIER,
                TokenType.PLUS, TokenType.IDENTIFIER, TokenType.RIGHT_PAREN, TokenType.MULTIPLY,
                TokenType.IDENTIFIER, TokenType.DIVIDE, TokenType.IDENTIFIER, TokenType.MODULO,
                TokenType.IDENTIFIER, TokenType.BITWISE_AND, TokenType.IDENTIFIER, TokenType.BITWISE_OR,
                TokenType.IDENTIFIER, TokenType.AND, TokenType.IDENTIFIER, TokenType.OR,
                TokenType.IDENTIFIER, TokenType.SEMICOLON, TokenType.EOF
        };

        assertTokens(expectedTypes, tokens);
    }

    @Test
    public void testPositionTracking() {
        String code = "int a = 5;\nstring b = \"test\";";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        Token firstToken = tokens.getFirst();
        assertEquals(TokenType.INT, firstToken.getType());
        assertEquals(1, firstToken.getLine());
        assertEquals(1, firstToken.getPosition());
        Token identifierToken = tokens.get(1);
        assertEquals(TokenType.IDENTIFIER, identifierToken.getType());
        assertEquals(1, identifierToken.getLine());
        assertEquals(5, identifierToken.getPosition());
        Token stringToken = tokens.get(5);
        assertEquals(TokenType.STRING, stringToken.getType());
        assertEquals(2, stringToken.getLine());
        assertEquals(1, stringToken.getPosition());
    }

    @Test
    public void testDetailedPositionTracking() {
        String code = "int x = 42;";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        assertEquals(1, tokens.get(0).getPosition());
        assertEquals(1, tokens.get(0).getLine());
        assertEquals(5, tokens.get(1).getPosition());
        assertEquals(1, tokens.get(1).getLine());
        assertEquals(7, tokens.get(2).getPosition());
        assertEquals(1, tokens.get(2).getLine());
        assertEquals(9, tokens.get(3).getPosition());
        assertEquals(1, tokens.get(3).getLine());
        assertEquals(11, tokens.get(4).getPosition());
        assertEquals(1, tokens.get(4).getLine());
    }

    @Test
    public void testMultiLinePositionTracking() {
        String code = "line1\nline2\nline3";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        assertEquals(4, tokens.size());
        assertEquals(1, tokens.get(0).getLine());
        assertEquals(1, tokens.get(0).getPosition());
        assertEquals(2, tokens.get(1).getLine());
        assertEquals(1, tokens.get(1).getPosition());
        assertEquals(3, tokens.get(2).getLine());
        assertEquals(1, tokens.get(2).getPosition());
    }

    @Test
    public void testAllKeywords() {
        String code = "int float string bool array func main return if else elif for while void struct";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        TokenType[] expectedTypes = {
                TokenType.INT, TokenType.FLOAT, TokenType.STRING, TokenType.BOOL,
                TokenType.ARRAY, TokenType.FUNC, TokenType.MAIN, TokenType.RETURN,
                TokenType.IF, TokenType.ELSE, TokenType.ELIF, TokenType.FOR,
                TokenType.WHILE, TokenType.VOID, TokenType.STRUCT, TokenType.EOF
        };

        assertTokens(expectedTypes, tokens);
    }

    @Test
    public void testUnterminatedString() {
        Lexer lexer = new Lexer("string s = \"unterminated;");
        List<Token> tokens = lexer.scanTokens();
        assertTrue(containsTokenType(tokens, TokenType.ERROR));
    }

    @Test
    public void testLargeNumbers() {
        Lexer lexer = new Lexer("int big = 9223372036854775807; float small = 0.0000001;");
        List<Token> tokens = lexer.scanTokens();
        assertEquals(9223372036854775807L, tokens.get(3).getLiteral());
        assertEquals(0.0000001, tokens.get(8).getLiteral());
    }

    @Test
    public void testMixedExpressions() {
        String code = "if (a && b || c & d | e) { result = (x + y) * z / w; }";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();
        assertTrue(containsTokenType(tokens, TokenType.AND));
        assertTrue(containsTokenType(tokens, TokenType.OR));
        assertTrue(containsTokenType(tokens, TokenType.BITWISE_AND));
        assertTrue(containsTokenType(tokens, TokenType.BITWISE_OR));
    }

    @Test
    public void testNestedStructures() {
        String code = """
                func void process(array int data[]) {
                    for (int i = 0; i < len(data); i++) {
                        if (data[i] > 0) {
                            print("Positive: " + data[i]);
                        }
                    }
                }""";

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        assertTrue(containsTokenType(tokens, TokenType.FUNC));
        assertTrue(containsTokenType(tokens, TokenType.VOID));
        assertTrue(containsTokenType(tokens, TokenType.ARRAY));
        assertTrue(containsTokenType(tokens, TokenType.FOR));
        assertTrue(containsTokenType(tokens, TokenType.IF));
    }

    @Test
    public void testSpecialCharacters() {
        String code = "a_b_c = 123; _private = 456; var123 = 789;";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(4).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(8).getType());
    }

    @Test
    public void testEmptyArray() {
        String code = "array int empty[] = [];";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        assertTrue(containsTokenType(tokens, TokenType.ARRAY));
        assertTrue(containsTokenType(tokens, TokenType.LEFT_BRACKET));
        assertTrue(containsTokenType(tokens, TokenType.RIGHT_BRACKET));
    }

    @Test
    public void testMultipleComments() {
        String code = """
                // First comment
                int a = 1; // Second comment
                // Third comment
                string b = "test";""";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        assertEquals(11, tokens.size());
        assertEquals(TokenType.INT, tokens.get(0).getType());
        assertEquals(TokenType.STRING, tokens.get(5).getType());
    }

    private void assertTokens(TokenType[] expectedTypes, List<Token> actualTokens) {
        assertEquals(expectedTypes.length, actualTokens.size(), "The number of tokens does not match");
        for (int i = 0; i < expectedTypes.length; i++) {
            assertEquals(expectedTypes[i], actualTokens.get(i).getType(), "The token at position " + i + " does not match");
        }
    }

    private boolean containsTokenType(List<Token> tokens, TokenType type) {
        for (Token token : tokens) {
            if (token.getType() == type) {
                return true;
            }
        }
        return false;
    }
}