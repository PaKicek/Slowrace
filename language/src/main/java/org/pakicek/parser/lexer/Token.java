package org.pakicek.parser.lexer;

public record Token(TokenType type, String lexeme, Object literal, int line, int position) {

    @Override
    public String toString() {
        return String.format("Token{type=%s, lexeme='%s', literal=%s, line=%d, position=%d}",
                type, lexeme, literal, line, position);
    }
}
