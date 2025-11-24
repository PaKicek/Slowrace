package org.pakicek.parser.ast.node.expression.literal;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.expression.ExpressionNode;

// Represents boolean literal: true, false
public class BooleanLiteralNode extends ExpressionNode {
    private final boolean value;

    public BooleanLiteralNode(boolean value, int line, int position) {
        super(line, position);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
