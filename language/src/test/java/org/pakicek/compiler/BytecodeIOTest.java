package org.pakicek.compiler;

import org.junit.Test;
import org.pakicek.runtime.ProgramImage;
import org.pakicek.runtime.bytecode.Chunk;
import org.pakicek.runtime.bytecode.OpCode;
import org.pakicek.runtime.vm.SrValue;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class BytecodeIOTest {

    @Test
    public void testSaveAndLoad() throws IOException {
        Chunk mainChunk = new Chunk();
        int c1 = mainChunk.addConstant(new SrValue(BigInteger.valueOf(42)));
        mainChunk.emit(OpCode.LOAD_CONST, 1);
        mainChunk.emitByte(c1, 1);
        mainChunk.emit(OpCode.PRINT, 1);
        mainChunk.emit(OpCode.HALT, 1);

        Chunk funcChunk = new Chunk();
        funcChunk.emit(OpCode.RETURN, 2);
        Map<String, Chunk> funcs = new HashMap<>();
        funcs.put("myFunc", funcChunk);
        ProgramImage original = new ProgramImage(mainChunk, funcs);

        File tempFile = File.createTempFile("test_program", ".srbyte");
        BytecodeIO.write(original, tempFile.getAbsolutePath());
        ProgramImage loaded = BytecodeIO.read(tempFile.getAbsolutePath());

        assertEquals(original.mainChunk.code.size(), loaded.mainChunk.code.size());
        assertEquals(original.mainChunk.constants.size(), loaded.mainChunk.constants.size());

        SrValue valOriginal = original.mainChunk.constants.get(0);
        SrValue valLoaded = loaded.mainChunk.constants.get(0);
        assertEquals(valOriginal.asInt(), valLoaded.asInt());

        for(int i=0; i<original.mainChunk.code.size(); i++) {
            assertEquals(original.mainChunk.code.get(i), loaded.mainChunk.code.get(i));
        }

        assertEquals(1, loaded.functions.size());
        assertTrue(loaded.functions.containsKey("myFunc"));
        assertEquals(1, loaded.functions.get("myFunc").code.size());

        assertTrue("Could not delete temp file", tempFile.delete());
    }
}