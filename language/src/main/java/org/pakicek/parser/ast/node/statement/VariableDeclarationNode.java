package org.pakicek.parser.ast.node.statement;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.expression.ExpressionNode;
import org.pakicek.parser.ast.node.type.TypeNode;

public class VariableDeclarationNode extends StatementNode {
    private final String name;
    private final TypeNode type;
    private ExpressionNode initialValue;

    public VariableDeclarationNode(String name, TypeNode type, int line, int position) {
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

    public ExpressionNode getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(ExpressionNode initialValue) {
        this.initialValue = initialValue;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
