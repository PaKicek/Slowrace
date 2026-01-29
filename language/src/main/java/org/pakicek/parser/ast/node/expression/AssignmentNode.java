package org.pakicek.parser.ast.node.expression;

import org.pakicek.parser.ast.ASTVisitor;

public class AssignmentNode extends ExpressionNode {
    private final ExpressionNode target;
    private final ExpressionNode value;

    public AssignmentNode(ExpressionNode target, ExpressionNode value, int line, int position) {
        super(line, position);
        this.target = target;
        this.value = value;
    }

    public ExpressionNode getTarget() {
        return target;
    }

    public ExpressionNode getValue() {
        return value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
