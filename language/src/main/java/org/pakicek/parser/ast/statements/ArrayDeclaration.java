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
    
    // ОДИН универсальный конструктор
    public ArrayDeclaration(String type, String name, List<Expression> elements, Expression size) {
        this.type = type;
        this.name = name;
        this.elements = elements;
        this.size = size;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitArrayDeclaration(this);
    }
}