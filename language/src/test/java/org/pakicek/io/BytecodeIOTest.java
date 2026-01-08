package org.pakicek.io;

import org.junit.Test;
import org.pakicek.vm.ProgramImage;
import org.pakicek.vm.bytecode.Chunk;
import org.pakicek.vm.bytecode.OpCode;
import org.pakicek.vm.runtime.SrValue;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class BytecodeIOTest {

    @Test
    public void testSaveAndLoad() throws IOException {
        // 1. Create a dummy ProgramImage manually
        Chunk mainChunk = new Chunk();
        // LOAD_CONST 42, PRINT, HALT
        int c1 = mainChunk.addConstant(new SrValue(BigInteger.valueOf(42)));
        mainChunk.emit(OpCode.LOAD_CONST, 1);
        mainChunk.emitByte(c1, 1);
        mainChunk.emit(OpCode.PRINT, 1);
        mainChunk.emit(OpCode.HALT, 1);

        Chunk funcChunk = new Chunk();
        // Just RETURN
        funcChunk.emit(OpCode.RETURN, 2);

        Map<String, Chunk> funcs = new HashMap<>();
        funcs.put("myFunc", funcChunk);

        ProgramImage original = new ProgramImage(mainChunk, funcs);

        // 2. Write to temp file
        File tempFile = File.createTempFile("test_program", ".srbyte");
        BytecodeIO.write(original, tempFile.getAbsolutePath());

        // 3. Read back
        ProgramImage loaded = BytecodeIO.read(tempFile.getAbsolutePath());

        // 4. Verify Main Chunk
        assertEquals(original.mainChunk.code.size(), loaded.mainChunk.code.size());
        assertEquals(original.mainChunk.constants.size(), loaded.mainChunk.constants.size());

        // Verify constant 42
        SrValue valOriginal = original.mainChunk.constants.get(0);
        SrValue valLoaded = loaded.mainChunk.constants.get(0);
        assertEquals(valOriginal.asInt(), valLoaded.asInt());

        // Verify opcodes
        for(int i=0; i<original.mainChunk.code.size(); i++) {
            assertEquals(original.mainChunk.code.get(i), loaded.mainChunk.code.get(i));
        }

        // 5. Verify Functions
        assertEquals(1, loaded.functions.size());
        assertTrue(loaded.functions.containsKey("myFunc"));
        assertEquals(1, loaded.functions.get("myFunc").code.size()); // RETURN is 1 byte opcode

        // Cleanup
        assertTrue("Could not delete temp file", tempFile.delete());
    }
}