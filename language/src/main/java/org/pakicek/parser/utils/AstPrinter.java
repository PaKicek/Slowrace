package org.pakicek.parser.utils;

import org.pakicek.parser.ast.Visitor;
import org.pakicek.parser.ast.expressions.*;
import org.pakicek.parser.ast.statements.*;
import java.util.List;

public class AstPrinter implements Visitor<String> {
    
    // ========== EXPRESSIONS ==========
    
    @Override
    public String visitBinaryExpression(BinaryExpression expr) {
        return parenthesize(expr.operator.lexeme(), expr.left, expr.right);
    }
    
    @Override
    public String visitLiteralExpression(LiteralExpression expr) {
        if (expr.value == null) return "null";
        if (expr.value instanceof String) return "\"" + expr.value + "\"";
        return expr.value.toString();
    }
    
    @Override
    public String visitIdentifierExpression(IdentifierExpression expr) {
        return expr.name;
    }
    
    @Override
    public String visitAssignmentExpression(AssignmentExpression expr) {
        return parenthesize("=", new IdentifierExpression(expr.identifier), expr.value);
    }
    
    @Override
    public String visitUnaryExpression(UnaryExpression expr) {
        return parenthesize(expr.operator.lexeme(), expr.right);
    }
    
    @Override
    public String visitGroupingExpression(GroupingExpression expr) {
        return parenthesize("group", expr.expression);
    }
    
    @Override
    public String visitArrayAccessExpression(ArrayAccessExpression expr) {
        return String.format("(array_access %s[%s])", 
            expr.array.accept(this), 
            expr.index.accept(this));
    }
    
    @Override
    public String visitFunctionCallExpression(FunctionCallExpression expr) {
        StringBuilder builder = new StringBuilder();
        builder.append("(call ").append(expr.callee.accept(this)).append(" ");
        
        if (!expr.arguments.isEmpty()) {
            builder.append("(args ");
            for (int i = 0; i < expr.arguments.size(); i++) {
                if (i > 0) builder.append(" ");
                builder.append(expr.arguments.get(i).accept(this));
            }
            builder.append(")");
        }
        
        builder.append(")");
        return builder.toString();
    }
    
    // ========== STATEMENTS ==========
    
    @Override
    public String visitExpressionStatement(ExpressionStatement stmt) {
        return parenthesize("expr", stmt.expression);
    }
    
    @Override
    public String visitVariableDeclaration(VariableDeclaration stmt) {
        if (stmt.initializer != null) {
            return String.format("(var %s %s %s)", 
                stmt.type, stmt.name, stmt.initializer.accept(this));
        } else {
            return String.format("(var %s %s)", stmt.type, stmt.name);
        }
    }
    
    @Override
    public String visitBlockStatement(BlockStatement stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(block");
        
        for (Statement statement : stmt.statements) {
            builder.append(" ").append(statement.accept(this));
        }
        
        builder.append(")");
        return builder.toString();
    }
    
    @Override
    public String visitIfStatement(IfStatement stmt) {
        if (stmt.elseBranch != null) {
            return String.format("(if %s %s %s)", 
                stmt.condition.accept(this),
                stmt.thenBranch.accept(this),
                stmt.elseBranch.accept(this));
        } else {
            return String.format("(if %s %s)", 
                stmt.condition.accept(this),
                stmt.thenBranch.accept(this));
        }
    }
    
    @Override
    public String visitWhileStatement(WhileStatement stmt) {
        return String.format("(while %s %s)", 
            stmt.condition.accept(this),
            stmt.body.accept(this));
    }
    
    @Override
    public String visitForStatement(ForStatement stmt) {
        return String.format("(for %s %s %s %s)", 
            stmt.initializer != null ? stmt.initializer.accept(this) : "null",
            stmt.condition != null ? stmt.condition.accept(this) : "null",
            stmt.increment != null ? stmt.increment.accept(this) : "null",
            stmt.body.accept(this));
    }
    
    @Override
    public String visitFunctionDeclaration(FunctionDeclaration stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(func ").append(stmt.name);
        builder.append(" (").append(stmt.returnType).append(")");
        
        if (!stmt.parameters.isEmpty()) {
            builder.append(" params[");
            for (int i = 0; i < stmt.parameters.size(); i++) {
                if (i > 0) builder.append(", ");
                builder.append(stmt.parameterTypes.get(i)).append(" ").append(stmt.parameters.get(i));
            }
            builder.append("]");
        }
        
        builder.append(" ").append(stmt.body.accept(this)).append(")");
        return builder.toString();
    }
    
    @Override
    public String visitReturnStatement(ReturnStatement stmt) {
        if (stmt.value != null) {
            return String.format("(return %s)", stmt.value.accept(this));
        } else {
            return "(return)";
        }
    }
    
    @Override
    public String visitArrayDeclaration(ArrayDeclaration stmt) {
        if (stmt.elements != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("(array ").append(stmt.type).append(" ").append(stmt.name).append(" [");
            for (int i = 0; i < stmt.elements.size(); i++) {
                if (i > 0) builder.append(", ");
                builder.append(stmt.elements.get(i).accept(this));
            }
            builder.append("])");
            return builder.toString();
        } else {
            return String.format("(array %s %s[%s])", 
                stmt.type, stmt.name, stmt.size.accept(this));
        }
    }
    
    // ========== UTILITY METHODS ==========
    
    private String parenthesize(String name, Expression... exprs) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        
        for (Expression expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        
        builder.append(")");
        return builder.toString();
    }
    
    public String print(Statement statement) {
        return statement.accept(this);
    }
    
    public String print(Expression expression) {
        return expression.accept(this);
    }
}