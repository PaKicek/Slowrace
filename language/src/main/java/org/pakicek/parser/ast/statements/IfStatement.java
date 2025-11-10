package org.pakicek.parser.ast.statements;

import org.pakicek.parser.ast.Statement;
import org.pakicek.parser.ast.Visitor;
import org.pakicek.parser.ast.expressions.Expression;

public class IfStatement implements Statement {
    public final Expression condition;
    public final Statement thenBranch;
    public final Statement elseBranch;
    
    public IfStatement(Expression condition, Statement thenBranch, Statement elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitIfStatement(this);
    }
}