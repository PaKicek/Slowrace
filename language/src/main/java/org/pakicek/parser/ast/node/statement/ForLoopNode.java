package org.pakicek.parser.ast.node.statement;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.expression.ExpressionNode;

// Represents for loop: for (init; condition; update) { body }
public class ForLoopNode extends StatementNode {
    private final StatementNode initialization;
    private final ExpressionNode condition;
    private final ExpressionNode update;
    private final BlockStatementNode body;

    public ForLoopNode(StatementNode initialization, ExpressionNode condition,
                       ExpressionNode update, BlockStatementNode body, int line, int position) {
        super(line, position);
        this.initialization = initialization;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    public StatementNode getInitialization() {
        return initialization;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public ExpressionNode getUpdate() {
        return update;
    }

    public BlockStatementNode getBody() {
        return body;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
