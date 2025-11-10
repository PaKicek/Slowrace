package org.pakicek.parser.ast.statements;

import org.pakicek.parser.ast.Statement;
import org.pakicek.parser.ast.Visitor;
import org.pakicek.parser.ast.expressions.Expression;

public class ReturnStatement implements Statement {
    public final Expression value;
    
    public ReturnStatement(Expression value) {
        this.value = value;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitReturnStatement(this);
    }
}