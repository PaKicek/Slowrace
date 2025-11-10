package org.pakicek.parser.ast.expressions;

import org.pakicek.parser.ast.Expression;
import org.pakicek.parser.ast.Visitor;
import java.util.List;

public class FunctionCallExpression implements Expression {
    public final Expression callee;
    public final List<Expression> arguments;
    
    public FunctionCallExpression(Expression callee, List<Expression> arguments) {
        this.callee = callee;
        this.arguments = arguments;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFunctionCallExpression(this);
    }
}