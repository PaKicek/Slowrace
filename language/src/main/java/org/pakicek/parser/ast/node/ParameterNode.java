package org.pakicek.parser.ast.node;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.type.TypeNode;

public class ParameterNode extends ASTNode {
    private final String name;
    private final TypeNode type;

    public ParameterNode(String name, TypeNode type, int line, int position) {
        super(line, position);
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public TypeNode getType() {
        return type;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}