package org.pakicek.parser.ast.statements;

import org.pakicek.parser.ast.Statement;
import org.pakicek.parser.ast.Visitor;
import java.util.List;

public class FunctionDeclaration implements Statement {
    public final String name;
    public final List<String> parameters;
    public final List<String> parameterTypes;
    public final String returnType;
    public final BlockStatement body;
    
    public FunctionDeclaration(String name, List<String> parameters, List<String> parameterTypes, 
                             String returnType, BlockStatement body) {
        this.name = name;
        this.parameters = parameters;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.body = body;
    }
    
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFunctionDeclaration(this);
    }
}