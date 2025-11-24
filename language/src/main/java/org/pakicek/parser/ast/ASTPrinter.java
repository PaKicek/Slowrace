package org.pakicek.parser.ast;

import org.pakicek.parser.ast.node.*;
import org.pakicek.parser.ast.node.expression.*;
import org.pakicek.parser.ast.node.expression.literal.*;
import org.pakicek.parser.ast.node.statement.*;
import org.pakicek.parser.ast.node.type.ArrayTypeNode;
import org.pakicek.parser.ast.node.type.BasicTypeNode;

public class ASTPrinter implements ASTVisitor<String> {
    private int indentLevel = 0;
    private final StringBuilder output = new StringBuilder();

    public String print(ASTNode node) {
        output.setLength(0); // Clear previous content
        indentLevel = 0;
        node.accept(this);
        return output.toString();
    }

    private void indent() {
        output.append("  ".repeat(Math.max(0, indentLevel)));
    }

    private void println(String text) {
        indent();
        output.append(text).append("\n");
    }

    private void print(String text) {
        output.append(text);
    }

    @Override
    public String visit(MainNode node) {
        println("Main Program");
        indentLevel++;

        println("Parameters:");
        indentLevel++;
        println("argc: int");
        println("argv: array string[]");
        indentLevel--;

        println("Body:");
        indentLevel++;
        node.getBody().accept(this);
        indentLevel--;

        indentLevel--;
        return "";
    }

    // Program structure
    @Override
    public String visit(ProgramNode node) {
        println("Program");
        indentLevel++;

        for (FunctionDeclarationNode function : node.getFunctions()) {
            function.accept(this);
        }

        if (node.getMainNode() != null) {
            node.getMainNode().accept(this);
        }

        indentLevel--;
        return output.toString();
    }

    @Override
    public String visit(FunctionDeclarationNode node) {
        println("Function: " + node.getName());
        indentLevel++;

        println("Return Type:");
        indentLevel++;
        node.getReturnType().accept(this);
        indentLevel--;

        if (!node.getParameters().isEmpty()) {
            println("Parameters:");
            indentLevel++;
            for (ParameterNode param : node.getParameters()) {
                param.accept(this);
            }
            indentLevel--;
        }

        println("Body:");
        indentLevel++;
        node.getBody().accept(this);
        indentLevel--;

        indentLevel--;
        return "";
    }

    @Override
    public String visit(ParameterNode node) {
        println("Parameter: " + node.getName());
        indentLevel++;
        node.getType().accept(this);
        indentLevel--;
        return "";
    }

    // Types
    @Override
    public String visit(BasicTypeNode node) {
        println("Type: " + node.getTypeName());
        return "";
    }

    @Override
    public String visit(ArrayTypeNode node) {
        if (node.isFixedSize()) {
            println("Array Type (fixed size: " + node.getFixedSize() + ")");
        } else if (node.isDynamicSize()) {
            println("Array Type (dynamic size)");
        } else {
            println("Array Type (unspecified size)");
        }

        indentLevel++;

        println("Element Type:");
        indentLevel++;
        node.getElementType().accept(this);
        indentLevel--;

        if (node.isDynamicSize() && node.getSizeExpression() != null) {
            println("Size Expression:");
            indentLevel++;
            node.getSizeExpression().accept(this);
            indentLevel--;
        }

        indentLevel--;
        return "";
    }

    // Statements
    @Override
    public String visit(BlockStatementNode node) {
        println("Block");
        indentLevel++;
        for (StatementNode stmt : node.getStatements()) {
            stmt.accept(this);
        }
        indentLevel--;
        return "";
    }

    @Override
    public String visit(VariableDeclarationNode node) {
        println("Variable Declaration: " + node.getName());
        indentLevel++;

        println("Type:");
        indentLevel++;
        node.getType().accept(this);
        indentLevel--;

        if (node.getInitialValue() != null) {
            println("Initial Value:");
            indentLevel++;
            node.getInitialValue().accept(this);
            indentLevel--;
        }

        indentLevel--;
        return "";
    }

    @Override
    public String visit(AssignmentNode node) {
        println("Assignment");
        indentLevel++;

        println("Target:");
        indentLevel++;
        node.getTarget().accept(this);
        indentLevel--;

        println("Value:");
        indentLevel++;
        node.getValue().accept(this);
        indentLevel--;

        indentLevel--;
        return "";
    }

