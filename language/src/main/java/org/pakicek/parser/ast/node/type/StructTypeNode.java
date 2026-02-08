package org.pakicek.parser.ast.node.type;

import org.pakicek.parser.ast.ASTVisitor;

public class StructTypeNode extends TypeNode {
    private final String structName;

    public StructTypeNode(String structName, int line, int position) {
        super(line, position);
        this.structName = structName;
    }

    public String getStructName() {
        return structName;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}