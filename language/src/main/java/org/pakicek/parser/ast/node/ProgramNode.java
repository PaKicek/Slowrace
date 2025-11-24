package org.pakicek.parser.ast.node;

import org.pakicek.parser.ast.ASTVisitor;

import java.util.ArrayList;
import java.util.List;

// Represents the entire program
public class ProgramNode extends ASTNode {
    private final List<FunctionDeclarationNode> functions;
    private MainNode mainNode;

    public ProgramNode(int line, int position) {
        super(line, position);
        this.functions = new ArrayList<>();
    }

    public List<FunctionDeclarationNode> getFunctions() {
        return functions;
    }

    public void addFunction(FunctionDeclarationNode function) {
        functions.add(function);
    }

    public MainNode getMainNode() {
        return mainNode;
    }

    public void setMainNode(MainNode mainNode) {
        this.mainNode = mainNode;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}