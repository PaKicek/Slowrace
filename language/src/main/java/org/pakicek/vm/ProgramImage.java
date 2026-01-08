package org.pakicek.vm;

import org.pakicek.vm.bytecode.Chunk;
import java.util.HashMap;
import java.util.Map;

public class ProgramImage {
    public final Chunk mainChunk;
    public final Map<String, Chunk> functions;

    public ProgramImage(Chunk mainChunk, Map<String, Chunk> functions) {
        this.mainChunk = mainChunk;
        this.functions = functions != null ? functions : new HashMap<>();
    }
}