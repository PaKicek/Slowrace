package org.pakicek.parser.ast.node.expression;

import org.pakicek.parser.ast.ASTVisitor;

public class FieldAccessNode extends ExpressionNode {
    private final ExpressionNode object;
    private final String fieldName;

    public FieldAccessNode(ExpressionNode object, String fieldName, int line, int position) {
        super(line, position);
        this.object = object;
        this.fieldName = fieldName;
    }

    public ExpressionNode getObject() {
        return object;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}