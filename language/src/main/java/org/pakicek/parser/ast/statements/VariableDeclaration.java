package org.pakicek.parser.ast.statements;

import org.pakicek.parser.ast.Statement;
import org.pakicek.parser.ast.Visitor;
import org.pakicek.parser.ast.expressions.Expression;

public class VariableDeclaration implements Statement {
    public final String type;
    public final String name;
    public final Expression initializer;
    
    public VariableDeclaration(String type, String name, Expression initializer) {
        this.type = type;
        this.name = name;
        this.initializer = initializer;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitVariableDeclaration(this);
    }
}