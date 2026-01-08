package org.pakicek.parser.ast;

import org.pakicek.parser.ast.node.*;
import org.pakicek.parser.ast.node.expression.*;
import org.pakicek.parser.ast.node.expression.literal.*;
import org.pakicek.parser.ast.node.statement.*;
import org.pakicek.parser.ast.node.type.*;

public interface ASTVisitor<T> {
    // Root & Declarations
    T visit(ProgramNode node);
    T visit(MainNode node);
    T visit(FunctionDeclarationNode node);
    T visit(ParameterNode node);
    T visit(StructDeclarationNode node); // New

    // Statements
    T visit(BlockStatementNode node);
    T visit(ExpressionStatementNode node);
    T visit(ForLoopNode node);
    T visit(IfStatementNode node);
    T visit(ReturnStatementNode node);
    T visit(VariableDeclarationNode node);
    T visit(WhileLoopNode node);

    // Expressions
    T visit(ArrayAccessNode node);
    T visit(AssignmentNode node);
    T visit(BinaryExpressionNode node);
    T visit(FunctionCallNode node);
    T visit(UnaryExpressionNode node);
    T visit(VariableNode node);
    T visit(FieldAccessNode node); // New

    // Literals
    T visit(ArrayLiteralNode node);
    T visit(BooleanLiteralNode node);
    T visit(FloatLiteralNode node);
    T visit(IntegerLiteralNode node);
    T visit(StringLiteralNode node);

    // Types
    T visit(ArrayTypeNode node);
    T visit(BasicTypeNode node);
    T visit(StructTypeNode node); // New
}