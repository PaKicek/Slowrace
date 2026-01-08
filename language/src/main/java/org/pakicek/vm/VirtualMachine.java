package org.pakicek.vm;

import org.pakicek.vm.bytecode.*;
import org.pakicek.vm.gc.*;
import org.pakicek.vm.jit.JitCompiler;
import org.pakicek.vm.runtime.*;

import java.math.BigInteger;
import java.util.*;

public class VirtualMachine {
    private final Stack<SrValue> stack = new Stack<>();
    private final Stack<CallFrame> frames = new Stack<>();
    private final Heap heap = new Heap();
    private final GarbageCollector gc;
    private final JitCompiler jit = new JitCompiler();

    // Global registry for functions (Name -> Bytecode Chunk)
    // In a real scenario, this would be populated before running main
    private final Map<String, Chunk> functions = new HashMap<>();

    // Call counter for JIT (hot spots detection)
    private final Map<Chunk, Integer> callCounts = new HashMap<>();

    public VirtualMachine() {
        this.gc = new GarbageCollector(heap, this);
    }

    public void run(ProgramImage image) {
        // 1. Register all functions
        this.functions.clear();
        this.functions.putAll(image.functions);

        // 2. Run main chunk
        run(image.mainChunk);
    }

    public void run(Chunk entryChunk) {
        // Initialize the first frame (main)
        frames.push(new CallFrame(entryChunk, 0));

        while (!frames.isEmpty()) {
            CallFrame frame = frames.peek();

            // Check if we reached the end of the bytecode in this frame
            if (frame.ip >= frame.chunk.code.size()) {
                frames.pop();
                continue;
            }

            // Fetch instruction
            byte opByte = frame.chunk.code.get(frame.ip++);
            OpCode op = OpCode.values()[opByte];

            // Execute instruction
            switch (op) {
                // --- Stack & Constants ---
                case LOAD_CONST -> {
                    int idx = frame.chunk.code.get(frame.ip++);
                    stack.push(frame.chunk.constants.get(idx));
                }
                case LOAD_TRUE -> stack.push(new SrValue(true));
                case LOAD_FALSE -> stack.push(new SrValue(false));

                // --- Variables ---
                case LOAD_LOCAL -> {
                    int slot = frame.chunk.code.get(frame.ip++);
                    stack.push(frame.locals[slot]);
                }
                case STORE_LOCAL -> {
                    int slot = frame.chunk.code.get(frame.ip++);
                    frame.locals[slot] = stack.pop();
                }
                case POP -> stack.pop();

                case DUP -> // Duplicates the top item
                        stack.push(stack.peek());

                case ROT -> {
                    // Swaps the top two items: [A, B] -> [B, A]
                    SrValue b = stack.pop();
                    SrValue a = stack.pop();
                    stack.push(b);
                    stack.push(a);
                }

                // --- Arithmetic ---
                case ADD -> binaryOp(BigInteger::add, Double::sum);
                case SUB -> binaryOp(BigInteger::subtract, (a, b) -> a - b);
                case MUL -> binaryOp(BigInteger::multiply, (a, b) -> a * b);
                case DIV -> binaryOp(BigInteger::divide, (a, b) -> a / b);
                case MOD -> {
                    SrValue b = stack.pop();
                    SrValue a = stack.pop();
                    if (a.type == SrValue.Type.INT && b.type == SrValue.Type.INT) {
                        stack.push(new SrValue(a.asInt().remainder(b.asInt())));
                    } else {
                        stack.push(new SrValue(a.asFloat() % b.asFloat()));
                    }
                }

                // --- Bitwise & Logic ---
                case BIT_AND -> {
                    SrValue b = stack.pop();
                    SrValue a = stack.pop();
                    stack.push(new SrValue(a.asInt().and(b.asInt())));
                }
                case BIT_OR -> {
                    SrValue b = stack.pop();
                    SrValue a = stack.pop();
                    stack.push(new SrValue(a.asInt().or(b.asInt())));
                }
                case LOGIC_AND -> {
                    boolean b = stack.pop().asBool();
                    boolean a = stack.pop().asBool();
                    stack.push(new SrValue(a && b));
                }
                case LOGIC_OR -> {
                    boolean b = stack.pop().asBool();
                    boolean a = stack.pop().asBool();
                    stack.push(new SrValue(a || b));
                }
                case NOT -> {
                    SrValue val = stack.pop();
                    if (val.type == SrValue.Type.BOOL) {
                        stack.push(new SrValue(!val.asBool()));
                    } else if (val.type == SrValue.Type.INT) {
                        stack.push(new SrValue(val.asInt().not()));
                    } else {
                        throw new RuntimeException("Type error: NOT applied to " + val.type);
                    }
                }

                // --- Comparisons ---
                case EQ -> {
                    SrValue b = stack.pop();
                    SrValue a = stack.pop();
                    // Simplified: relying on object equals or BigInt equals
                    if (a.type == SrValue.Type.INT && b.type == SrValue.Type.INT) {
                        stack.push(new SrValue(a.asInt().equals(b.asInt())));
                    } else {
                        stack.push(new SrValue(Objects.equals(a.asString(), b.asString()))); // Fallback
                    }
                }
                case NEQ -> {
                    SrValue b = stack.pop();
                    SrValue a = stack.pop();
                    stack.push(new SrValue(!a.asInt().equals(b.asInt()))); // Simplified logic
                }
                case GT -> compareOp((i) -> i > 0);
                case LT -> compareOp((i) -> i < 0);
                case GTE -> compareOp((i) -> i >= 0);
                case LTE -> compareOp((i) -> i <= 0);

                // --- Control Flow ---
                case JMP -> {
                    byte b1 = frame.chunk.code.get(frame.ip++);
                    byte b2 = frame.chunk.code.get(frame.ip++);
                    short offset = (short) ((b1 << 8) | (b2 & 0xFF));
                    frame.ip += offset;
                }
                case JMP_FALSE -> {
                    byte b1 = frame.chunk.code.get(frame.ip++);
                    byte b2 = frame.chunk.code.get(frame.ip++);
                    short offset = (short) ((b1 << 8) | (b2 & 0xFF));
                    if (!stack.pop().asBool()) {
                        frame.ip += offset;
                    }
                }

                // --- Structs & Arrays ---
                case NEW_ARRAY -> {
                    // Stack: [size]
                    int size = stack.pop().asInt().intValue();
                    allocate(new SrArray(size));
                }
                case GET_ARRAY -> {
                    // Stack: [Array, Index] -> Pushes [Element]
                    SrValue index = stack.pop();
                    SrValue arrVal = stack.pop();
                    if (arrVal.type != SrValue.Type.OBJECT || !(arrVal.asObject() instanceof SrArray array)) {
                        throw new RuntimeException("Type Error: Expected Array");
                    }
                    int idx = index.asInt().intValue();
                    if (idx < 0 || idx >= array.elements.length) {
                        throw new RuntimeException("Index Error: Array index out of bounds");
                    }
                    stack.push(array.elements[idx]);
                }
                case SET_ARRAY -> {
                    // Stack: [Array, Index, Value]
                    SrValue val = stack.pop();
                    SrValue index = stack.pop();
                    SrValue arrVal = stack.pop();
                    SrArray array = (SrArray) arrVal.asObject();
                    int idx = index.asInt().intValue();
                    if (idx < 0 || idx >= array.elements.length) {
                        throw new RuntimeException("Index Error");
                    }
                    array.elements[idx] = val;
                }
                case LEN -> {
                    SrValue val = stack.pop();
                    if (val.type == SrValue.Type.STRING) {
                        stack.push(new SrValue(BigInteger.valueOf(val.asString().length())));
                    } else if (val.type == SrValue.Type.OBJECT && val.asObject() != null) {
                        stack.push(new SrValue(BigInteger.valueOf(val.asObject().getSize())));
                    } else {
                        throw new RuntimeException("Type Error: len() argument");
                    }
                }

                case NEW_STRUCT -> {
                    // Stack: [] -> Pushes [Struct]
                    // Byte operand: name index
                    int nameIdx = frame.chunk.code.get(frame.ip++);
                    String structName = frame.chunk.constants.get(nameIdx).asString();
                    allocate(new SrStruct(structName));
                }
                case SET_FIELD -> {
                    // Stack: [Object, Value] (Value is on top)
                    // Byte operand: field name index
                    int fieldNameIdx = frame.chunk.code.get(frame.ip++);
                    String fieldName = frame.chunk.constants.get(fieldNameIdx).asString();

                    SrValue val = stack.pop();
                    SrValue objVal = stack.pop();

                    if (objVal.asObject() instanceof SrStruct struct) {
                        struct.fields.put(fieldName, val);
                    } else {
                        throw new RuntimeException("Type Error: Setting field on non-struct");
                    }
                }
                case GET_FIELD -> {
                    // Stack: [Object]
                    // Byte operand: field name index
                    int fieldNameIdx = frame.chunk.code.get(frame.ip++);
                    String fieldName = frame.chunk.constants.get(fieldNameIdx).asString();

                    SrValue objVal = stack.pop();
                    if (objVal.asObject() instanceof SrStruct struct) {
                        SrValue fieldVal = struct.fields.getOrDefault(fieldName, SrValue.VOID);
                        stack.push(fieldVal);
                    } else {
                        throw new RuntimeException("Type Error: Getting field from non-struct");
                    }
                }

                // --- Built-ins ---
                case PRINT -> System.out.println(stack.pop());
                case SQRT -> {
                    SrValue val = stack.pop();
                    double res = Math.sqrt(val.asFloat());
                    stack.push(new SrValue(res));
                }
                case TO_INT -> {
                    SrValue val = stack.pop();
                    try {
                        stack.push(new SrValue(new BigInteger(val.asString())));
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Type Error: Cannot convert to int");
                    }
                }

                // --- Functions ---
                case CALL -> {
                    // Operand 1: Function name index
                    int nameIdx = frame.chunk.code.get(frame.ip++);
                    String funcName = frame.chunk.constants.get(nameIdx).asString();

                    // Operand 2: Argument count
                    int argCount = frame.chunk.code.get(frame.ip++);

                    Chunk funcChunk = functions.get(funcName);
                    if (funcChunk == null) {
                        throw new RuntimeException("Runtime Error: Function " + funcName + " not found");
                    }

                    // JIT: Hot spot detection
                    int calls = callCounts.getOrDefault(funcChunk, 0) + 1;
                    callCounts.put(funcChunk, calls);
                    if (calls > 10) {
                        // Simple JIT: Optimize chunk
                        // Note: In production this should cache the optimized chunk
                        funcChunk = jit.optimize(funcChunk);
                    }

                    // Create new frame
                    CallFrame nextFrame = new CallFrame(funcChunk, stack.size() - argCount);

                    // Pop args from stack into locals (reverse order)
                    for (int i = argCount - 1; i >= 0; i--) {
                        nextFrame.locals[i] = stack.pop();
                    }

                    frames.push(nextFrame);
                }
                case RETURN -> {
                    SrValue result = stack.isEmpty() ? SrValue.VOID : stack.pop();
                    frames.pop();

                    // If not the last frame, push result to the caller's stack
                    if (!frames.isEmpty()) {
                        stack.push(result);
                    }
                }
                case HALT -> {
                    frames.clear();
                    return;
                }
            }
        }
    }

