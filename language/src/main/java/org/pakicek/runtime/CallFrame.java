package org.pakicek.runtime;

import org.pakicek.runtime.bytecode.Chunk;
import org.pakicek.runtime.vm.SrValue;

public class CallFrame {
    public final Chunk chunk;
    public int ip = 0;
    public final int stackOffset;
    public final SrValue[] locals = new SrValue[256];

    public CallFrame(Chunk chunk, int stackOffset) {
        this.chunk = chunk;
        this.stackOffset = stackOffset;
    }
}