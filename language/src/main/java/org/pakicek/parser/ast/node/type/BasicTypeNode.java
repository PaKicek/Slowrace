package org.pakicek.parser.ast.node.type;

import org.pakicek.parser.ast.ASTVisitor;

// Represents basic types: int, float, string, bool, void
public class BasicTypeNode extends TypeNode {
    private final String typeName;

    public BasicTypeNode(String typeName, int line, int position) {
        super(line, position);
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