    // Helper for binary arithmetic operations
    private interface BigIntOp { BigInteger apply(BigInteger a, BigInteger b); }
    private interface DoubleOp { double apply(double a, double b); }

    private void binaryOp(BigIntOp intOp, DoubleOp doubleOp) {
        SrValue b = stack.pop();
        SrValue a = stack.pop();
        if (a.type == SrValue.Type.INT && b.type == SrValue.Type.INT) {
            stack.push(new SrValue(intOp.apply(a.asInt(), b.asInt())));
        } else {
            double da = (a.type == SrValue.Type.INT) ? a.asInt().doubleValue() : a.asFloat();
            double db = (b.type == SrValue.Type.INT) ? b.asInt().doubleValue() : b.asFloat();
            stack.push(new SrValue(doubleOp.apply(da, db)));
        }
    }

    // Helper for comparison operations
    private interface CompOp { boolean check(int comparisonResult); }

    private void compareOp(CompOp op) {
        SrValue b = stack.pop();
        SrValue a = stack.pop();
        int res;
        if (a.type == SrValue.Type.INT && b.type == SrValue.Type.INT) {
            res = a.asInt().compareTo(b.asInt());
        } else {
            double da = (a.type == SrValue.Type.INT) ? a.asInt().doubleValue() : a.asFloat();
            double db = (b.type == SrValue.Type.INT) ? b.asInt().doubleValue() : b.asFloat();
            res = Double.compare(da, db);
        }
        stack.push(new SrValue(op.check(res)));
    }

    private void allocate(SrObject obj) {
        if (heap.shouldCollect()) {
            gc.collect();
        }
        heap.register(obj);
        stack.push(new SrValue(obj));
    }

    // API for GC
    public List<SrValue> getStackRoots() {
        // Returns entire stack + local variables of all frames
        List<SrValue> roots = new ArrayList<>(stack);
        for(CallFrame f : frames) {
            for(SrValue v : f.locals) if(v != null) roots.add(v);
        }
        return roots;
    }
}