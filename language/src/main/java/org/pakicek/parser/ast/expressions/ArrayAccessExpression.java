package org.pakicek.parser.ast.expressions;

import org.pakicek.parser.ast.Expression;
import org.pakicek.parser.ast.Visitor;

public class ArrayAccessExpression implements Expression {
    public final Expression array;
    public final Expression index;
    
    public ArrayAccessExpression(Expression array, Expression index) {
        this.array = array;
        this.index = index;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitArrayAccessExpression(this);
    }
}