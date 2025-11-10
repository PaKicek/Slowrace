package org.pakicek.parser.ast.expressions;

import org.pakicek.parser.ast.Expression;
import org.pakicek.parser.ast.Visitor;
import org.pakicek.parser.lexer.Token;

public class UnaryExpression implements Expression {
    public final Token operator;
    public final Expression right;
    
    public UnaryExpression(Token operator, Expression right) {
        this.operator = operator;
        this.right = right;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitUnaryExpression(this);
    }
}