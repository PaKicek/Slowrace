package org.pakicek.parser.ast.node.expression.literal;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.expression.ExpressionNode;

import java.util.ArrayList;
import java.util.List;

// Represents array literal: [element1, element2, ...]
public class ArrayLiteralNode extends ExpressionNode {
    private final List<ExpressionNode> elements;

    public ArrayLiteralNode(int line, int position) {
        super(line, position);
        this.elements = new ArrayList<>();
    }

    public List<ExpressionNode> getElements() {
        return elements;
    }

    public void addElement(ExpressionNode element) {
        elements.add(element);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
