package org.pakicek.parser.utils;

import org.pakicek.parser.ast.Visitor;
import org.pakicek.parser.ast.expressions.*;
import org.pakicek.parser.ast.statements.*;
import java.util.HashSet;
import java.util.Set;

public class Validator implements Visitor<Void> {
    private final Set<String> declaredVariables = new HashSet<>();
    private final Set<String> declaredFunctions = new HashSet<>();
    private final Set<String> currentScopeVariables = new HashSet<>();
    
    @Override
    public Void visitBinaryExpression(BinaryExpression expr) {
        expr.left.accept(this);
        expr.right.accept(this);
        return null;
    }
    
    @Override
    public Void visitLiteralExpression(LiteralExpression expr) {
        return null;
    }
    
    @Override
    public Void visitIdentifierExpression(IdentifierExpression expr) {
        if (!declaredVariables.contains(expr.name) && !currentScopeVariables.contains(expr.name)) {
            ErrorHandler.warning(-1, "Undeclared variable: " + expr.name);
        }
        return null;
    }
    
    @Override
    public Void visitAssignmentExpression(AssignmentExpression expr) {
        if (!declaredVariables.contains(expr.identifier) && !currentScopeVariables.contains(expr.identifier)) {
            ErrorHandler.warning(-1, "Assignment to undeclared variable: " + expr.identifier);
        }
        expr.value.accept(this);
        return null;
    }
    
    @Override
    public Void visitUnaryExpression(UnaryExpression expr) {
        expr.right.accept(this);
        return null;
    }
    
    @Override
    public Void visitGroupingExpression(GroupingExpression expr) {
        expr.expression.accept(this);
        return null;
    }
    
    @Override
    public Void visitArrayAccessExpression(ArrayAccessExpression expr) {
        expr.array.accept(this);
        expr.index.accept(this);
        return null;
    }
    
    @Override
    public Void visitFunctionCallExpression(FunctionCallExpression expr) {
        expr.callee.accept(this);
        for (Expression arg : expr.arguments) {
            arg.accept(this);
        }
        return null;
    }
    
    @Override
    public Void visitExpressionStatement(ExpressionStatement stmt) {
        stmt.expression.accept(this);
        return null;
    }
    
    @Override
    public Void visitVariableDeclaration(VariableDeclaration stmt) {
        if (declaredVariables.contains(stmt.name)) {
            ErrorHandler.warning(-1, "Redeclaration of variable: " + stmt.name);
        }
        declaredVariables.add(stmt.name);
        currentScopeVariables.add(stmt.name);
        
        if (stmt.initializer != null) {
            stmt.initializer.accept(this);
        }
        return null;
    }
    
    @Override
    public Void visitBlockStatement(BlockStatement stmt) {
        Set<String> previousScope = new HashSet<>(currentScopeVariables);
        
        for (Statement statement : stmt.statements) {
            statement.accept(this);
        }
        
        currentScopeVariables.clear();
        currentScopeVariables.addAll(previousScope);
        return null;
    }
    
    @Override
    public Void visitIfStatement(IfStatement stmt) {
        stmt.condition.accept(this);
        stmt.thenBranch.accept(this);
        if (stmt.elseBranch != null) {
            stmt.elseBranch.accept(this);
        }
        return null;
    }
    
    @Override
    public Void visitWhileStatement(WhileStatement stmt) {
        stmt.condition.accept(this);
        stmt.body.accept(this);
        return null;
    }
    
    @Override
    public Void visitForStatement(ForStatement stmt) {
        if (stmt.initializer != null) stmt.initializer.accept(this);
        if (stmt.condition != null) stmt.condition.accept(this);
        if (stmt.increment != null) stmt.increment.accept(this);
        stmt.body.accept(this);
        return null;
    }
    
    @Override
    public Void visitFunctionDeclaration(FunctionDeclaration stmt) {
        declaredFunctions.add(stmt.name);
        Set<String> previousScope = new HashSet<>(currentScopeVariables);
        currentScopeVariables.addAll(stmt.parameters);
        
        stmt.body.accept(this);
        
        currentScopeVariables.clear();
        currentScopeVariables.addAll(previousScope);
        return null;
    }
    
    @Override
    public Void visitReturnStatement(ReturnStatement stmt) {
        if (stmt.value != null) {
            stmt.value.accept(this);
        }
        return null;
    }
    
    @Override
    public Void visitArrayDeclaration(ArrayDeclaration stmt) {
        declaredVariables.add(stmt.name);
        currentScopeVariables.add(stmt.name);
        
        if (stmt.elements != null) {
            for (Expression element : stmt.elements) {
                element.accept(this);
            }
        }
        if (stmt.size != null) {
            stmt.size.accept(this);
        }
        return null;
    }
    
    public void validate(Statement statement) {
        statement.accept(this);
    }
}