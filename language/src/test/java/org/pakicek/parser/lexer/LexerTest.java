package org.pakicek.parser.lexer;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

public class LexerTest {
    private Lexer lexer;

    @Before
    public void setUp() {
        // Setting up before test
    }

    @Test
    public void testEmptySource() {
        lexer = new Lexer("");
        List<Token> tokens = lexer.scanTokens();

        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).getType());
    }

    @Test
    public void testHelloWorld() {
        String code = "main (int argc, array string argv[]) {\n    println(\"Hello World!\");\n}";
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

        // Testing string literal
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

        // Testing literals
        assertEquals(42L, tokens.get(3).getLiteral());
        assertEquals(3.14, tokens.get(8).getLiteral());
        assertEquals("test", tokens.get(13).getLiteral());
        assertEquals(true, tokens.get(18).getLiteral());
    }

    @Test
    public void testOperators() {
        String code = "a + b - c * d / e % f & g | h && i || j == k != l > m < n >= o <= p = q;";
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
                TokenType.IDENTIFIER, TokenType.SEMICOLON, TokenType.EOF
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

        // Testing keywords
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

        // Comments must be ignored
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

        // Must be 3 string literals + EOF
        //assertEquals(4, tokens.size());
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

        // Checking numeric literals
        assertEquals(42L, tokens.get(3).getLiteral());
        assertEquals(3.14, tokens.get(8).getLiteral());
        // Negative numbers are treated as unary minus + number
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

        // Printing all tokens
        System.out.println("=== Debug Token Positions ===");
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            System.out.println(i + ": " + token.getType() + " '" + token.getLexeme() +
                    "' line=" + token.getLine() + " pos=" + token.getPosition());
        }

        // Checking the first token position
        Token firstToken = tokens.get(0);
        assertEquals(TokenType.INT, firstToken.getType());
        assertEquals(1, firstToken.getLine());
        assertEquals(1, firstToken.getPosition());

        // Token 'a'
        Token identifierToken = tokens.get(1);
        assertEquals(TokenType.IDENTIFIER, identifierToken.getType());
        assertEquals(1, identifierToken.getLine());
        assertEquals(5, identifierToken.getPosition()); // "int a" - 'a' on position 5

        // Token 'string' on the second line
        Token stringToken = tokens.get(5);
        assertEquals(TokenType.STRING, stringToken.getType());
        assertEquals(2, stringToken.getLine());
        assertEquals(1, stringToken.getPosition()); // Start of the second line
    }

    @Test
    public void testDetailedPositionTracking() {
        String code = "int x = 42;";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        // int - position 1
        assertEquals(1, tokens.get(0).getPosition());
        assertEquals(1, tokens.get(0).getLine());

        // x - position 5 (after "int ")
        assertEquals(5, tokens.get(1).getPosition());
        assertEquals(1, tokens.get(1).getLine());

        // = - position 7
        assertEquals(7, tokens.get(2).getPosition());
        assertEquals(1, tokens.get(2).getLine());

        // 42 - position 9
        assertEquals(9, tokens.get(3).getPosition());
        assertEquals(1, tokens.get(3).getLine());

        // ; - position 11
        assertEquals(11, tokens.get(4).getPosition());
        assertEquals(1, tokens.get(4).getLine());
    }

    @Test
    public void testMultiLinePositionTracking() {
        String code = "line1\nline2\nline3";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        // Must be 3 identifiers + EOF
        assertEquals(4, tokens.size());

        // line1 - line 1, position 1
        assertEquals(1, tokens.get(0).getLine());
        assertEquals(1, tokens.get(0).getPosition());

        // line2 - line 2, position 1
        assertEquals(2, tokens.get(1).getLine());
        assertEquals(1, tokens.get(1).getPosition());

        // line3 - line 3, position 1
        assertEquals(3, tokens.get(2).getLine());
        assertEquals(1, tokens.get(2).getPosition());
    }

    @Test
    public void testAllKeywords() {
        String code = "int float string bool array func main return if else elif for while void";
        lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        TokenType[] expectedTypes = {
                TokenType.INT, TokenType.FLOAT, TokenType.STRING, TokenType.BOOL,
                TokenType.ARRAY, TokenType.FUNC, TokenType.MAIN, TokenType.RETURN,
                TokenType.IF, TokenType.ELSE, TokenType.ELIF, TokenType.FOR,
                TokenType.WHILE, TokenType.VOID, TokenType.EOF
        };

        assertTokens(expectedTypes, tokens);
    }

    @Test
    public void testUnterminatedString() {
        Lexer lexer = new Lexer("string s = \"unterminated;");
        List<Token> tokens = lexer.scanTokens();

        // Must catch ERROR on unfinished string
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

        // Testing if all operators are recognized correctly
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

        // Testing keywords
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

        // All identifiers must be recognized correctly
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

        // There should be only code tokens, no comments
        assertEquals(11, tokens.size()); // int, identifier, =, number, ;, string, identifier, =, string, ;, EOF
        assertEquals(TokenType.INT, tokens.get(0).getType());
        assertEquals(TokenType.STRING, tokens.get(5).getType());
    }

    private void assertTokens(TokenType[] expectedTypes, List<Token> actualTokens) {
        assertEquals("The number of tokens does not match", expectedTypes.length, actualTokens.size());

        for (int i = 0; i < expectedTypes.length; i++) {
            assertEquals("The token at position " + i + " does not match",
                    expectedTypes[i], actualTokens.get(i).getType());
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
