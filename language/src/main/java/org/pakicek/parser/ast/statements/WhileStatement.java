package org.pakicek.parser.ast.statements;

import org.pakicek.parser.ast.Statement;
import org.pakicek.parser.ast.Visitor;
import org.pakicek.parser.ast.expressions.Expression;

public class WhileStatement implements Statement {
    public final Expression condition;
    public final Statement body;
    
    public WhileStatement(Expression condition, Statement body) {
        this.condition = condition;
        this.body = body;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitWhileStatement(this);
    }
}