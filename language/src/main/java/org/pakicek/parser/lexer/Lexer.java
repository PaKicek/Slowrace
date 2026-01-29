package org.pakicek.parser.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int position = 1;

    private int tokenStartLine = 1;
    private int tokenStartPosition = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("int", TokenType.INT);
        keywords.put("float", TokenType.FLOAT);
        keywords.put("string", TokenType.STRING);
        keywords.put("bool", TokenType.BOOL);
        keywords.put("void", TokenType.VOID);
        keywords.put("array", TokenType.ARRAY);
        keywords.put("func", TokenType.FUNC);
        keywords.put("struct", TokenType.STRUCT);
        keywords.put("main", TokenType.MAIN);
        keywords.put("return", TokenType.RETURN);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("elif", TokenType.ELIF);
        keywords.put("for", TokenType.FOR);
        keywords.put("while", TokenType.WHILE);
        keywords.put("true", TokenType.BOOLEAN_LITERAL);
        keywords.put("false", TokenType.BOOLEAN_LITERAL);
    }

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            tokenStartLine = line;
            tokenStartPosition = position;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line, position));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case '[': addToken(TokenType.LEFT_BRACKET); break;
            case ']': addToken(TokenType.RIGHT_BRACKET); break;
            case ',': addToken(TokenType.COMMA); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '.': addToken(TokenType.DOT); break;
            case '+':
                if (match('+')) {
                    addToken(TokenType.INCREMENT);
                } else {
                    addToken(TokenType.PLUS);
                }
                break;
            case '-':
                if (match('-')) {
                    addToken(TokenType.DECREMENT);
                } else {
                    addToken(TokenType.MINUS);
                }
                break;
            case '*': addToken(TokenType.MULTIPLY); break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(TokenType.DIVIDE);
                }
                break;
            case '%': addToken(TokenType.MODULO); break;
            case '&':
                if (match('&')) {
                    addToken(TokenType.AND);
                } else {
                    addToken(TokenType.BITWISE_AND);
                }
                break;
            case '|':
                if (match('|')) {
                    addToken(TokenType.OR);
                } else {
                    addToken(TokenType.BITWISE_OR);
                }
                break;
            case '~': addToken(TokenType.NOT); break;
            case '=':
                if (match('=')) {
                    addToken(TokenType.EQUALS);
                } else {
                    addToken(TokenType.ASSIGN);
                }
                break;
            case '!':
                if (match('=')) {
                    addToken(TokenType.NOT_EQUALS);
                } else {
                    addToken(TokenType.NOT);
                }
                break;
            case '>':
                if (match('=')) {
                    addToken(TokenType.GREATER_EQUAL);
                } else {
                    addToken(TokenType.GREATER);
                }
                break;
            case '<':
                if (match('=')) {
                    addToken(TokenType.LESS_EQUAL);
                } else {
                    addToken(TokenType.LESS);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                position = 1;
                break;
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    error("Invalid symbol: " + c);
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);

        if (type == null) {
            type = TokenType.IDENTIFIER;
        }

        Object literal = null;
        if (type == TokenType.BOOLEAN_LITERAL) {
            literal = Boolean.valueOf(text);
        }

        addToken(type, literal);
    }

    private void number() {
        boolean isFloat = false;

        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            isFloat = true;
            do advance();
            while (isDigit(peek()));
        }

        String numberStr = source.substring(start, current);

        if (isFloat) {
            addToken(TokenType.FLOAT_LITERAL, Double.parseDouble(numberStr));
        } else {
            addToken(TokenType.INTEGER_LITERAL, Long.parseLong(numberStr));
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                position = 1;
            }
            advance();
        }

        if (isAtEnd()) {
            error("Unfinished string literal");
            return;
        }

        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING_LITERAL, value);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        position++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        current++;
        position++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, tokenStartLine, tokenStartPosition));
    }

    private void error(String message) {
        System.err.println("Syntax error [line " + line + ", position " + position + "]: " + message);
        addToken(TokenType.ERROR);
    }
}