package org.pakicek.parser.ast.expressions;

import org.pakicek.parser.ast.Expression;
import org.pakicek.parser.ast.Visitor;

public class GroupingExpression implements Expression {
    public final Expression expression;
    
    public GroupingExpression(Expression expression) {
        this.expression = expression;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitGroupingExpression(this);
    }
}