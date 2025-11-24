package org.pakicek.parser.ast.node.statement;

import org.pakicek.parser.ast.node.ASTNode;

// Base class for all statements
public abstract class StatementNode extends ASTNode {
    public StatementNode(int line, int position) {
        super(line, position);
    }
}
