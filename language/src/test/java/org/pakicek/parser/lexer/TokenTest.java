package org.pakicek.parser.lexer;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokenTest {

    @Test
    public void testTokenCreation() {
        Token token = new Token(TokenType.INT, "int", null, 1, 1);
        assertEquals(TokenType.INT, token.getType());
        assertEquals("int", token.getLexeme());
        assertNull(token.getLiteral());
        assertEquals(1, token.getLine());
        assertEquals(1, token.getPosition());
    }

    @Test
    public void testTokenWithLiteral() {
        Token token = new Token(TokenType.INTEGER_LITERAL, "42", 42L, 2, 5);
        assertEquals(TokenType.INTEGER_LITERAL, token.getType());
        assertEquals("42", token.getLexeme());
        assertEquals(42L, token.getLiteral());
        assertEquals(2, token.getLine());
        assertEquals(5, token.getPosition());
    }

    @Test
    public void testTokenToString() {
        Token token = new Token(TokenType.STRING_LITERAL, "\"test\"", "test", 3, 10);
        String toString = token.toString();
        assertTrue(toString.contains("STRING_LITERAL"));
        assertTrue(toString.contains("\"test\""));
        assertTrue(toString.contains("test"));
        assertTrue(toString.contains("line=3"));
        assertTrue(toString.contains("position=10"));
    }

    @Test
    public void testStructToken() {
        Token token = new Token(TokenType.STRUCT, "struct", null, 1, 1);
        assertEquals(TokenType.STRUCT, token.getType());
    }
}