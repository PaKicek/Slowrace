// org/pakicek/parser/ast/expressions/LiteralExpression.java
package org.pakicek.parser.ast.expressions;

import org.pakicek.parser.ast.Expression;

public class LiteralExpression implements Expression {
    public final Object value;
    
    public LiteralExpression(Object value) {
        this.value = value;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitLiteralExpression(this);
    }
}