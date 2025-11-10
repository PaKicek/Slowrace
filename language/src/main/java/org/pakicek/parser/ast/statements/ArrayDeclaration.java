package org.pakicek.parser.ast.statements;

import org.pakicek.parser.ast.Statement;
import org.pakicek.parser.ast.Visitor;
import org.pakicek.parser.ast.expressions.Expression;
import java.util.List;

public class ArrayDeclaration implements Statement {
    public final String type;
    public final String name;
    public final List<Expression> elements;
    public final Expression size;
    
    public ArrayDeclaration(String type, String name, List<Expression> elements) {
        this.type = type;
        this.name = name;
        this.elements = elements;
        this.size = null;
    }
    
    public ArrayDeclaration(String type, String name, Expression size) {
        this.type = type;
        this.name = name;
        this.elements = null;
        this.size = size;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitArrayDeclaration(this);
    }
}