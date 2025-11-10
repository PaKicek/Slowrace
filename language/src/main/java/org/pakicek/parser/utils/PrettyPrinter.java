package org.pakicek.parser.utils;

import org.pakicek.parser.ast.Visitor;
import org.pakicek.parser.ast.expressions.*;
import org.pakicek.parser.ast.statements.*;

public class PrettyPrinter implements Visitor<String> {
    private int indentLevel = 0;
    
    private String indent() {
        return "  ".repeat(indentLevel);
    }
    
    @Override
    public String visitBinaryExpression(BinaryExpression expr) {
        return String.format("%s %s %s", 
            expr.left.accept(this), 
            expr.operator.lexeme(), 
            expr.right.accept(this));
    }
    
    @Override
    public String visitLiteralExpression(LiteralExpression expr) {
        return expr.value == null ? "null" : expr.value.toString();
    }
    
    @Override
    public String visitIdentifierExpression(IdentifierExpression expr) {
        return expr.name;
    }
    
    @Override
    public String visitAssignmentExpression(AssignmentExpression expr) {
        return String.format("%s = %s", expr.identifier, expr.value.accept(this));
    }
    
    @Override
    public String visitUnaryExpression(UnaryExpression expr) {
        return String.format("%s%s", expr.operator.lexeme(), expr.right.accept(this));
    }
    
    @Override
    public String visitGroupingExpression(GroupingExpression expr) {
        return String.format("(%s)", expr.expression.accept(this));
    }
    
    @Override
    public String visitArrayAccessExpression(ArrayAccessExpression expr) {
        return String.format("%s[%s]", 
            expr.array.accept(this), 
            expr.index.accept(this));
    }
    
    @Override
    public String visitFunctionCallExpression(FunctionCallExpression expr) {
        StringBuilder builder = new StringBuilder();
        builder.append(expr.callee.accept(this)).append("(");
        
        for (int i = 0; i < expr.arguments.size(); i++) {
            if (i > 0) builder.append(", ");
            builder.append(expr.arguments.get(i).accept(this));
        }
        
        builder.append(")");
        return builder.toString();
    }
    
    @Override
    public String visitExpressionStatement(ExpressionStatement stmt) {
        return stmt.expression.accept(this) + ";";
    }
    
    @Override
    public String visitVariableDeclaration(VariableDeclaration stmt) {
        if (stmt.initializer != null) {
            return String.format("%s %s = %s;", stmt.type, stmt.name, stmt.initializer.accept(this));
        } else {
            return String.format("%s %s;", stmt.type, stmt.name);
        }
    }
    
    @Override
    public String visitBlockStatement(BlockStatement stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        indentLevel++;
        
        for (Statement statement : stmt.statements) {
            builder.append(indent()).append(statement.accept(this)).append("\n");
        }
        
        indentLevel--;
        builder.append(indent()).append("}");
        return builder.toString();
    }
    
    @Override
    public String visitIfStatement(IfStatement stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("if (").append(stmt.condition.accept(this)).append(") ");
        builder.append(stmt.thenBranch.accept(this));
        
        if (stmt.elseBranch != null) {
            builder.append(" else ").append(stmt.elseBranch.accept(this));
        }
        
        return builder.toString();
    }
    
    @Override
    public String visitWhileStatement(WhileStatement stmt) {
        return String.format("while (%s) %s", 
            stmt.condition.accept(this), 
            stmt.body.accept(this));
    }
    
    @Override
    public String visitForStatement(ForStatement stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("for (");
        
        if (stmt.initializer != null) {
            builder.append(stmt.initializer.accept(this));
        }
        builder.append("; ");
        
        if (stmt.condition != null) {
            builder.append(stmt.condition.accept(this));
        }
        builder.append("; ");
        
        if (stmt.increment != null) {
            builder.append(stmt.increment.accept(this));
        }
        
        builder.append(") ").append(stmt.body.accept(this));
        return builder.toString();
    }
    
    @Override
    public String visitFunctionDeclaration(FunctionDeclaration stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("func ").append(stmt.name).append("(");
        
        for (int i = 0; i < stmt.parameters.size(); i++) {
            if (i > 0) builder.append(", ");
            builder.append(stmt.parameterTypes.get(i)).append(" ").append(stmt.parameters.get(i));
        }
        
        builder.append(") -> ").append(stmt.returnType).append(" ");
        builder.append(stmt.body.accept(this));
        return builder.toString();
    }
    
    @Override
    public String visitReturnStatement(ReturnStatement stmt) {
        if (stmt.value != null) {
            return "return " + stmt.value.accept(this) + ";";
        } else {
            return "return;";
        }
    }
    
    @Override
    public String visitArrayDeclaration(ArrayDeclaration stmt) {
        if (stmt.elements != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(stmt.type).append(" ").append(stmt.name).append(" = [");
            
            for (int i = 0; i < stmt.elements.size(); i++) {
                if (i > 0) builder.append(", ");
                builder.append(stmt.elements.get(i).accept(this));
            }
            
            builder.append("];");
            return builder.toString();
        } else {
            return String.format("%s %s[%s];", 
                stmt.type, stmt.name, stmt.size.accept(this));
        }
    }
}