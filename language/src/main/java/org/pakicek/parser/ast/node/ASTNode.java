package org.pakicek.parser.ast.node;

import org.pakicek.parser.ast.ASTVisitor;

public abstract class ASTNode {
    protected int line;
    protected int position;

    public ASTNode(int line, int position) {
        this.line = line;
        this.position = position;
    }

    public int getLine() {
        return line;
    }

    public int getPosition() {
        return position;
    }

    public abstract <T> T accept(ASTVisitor<T> visitor);
}