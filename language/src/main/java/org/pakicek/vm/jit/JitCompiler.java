package org.pakicek.vm.jit;

import org.pakicek.vm.bytecode.Chunk;
import org.pakicek.vm.bytecode.OpCode;
import org.pakicek.vm.runtime.SrValue;
import java.math.BigInteger;
import java.util.List;

public class JitCompiler {

    /**
     * Runs optimization passes on the chunk.
     * Currently supports:
     * 1. Constant Folding (e.g., 2 + 3 -> 5)
     * 2. Arithmetic Identities (e.g., x + 0 -> x)
     */
    public Chunk optimize(Chunk original) {
        // Pass 1: Constant Folding & Identities
        return optimizePass(original);
    }

    private Chunk optimizePass(Chunk original) {
        Chunk optimized = new Chunk();
        // Copy constants so indices remain valid
        optimized.constants.addAll(original.constants);

        List<Byte> code = original.code;

        for (int i = 0; i < code.size(); i++) {
            byte opByte = code.get(i);
            OpCode op = OpCode.values()[opByte];

            // --- Optimization 1: Constant Folding ---
            // Pattern: [LOAD_CONST A] [LOAD_CONST B] [OP]
            if (tryConstantFolding(code, i, original, optimized)) {
                i += 4; // Skip the sequence (2 ops + 2 args)
                continue;
            }

            // --- Optimization 2: Arithmetic Identities ---
            // Pattern: [LOAD_CONST 0/1] [OP] (where OP is commutative like ADD/MUL)
            // Note: This only handles "x [OP] const". For "const [OP] x" stack order is different.
            // Stack top is Right operand. So "x + 0": Stack [x, 0].
            // Sequence: ... code for x ..., LOAD_CONST 0, ADD.
            if (tryArithmeticIdentities(code, i, original, optimized)) {
                i += 2; // Skip LOAD_CONST + arg. Current op (ADD/MUL) is also skipped by logic.
                continue;
            }

            // No optimization applied, copy instruction
            copyInstruction(code, i, original, optimized, op);

            // Advance index by arity to skip arguments we just copied
            i += getOpcodeArity(op);
        }
        return optimized;
    }

    private boolean tryConstantFolding(List<Byte> code, int i, Chunk original, Chunk optimized) {
        if (i + 4 >= code.size()) return false;

        OpCode op1 = OpCode.values()[code.get(i)];
        OpCode op2 = OpCode.values()[code.get(i + 2)];
        OpCode mathOp = OpCode.values()[code.get(i + 4)];

        if (op1 == OpCode.LOAD_CONST && op2 == OpCode.LOAD_CONST && isMathOp(mathOp)) {
            int idx1 = code.get(i + 1);
            int idx2 = code.get(i + 3);

            SrValue v1 = original.constants.get(idx1);
            SrValue v2 = original.constants.get(idx2);

            if (v1.type == SrValue.Type.INT && v2.type == SrValue.Type.INT) {
                BigInteger res = calculate(v1.asInt(), v2.asInt(), mathOp);
                if (res != null) {
                    int newIdx = optimized.addConstant(new SrValue(res));
                    optimized.emit(OpCode.LOAD_CONST, 0);
                    optimized.emitByte(newIdx, 0);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryArithmeticIdentities(List<Byte> code, int i, Chunk original, Chunk optimized) {
        // Check for pattern: LOAD_CONST X, ADD/MUL/SUB/DIV
        // Current index 'i' points to LOAD_CONST
        if (i + 2 >= code.size()) return false;

        OpCode op1 = OpCode.values()[code.get(i)];
        if (op1 != OpCode.LOAD_CONST) return false;

        OpCode mathOp = OpCode.values()[code.get(i + 2)];
        int constIdx = code.get(i + 1);
        SrValue val = original.constants.get(constIdx);

        if (val.type != SrValue.Type.INT) return false; // Float identities skipped for simplicity (precision)

        BigInteger num = val.asInt();

        // Case: x + 0 -> x
        if (mathOp == OpCode.ADD && num.equals(BigInteger.ZERO)) {
            // Optimization: Do nothing! (Skip LOAD_CONST 0 and ADD)
            // Just don't emit anything. The value 'x' is already on stack.
            return true;
        }

        // Case: x - 0 -> x
        if (mathOp == OpCode.SUB && num.equals(BigInteger.ZERO)) {
            return true;
        }

        // Case: x * 1 -> x
        if (mathOp == OpCode.MUL && num.equals(BigInteger.ONE)) {
            return true;
        }

        // Case: x / 1 -> x
        if (mathOp == OpCode.DIV && num.equals(BigInteger.ONE)) {
            return true;
        }

        // Case: x * 0 -> 0
        if (mathOp == OpCode.MUL && num.equals(BigInteger.ZERO)) {
            // Optimization: Replace "LOAD 0, MUL" with "POP, LOAD 0".
            // Stack: [x, 0] -> MUL -> [0].
            // Optimized: [x] -> POP -> [] -> LOAD 0 -> [0].
            optimized.emit(OpCode.POP, 0);
            optimized.emit(OpCode.LOAD_CONST, 0);
            optimized.emitByte(constIdx, 0); // Reuse index of 0
            return true;
        }

        return false;
    }

    private BigInteger calculate(BigInteger a, BigInteger b, OpCode op) {
        return switch (op) {
            case ADD -> a.add(b);
            case SUB -> a.subtract(b);
            case MUL -> a.multiply(b);
            default -> null; // DIV/MOD ignored for safety (div by zero check needed)
        };
    }

    private void copyInstruction(List<Byte> code, int i, Chunk original, Chunk optimized, OpCode op) {
        optimized.code.add(code.get(i));
        int line = (i < original.lines.size()) ? original.lines.get(i) : 0;
        optimized.lines.add(line);

        int arity = getOpcodeArity(op);
        for (int k = 0; k < arity; k++) {
            if (i + 1 + k < code.size()) {
                optimized.code.add(code.get(i + 1 + k));
                optimized.lines.add(line);
            }
        }
    }

    private boolean isMathOp(OpCode op) {
        return op == OpCode.ADD || op == OpCode.SUB || op == OpCode.MUL;
    }

    private int getOpcodeArity(OpCode op) {
        return switch (op) {
            case LOAD_CONST, LOAD_LOCAL, STORE_LOCAL, JMP, JMP_FALSE, NEW_STRUCT, GET_FIELD, SET_FIELD -> 1;
            case CALL -> 2;
            default -> 0;
        };
    }
}