package org.pakicek.parser.ast.node.type;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.expression.ExpressionNode;

// Represents array types: array type[size] or array type[expression]
public class ArrayTypeNode extends TypeNode {
    private final TypeNode elementType;
    private final Integer fixedSize;        // For fixed-size arrays: array int[5]
    private final ExpressionNode sizeExpression; // For dynamic arrays: array int[n + 1]

    public ArrayTypeNode(TypeNode elementType, Integer fixedSize, int line, int position) {
        super(line, position);
        this.elementType = elementType;
        this.fixedSize = fixedSize;
        this.sizeExpression = null;
    }

    public ArrayTypeNode(TypeNode elementType, ExpressionNode sizeExpression, int line, int position) {
        super(line, position);
        this.elementType = elementType;
        this.fixedSize = null;
        this.sizeExpression = sizeExpression;
    }

    public TypeNode getElementType() {
        return elementType;
    }

    public Integer getFixedSize() {
        return fixedSize;
    }

    public ExpressionNode getSizeExpression() {
        return sizeExpression;
    }

    public boolean isFixedSize() {
        return fixedSize != null;
    }

    public boolean isDynamicSize() {
        return sizeExpression != null;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
