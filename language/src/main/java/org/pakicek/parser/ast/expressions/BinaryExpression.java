// org/pakicek/parser/ast/expressions/
package org.pakicek.parser.ast.expressions;

import org.pakicek.parser.ast.Expression;
import org.pakicek.parser.lexer.Token;

public class BinaryExpression implements Expression {
    public final Expression left;
    public final Token operator;
    public final Expression right;
    
    public BinaryExpression(Expression left, Token operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitBinaryExpression(this);
    }
}