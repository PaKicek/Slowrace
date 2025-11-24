package org.pakicek.parser.ast.node;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.statement.BlockStatementNode;
import org.pakicek.parser.ast.node.type.TypeNode;

import java.util.ArrayList;
import java.util.List;

// Represents function declaration: func returnType name(params) { body }
public class FunctionDeclarationNode extends ASTNode {
    private final String name;
    private final TypeNode returnType;
    private final List<ParameterNode> parameters;
    private BlockStatementNode body;

    public FunctionDeclarationNode(String name, TypeNode returnType, int line, int position) {
        super(line, position);
        this.name = name;
        this.returnType = returnType;
        this.parameters = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public TypeNode getReturnType() {
        return returnType;
    }

    public List<ParameterNode> getParameters() {
        return parameters;
    }

    public void addParameter(ParameterNode parameter) {
        parameters.add(parameter);
    }

    public BlockStatementNode getBody() {
        return body;
    }

    public void setBody(BlockStatementNode body) {
        this.body = body;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}