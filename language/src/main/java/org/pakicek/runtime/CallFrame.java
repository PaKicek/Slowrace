package org.pakicek.runtime;

import org.pakicek.runtime.bytecode.Chunk;
import org.pakicek.runtime.vm.SrValue;

public class CallFrame {
    public final Chunk chunk;
    public int ip = 0; // Instruction Pointer
    public final int stackOffset; // Where this function's stack starts in global stack
    public final SrValue[] locals = new SrValue[256]; // Local variables

    public CallFrame(Chunk chunk, int stackOffset) {
        this.chunk = chunk;
        this.stackOffset = stackOffset;
    }
}