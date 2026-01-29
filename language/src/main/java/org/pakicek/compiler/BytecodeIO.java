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

    public static void write(ProgramImage program, String filename) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename))) {
            out.writeBytes(MAGIC);
            out.writeInt(VERSION);
            out.writeInt(program.functions.size());
            for (Map.Entry<String, Chunk> entry : program.functions.entrySet()) {
                out.writeUTF(entry.getKey());
                writeChunk(out, entry.getValue());
            }
            writeChunk(out, program.mainChunk);
        }
    }

    private static void writeChunk(DataOutputStream out, Chunk chunk) throws IOException {
        out.writeInt(chunk.constants.size());
        for (SrValue val : chunk.constants) {
            writeConstant(out, val);
        }
        out.writeInt(chunk.code.size());
        for (Byte b : chunk.code) {
            out.writeByte(b);
        }
        for (Integer line : chunk.lines) {
            out.writeInt(line);
        }
    }

    private static void writeConstant(DataOutputStream out, SrValue val) throws IOException {
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

    public static ProgramImage read(String filename) throws IOException {
        try (DataInputStream in = new DataInputStream(new FileInputStream(filename))) {
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
            Map<String, Chunk> functions = new HashMap<>();
            int funcCount = in.readInt();
            for (int i = 0; i < funcCount; i++) {
                String name = in.readUTF();
                Chunk chunk = readChunk(in);
                functions.put(name, chunk);
            }
            Chunk mainChunk = readChunk(in);
            return new ProgramImage(mainChunk, functions);
        }
    }

    private static Chunk readChunk(DataInputStream in) throws IOException {
        Chunk chunk = new Chunk();
        int constCount = in.readInt();
        for (int i = 0; i < constCount; i++) {
            chunk.addConstant(readConstant(in));
        }
        int codeSize = in.readInt();
        for (int i = 0; i < codeSize; i++) {
            byte b = in.readByte();
            chunk.code.add(b);
        }
        for (int i = 0; i < codeSize; i++) {
            int line = in.readInt();
            chunk.lines.add(line);
        }
        return chunk;
    }

    private static SrValue readConstant(DataInputStream in) throws IOException {
        byte tag = in.readByte();
        switch (tag) {
            case 0 -> {
                int len = in.readInt();
                byte[] bytes = new byte[len];
                in.readFully(bytes);
                return new SrValue(new BigInteger(bytes));
            }
            case 1 -> {
                return new SrValue(in.readDouble());
            }
            case 2 -> {
                return new SrValue(in.readBoolean());
            }
            case 3 -> {
                return new SrValue(in.readUTF());
            }
            case 4 -> {
                return SrValue.VOID;
            }
            default -> throw new IOException("Unknown constant tag: " + tag);
        }
    }
}