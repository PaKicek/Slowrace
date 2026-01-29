package org.pakicek.parser.lexer;

public class Token {
    private final TokenType type;
    private final String lexeme;
    private final Object literal;
    private final int line;
    private final int position;

    public Token(TokenType type, String lexeme, Object literal, int line, int position) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.position = position;
    }

    public TokenType getType() { return type; }
    public String getLexeme() { return lexeme; }
    public Object getLiteral() { return literal; }
    public int getLine() { return line; }
    public int getPosition() { return position; }

    @Override
    public String toString() {
        return String.format("Token{type=%s, lexeme='%s', literal=%s, line=%d, position=%d}", type, lexeme, literal, line, position);
    }
}
