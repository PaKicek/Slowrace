// org/pakicek/parser/ast/expressions/AssignmentExpression.java
package org.pakicek.parser.ast.expressions;

import org.pakicek.parser.ast.Expression;

public class AssignmentExpression implements Expression {
    public final String identifier;
    public final Expression value;
    
    public AssignmentExpression(String identifier, Expression value) {
        this.identifier = identifier;
        this.value = value;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitAssignmentExpression(this);
    }
}