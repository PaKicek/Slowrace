package org.pakicek.parser.ast.node;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.statement.VariableDeclarationNode;

import java.util.ArrayList;
import java.util.List;

public class StructDeclarationNode extends ASTNode {
    private final String name;
    private final List<VariableDeclarationNode> fields;

    public StructDeclarationNode(String name, int line, int position) {
        super(line, position);
        this.name = name;
        this.fields = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<VariableDeclarationNode> getFields() {
        return fields;
    }

    public void addField(VariableDeclarationNode field) {
        fields.add(field);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}