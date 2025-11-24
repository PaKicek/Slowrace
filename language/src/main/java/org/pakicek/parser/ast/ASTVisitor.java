package org.pakicek.parser.ast;

import org.pakicek.parser.ast.node.FunctionDeclarationNode;
import org.pakicek.parser.ast.node.MainNode;
import org.pakicek.parser.ast.node.ParameterNode;
import org.pakicek.parser.ast.node.ProgramNode;
import org.pakicek.parser.ast.node.expression.*;
import org.pakicek.parser.ast.node.expression.literal.*;
import org.pakicek.parser.ast.node.statement.*;
import org.pakicek.parser.ast.node.type.ArrayTypeNode;
import org.pakicek.parser.ast.node.type.BasicTypeNode;

public interface ASTVisitor<T> {
    // Program structure
    T visit(ProgramNode node);
    T visit(FunctionDeclarationNode node);
    T visit(MainNode node);
    T visit(ParameterNode node);

    // Statements
    T visit(BlockStatementNode node);
    T visit(VariableDeclarationNode node);
    T visit(AssignmentNode node);
    T visit(IfStatementNode node);
    T visit(ForLoopNode node);
    T visit(WhileLoopNode node);
    T visit(ReturnStatementNode node);
    T visit(ExpressionStatementNode node);

    // Expressions
    T visit(BinaryExpressionNode node);
    T visit(UnaryExpressionNode node);
    T visit(FunctionCallNode node);
    T visit(ArrayAccessNode node);
    T visit(ArrayLiteralNode node);
    T visit(VariableNode node);

    // Literals
    T visit(IntegerLiteralNode node);
    T visit(FloatLiteralNode node);
    T visit(StringLiteralNode node);
    T visit(BooleanLiteralNode node);

    // Types
    T visit(ArrayTypeNode node);
    T visit(BasicTypeNode node);
}