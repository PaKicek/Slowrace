package org.pakicek.parser.ast.node;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.statement.BlockStatementNode;

// Represents the main program entry point
public class MainNode extends ASTNode {
    private BlockStatementNode body;

    public MainNode(int line, int position) {
        super(line, position);
    }

    public BlockStatementNode getBody() {
        return body;
    }

    public void setBody(BlockStatementNode body) {
        this.body = body;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
