package org.pakicek.parser.ast.node.statement;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.expression.ExpressionNode;

// Represents while loop: while (condition) { body }
public class WhileLoopNode extends StatementNode {
    private final ExpressionNode condition;
    private final BlockStatementNode body;

    public WhileLoopNode(ExpressionNode condition, BlockStatementNode body, int line, int position) {
        super(line, position);
        this.condition = condition;
        this.body = body;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public BlockStatementNode getBody() {
        return body;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
