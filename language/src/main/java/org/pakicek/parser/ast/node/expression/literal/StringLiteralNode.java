package org.pakicek.parser.ast.node.expression.literal;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.expression.ExpressionNode;

public class StringLiteralNode extends ExpressionNode {
    private final String value;

    public StringLiteralNode(String value, int line, int position) {
        super(line, position);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
