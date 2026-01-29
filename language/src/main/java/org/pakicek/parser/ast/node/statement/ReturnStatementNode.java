package org.pakicek.parser.ast.node.statement;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.expression.ExpressionNode;

public class ReturnStatementNode extends StatementNode {
    private ExpressionNode value;

    public ReturnStatementNode(int line, int position) {
        super(line, position);
    }

    public ReturnStatementNode(ExpressionNode value, int line, int position) {
        super(line, position);
        this.value = value;
    }

    public ExpressionNode getValue() {
        return value;
    }

    public void setValue(ExpressionNode value) {
        this.value = value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
