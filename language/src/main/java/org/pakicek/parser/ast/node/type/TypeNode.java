package org.pakicek.parser.ast.node.type;

import org.pakicek.parser.ast.node.ASTNode;

// Base class for type nodes
public abstract class TypeNode extends ASTNode {
    public TypeNode(int line, int position) {
        super(line, position);
    }
}
