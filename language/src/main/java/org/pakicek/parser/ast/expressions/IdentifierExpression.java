// org/pakicek/parser/ast/expressions/IdentifierExpression.java
package org.pakicek.parser.ast.expressions;

import org.pakicek.parser.ast.Expression;

public class IdentifierExpression implements Expression {
    public final String name;
    
    public IdentifierExpression(String name) {
        this.name = name;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitIdentifierExpression(this);
    }
}