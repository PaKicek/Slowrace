package org.pakicek.parser;

import org.junit.jupiter.api.Test;

import org.pakicek.parser.lexer.Lexer;
import org.pakicek.parser.lexer.Token;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParserErrorTest {

    @Test
    public void testMissingSemicolon() {
        String code = """
            func void test() {
                int a = 5  // missing semicolon
            }
            """;
        assertThrows(Parser.ParseError.class, () -> parseAndExpectError(code));
    }

    @Test
    public void testMissingBrace() {
        String code = """
            func void test() {
                int a = 5;
            // missing closing brace
            """;
        assertThrows(Parser.ParseError.class, () -> parseAndExpectError(code));
    }

    @Test
    public void testInvalidAssignment() {
        String code = """
            func void test() {
                5 = 10;  // cannot assign to literal
            }
            """;
        assertThrows(Parser.ParseError.class, () -> parseAndExpectError(code));
    }

    @Test
    public void testMissingParenthesis() {
        String code = """
            func void test(int a {
                return a;
            }
            """;
        assertThrows(Parser.ParseError.class, () -> parseAndExpectError(code));
    }

    @Test
    public void testUnexpectedToken() {
        String code = """
            func void test() {
                int a = = 5;  // double equals
            }
            """;
        assertThrows(Parser.ParseError.class, () -> parseAndExpectError(code));
    }

    private void parseAndExpectError(String code) {
        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        parser.parse();
    }
}