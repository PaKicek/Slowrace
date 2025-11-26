package org.pakicek.parser;

import org.junit.Test;
import org.pakicek.parser.lexer.Lexer;
import org.pakicek.parser.lexer.Token;

import java.util.List;

public class ParserErrorTest {

    @Test(expected = Parser.ParseError.class)
    public void testMissingSemicolon() {
        String code = """
            func void test() {
                int a = 5  // missing semicolon
            }
            """;
        parseAndExpectError(code);
    }

    @Test(expected = Parser.ParseError.class)
    public void testMissingBrace() {
        String code = """
            func void test() {
                int a = 5;
            // missing closing brace
            """;
        parseAndExpectError(code);
    }

    @Test(expected = Parser.ParseError.class)
    public void testInvalidAssignment() {
        String code = """
            func void test() {
                5 = 10;  // cannot assign to literal
            }
            """;
        parseAndExpectError(code);
    }

    @Test(expected = Parser.ParseError.class)
    public void testMissingParenthesis() {
        String code = """
            func void test(int a {
                return a;
            }
            """;
        parseAndExpectError(code);
    }

    @Test(expected = Parser.ParseError.class)
    public void testUnexpectedToken() {
        String code = """
            func void test() {
                int a = = 5;  // double equals
            }
            """;
        parseAndExpectError(code);
    }

    private void parseAndExpectError(String code) {
        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        parser.parse();
    }
}