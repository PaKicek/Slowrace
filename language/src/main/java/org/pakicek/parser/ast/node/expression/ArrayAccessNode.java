package org.pakicek.parser.ast.node.expression;

import org.pakicek.parser.ast.ASTVisitor;

// Represents array access: array[index]
public class ArrayAccessNode extends ExpressionNode {
    private final ExpressionNode array;
    private final ExpressionNode index;

    public ArrayAccessNode(ExpressionNode array, ExpressionNode index, int line, int position) {
        super(line, position);
        this.array = array;
        this.index = index;
    }

    public ExpressionNode getArray() {
        return array;
    }

    public ExpressionNode getIndex() {
        return index;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
