package org.pakicek.parser.ast.node.expression;

import org.pakicek.parser.ast.ASTVisitor;

public class VariableNode extends ExpressionNode {
    private final String name;

    public VariableNode(String name, int line, int position) {
        super(line, position);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
