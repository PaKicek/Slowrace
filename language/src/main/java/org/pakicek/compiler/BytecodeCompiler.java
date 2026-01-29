package org.pakicek.compiler;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.*;
import org.pakicek.parser.ast.node.expression.*;
import org.pakicek.parser.ast.node.expression.literal.*;
import org.pakicek.parser.ast.node.statement.*;
import org.pakicek.parser.ast.node.type.*;
import org.pakicek.runtime.ProgramImage;
import org.pakicek.runtime.bytecode.*;
import org.pakicek.runtime.vm.SrValue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BytecodeCompiler implements ASTVisitor<Void> {
    private Chunk currentChunk;
    private final Map<String, Chunk> functions = new HashMap<>();
    
    private static class Local {
        String name;
        int depth;
        Local(String name, int depth) {
            this.name = name;
            this.depth = depth;
        }
    }

    private final List<Local> locals = new ArrayList<>();
    private int scopeDepth = 0;
    
    public ProgramImage compile(ProgramNode program) {
        functions.clear();
        visit(program);
        currentChunk = new Chunk();
        locals.clear();
        scopeDepth = 0;
        if (program.getMainNode() != null) {
            program.getMainNode().accept(this);
        }
        currentChunk.emit(OpCode.HALT, 0);
        return new ProgramImage(currentChunk, new HashMap<>(functions));
    }

    public Map<String, Chunk> getFunctions() {
        return functions;
    }
    
    @Override
    public Void visit(ProgramNode node) {
        for (FunctionDeclarationNode func : node.getFunctions()) {
            func.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(FunctionDeclarationNode node) {
        Chunk previousChunk = currentChunk;
        List<Local> previousLocals = new ArrayList<>(locals);
        int previousScope = scopeDepth;
        
        currentChunk = new Chunk();
        locals.clear();
        scopeDepth = 0;
        scopeDepth++;
        
        for (ParameterNode param : node.getParameters()) {
            addLocal(param.getName());
        }
        
        node.getBody().accept(this);
        if (currentChunk.code.isEmpty() || currentChunk.code.get(currentChunk.code.size() - 1) != (byte)OpCode.RETURN.ordinal()) {
            int idx = currentChunk.addConstant(SrValue.VOID);
            currentChunk.emit(OpCode.LOAD_CONST, node.getLine());
            currentChunk.emitByte(idx, node.getLine());
            currentChunk.emit(OpCode.RETURN, node.getLine());
        }
        
        functions.put(node.getName(), currentChunk);
        currentChunk = previousChunk;
        locals.clear();
        locals.addAll(previousLocals);
        scopeDepth = previousScope;

        return null;
    }

    @Override
    public Void visit(MainNode node) {
        scopeDepth++;
        addLocal("argc");
        addLocal("argv");
        node.getBody().accept(this);
        scopeDepth--;
        return null;
    }

    @Override
    public Void visit(StructDeclarationNode node) {
        return null;
    }

    @Override
    public Void visit(ParameterNode node) {
        return null;
    }
    
    @Override
    public Void visit(BlockStatementNode node) {
        scopeDepth++;
        for (StatementNode stmt : node.getStatements()) {
            stmt.accept(this);
        }
        scopeDepth--;
        return null;
    }

    @Override
    public Void visit(ExpressionStatementNode node) {
        node.getExpression().accept(this);
        currentChunk.emit(OpCode.POP, node.getLine());
        return null;
    }

    @Override
    public Void visit(VariableDeclarationNode node) {
        if (node.getInitialValue() != null) {
            node.getInitialValue().accept(this);
        } else {
            emitDefaultValue(node.getType(), node.getLine());
        }
        addLocal(node.getName());
        currentChunk.emit(OpCode.STORE_LOCAL, node.getLine());
        currentChunk.emitByte(locals.size() - 1, node.getLine());
        return null;
    }

    @Override
    public Void visit(ReturnStatementNode node) {
        if (node.getValue() != null) {
            node.getValue().accept(this);
        } else {
            int idx = currentChunk.addConstant(SrValue.VOID);
            currentChunk.emit(OpCode.LOAD_CONST, node.getLine());
            currentChunk.emitByte(idx, node.getLine());
        }
        currentChunk.emit(OpCode.RETURN, node.getLine());
        return null;
    }
    
    @Override
    public Void visit(IfStatementNode node) {
        node.getCondition().accept(this);
        int thenJump = emitJump(OpCode.JMP_FALSE, node.getLine());
        node.getThenBlock().accept(this);
        int elseJump = emitJump(OpCode.JMP, node.getLine());
        patchJump(thenJump);

        for (IfStatementNode.ElifBranch branch : node.getElifBranches()) {
            branch.getCondition().accept(this);
            int elifJump = emitJump(OpCode.JMP_FALSE, node.getLine());
            branch.getBlock().accept(this);
            patchJump(elifJump);
        }

        if (node.getElseBlock() != null) {
            node.getElseBlock().accept(this);
        }

        patchJump(elseJump);
        return null;
    }

    @Override
    public Void visit(WhileLoopNode node) {
        int loopStart = currentChunk.code.size();
        node.getCondition().accept(this);
        int exitJump = emitJump(OpCode.JMP_FALSE, node.getLine());
        node.getBody().accept(this);
        emitLoop(loopStart, node.getLine());
        patchJump(exitJump);
        return null;
    }

    @Override
    public Void visit(ForLoopNode node) {
        scopeDepth++;
        if (node.getInitialization() != null) {
            node.getInitialization().accept(this);
        }
        
        int loopStart = currentChunk.code.size();
        int exitJump = -1;
        if (node.getCondition() != null) {
            node.getCondition().accept(this);
            exitJump = emitJump(OpCode.JMP_FALSE, node.getLine());
        }
        
        node.getBody().accept(this);
        if (node.getUpdate() != null) {
            node.getUpdate().accept(this);
            currentChunk.emit(OpCode.POP, node.getLine());
        }
        
        emitLoop(loopStart, node.getLine());
        if (exitJump != -1) patchJump(exitJump);
        scopeDepth--;
        return null;
    }
    
    @Override
    public Void visit(AssignmentNode node) {
        ExpressionNode target = node.getTarget();
        if (target instanceof VariableNode) {
            node.getValue().accept(this);
            String name = ((VariableNode) target).getName();
            int index = resolveLocal(name);
            if (index == -1) throw new RuntimeException("Undefined variable: " + name);
            currentChunk.emit(OpCode.DUP, node.getLine());
            currentChunk.emit(OpCode.STORE_LOCAL, node.getLine());
            currentChunk.emitByte(index, node.getLine());
        } else if (target instanceof FieldAccessNode fieldAccess) {
            fieldAccess.getObject().accept(this);
            node.getValue().accept(this);
            SrValue nameVal = new SrValue(fieldAccess.getFieldName());
            int nameIdx = currentChunk.addConstant(nameVal);
            currentChunk.emit(OpCode.SET_FIELD, node.getLine());
            currentChunk.emitByte(nameIdx, node.getLine());
        } else if (target instanceof ArrayAccessNode arrayAccess) {
            arrayAccess.getArray().accept(this);
            arrayAccess.getIndex().accept(this);
            node.getValue().accept(this);
            currentChunk.emit(OpCode.SET_ARRAY, node.getLine());
        }
        return null;
    }

    @Override
    public Void visit(BinaryExpressionNode node) {
        if (node.getOperator().equals("&&")) {
            node.getLeft().accept(this);
            currentChunk.emit(OpCode.DUP, node.getLine());
            int endJump = emitJump(OpCode.JMP_FALSE, node.getLine());
            currentChunk.emit(OpCode.POP, node.getLine());
            node.getRight().accept(this);
            patchJump(endJump);
            return null;
        }

        if (node.getOperator().equals("||")) {
            node.getLeft().accept(this);
            currentChunk.emit(OpCode.DUP, node.getLine());
            int evalBJump = emitJump(OpCode.JMP_FALSE, node.getLine());
            int endJump = emitJump(OpCode.JMP, node.getLine());
            patchJump(evalBJump);
            currentChunk.emit(OpCode.POP, node.getLine());
            node.getRight().accept(this);
            patchJump(endJump);
            return null;
        }

        node.getLeft().accept(this);
        node.getRight().accept(this);

        switch (node.getOperator()) {
            case "+" -> currentChunk.emit(OpCode.ADD, node.getLine());
            case "-" -> currentChunk.emit(OpCode.SUB, node.getLine());
            case "*" -> currentChunk.emit(OpCode.MUL, node.getLine());
            case "/" -> currentChunk.emit(OpCode.DIV, node.getLine());
            case "%" -> currentChunk.emit(OpCode.MOD, node.getLine());
            case "==" -> currentChunk.emit(OpCode.EQ, node.getLine());
            case "!=" -> currentChunk.emit(OpCode.NEQ, node.getLine());
            case "<" -> currentChunk.emit(OpCode.LT, node.getLine());
            case ">" -> currentChunk.emit(OpCode.GT, node.getLine());
            case "<=" -> currentChunk.emit(OpCode.LTE, node.getLine());
            case ">=" -> currentChunk.emit(OpCode.GTE, node.getLine());
            case "&" -> currentChunk.emit(OpCode.BIT_AND, node.getLine());
            case "|" -> currentChunk.emit(OpCode.BIT_OR, node.getLine());
        }
        return null;
    }

    @Override
    public Void visit(UnaryExpressionNode node) {
        if (node.getOperator().equals("++") || node.getOperator().equals("--")) {
            if (node.getOperand() instanceof VariableNode varNode) {
                int idx = resolveLocal(varNode.getName());
                if (idx == -1) throw new RuntimeException("Undefined var");
                currentChunk.emit(OpCode.LOAD_LOCAL, node.getLine());
                currentChunk.emitByte(idx, node.getLine());
                int oneIdx = currentChunk.addConstant(new SrValue(BigInteger.ONE));
                currentChunk.emit(OpCode.LOAD_CONST, node.getLine());
                currentChunk.emitByte(oneIdx, node.getLine());
                currentChunk.emit(node.getOperator().equals("++") ? OpCode.ADD : OpCode.SUB, node.getLine());
                currentChunk.emit(OpCode.DUP, node.getLine());
                currentChunk.emit(OpCode.STORE_LOCAL, node.getLine());
                currentChunk.emitByte(idx, node.getLine());
                return null;
            } else {
                throw new RuntimeException("Target of ++/-- must be a variable");
            }
        }

        node.getOperand().accept(this);
        switch (node.getOperator()) {
            case "-" -> {
                SrValue minusOne = new SrValue(BigInteger.valueOf(-1));
                int idx = currentChunk.addConstant(minusOne);
                currentChunk.emit(OpCode.LOAD_CONST, node.getLine());
                currentChunk.emitByte(idx, node.getLine());
                currentChunk.emit(OpCode.MUL, node.getLine());
            }
            case "!", "~" -> currentChunk.emit(OpCode.NOT, node.getLine());
        }
        return null;
    }

    @Override
    public Void visit(VariableNode node) {
        int index = resolveLocal(node.getName());
        if (index == -1) throw new RuntimeException("Undefined variable: " + node.getName());
        currentChunk.emit(OpCode.LOAD_LOCAL, node.getLine());
        currentChunk.emitByte(index, node.getLine());
        return null;
    }

    @Override
    public Void visit(FunctionCallNode node) {
        String name = node.getFunctionName();

        switch (name) {
            case "print" -> {
                for (ExpressionNode arg : node.getArguments()) arg.accept(this);
                currentChunk.emit(OpCode.PRINT, node.getLine());
                int vIdx = currentChunk.addConstant(SrValue.VOID);
                currentChunk.emit(OpCode.LOAD_CONST, node.getLine());
                currentChunk.emitByte(vIdx, node.getLine());
                return null;
            }
            case "println" -> {
                for (ExpressionNode arg : node.getArguments()) arg.accept(this);
                currentChunk.emit(OpCode.PRINTLN, node.getLine());
                int vIdx = currentChunk.addConstant(SrValue.VOID);
                currentChunk.emit(OpCode.LOAD_CONST, node.getLine());
                currentChunk.emitByte(vIdx, node.getLine());
                return null;
            }
            case "len" -> {
                node.getArguments().get(0).accept(this);
                currentChunk.emit(OpCode.LEN, node.getLine());
                return null;
            }
            case "sqrt" -> {
                node.getArguments().get(0).accept(this);
                currentChunk.emit(OpCode.SQRT, node.getLine());
                return null;
            }
            case "to_int" -> {
                node.getArguments().get(0).accept(this);
                currentChunk.emit(OpCode.TO_INT, node.getLine());
                return null;
            }
            case "random" -> {
                if (node.getArguments().size() != 2) {
                    throw new RuntimeException("random() expects 2 arguments: min and max");
                }
                node.getArguments().get(0).accept(this);
                node.getArguments().get(1).accept(this);
                currentChunk.emit(OpCode.RANDOM, node.getLine());
                return null;
            }
        }

        for (ExpressionNode arg : node.getArguments()) {
            arg.accept(this);
        }

        SrValue nameVal = new SrValue(name);
        int idx = currentChunk.addConstant(nameVal);

        currentChunk.emit(OpCode.CALL, node.getLine());
        currentChunk.emitByte(idx, node.getLine());
        currentChunk.emitByte(node.getArguments().size(), node.getLine());

        return null;
    }

    @Override
    public Void visit(ArrayAccessNode node) {
        node.getArray().accept(this);
        node.getIndex().accept(this);
        currentChunk.emit(OpCode.GET_ARRAY, node.getLine());
        return null;
    }

    @Override
    public Void visit(FieldAccessNode node) {
        node.getObject().accept(this);
        SrValue nameVal = new SrValue(node.getFieldName());
        int idx = currentChunk.addConstant(nameVal);
        currentChunk.emit(OpCode.GET_FIELD, node.getLine());
        currentChunk.emitByte(idx, node.getLine());
        return null;
    }
    
    @Override
    public Void visit(IntegerLiteralNode node) {
        int idx = currentChunk.addConstant(new SrValue(BigInteger.valueOf(node.getValue())));
        currentChunk.emit(OpCode.LOAD_CONST, node.getLine());
        currentChunk.emitByte(idx, node.getLine());
        return null;
    }

    @Override
    public Void visit(FloatLiteralNode node) {
        int idx = currentChunk.addConstant(new SrValue(node.getValue()));
        currentChunk.emit(OpCode.LOAD_CONST, node.getLine());
        currentChunk.emitByte(idx, node.getLine());
        return null;
    }

    @Override
    public Void visit(BooleanLiteralNode node) {
        currentChunk.emit(node.getValue() ? OpCode.LOAD_TRUE : OpCode.LOAD_FALSE, node.getLine());
        return null;
    }

    @Override
    public Void visit(StringLiteralNode node) {
        int idx = currentChunk.addConstant(new SrValue(node.getValue()));
        currentChunk.emit(OpCode.LOAD_CONST, node.getLine());
        currentChunk.emitByte(idx, node.getLine());
        return null;
    }

    @Override
    public Void visit(ArrayLiteralNode node) {
        List<ExpressionNode> elements = node.getElements();
        int size = elements.size();
        int sizeIdx = currentChunk.addConstant(new SrValue(BigInteger.valueOf(size)));
        currentChunk.emit(OpCode.LOAD_CONST, node.getLine());
        currentChunk.emitByte(sizeIdx, node.getLine());
        currentChunk.emit(OpCode.NEW_ARRAY, node.getLine());
        
        for (int i = 0; i < size; i++) {
            currentChunk.emit(OpCode.DUP, node.getLine());
            int idxConst = currentChunk.addConstant(new SrValue(BigInteger.valueOf(i)));
            currentChunk.emit(OpCode.LOAD_CONST, node.getLine());
            currentChunk.emitByte(idxConst, node.getLine());
            elements.get(i).accept(this);
            currentChunk.emit(OpCode.SET_ARRAY, node.getLine());
            currentChunk.emit(OpCode.POP, node.getLine());
        }
        
        return null;
    }
    
    @Override public Void visit(BasicTypeNode node) { return null; }
    @Override public Void visit(ArrayTypeNode node) { return null; }
    @Override public Void visit(StructTypeNode node) { return null; }
    
    private void addLocal(String name) {
        locals.add(new Local(name, scopeDepth));
    }

    private int resolveLocal(String name) {
        for (int i = locals.size() - 1; i >= 0; i--) {
            if (locals.get(i).name.equals(name)) return i;
        }
        return -1;
    }

    private void emitDefaultValue(TypeNode type, int line) {
        if (type instanceof BasicTypeNode basic) {
            switch (basic.getTypeName()) {
                case "int" -> {
                    int idx = currentChunk.addConstant(new SrValue(BigInteger.ZERO));
                    currentChunk.emit(OpCode.LOAD_CONST, line);
                    currentChunk.emitByte(idx, line);
                }
                case "float" -> {
                    int idx = currentChunk.addConstant(new SrValue(0.0));
                    currentChunk.emit(OpCode.LOAD_CONST, line);
                    currentChunk.emitByte(idx, line);
                }
                case "bool" -> currentChunk.emit(OpCode.LOAD_FALSE, line);
                case "string" -> {
                    int idx = currentChunk.addConstant(new SrValue(""));
                    currentChunk.emit(OpCode.LOAD_CONST, line);
                    currentChunk.emitByte(idx, line);
                }
            }
        } else if (type instanceof StructTypeNode st) {
            int idx = currentChunk.addConstant(new SrValue(st.getStructName()));
            currentChunk.emit(OpCode.NEW_STRUCT, line);
            currentChunk.emitByte(idx, line);
        } else if (type instanceof ArrayTypeNode arr) {
            if (arr.isFixedSize()) {
                int idx = currentChunk.addConstant(new SrValue(BigInteger.valueOf(arr.getFixedSize())));
                currentChunk.emit(OpCode.LOAD_CONST, line);
                currentChunk.emitByte(idx, line);
                currentChunk.emit(OpCode.NEW_ARRAY, line);
            } else if (arr.isDynamicSize()) {
                arr.getSizeExpression().accept(this);
                currentChunk.emit(OpCode.NEW_ARRAY, line);
            }
        }
    }

    private int emitJump(OpCode instruction, int line) {
        currentChunk.emit(instruction, line);
        currentChunk.emitByte(0xff, line);
        currentChunk.emitByte(0xff, line);
        return currentChunk.code.size() - 2;
    }

    private void patchJump(int offsetIndex) {
        int jump = currentChunk.code.size() - (offsetIndex + 2);
        if (jump > 32767) throw new RuntimeException("Jump too large");
        currentChunk.code.set(offsetIndex, (byte) ((jump >> 8) & 0xFF));
        currentChunk.code.set(offsetIndex + 1, (byte) (jump & 0xFF));
    }

    private void emitLoop(int loopStart, int line) {
        currentChunk.emit(OpCode.JMP, line);
        int ipAfterJump = currentChunk.code.size() + 2;
        int offset = loopStart - ipAfterJump;
        currentChunk.emitByte((offset >> 8) & 0xFF, line);
        currentChunk.emitByte(offset & 0xFF, line);
    }
}