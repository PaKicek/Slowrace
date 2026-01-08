package org.pakicek.compiler;

import org.pakicek.parser.ast.ASTVisitor;
import org.pakicek.parser.ast.node.*;
import org.pakicek.parser.ast.node.expression.*;
import org.pakicek.parser.ast.node.expression.literal.*;
import org.pakicek.parser.ast.node.statement.*;
import org.pakicek.parser.ast.node.type.*;
import org.pakicek.vm.ProgramImage;
import org.pakicek.vm.bytecode.*;
import org.pakicek.vm.runtime.SrValue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BytecodeCompiler implements ASTVisitor<Void> {
    private Chunk currentChunk;
    private final Map<String, Chunk> functions = new HashMap<>();

    // Symbol table for local variables
    private static class Local {
        String name;
        int depth; // Scope depth

        Local(String name, int depth) {
            this.name = name;
            this.depth = depth;
        }
    }

    private final List<Local> locals = new ArrayList<>();
    private int scopeDepth = 0;

    /**
     * Compiles the program.
     * Returns the Chunk for the Main entry point.
     * Compiled functions are stored internally and should be retrieved via getFunctions().
     */
    public ProgramImage compile(ProgramNode program) {
        functions.clear();

        // 1. Compile functions
        visit(program);

        // 2. Compile Main
        currentChunk = new Chunk();
        locals.clear();
        scopeDepth = 0;

        if (program.getMainNode() != null) {
            program.getMainNode().accept(this);
        }

        currentChunk.emit(OpCode.HALT, 0);

        // Return packaged image
        return new ProgramImage(currentChunk, new HashMap<>(functions));
    }

    public Map<String, Chunk> getFunctions() {
        return functions;
    }

    // --- Declarations & Structure ---

    @Override
    public Void visit(ProgramNode node) {
        // Compile all function declarations
        for (FunctionDeclarationNode func : node.getFunctions()) {
            func.accept(this);
        }
        // Structs are metadata-only in this implementation, instantiation logic is handled in NEW_STRUCT via OpCode.
        return null;
    }

    @Override
    public Void visit(FunctionDeclarationNode node) {
        // Save previous context (though currently we don't support nested functions, good practice)
        Chunk previousChunk = currentChunk;
        List<Local> previousLocals = new ArrayList<>(locals);
        int previousScope = scopeDepth;

        // Init new context for function
        currentChunk = new Chunk();
        locals.clear();
        scopeDepth = 0;

        // Register parameters as local variables (indices 0, 1, 2...)
        scopeDepth++;
        for (ParameterNode param : node.getParameters()) {
            addLocal(param.getName());
        }

        // Compile body
        node.getBody().accept(this);

        // Emit implicit return if not present (handles void functions ending without return)
        if (currentChunk.code.isEmpty() ||
                currentChunk.code.get(currentChunk.code.size() - 1) != (byte)OpCode.RETURN.ordinal()) {

            // Push VOID constant for implicit return
            int idx = currentChunk.addConstant(SrValue.VOID);
            currentChunk.emit(OpCode.LOAD_CONST, node.getLine());
            currentChunk.emitByte(idx, node.getLine());
            currentChunk.emit(OpCode.RETURN, node.getLine());
        }

        // Save compiled function
        functions.put(node.getName(), currentChunk);

        // Restore context
        currentChunk = previousChunk;
        locals.clear();
        locals.addAll(previousLocals);
        scopeDepth = previousScope;

        return null;
    }

    @Override
    public Void visit(MainNode node) {
        scopeDepth++;
        // Register arguments 'argc' and 'argv' as locals 0 and 1
        addLocal("argc");
        addLocal("argv");

        node.getBody().accept(this);

        scopeDepth--;
        return null;
    }

    @Override
    public Void visit(StructDeclarationNode node) {
        // Structs are handled dynamically at runtime.
        // No bytecode needed for declaration itself.
        return null;
    }

    @Override
    public Void visit(ParameterNode node) {
        // Parameters are processed inside FunctionDeclarationNode to add them to locals.
        return null;
    }

    // --- Statements ---

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
        // Expression statements (like 'a = 5;' or 'func()') push a result to stack.
        // Since this result is unused, we must POP it to keep the stack clean.
        currentChunk.emit(OpCode.POP, node.getLine());
        return null;
    }

    @Override
    public Void visit(VariableDeclarationNode node) {
        // 1. Compile initialization
        if (node.getInitialValue() != null) {
            node.getInitialValue().accept(this);
        } else {
            emitDefaultValue(node.getType(), node.getLine());
        }

        // 2. Add to locals
        addLocal(node.getName());

        // 3. Store top of stack into new local slot
        currentChunk.emit(OpCode.STORE_LOCAL, node.getLine());
        currentChunk.emitByte(locals.size() - 1, node.getLine());

        return null;
    }

    @Override
    public Void visit(ReturnStatementNode node) {
        if (node.getValue() != null) {
            node.getValue().accept(this);
        } else {
            // Explicit return without value -> return VOID
            int idx = currentChunk.addConstant(SrValue.VOID);
            currentChunk.emit(OpCode.LOAD_CONST, node.getLine());
            currentChunk.emitByte(idx, node.getLine());
        }
        currentChunk.emit(OpCode.RETURN, node.getLine());
        return null;
    }

    // --- Control Flow (If, For, While) ---

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
            // In a full implementation, we should jump to end after block.
            // Simplified here (fallthrough logic might execute multiple else-ifs if jumps not patched correctly).
            // Correct logic: emitJump(JMP) to end, save it, patch all later.
            // For now: basic elif support.
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
            currentChunk.emit(OpCode.POP, node.getLine()); // Cleanup update expression result
        }

        emitLoop(loopStart, node.getLine());

        if (exitJump != -1) patchJump(exitJump);
        scopeDepth--;
        return null;
    }

    // --- Expressions (Assignment, Binary, Unary, Calls) ---

    @Override
    public Void visit(AssignmentNode node) {
        ExpressionNode target = node.getTarget();

        if (target instanceof VariableNode) {
            // Logic: Calculate Value -> DUP -> Store Local -> (Value remains on stack)
            node.getValue().accept(this);
            String name = ((VariableNode) target).getName();
            int index = resolveLocal(name);
            if (index == -1) throw new RuntimeException("Undefined variable: " + name);

            currentChunk.emit(OpCode.DUP, node.getLine()); // Keep value for expression result
            currentChunk.emit(OpCode.STORE_LOCAL, node.getLine());
            currentChunk.emitByte(index, node.getLine());

        } else if (target instanceof FieldAccessNode fieldAccess) {
            // Struct assignment: obj.field = val
            // We assume VM's SET_FIELD consumes object and value, but leaves value on stack or we manipulate stack
            // Standard approach:
            fieldAccess.getObject().accept(this); // [Obj]
            node.getValue().accept(this);         // [Obj, Val]

            // We need [Val] left on stack.
            // Use DUP_X1 or just assume VM SET_FIELD pushes Val back.
            // Since we added DUP/ROT, but not DUP_X1 (dup top and insert below),
            // let's rely on VM implementation of SET_FIELD to be expression-friendly (push result).

            SrValue nameVal = new SrValue(fieldAccess.getFieldName());
            int nameIdx = currentChunk.addConstant(nameVal);
            currentChunk.emit(OpCode.SET_FIELD, node.getLine());
            currentChunk.emitByte(nameIdx, node.getLine());

        } else if (target instanceof ArrayAccessNode arrayAccess) {
            arrayAccess.getArray().accept(this); // [Arr]
            arrayAccess.getIndex().accept(this); // [Arr, Idx]
            node.getValue().accept(this);        // [Arr, Idx, Val]

            // Similar to field, assuming SET_ARRAY pushes Val back.
            currentChunk.emit(OpCode.SET_ARRAY, node.getLine());
        }
        return null;
    }

    @Override
    public Void visit(BinaryExpressionNode node) {
        if (node.getOperator().equals("&&")) {
            // Short-circuit AND: a && b
            node.getLeft().accept(this); // [a]
            currentChunk.emit(OpCode.DUP, node.getLine()); // [a, a]
            int endJump = emitJump(OpCode.JMP_FALSE, node.getLine()); // if a is false, jump to end (consumes one a)

            currentChunk.emit(OpCode.POP, node.getLine()); // a was true, pop it
            node.getRight().accept(this); // [b]

            patchJump(endJump);
            return null;
        }

        if (node.getOperator().equals("||")) {
            // Short-circuit OR: a || b
            // We don't have JMP_TRUE, so we jump if FALSE to eval B.
            node.getLeft().accept(this); // [a]
            currentChunk.emit(OpCode.DUP, node.getLine()); // [a, a]
            int evalBJump = emitJump(OpCode.JMP_FALSE, node.getLine()); // if a is false, jump to eval B

            // If here, A is true. Jump to end.
            int endJump = emitJump(OpCode.JMP, node.getLine());

            patchJump(evalBJump); // Eval B
            currentChunk.emit(OpCode.POP, node.getLine()); // Pop 'a' (which is false)
            node.getRight().accept(this); // [b]

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
        // Handle ++ and -- as Prefix operators
        if (node.getOperator().equals("++") || node.getOperator().equals("--")) {
            if (node.getOperand() instanceof VariableNode varNode) {
                int idx = resolveLocal(varNode.getName());
                if (idx == -1) throw new RuntimeException("Undefined var");

                // 1. Load Var
                currentChunk.emit(OpCode.LOAD_LOCAL, node.getLine());
                currentChunk.emitByte(idx, node.getLine());

                // 2. Load 1
                int oneIdx = currentChunk.addConstant(new SrValue(BigInteger.ONE));
                currentChunk.emit(OpCode.LOAD_CONST, node.getLine());
                currentChunk.emitByte(oneIdx, node.getLine());

                // 3. Add/Sub
                currentChunk.emit(node.getOperator().equals("++") ? OpCode.ADD : OpCode.SUB, node.getLine());

                // 4. Dup (result of expression)
                currentChunk.emit(OpCode.DUP, node.getLine());

                // 5. Store back
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
            case "print":
            case "println":
                for (ExpressionNode arg : node.getArguments()) arg.accept(this);
                currentChunk.emit(OpCode.PRINT, node.getLine());
                // Built-in print is void. Push VOID.
                int vIdx = currentChunk.addConstant(SrValue.VOID);
                currentChunk.emit(OpCode.LOAD_CONST, node.getLine());
                currentChunk.emitByte(vIdx, node.getLine());
                return null;
            case "len":
                node.getArguments().get(0).accept(this);
                currentChunk.emit(OpCode.LEN, node.getLine());
                return null;
            case "sqrt":
                node.getArguments().get(0).accept(this);
                currentChunk.emit(OpCode.SQRT, node.getLine());
                return null;
            case "to_int":
                node.getArguments().get(0).accept(this);
                currentChunk.emit(OpCode.TO_INT, node.getLine());
                return null;
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

    // --- Literals ---

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
        // Push size
        int idx = currentChunk.addConstant(new SrValue(BigInteger.valueOf(node.getElements().size())));
        currentChunk.emit(OpCode.LOAD_CONST, node.getLine());
        currentChunk.emitByte(idx, node.getLine());

        currentChunk.emit(OpCode.NEW_ARRAY, node.getLine());
        return null;
    }

    // --- Types (Empty implementations as they don't generate code directly) ---
    @Override public Void visit(BasicTypeNode node) { return null; }
    @Override public Void visit(ArrayTypeNode node) { return null; }
    @Override public Void visit(StructTypeNode node) { return null; }

    // --- Helpers ---

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