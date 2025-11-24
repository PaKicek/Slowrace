package org.pakicek.parser;

import org.pakicek.parser.ast.node.FunctionDeclarationNode;
import org.pakicek.parser.ast.node.MainNode;
import org.pakicek.parser.ast.node.ParameterNode;
import org.pakicek.parser.ast.node.ProgramNode;
import org.pakicek.parser.ast.node.expression.*;
import org.pakicek.parser.ast.node.expression.literal.*;
import org.pakicek.parser.ast.node.statement.*;
import org.pakicek.parser.ast.node.type.ArrayTypeNode;
import org.pakicek.parser.ast.node.type.BasicTypeNode;
import org.pakicek.parser.ast.node.type.TypeNode;
import org.pakicek.parser.lexer.Token;
import org.pakicek.parser.lexer.TokenType;

import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ProgramNode parse() {
        ProgramNode program = new ProgramNode(1, 1);

        while (!isAtEnd()) {
            if (match(TokenType.FUNC)) {
                FunctionDeclarationNode function = parseFunctionDeclaration();
                program.addFunction(function);
            } else if (match(TokenType.MAIN)) {
                MainNode mainNode = parseMain();
                program.setMainNode(mainNode);
            } else {
                throw error(peek(), "Expected function or main declaration");
            }
        }

        return program;
    }

    private MainNode parseMain() {
        Token mainToken = previous();

        // Parse main signature: main (int argc, array string argv[])
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'main'");

        // Parse argc parameter
        consume(TokenType.INT, "Expected 'int' for argc parameter");
        consume(TokenType.IDENTIFIER, "Expected 'argc' parameter name");

        consume(TokenType.COMMA, "Expected ',' after argc parameter");

        // Parse argv parameter
        consume(TokenType.ARRAY, "Expected 'array' for argv parameter");
        consume(TokenType.STRING, "Expected 'string' for argv element type");
        consume(TokenType.IDENTIFIER, "Expected 'argv' parameter name");
        consume(TokenType.LEFT_BRACKET, "Expected '[' after argv");
        consume(TokenType.RIGHT_BRACKET, "Expected ']' after argv");

        consume(TokenType.RIGHT_PAREN, "Expected ')' after main parameters");

        MainNode mainNode = new MainNode(mainToken.getLine(), mainToken.getPosition());

        // Parse main body
        BlockStatementNode body = parseBlockStatement();
        mainNode.setBody(body);

        return mainNode;
    }

    private FunctionDeclarationNode parseFunctionDeclaration() {
        Token funcToken = previous();
        TypeNode returnType = parseType();
        Token name = consume(TokenType.IDENTIFIER, "Expected function name");

        consume(TokenType.LEFT_PAREN, "Expected '(' after function name");

        FunctionDeclarationNode function = new FunctionDeclarationNode(
                name.getLexeme(), returnType, funcToken.getLine(), funcToken.getPosition()
        );

        // Parse parameters
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                ParameterNode parameter = parseParameter();
                function.addParameter(parameter);
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RIGHT_PAREN, "Expected ')' after parameters");

        // Parse function body
        BlockStatementNode body = parseBlockStatement();
        function.setBody(body);

        return function;
    }

    private ParameterNode parseParameter() {
        TypeNode type = parseType();
        Token name = consume(TokenType.IDENTIFIER, "Expected parameter name");

        return new ParameterNode(name.getLexeme(), type, name.getLine(), name.getPosition());
    }

    private TypeNode parseType() {
        Token token = advance();

        if (token.getType() == TokenType.ARRAY) {
            TypeNode elementType = parseType();
            consume(TokenType.LEFT_BRACKET, "Expected '[' after array type");

            Integer size = null;
            ExpressionNode sizeExpression = null;

            if (match(TokenType.INTEGER_LITERAL)) {
                size = ((Long) previous().getLiteral()).intValue();
            } else if (check(TokenType.RIGHT_BRACKET)) {
            } else {
                throw error(peek(), "Expected integer literal or ']' for array size in parameter");
            }

            consume(TokenType.RIGHT_BRACKET, "Expected ']' after array size");
            return new ArrayTypeNode(elementType, size, token.getLine(), token.getPosition());
        } else if (isBasicType(token.getType())) {
            return new BasicTypeNode(token.getLexeme(), token.getLine(), token.getPosition());
        } else {
            throw error(token, "Expected type");
        }
    }

    private StatementNode parseStatement() {
        if (match(TokenType.IF)) return parseIfStatement();
        if (match(TokenType.FOR)) return parseForLoop();
        if (match(TokenType.WHILE)) return parseWhileLoop();
        if (match(TokenType.RETURN)) return parseReturnStatement();
        if (match(TokenType.LEFT_BRACE)) return parseBlockStatement();

        // Variable declaration or assignment
        if (isType(peek().getType())) {
            return parseVariableDeclaration();
        }

        // Expression statement
        return parseExpressionStatement();
    }

    private IfStatementNode parseIfStatement() {
        Token ifToken = previous();

        consume(TokenType.LEFT_PAREN, "Expected '(' after 'if'");
        ExpressionNode condition = parseExpression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after if condition");

        // Parse then block
        BlockStatementNode thenBlock = parseBlockStatement();
        IfStatementNode ifStatement = new IfStatementNode(condition, thenBlock,
                ifToken.getLine(), ifToken.getPosition());

        // Parse elif branches
        while (match(TokenType.ELIF)) {
            consume(TokenType.LEFT_PAREN, "Expected '(' after 'elif'");
            ExpressionNode elifCondition = parseExpression();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after elif condition");
            BlockStatementNode elifBlock = parseBlockStatement();
            ifStatement.addElifBranch(new IfStatementNode.ElifBranch(elifCondition, elifBlock));
        }

        // Parse else branch
        if (match(TokenType.ELSE)) {
            BlockStatementNode elseBlock = parseBlockStatement();
            ifStatement.setElseBlock(elseBlock);
        }

        return ifStatement;
    }

    private ForLoopNode parseForLoop() {
        Token forToken = previous();
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'for'");

        // Parse initialization
        StatementNode initialization;
        if (match(TokenType.SEMICOLON)) {
            initialization = null;
        } else if (isType(peek().getType())) {
            initialization = parseVariableDeclaration();
        } else {
            initialization = parseExpressionStatement();
        }

        // Parse condition
        ExpressionNode condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = parseExpression();
        }
        consume(TokenType.SEMICOLON, "Expected ';' after for condition");

        // Parse update
        ExpressionNode update = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            update = parseExpression();
        }
        consume(TokenType.RIGHT_PAREN, "Expected ')' after for clauses");

        // Parse body
        BlockStatementNode body = parseBlockStatement();

        return new ForLoopNode(initialization, condition, update, body,
                forToken.getLine(), forToken.getPosition());
    }

    private WhileLoopNode parseWhileLoop() {
        Token whileToken = previous();
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'while'");
        ExpressionNode condition = parseExpression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after while condition");

        // Parse body
        BlockStatementNode body = parseBlockStatement();

        return new WhileLoopNode(condition, body, whileToken.getLine(), whileToken.getPosition());
    }

    private ReturnStatementNode parseReturnStatement() {
        Token returnToken = previous();
        ReturnStatementNode returnStatement = new ReturnStatementNode(
                returnToken.getLine(), returnToken.getPosition()
        );

        if (!check(TokenType.SEMICOLON)) {
            ExpressionNode value = parseExpression();
            returnStatement.setValue(value);
        }

        consume(TokenType.SEMICOLON, "Expected ';' after return statement");
        return returnStatement;
    }

    private BlockStatementNode parseBlockStatement() {
        consume(TokenType.LEFT_BRACE, "Expected '{' before block");

        BlockStatementNode block = new BlockStatementNode(
                previous().getLine(), previous().getPosition()
        );

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            StatementNode statement = parseStatement();
            block.addStatement(statement);
        }

        if (isAtEnd()) {
            throw error(previous(), "Expected '}' before end of file");
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' after block");
        return block;
    }

    private VariableDeclarationNode parseVariableDeclaration() {
        TypeNode type = parseType();
        Token name = consume(TokenType.IDENTIFIER, "Expected variable name");

        if (type instanceof ArrayTypeNode arrayType && match(TokenType.LEFT_BRACKET)) {
            ExpressionNode sizeExpression = null;

            if (!check(TokenType.RIGHT_BRACKET)) {
                sizeExpression = parseExpression();
            }
            consume(TokenType.RIGHT_BRACKET, "Expected ']' after array size expression");

            type = new ArrayTypeNode(arrayType.getElementType(), sizeExpression,
                    type.getLine(), type.getPosition());
        }

        VariableDeclarationNode declaration = new VariableDeclarationNode(
                name.getLexeme(), type, name.getLine(), name.getPosition()
        );

        if (match(TokenType.ASSIGN)) {
            ExpressionNode initialValue = parseExpression();
            declaration.setInitialValue(initialValue);
        }

        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration");
        return declaration;
    }

    private ExpressionStatementNode parseExpressionStatement() {
        ExpressionNode expression = parseExpression();
        ExpressionStatementNode statement = new ExpressionStatementNode(
                expression, expression.getLine(), expression.getPosition()
        );

        consume(TokenType.SEMICOLON, "Expected ';' after expression");
        return statement;
    }

    private ExpressionNode parseExpression() {
        return parseAssignment();
    }

    private ExpressionNode parseAssignment() {
        ExpressionNode expression = parseLogicalOr();

        if (match(TokenType.ASSIGN)) {
            Token equals = previous();
            ExpressionNode value = parseAssignment();

            if (expression instanceof VariableNode || expression instanceof ArrayAccessNode) {
                return new AssignmentNode(expression, value, equals.getLine(), equals.getPosition());
            }

            throw error(equals, "Invalid assignment target");
        }

        return expression;
    }

    private ExpressionNode parseLogicalOr() {
        ExpressionNode expression = parseLogicalAnd();

        while (match(TokenType.OR)) {
            Token operator = previous();
            ExpressionNode right = parseLogicalAnd();
            expression = new BinaryExpressionNode(expression, operator.getLexeme(), right,
                    operator.getLine(), operator.getPosition());
        }

        return expression;
    }

    private ExpressionNode parseLogicalAnd() {
        ExpressionNode expression = parseEquality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            ExpressionNode right = parseEquality();
            expression = new BinaryExpressionNode(expression, operator.getLexeme(), right,
                    operator.getLine(), operator.getPosition());
        }

        return expression;
    }

    private ExpressionNode parseEquality() {
        ExpressionNode expression = parseComparison();

        while (match(TokenType.EQUALS, TokenType.NOT_EQUALS)) {
            Token operator = previous();
            ExpressionNode right = parseComparison();
            expression = new BinaryExpressionNode(expression, operator.getLexeme(), right,
                    operator.getLine(), operator.getPosition());
        }

        return expression;
    }

    private ExpressionNode parseComparison() {
        ExpressionNode expression = parseTerm();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL,
                TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            ExpressionNode right = parseTerm();
            expression = new BinaryExpressionNode(expression, operator.getLexeme(), right,
                    operator.getLine(), operator.getPosition());
        }

        return expression;
    }

    private ExpressionNode parseTerm() {
        ExpressionNode expression = parseFactor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            ExpressionNode right = parseFactor();
            expression = new BinaryExpressionNode(expression, operator.getLexeme(), right,
                    operator.getLine(), operator.getPosition());
        }

        return expression;
    }

    private ExpressionNode parseFactor() {
        ExpressionNode expression = parseUnary();

        while (match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO,
                TokenType.BITWISE_AND, TokenType.BITWISE_OR)) {
            Token operator = previous();
            ExpressionNode right = parseUnary();
            expression = new BinaryExpressionNode(expression, operator.getLexeme(), right,
                    operator.getLine(), operator.getPosition());
        }

        return expression;
    }

    private ExpressionNode parseUnary() {
        if (match(TokenType.NOT, TokenType.MINUS, TokenType.INCREMENT, TokenType.DECREMENT)) {
            Token operator = previous();
            ExpressionNode right = parseUnary();
            return new UnaryExpressionNode(operator.getLexeme(), right,
                    operator.getLine(), operator.getPosition());
        }

        return parsePrimary();
    }

    private ExpressionNode parsePrimary() {
        if (match(TokenType.BOOLEAN_LITERAL)) {
            Token token = previous();
            boolean value = Boolean.parseBoolean(token.getLexeme());
            return new BooleanLiteralNode(value, token.getLine(), token.getPosition());
        }
        if (match(TokenType.INTEGER_LITERAL)) {
            Token token = previous();
            return new IntegerLiteralNode((Long) token.getLiteral(), token.getLine(), token.getPosition());
        }
        if (match(TokenType.FLOAT_LITERAL)) {
            Token token = previous();
            return new FloatLiteralNode((Double) token.getLiteral(), token.getLine(), token.getPosition());
        }
        if (match(TokenType.STRING_LITERAL)) {
            Token token = previous();
            return new StringLiteralNode((String) token.getLiteral(), token.getLine(), token.getPosition());
        }
        if (match(TokenType.LEFT_BRACKET)) {
            return parseArrayLiteral();
        }
        if (match(TokenType.LEFT_PAREN)) {
            ExpressionNode expression = parseExpression();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression");
            return expression;
        }
        if (match(TokenType.IDENTIFIER)) {
            Token identifier = previous();

            // Check if this is a function call or array access
            if (match(TokenType.LEFT_PAREN)) {
                return parseFunctionCall(identifier);
            } else if (match(TokenType.LEFT_BRACKET)) {
                return parseArrayAccess(identifier);
            }

            return new VariableNode(identifier.getLexeme(), identifier.getLine(), identifier.getPosition());
        }

        throw error(peek(), "Expected expression");
    }

    private ExpressionNode parseFunctionCall(Token identifier) {
        FunctionCallNode call = new FunctionCallNode(identifier.getLexeme(),
                identifier.getLine(), identifier.getPosition());

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                ExpressionNode argument = parseExpression();
                call.addArgument(argument);
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RIGHT_PAREN, "Expected ')' after function arguments");
        return call;
    }

    private ExpressionNode parseArrayAccess(Token identifier) {
        ExpressionNode index = parseExpression();
        consume(TokenType.RIGHT_BRACKET, "Expected ']' after array index");

        VariableNode array = new VariableNode(identifier.getLexeme(),
                identifier.getLine(), identifier.getPosition());
        return new ArrayAccessNode(array, index, identifier.getLine(), identifier.getPosition());
    }

    private ExpressionNode parseArrayLiteral() {
        Token bracketToken = previous();
        ArrayLiteralNode array = new ArrayLiteralNode(bracketToken.getLine(), bracketToken.getPosition());

        if (!check(TokenType.RIGHT_BRACKET)) {
            do {
                ExpressionNode element = parseExpression();
                array.addElement(element);
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RIGHT_BRACKET, "Expected ']' after array literal");
        return array;
    }

    // Utility methods

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
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isType(TokenType type) {
        return type == TokenType.INT || type == TokenType.FLOAT ||
                type == TokenType.STRING || type == TokenType.BOOL ||
                type == TokenType.VOID || type == TokenType.ARRAY;
    }

    private boolean isBasicType(TokenType type) {
        return type == TokenType.INT || type == TokenType.FLOAT ||
                type == TokenType.STRING || type == TokenType.BOOL ||
                type == TokenType.VOID;
    }

    private ParseError error(Token token, String message) {
        System.err.println("Parse error [line " + token.getLine() + ", position " +
                token.getPosition() + "]: " + message);
        return new ParseError(message, token.getLine(), token.getPosition());
    }

    // Custom exception for parse errors
    public static class ParseError extends RuntimeException {
        private final int line;
        private final int position;

        public ParseError(String message, int line, int position) {
            super(message);
            this.line = line;
            this.position = position;
        }

        public int getLine() {
            return line;
        }

        public int getPosition() {
            return position;
        }
    }
}