package org.pakicek.parser.ast.node.statement;

import org.pakicek.parser.ast.ASTVisitor;

import java.util.ArrayList;
import java.util.List;

public class BlockStatementNode extends StatementNode {
    private final List<StatementNode> statements;

    public BlockStatementNode(int line, int position) {
        super(line, position);
        this.statements = new ArrayList<>();
    }

    public List<StatementNode> getStatements() {
        return statements;
    }

    public void addStatement(StatementNode statement) {
        statements.add(statement);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}