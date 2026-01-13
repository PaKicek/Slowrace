package org.pakicek.runtime.jit;

import org.junit.Test;
import org.pakicek.runtime.bytecode.Chunk;
import org.pakicek.runtime.bytecode.OpCode;
import org.pakicek.runtime.vm.SrValue;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class JitTest {

    @Test
    public void testConstantFolding() {
        // 5 + 10 -> 15
        Chunk chunk = new Chunk();
        int idx1 = chunk.addConstant(new SrValue(BigInteger.valueOf(5)));
        int idx2 = chunk.addConstant(new SrValue(BigInteger.valueOf(10)));

        chunk.emit(OpCode.LOAD_CONST, 1); chunk.emitByte(idx1, 1);
        chunk.emit(OpCode.LOAD_CONST, 1); chunk.emitByte(idx2, 1);
        chunk.emit(OpCode.ADD, 1);
        chunk.emit(OpCode.HALT, 1);

        JitCompiler jit = new JitCompiler();
        Chunk optimized = jit.optimize(chunk);

        // Expected: LOAD_CONST 15, HALT (3 bytes: 1+1 + 1)
        assertEquals(3, optimized.code.size());
        assertEquals(OpCode.LOAD_CONST, OpCode.values()[optimized.code.get(0)]);

        int resIdx = optimized.code.get(1);
        assertEquals(BigInteger.valueOf(15), optimized.constants.get(resIdx).asInt());
    }

    @Test
    public void testIdentityAddZero() {
        // x + 0 -> x
        // Setup: LOAD_LOCAL 0, LOAD_CONST 0, ADD
        Chunk chunk = new Chunk();
        int idx0 = chunk.addConstant(new SrValue(BigInteger.ZERO));

        chunk.emit(OpCode.LOAD_LOCAL, 1); chunk.emitByte(0, 1); // x
        chunk.emit(OpCode.LOAD_CONST, 1); chunk.emitByte(idx0, 1); // 0
        chunk.emit(OpCode.ADD, 1);
        chunk.emit(OpCode.HALT, 1);

        JitCompiler jit = new JitCompiler();
        Chunk optimized = jit.optimize(chunk);

        // Expected: LOAD_LOCAL 0, HALT (skip LOAD 0 and ADD)
        // Size: 2 (LOAD_LOCAL) + 1 (HALT) = 3 bytes
        assertEquals(3, optimized.code.size());
        assertEquals(OpCode.LOAD_LOCAL, OpCode.values()[optimized.code.get(0)]);
        assertEquals(OpCode.HALT, OpCode.values()[optimized.code.get(2)]);
    }

    @Test
    public void testIdentityMulOne() {
        // x * 1 -> x
        Chunk chunk = new Chunk();
        int idx1 = chunk.addConstant(new SrValue(BigInteger.ONE));

        chunk.emit(OpCode.LOAD_LOCAL, 1); chunk.emitByte(0, 1);
        chunk.emit(OpCode.LOAD_CONST, 1); chunk.emitByte(idx1, 1);
        chunk.emit(OpCode.MUL, 1);
        chunk.emit(OpCode.HALT, 1);

        JitCompiler jit = new JitCompiler();
        Chunk optimized = jit.optimize(chunk);

        assertEquals(3, optimized.code.size());
        assertEquals(OpCode.LOAD_LOCAL, OpCode.values()[optimized.code.get(0)]);
    }

    @Test
    public void testZeroMultiplication() {
        // x * 0 -> 0 (side effect free x)
        // Setup: LOAD_LOCAL 0, LOAD_CONST 0, MUL
        // Expected: LOAD_LOCAL 0, POP, LOAD_CONST 0
        Chunk chunk = new Chunk();
        int idx0 = chunk.addConstant(new SrValue(BigInteger.ZERO));

        chunk.emit(OpCode.LOAD_LOCAL, 1); chunk.emitByte(0, 1);
        chunk.emit(OpCode.LOAD_CONST, 1); chunk.emitByte(idx0, 1);
        chunk.emit(OpCode.MUL, 1);
        chunk.emit(OpCode.HALT, 1);

        JitCompiler jit = new JitCompiler();
        Chunk optimized = jit.optimize(chunk);

        // LOAD_LOCAL (2) + POP (1) + LOAD_CONST (2) + HALT (1) = 6 bytes
        assertEquals(6, optimized.code.size());
        assertEquals(OpCode.POP, OpCode.values()[optimized.code.get(2)]);
        assertEquals(OpCode.LOAD_CONST, OpCode.values()[optimized.code.get(3)]);
    }
}