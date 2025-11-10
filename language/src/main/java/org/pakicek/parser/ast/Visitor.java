package org.pakicek.parser.ast;

import org.pakicek.parser.ast.expressions.*;
import org.pakicek.parser.ast.statements.*;

public interface Visitor<T> {
    // Expressions
    T visitBinaryExpression(BinaryExpression expr);
    T visitLiteralExpression(LiteralExpression expr);
    T visitIdentifierExpression(IdentifierExpression expr);
    T visitAssignmentExpression(AssignmentExpression expr);
    T visitUnaryExpression(UnaryExpression expr);
    T visitGroupingExpression(GroupingExpression expr);
    T visitArrayAccessExpression(ArrayAccessExpression expr);
    T visitFunctionCallExpression(FunctionCallExpression expr);
    
    // Statements
    T visitExpressionStatement(ExpressionStatement stmt);
    T visitVariableDeclaration(VariableDeclaration stmt);
    T visitBlockStatement(BlockStatement stmt);
    T visitIfStatement(IfStatement stmt);
    T visitWhileStatement(WhileStatement stmt);
    T visitForStatement(ForStatement stmt);
    T visitFunctionDeclaration(FunctionDeclaration stmt);
    T visitReturnStatement(ReturnStatement stmt);
    T visitArrayDeclaration(ArrayDeclaration stmt);
}