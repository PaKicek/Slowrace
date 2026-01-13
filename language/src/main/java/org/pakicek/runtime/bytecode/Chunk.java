package org.pakicek.runtime.bytecode;

import org.pakicek.runtime.vm.SrValue;
import java.util.ArrayList;
import java.util.List;

public class Chunk {
    public final List<Byte> code = new ArrayList<>();
    public final List<SrValue> constants = new ArrayList<>();
    // For debugging: maps instruction index to source code line
    public final List<Integer> lines = new ArrayList<>();

    public void emit(OpCode op, int line) {
        code.add((byte) op.ordinal());
        lines.add(line);
    }

    // For opcode arguments (e.g. constant index or jump offset)
    public void emitByte(int b, int line) {
        code.add((byte) b);
        lines.add(line);
    }

    // Adds a constant and returns its index
    public int addConstant(SrValue value) {
        constants.add(value);
        return constants.size() - 1;
    }
}