    @Override
    public String visit(IfStatementNode node) {
        println("If Statement");
        indentLevel++;

        println("Condition:");
        indentLevel++;
        node.getCondition().accept(this);
        indentLevel--;

        println("Then Block:");
        indentLevel++;
        node.getThenBlock().accept(this);
        indentLevel--;

        for (IfStatementNode.ElifBranch elif : node.getElifBranches()) {
            println("Elif Branch:");
            indentLevel++;

            println("Condition:");
            indentLevel++;
            elif.getCondition().accept(this);
            indentLevel--;

            println("Block:");
            indentLevel++;
            elif.getBlock().accept(this);
            indentLevel--;

            indentLevel--;
        }

        if (node.getElseBlock() != null) {
            println("Else Block:");
            indentLevel++;
            node.getElseBlock().accept(this);
            indentLevel--;
        }

        indentLevel--;
        return "";
    }

    @Override
    public String visit(ForLoopNode node) {
        println("For Loop");
        indentLevel++;

        if (node.getInitialization() != null) {
            println("Initialization:");
            indentLevel++;
            node.getInitialization().accept(this);
            indentLevel--;
        }

        if (node.getCondition() != null) {
            println("Condition:");
            indentLevel++;
            node.getCondition().accept(this);
            indentLevel--;
        }

        if (node.getUpdate() != null) {
            println("Update:");
            indentLevel++;
            node.getUpdate().accept(this);
            indentLevel--;
        }

        println("Body:");
        indentLevel++;
        node.getBody().accept(this);
        indentLevel--;

        indentLevel--;
        return "";
    }

    @Override
    public String visit(WhileLoopNode node) {
        println("While Loop");
        indentLevel++;

        println("Condition:");
        indentLevel++;
        node.getCondition().accept(this);
        indentLevel--;

        println("Body:");
        indentLevel++;
        node.getBody().accept(this);
        indentLevel--;

        indentLevel--;
        return "";
    }

    @Override
    public String visit(ReturnStatementNode node) {
        println("Return Statement");
        indentLevel++;

        if (node.getValue() != null) {
            println("Value:");
            indentLevel++;
            node.getValue().accept(this);
            indentLevel--;
        } else {
            println("Value: void");
        }

        indentLevel--;
        return "";
    }

    @Override
    public String visit(ExpressionStatementNode node) {
        println("Expression Statement");
        indentLevel++;
        node.getExpression().accept(this);
        indentLevel--;
        return "";
    }

    // Expressions
    @Override
    public String visit(BinaryExpressionNode node) {
        println("Binary Expression: " + node.getOperator());
        indentLevel++;

        println("Left:");
        indentLevel++;
        node.getLeft().accept(this);
        indentLevel--;

        println("Right:");
        indentLevel++;
        node.getRight().accept(this);
        indentLevel--;

        indentLevel--;
        return "";
    }

    @Override
    public String visit(UnaryExpressionNode node) {
        println("Unary Expression: " + node.getOperator());
        indentLevel++;

        println("Operand:");
        indentLevel++;
        node.getOperand().accept(this);
        indentLevel--;

        indentLevel--;
        return "";
    }

    @Override
    public String visit(FunctionCallNode node) {
        println("Function Call: " + node.getFunctionName());
        indentLevel++;

        if (!node.getArguments().isEmpty()) {
            println("Arguments:");
            indentLevel++;
            for (ExpressionNode arg : node.getArguments()) {
                arg.accept(this);
            }
            indentLevel--;
        } else {
            println("Arguments: none");
        }

        indentLevel--;
        return "";
    }

    @Override
    public String visit(ArrayAccessNode node) {
        println("Array Access");
        indentLevel++;

        println("Array:");
        indentLevel++;
        node.getArray().accept(this);
        indentLevel--;

        println("Index:");
        indentLevel++;
        node.getIndex().accept(this);
        indentLevel--;

        indentLevel--;
        return "";
    }

    @Override
    public String visit(ArrayLiteralNode node) {
        println("Array Literal");
        indentLevel++;

        if (!node.getElements().isEmpty()) {
            println("Elements:");
            indentLevel++;
            for (ExpressionNode element : node.getElements()) {
                element.accept(this);
            }
            indentLevel--;
        } else {
            println("Elements: empty");
        }

        indentLevel--;
        return "";
    }

    @Override
    public String visit(VariableNode node) {
        println("Variable: " + node.getName());
        return "";
    }

    // Literals
    @Override
    public String visit(IntegerLiteralNode node) {
        println("Integer Literal: " + node.getValue());
        return "";
    }

    @Override
    public String visit(FloatLiteralNode node) {
        println("Float Literal: " + node.getValue());
        return "";
    }

    @Override
    public String visit(StringLiteralNode node) {
        println("String Literal: \"" + node.getValue() + "\"");
        return "";
    }

    @Override
    public String visit(BooleanLiteralNode node) {
        println("Boolean Literal: " + node.getValue());
        return "";
    }
}