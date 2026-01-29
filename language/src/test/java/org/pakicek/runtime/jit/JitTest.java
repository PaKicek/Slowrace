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
        Chunk chunk = new Chunk();
        int idx1 = chunk.addConstant(new SrValue(BigInteger.valueOf(5)));
        int idx2 = chunk.addConstant(new SrValue(BigInteger.valueOf(10)));

        chunk.emit(OpCode.LOAD_CONST, 1); chunk.emitByte(idx1, 1);
        chunk.emit(OpCode.LOAD_CONST, 1); chunk.emitByte(idx2, 1);
        chunk.emit(OpCode.ADD, 1);
        chunk.emit(OpCode.HALT, 1);

        JitOptimizer jit = new JitOptimizer();
        Chunk optimized = jit.optimize(chunk);
        assertEquals(3, optimized.code.size());
        assertEquals(OpCode.LOAD_CONST, OpCode.values()[optimized.code.get(0)]);

        int resIdx = optimized.code.get(1);
        assertEquals(BigInteger.valueOf(15), optimized.constants.get(resIdx).asInt());
    }

    @Test
    public void testIdentityAddZero() {
        Chunk chunk = new Chunk();
        int idx0 = chunk.addConstant(new SrValue(BigInteger.ZERO));

        chunk.emit(OpCode.LOAD_LOCAL, 1); chunk.emitByte(0, 1);
        chunk.emit(OpCode.LOAD_CONST, 1); chunk.emitByte(idx0, 1);
        chunk.emit(OpCode.ADD, 1);
        chunk.emit(OpCode.HALT, 1);

        JitOptimizer jit = new JitOptimizer();
        Chunk optimized = jit.optimize(chunk);
        assertEquals(3, optimized.code.size());
        assertEquals(OpCode.LOAD_LOCAL, OpCode.values()[optimized.code.get(0)]);
        assertEquals(OpCode.HALT, OpCode.values()[optimized.code.get(2)]);
    }

    @Test
    public void testIdentityMulOne() {
        Chunk chunk = new Chunk();
        int idx1 = chunk.addConstant(new SrValue(BigInteger.ONE));

        chunk.emit(OpCode.LOAD_LOCAL, 1); chunk.emitByte(0, 1);
        chunk.emit(OpCode.LOAD_CONST, 1); chunk.emitByte(idx1, 1);
        chunk.emit(OpCode.MUL, 1);
        chunk.emit(OpCode.HALT, 1);

        JitOptimizer jit = new JitOptimizer();
        Chunk optimized = jit.optimize(chunk);
        assertEquals(3, optimized.code.size());
        assertEquals(OpCode.LOAD_LOCAL, OpCode.values()[optimized.code.get(0)]);
    }

    @Test
    public void testZeroMultiplication() {
        Chunk chunk = new Chunk();
        int idx0 = chunk.addConstant(new SrValue(BigInteger.ZERO));

        chunk.emit(OpCode.LOAD_LOCAL, 1); chunk.emitByte(0, 1);
        chunk.emit(OpCode.LOAD_CONST, 1); chunk.emitByte(idx0, 1);
        chunk.emit(OpCode.MUL, 1);
        chunk.emit(OpCode.HALT, 1);

        JitOptimizer jit = new JitOptimizer();
        Chunk optimized = jit.optimize(chunk);

        assertEquals(6, optimized.code.size());
        assertEquals(OpCode.POP, OpCode.values()[optimized.code.get(2)]);
        assertEquals(OpCode.LOAD_CONST, OpCode.values()[optimized.code.get(3)]);
    }

    @Test
    public void testDeadCodeElimination() {
        Chunk chunk = new Chunk();
        int idx = chunk.addConstant(new SrValue(BigInteger.valueOf(5)));

        chunk.emit(OpCode.RETURN, 1);
        chunk.emit(OpCode.LOAD_CONST, 1);
        chunk.emitByte(idx, 1);
        chunk.emit(OpCode.ADD, 1);
        chunk.emit(OpCode.HALT, 1);

        JitOptimizer jit = new JitOptimizer();
        Chunk optimized = jit.optimize(chunk);

        assertEquals(1, optimized.code.size());
        assertEquals(OpCode.RETURN, OpCode.values()[optimized.code.get(0)]);
    }
}