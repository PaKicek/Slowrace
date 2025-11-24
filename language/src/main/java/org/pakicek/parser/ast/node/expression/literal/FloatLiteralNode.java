package org.pakicek.parser.ast.node.expression.literal;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.expression.ExpressionNode;

// Represents float literal: 3.14, -2.5, etc.
public class FloatLiteralNode extends ExpressionNode {
    private final double value;

    public FloatLiteralNode(double value, int line, int position) {
        super(line, position);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
