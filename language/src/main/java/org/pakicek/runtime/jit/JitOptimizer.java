package org.pakicek.runtime.jit;

import org.pakicek.runtime.bytecode.Chunk;
import org.pakicek.runtime.bytecode.OpCode;
import org.pakicek.runtime.vm.SrValue;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class JitOptimizer {

    public Chunk optimize(Chunk original) {
        Chunk folded = optimizePass(original);
        return deadCodeElimination(folded);
    }

    private Chunk optimizePass(Chunk original) {
        Chunk optimized = new Chunk();
        optimized.constants.addAll(original.constants);
        List<Byte> code = original.code;

        int i = 0;
        while (i < code.size()) {
            int opIndex = code.get(i) & 0xFF;
            if (opIndex >= OpCode.values().length) {
                throw new RuntimeException("Invalid OpCode " + opIndex + " at index " + i);
            }
            OpCode op = OpCode.values()[opIndex];

            if (tryConstantFolding(code, i, original, optimized)) {
                i += 5;
                continue;
            }

            if (tryArithmeticIdentities(code, i, original, optimized)) {
                i += 3;
                continue;
            }

            copyInstruction(code, i, original, optimized, op);
            i += 1 + getOpcodeArity(op);
        }
        return optimized;
    }

    private boolean tryConstantFolding(List<Byte> code, int i, Chunk original, Chunk optimized) {
        if (i + 4 >= code.size()) return false;

        int op1Idx = code.get(i) & 0xFF;
        int op2Idx = code.get(i + 2) & 0xFF;
        int mathOpIdx = code.get(i + 4) & 0xFF;

        if (op1Idx >= OpCode.values().length || op2Idx >= OpCode.values().length || mathOpIdx >= OpCode.values().length) return false;

        OpCode op1 = OpCode.values()[op1Idx];
        OpCode op2 = OpCode.values()[op2Idx];
        OpCode mathOp = OpCode.values()[mathOpIdx];

        if (op1 == OpCode.LOAD_CONST && op2 == OpCode.LOAD_CONST && isMathOp(mathOp)) {
            int idx1 = code.get(i + 1) & 0xFF;
            int idx2 = code.get(i + 3) & 0xFF;

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
        if (i + 2 >= code.size()) return false;

        int op1Idx = code.get(i) & 0xFF;
        if (op1Idx >= OpCode.values().length) return false;
        OpCode op1 = OpCode.values()[op1Idx];

        if (op1 != OpCode.LOAD_CONST) return false;

        int mathOpIdx = code.get(i + 2) & 0xFF;
        if (mathOpIdx >= OpCode.values().length) return false;
        OpCode mathOp = OpCode.values()[mathOpIdx];

        int constIdx = code.get(i + 1) & 0xFF;
        SrValue val = original.constants.get(constIdx);

        if (val.type != SrValue.Type.INT) return false;

        BigInteger num = val.asInt();

        if (mathOp == OpCode.ADD && num.equals(BigInteger.ZERO)) return true;
        if (mathOp == OpCode.SUB && num.equals(BigInteger.ZERO)) return true;
        if (mathOp == OpCode.MUL && num.equals(BigInteger.ONE)) return true;
        if (mathOp == OpCode.DIV && num.equals(BigInteger.ONE)) return true;

        if (mathOp == OpCode.MUL && num.equals(BigInteger.ZERO)) {
            optimized.emit(OpCode.POP, 0);
            optimized.emit(OpCode.LOAD_CONST, 0);
            optimized.emitByte(constIdx, 0);
            return true;
        }

        return false;
    }

    private Chunk deadCodeElimination(Chunk input) {
        List<Byte> oldCode = input.code;
        int codeSize = oldCode.size();
        boolean[] reachable = new boolean[codeSize];
        int[] oldToNewAddress = new int[codeSize + 1];
        Arrays.fill(oldToNewAddress, -1);

        Queue<Integer> queue = new LinkedList<>();
        queue.add(0);
        reachable[0] = true;

        while (!queue.isEmpty()) {
            int ip = queue.poll();
            if (ip >= codeSize) continue;

            int opByte = oldCode.get(ip) & 0xFF;
            OpCode op = OpCode.values()[opByte];
            int nextIp = ip + 1 + getOpcodeArity(op);

            if (op != OpCode.JMP && op != OpCode.RETURN && op != OpCode.HALT) {
                if (nextIp < codeSize && !reachable[nextIp]) {
                    reachable[nextIp] = true;
                    queue.add(nextIp);
                }
            }

            if (op == OpCode.JMP || op == OpCode.JMP_FALSE) {
                int b1 = oldCode.get(ip + 1) & 0xFF;
                int b2 = oldCode.get(ip + 2) & 0xFF;
                short offset = (short) ((b1 << 8) | b2);
                int jumpTarget = ip + 3 + offset;

                if (jumpTarget >= 0 && jumpTarget < codeSize && !reachable[jumpTarget]) {
                    reachable[jumpTarget] = true;
                    queue.add(jumpTarget);
                }
            }
        }

        Chunk cleaned = new Chunk();
        cleanConstants(input, cleaned);

        for (int i = 0; i < codeSize; ) {
            int opByte = oldCode.get(i) & 0xFF;
            OpCode op = OpCode.values()[opByte];
            int len = 1 + getOpcodeArity(op);
            if (reachable[i]) {
                oldToNewAddress[i] = cleaned.code.size();
                copyInstruction(oldCode, i, input, cleaned, op);
            }
            i += len;
        }
        oldToNewAddress[codeSize] = cleaned.code.size();

        cleaned = new Chunk();
        cleaned.constants.addAll(input.constants);

        for (int i = 0; i < codeSize; ) {
            int opByte = oldCode.get(i) & 0xFF;
            OpCode op = OpCode.values()[opByte];
            int len = 1 + getOpcodeArity(op);

            if (reachable[i]) {
                if (op == OpCode.JMP || op == OpCode.JMP_FALSE) {
                    int b1 = oldCode.get(i + 1) & 0xFF;
                    int b2 = oldCode.get(i + 2) & 0xFF;
                    short oldOffset = (short) ((b1 << 8) | b2);
                    int oldTarget = i + 3 + oldOffset;
                    int newTarget = oldToNewAddress[oldTarget];
                    int newCurrentIp = cleaned.code.size();
                    int newOffset = newTarget - (newCurrentIp + 3);
                    cleaned.emit(op, 0);
                    cleaned.emitByte((newOffset >> 8) & 0xFF, 0);
                    cleaned.emitByte(newOffset & 0xFF, 0);
                } else {
                    copyInstruction(oldCode, i, input, cleaned, op);
                }
            }
            i += len;
        }

        return cleaned;
    }

    private void cleanConstants(Chunk input, Chunk output) {
        output.constants.addAll(input.constants);
    }

    private BigInteger calculate(BigInteger a, BigInteger b, OpCode op) {
        return switch (op) {
            case ADD -> a.add(b);
            case SUB -> a.subtract(b);
            case MUL -> a.multiply(b);
            default -> null;
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
            case LOAD_CONST, LOAD_LOCAL, STORE_LOCAL, NEW_STRUCT, GET_FIELD, SET_FIELD -> 1;
            case CALL, JMP, JMP_FALSE -> 2;
            default -> 0;
        };
    }
}