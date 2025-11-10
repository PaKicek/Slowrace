package org.pakicek.parser.ast.statements;

import org.pakicek.parser.ast.Statement;
import org.pakicek.parser.ast.Visitor;
import org.pakicek.parser.ast.expressions.Expression;

public class ExpressionStatement implements Statement {
    public final Expression expression;
    
    public ExpressionStatement(Expression expression) {
        this.expression = expression;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitExpressionStatement(this);
    }
}