package org.pakicek.parser.ast.statements;

import org.pakicek.parser.ast.Statement;
import org.pakicek.parser.ast.Visitor;
import org.pakicek.parser.ast.expressions.Expression;

public class ForStatement implements Statement {
    public final Statement initializer;
    public final Expression condition;
    public final Expression increment;
    public final Statement body;
    
    public ForStatement(Statement initializer, Expression condition, Expression increment, Statement body) {
        this.initializer = initializer;
        this.condition = condition;
        this.increment = increment;
        this.body = body;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitForStatement(this);
    }
}