package org.pakicek.parser.ast.node.expression;

import org.pakicek.parser.ast.ASTVisitor;

public class UnaryExpressionNode extends ExpressionNode {
    private final String operator;
    private final ExpressionNode operand;

    public UnaryExpressionNode(String operator, ExpressionNode operand, int line, int position) {
        super(line, position);
        this.operator = operator;
        this.operand = operand;
    }

    public String getOperator() {
        return operator;
    }

    public ExpressionNode getOperand() {
        return operand;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
