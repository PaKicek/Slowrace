package org.pakicek.parser.ast.node.expression;

import org.pakicek.parser.ast.node.ASTNode;

// Base class for all expressions
public abstract class ExpressionNode extends ASTNode {
    public ExpressionNode(int line, int position) {
        super(line, position);
    }
}
