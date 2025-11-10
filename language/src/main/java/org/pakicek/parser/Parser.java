package org.pakicek.parser;

import org.pakicek.parser.lexer.Token;
import org.pakicek.parser.lexer.TokenType;
import org.pakicek.parser.ast.Statement;
import org.pakicek.parser.ast.Expression;
import org.pakicek.parser.ast.expressions.*;
import org.pakicek.parser.ast.statements.*;
import org.pakicek.parser.utils.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    public List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }
    
    private Statement declaration() {
        try {
            if (match(TokenType.FUNC)) return functionDeclaration();
            if (match(TokenType.INT, TokenType.FLOAT, TokenType.STRING, TokenType.BOOL)) {
                if (match(TokenType.LEFT_BRACKET)) {
                    return arrayDeclaration();
                }
                return variableDeclaration();
            }
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }
    
    private Statement functionDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect function name");
        
        consume(TokenType.LEFT_PAREN, "Expect '(' after function name");
        List<String> parameters = new ArrayList<>();
        List<String> parameterTypes = new ArrayList<>();
        
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters");
                }
                
                Token type = consume(TokenType.INT, TokenType.FLOAT, TokenType.STRING, TokenType.BOOL, 
                                   "Expect parameter type");
                Token paramName = consume(TokenType.IDENTIFIER, "Expect parameter name");
                
                parameters.add(paramName.lexeme());
                parameterTypes.add(type.lexeme());
            } while (match(TokenType.COMMA));
        }
        
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters");
        
        // Return type
        String returnType = "void";
        if (match(TokenType.ARROW)) {
            Token returnTypeToken = consume(TokenType.INT, TokenType.FLOAT, TokenType.STRING, 
                                          TokenType.BOOL, TokenType.VOID, "Expect return type");
            returnType = returnTypeToken.lexeme();
        }
        
        consume(TokenType.LEFT_BRACE, "Expect '{' before function body");
        BlockStatement body = (BlockStatement) blockStatement();
        
        return new FunctionDeclaration(name.lexeme(), parameters, parameterTypes, returnType, body);
    }
    
    private Statement arrayDeclaration() {
        Token type = previous(); // тип массива (INT, FLOAT, etc.)
        consume(TokenType.RIGHT_BRACKET, "Expect ']' after array type");
        Token name = consume(TokenType.IDENTIFIER, "Expect array name");
        
        Expression size = null;
        List<Expression> elements = null;
        
        if (match(TokenType.ASSIGN)) {
            if (match(TokenType.LEFT_BRACKET)) {
                // Массив с элементами: int[] arr = [1, 2, 3]
                elements = new ArrayList<>();
                if (!check(TokenType.RIGHT_BRACKET)) {
                    do {
                        elements.add(expression());
                    } while (match(TokenType.COMMA));
                }
                consume(TokenType.RIGHT_BRACKET, "Expect ']' after array elements");
            } else {
                // Массив с размером: int[] arr = 10
                size = expression();
            }
        }
        
        consume(TokenType.SEMICOLON, "Expect ';' after array declaration");
        return new ArrayDeclaration(type.lexeme(), name.lexeme(), elements, size);
    }
    
    private Statement variableDeclaration() {
        Token type = previous();
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name");
        
        Expression initializer = null;
        if (match(TokenType.ASSIGN)) {
            initializer = expression();
        }
        
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration");
        return new VariableDeclaration(type.lexeme(), name.lexeme(), initializer);
    }
    
    private Statement statement() {
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.FOR)) return forStatement();
        if (match(TokenType.RETURN)) return returnStatement();
        if (match(TokenType.LEFT_BRACE)) return blockStatement();
        return expressionStatement();
    }
    
    private Statement ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'");
        Expression condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition");
        
        Statement thenBranch = statement();
        Statement elseBranch = null;
        
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }
        
        return new IfStatement(condition, thenBranch, elseBranch);
    }
    
    private Statement whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'");
        Expression condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition");
        
        Statement body = statement();
        return new WhileStatement(condition, body);
    }
    
    private Statement forStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'");
        
        Statement initializer;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.INT, TokenType.FLOAT, TokenType.STRING, TokenType.BOOL)) {
            initializer = variableDeclaration();
        } else {
            initializer = expressionStatement();
        }
        
        Expression condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition");
        
        Expression increment = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses");
        
        Statement body = statement();
        
        // Desugar for loop into while loop
        if (increment != null) {
            List<Statement> statements = new ArrayList<>();
            statements.add(body);
            statements.add(new ExpressionStatement(increment));
            body = new BlockStatement(statements);
        }
        
        if (condition == null) condition = new LiteralExpression(true);
        Statement whileLoop = new WhileStatement(condition, body);
        
        if (initializer != null) {
            List<Statement> statements = new ArrayList<>();
            statements.add(initializer);
            statements.add(whileLoop);
            return new BlockStatement(statements);
        }
        
        return whileLoop;
    }
    
    private Statement returnStatement() {
        Expression value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = expression();
        }
        
        consume(TokenType.SEMICOLON, "Expect ';' after return value");
        return new ReturnStatement(value);
    }
    
    private Statement blockStatement() {
        List<Statement> statements = new ArrayList<>();
        
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block");
        return new BlockStatement(statements);
    }
    
    private Statement expressionStatement() {
        Expression expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression");
        return new ExpressionStatement(expr);
    }
    
    private Expression expression() {
        return assignment();
    }
    
    private Expression assignment() {
        Expression expr = or();
        
        if (match(TokenType.ASSIGN)) {
            Token equals = previous();
            Expression value = assignment();
            
            if (expr instanceof IdentifierExpression) {
                String name = ((IdentifierExpression) expr).name;
                return new AssignmentExpression(name, value);
            } else if (expr instanceof ArrayAccessExpression) {
                ArrayAccessExpression arrayExpr = (ArrayAccessExpression) expr;
                return new AssignmentExpression(((IdentifierExpression) arrayExpr.array).name, value);
            }
            
            error(equals, "Invalid assignment target");
        }
        
        return expr;
    }
    
    private Expression or() {
        Expression expr = and();
        
        while (match(TokenType.OR)) {
            Token operator = previous();
            Expression right = and();
            expr = new BinaryExpression(expr, operator, right);
        }
        
        return expr;
    }
    
    private Expression and() {
        Expression expr = equality();
        
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expression right = equality();
            expr = new BinaryExpression(expr, operator, right);
        }
        
        return expr;
    }
    
    private Expression equality() {
        Expression expr = comparison();
        
        while (match(TokenType.EQUALS, TokenType.NOT_EQUALS)) {
            Token operator = previous();
            Expression right = comparison();
            expr = new BinaryExpression(expr, operator, right);
        }
        
        return expr;
    }
    
    private Expression comparison() {
        Expression expr = term();
        
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expr = new BinaryExpression(expr, operator, right);
        }
        
        return expr;
    }
    
    private Expression term() {
        Expression expr = factor();
        
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = factor();
            expr = new BinaryExpression(expr, operator, right);
        }
        
        return expr;
    }
    
    private Expression factor() {
        Expression expr = unary();
        
        while (match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO)) {
            Token operator = previous();
            Expression right = unary();
            expr = new BinaryExpression(expr, operator, right);
        }
        
        return expr;
    }
    
    private Expression unary() {
        if (match(TokenType.MINUS, TokenType.NOT)) {
            Token operator = previous();
            Expression right = unary();
            return new UnaryExpression(operator, right);
        }
        
        return call();
    }
    
    private Expression call() {
        Expression expr = primary();
        
        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(TokenType.LEFT_BRACKET)) {
                Expression index = expression();
                consume(TokenType.RIGHT_BRACKET, "Expect ']' after array index");
                expr = new ArrayAccessExpression(expr, index);
            } else {
                break;
            }
        }
        
        return expr;
    }
    
    private Expression finishCall(Expression callee) {
        List<Expression> arguments = new ArrayList<>();
        
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments");
                }
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }
        
        Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments");
        return new FunctionCallExpression(callee, arguments);
    }
    
    private Expression primary() {
        if (match(TokenType.FALSE)) return new LiteralExpression(false);
        if (match(TokenType.TRUE)) return new LiteralExpression(true);
        if (match(TokenType.INTEGER_LITERAL, TokenType.FLOAT_LITERAL, TokenType.STRING_LITERAL)) {
            return new LiteralExpression(previous().literal());
        }
        if (match(TokenType.IDENTIFIER)) {
            return new IdentifierExpression(previous().lexeme());
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expression expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression");
            return new GroupingExpression(expr);
        }
        
        throw error(peek(), "Expect expression");
    }
    
    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========
    
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }
    
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }
    
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    
    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }
    
    private Token peek() {
        return tokens.get(current);
    }
    
    private Token previous() {
        return tokens.get(current - 1);
    }
    
    private ParseError error(Token token, String message) {
        ErrorHandler.error(token, message);
        return new ParseError();
    }
    
    private void synchronize() {
        advance();
        
        while (!isAtEnd()) {
            if (previous().type() == TokenType.SEMICOLON) return;
            
            switch (peek().type()) {
                case FUNC, INT, FLOAT, STRING, BOOL, FOR, IF, RETURN, WHILE -> { return; }
            }
            
            advance();
        }
    }
    
    private static class ParseError extends RuntimeException {}
}