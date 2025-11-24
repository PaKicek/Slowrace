package org.pakicek.parser.ast.node.expression;

import org.pakicek.parser.ast.ASTVisitor;

import java.util.ArrayList;
import java.util.List;

// Represents function call: name(arg1, arg2, ...)
public class FunctionCallNode extends ExpressionNode {
    private final String functionName;
    private final List<ExpressionNode> arguments;

    public FunctionCallNode(String functionName, int line, int position) {
        super(line, position);
        this.functionName = functionName;
        this.arguments = new ArrayList<>();
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<ExpressionNode> getArguments() {
        return arguments;
    }

    public void addArgument(ExpressionNode argument) {
        arguments.add(argument);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
