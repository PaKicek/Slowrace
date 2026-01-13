package org.pakicek.compiler;

import org.pakicek.runtime.ProgramImage;
import org.pakicek.runtime.bytecode.Chunk;
import org.pakicek.runtime.vm.SrValue;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BytecodeIO {
    private static final String MAGIC = "SLOW";
    private static final int VERSION = 1;

    // --- WRITING ---

    public static void write(ProgramImage program, String filename) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename))) {
            // Header
            out.writeBytes(MAGIC);
            out.writeInt(VERSION);

            // Functions
            out.writeInt(program.functions.size());
            for (Map.Entry<String, Chunk> entry : program.functions.entrySet()) {
                out.writeUTF(entry.getKey()); // Function Name
                writeChunk(out, entry.getValue());
            }

            // Main Chunk
            writeChunk(out, program.mainChunk);
        }
    }

    private static void writeChunk(DataOutputStream out, Chunk chunk) throws IOException {
        // 1. Constants
        out.writeInt(chunk.constants.size());
        for (SrValue val : chunk.constants) {
            writeConstant(out, val);
        }

        // 2. Code
        out.writeInt(chunk.code.size());
        for (Byte b : chunk.code) {
            out.writeByte(b);
        }

        // 3. Lines (Debug Info)
        // Since lines list size matches code size, we don't need to write size again explicitly,
        // but for robustness we write it (or assume it matches code size).
        for (Integer line : chunk.lines) {
            out.writeInt(line);
        }
    }

    private static void writeConstant(DataOutputStream out, SrValue val) throws IOException {
        // Tag byte: 0=INT, 1=FLOAT, 2=BOOL, 3=STRING, 4=VOID, 5=OBJECT(error?)
        switch (val.type) {
            case INT -> {
                out.writeByte(0);
                byte[] bytes = val.asInt().toByteArray();
                out.writeInt(bytes.length);
                out.write(bytes);
            }
            case FLOAT -> {
                out.writeByte(1);
                out.writeDouble(val.asFloat());
            }
            case BOOL -> {
                out.writeByte(2);
                out.writeBoolean(val.asBool());
            }
            case STRING -> {
                out.writeByte(3);
                out.writeUTF(val.asString());
            }
            case VOID -> out.writeByte(4);
            default -> throw new IOException("Cannot serialize runtime object in constant pool: " + val.type);
        }
    }

    // --- READING ---

    public static ProgramImage read(String filename) throws IOException {
        try (DataInputStream in = new DataInputStream(new FileInputStream(filename))) {
            // Header Check
            byte[] magicBytes = new byte[4];
            in.readFully(magicBytes);
            String magic = new String(magicBytes, StandardCharsets.US_ASCII);
            if (!MAGIC.equals(magic)) {
                throw new IOException("Invalid file format. Expected SLOW, got " + magic);
            }

            int version = in.readInt();
            if (version != VERSION) {
                throw new IOException("Unsupported version: " + version);
            }

            // Functions
            Map<String, Chunk> functions = new HashMap<>();
            int funcCount = in.readInt();
            for (int i = 0; i < funcCount; i++) {
                String name = in.readUTF();
                Chunk chunk = readChunk(in);
                functions.put(name, chunk);
            }

            // Main Chunk
            Chunk mainChunk = readChunk(in);

            return new ProgramImage(mainChunk, functions);
        }
    }

    private static Chunk readChunk(DataInputStream in) throws IOException {
        Chunk chunk = new Chunk();

        // 1. Constants
        int constCount = in.readInt();
        for (int i = 0; i < constCount; i++) {
            chunk.addConstant(readConstant(in));
        }

        // 2. Code
        int codeSize = in.readInt();
        for (int i = 0; i < codeSize; i++) {
            byte b = in.readByte();
            chunk.code.add(b);
        }

        // 3. Lines
        // We read exactly 'codeSize' integers for line info
        for (int i = 0; i < codeSize; i++) {
            int line = in.readInt();
            chunk.lines.add(line);
        }

        return chunk;
    }

    private static SrValue readConstant(DataInputStream in) throws IOException {
        byte tag = in.readByte();
        switch (tag) {
            case 0 -> { // INT
                int len = in.readInt();
                byte[] bytes = new byte[len];
                in.readFully(bytes);
                return new SrValue(new BigInteger(bytes));
            }
            case 1 -> { // FLOAT
                return new SrValue(in.readDouble());
            }
            case 2 -> { // BOOL
                return new SrValue(in.readBoolean());
            }
            case 3 -> { // STRING
                return new SrValue(in.readUTF());
            }
            case 4 -> { // VOID
                return SrValue.VOID;
            }
            default -> throw new IOException("Unknown constant tag: " + tag);
        }
    }
}