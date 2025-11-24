package org.pakicek.parser.ast.node.statement;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.expression.ExpressionNode;

import java.util.ArrayList;
import java.util.List;

// Represents if statement: if (condition) { thenBlock } [elif...] [else...]
public class IfStatementNode extends StatementNode {
    private final ExpressionNode condition;
    private final BlockStatementNode thenBlock;
    private final List<ElifBranch> elifBranches;
    private BlockStatementNode elseBlock;

    public IfStatementNode(ExpressionNode condition, BlockStatementNode thenBlock, int line, int position) {
        super(line, position);
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elifBranches = new ArrayList<>();
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public BlockStatementNode getThenBlock() {
        return thenBlock;
    }

    public List<ElifBranch> getElifBranches() {
        return elifBranches;
    }

    public void addElifBranch(ElifBranch elifBranch) {
        elifBranches.add(elifBranch);
    }

    public BlockStatementNode getElseBlock() {
        return elseBlock;
    }

    public void setElseBlock(BlockStatementNode elseBlock) {
        this.elseBlock = elseBlock;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    // Inner class for elif branches
    public static class ElifBranch {
        private final ExpressionNode condition;
        private final BlockStatementNode block;

        public ElifBranch(ExpressionNode condition, BlockStatementNode block) {
            this.condition = condition;
            this.block = block;
        }

        public ExpressionNode getCondition() {
            return condition;
        }

        public BlockStatementNode getBlock() {
            return block;
        }
    }
}