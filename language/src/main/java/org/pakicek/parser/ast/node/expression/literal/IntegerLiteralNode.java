package org.pakicek.parser.ast.node.expression.literal;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.expression.ExpressionNode;

// Represents integer literal: 42, -100, etc.
public class IntegerLiteralNode extends ExpressionNode {
    private final long value;

    public IntegerLiteralNode(long value, int line, int position) {
        super(line, position);
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
