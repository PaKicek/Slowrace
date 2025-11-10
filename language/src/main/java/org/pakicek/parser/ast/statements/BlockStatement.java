package org.pakicek.parser.ast.statements;

import org.pakicek.parser.ast.Statement;
import org.pakicek.parser.ast.Visitor;
import java.util.List;

public class BlockStatement implements Statement {
    public final List<Statement> statements;
    
    public BlockStatement(List<Statement> statements) {
        this.statements = statements;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitBlockStatement(this);
    }
}