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
    private final Map<String, Chunk> functions = new HashMap<>();

    // Call counter for JIT (hot spots detection)
    private final Map<Chunk, Integer> callCounts = new HashMap<>();

    public VirtualMachine() {
        this.gc = new GarbageCollector(heap, this);
    }

    public void run(ProgramImage image, String[] args) {
        this.functions.clear();
        this.functions.putAll(image.functions);

        // Clear state
        frames.clear();
        stack.clear();

        // Preparing args for Main
        // 1. argc (int)
        int argc = args != null ? args.length : 0;
        SrValue argcVal = new SrValue(BigInteger.valueOf(argc));

        // 2. argv (array string)
        SrArray argvObj = new SrArray(argc);
        if (args != null) {
            for (int i = 0; i < argc; i++) {
                argvObj.elements[i] = new SrValue(args[i]);
            }
        }
        // Register array in heap for GC
        heap.register(argvObj);
        SrValue argvVal = new SrValue(argvObj);

        // Create main frame
        CallFrame mainFrame = new CallFrame(image.mainChunk, 0);

        // Initialize local variables (0=argc, 1=argv)
        mainFrame.locals[0] = argcVal;
        mainFrame.locals[1] = argvVal;

        frames.push(mainFrame);

        // Starting execution loop
        loop();
    }

    private void loop() {
        while (!frames.isEmpty()) {
            CallFrame frame = frames.peek();

            if (frame.ip >= frame.chunk.code.size()) {
                frames.pop();
                continue;
            }

            byte opByte = frame.chunk.code.get(frame.ip++);
            OpCode op = OpCode.values()[opByte];

            switch (op) {
                // --- Stack Operations ---
                case LOAD_CONST -> {
                    int idx = frame.chunk.code.get(frame.ip++);
                    stack.push(frame.chunk.constants.get(idx));
                }
                case LOAD_TRUE -> stack.push(new SrValue(true));
                case LOAD_FALSE -> stack.push(new SrValue(false));
                case POP -> {
                    if (!stack.isEmpty()) stack.pop();
                }
                case DUP -> stack.push(stack.peek());
                case ROT -> {
                    SrValue b = stack.pop();
                    SrValue a = stack.pop();
                    stack.push(b);
                    stack.push(a);
                }

                // --- Variables ---
                case LOAD_LOCAL -> {
                    int slot = frame.chunk.code.get(frame.ip++);
                    stack.push(frame.locals[slot]);
                }
                case STORE_LOCAL -> {
                    int slot = frame.chunk.code.get(frame.ip++);
                    frame.locals[slot] = stack.pop();
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

                // --- Logic & Bitwise ---
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

                // --- Comparison ---
                case EQ -> {
                    SrValue b = stack.pop();
                    SrValue a = stack.pop();
                    if (a.type == SrValue.Type.INT && b.type == SrValue.Type.INT) {
                        stack.push(new SrValue(a.asInt().equals(b.asInt())));
                    } else {
                        stack.push(new SrValue(Objects.equals(a.asString(), b.asString())));
                    }
                }
                case NEQ -> {
                    SrValue b = stack.pop();
                    SrValue a = stack.pop();
                    if (a.type == SrValue.Type.INT && b.type == SrValue.Type.INT) {
                        stack.push(new SrValue(!a.asInt().equals(b.asInt())));
                    } else {
                        stack.push(new SrValue(!Objects.equals(a.asString(), b.asString())));
                    }
                }
                case GT -> compareOp((i) -> i > 0);
                case LT -> compareOp((i) -> i < 0);
                case GTE -> compareOp((i) -> i >= 0);
                case LTE -> compareOp((i) -> i <= 0);

                // --- Control Flow (2-byte jumps) ---
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

                // --- Heap (Arrays & Structs) ---
                case NEW_ARRAY -> {
                    int size = stack.pop().asInt().intValue();
                    allocate(new SrArray(size));
                }
                case GET_ARRAY -> {
                    SrValue index = stack.pop();
                    SrValue arrVal = stack.pop();
                    if (arrVal.type != SrValue.Type.OBJECT || !(arrVal.asObject() instanceof SrArray array)) {
                        throw new RuntimeException("Type Error: Expected Array");
                    }
                    int idx = index.asInt().intValue();
                    if (idx < 0 || idx >= array.elements.length) {
                        throw new RuntimeException("Index Error: " + idx);
                    }
                    stack.push(array.elements[idx]);
                }
                case SET_ARRAY -> {
                    SrValue val = stack.pop();
                    SrValue index = stack.pop();
                    SrValue arrVal = stack.pop();

                    if (arrVal.type != SrValue.Type.OBJECT || !(arrVal.asObject() instanceof SrArray array)) {
                        throw new RuntimeException("Type Error: Expected Array");
                    }
                    int idx = index.asInt().intValue();

                    if (idx < 0 || idx >= array.elements.length) {
                        throw new RuntimeException("Index Error: " + idx);
                    }
                    array.elements[idx] = val;
                    // Fix: Return value for expression chaining
                    stack.push(val);
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
                    int nameIdx = frame.chunk.code.get(frame.ip++);
                    String structName = frame.chunk.constants.get(nameIdx).asString();
                    allocate(new SrStruct(structName));
                }
                case SET_FIELD -> {
                    int fieldNameIdx = frame.chunk.code.get(frame.ip++);
                    String fieldName = frame.chunk.constants.get(fieldNameIdx).asString();
                    SrValue val = stack.pop();
                    SrValue objVal = stack.pop();

                    if (objVal.asObject() instanceof SrStruct struct) {
                        struct.fields.put(fieldName, val);
                    } else {
                        throw new RuntimeException("Type Error: Setting field on non-struct");
                    }
                    // Fix: Return value for expression chaining
                    stack.push(val);
                }
                case GET_FIELD -> {
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
                case PRINT -> System.out.print(stack.pop());
                case PRINTLN -> System.out.println(stack.pop());
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
                        throw new RuntimeException("Type Error: Cannot convert to int: " + val);
                    }
                }

                // --- Functions ---
                case CALL -> {
                    int nameIdx = frame.chunk.code.get(frame.ip++);
                    String funcName = frame.chunk.constants.get(nameIdx).asString();
                    int argCount = frame.chunk.code.get(frame.ip++);

                    Chunk funcChunk = functions.get(funcName);
                    if (funcChunk == null) {
                        throw new RuntimeException("Runtime Error: Function " + funcName + " not found");
                    }

                    // JIT check
                    int calls = callCounts.getOrDefault(funcChunk, 0) + 1;
                    callCounts.put(funcChunk, calls);
                    if (calls > 10) {
                        // In real VM we would cache this
                        funcChunk = jit.optimize(funcChunk);
                    }

                    CallFrame nextFrame = new CallFrame(funcChunk, stack.size() - argCount);
                    // Pop args (reverse order)
                    for (int i = argCount - 1; i >= 0; i--) {
                        nextFrame.locals[i] = stack.pop();
                    }
                    frames.push(nextFrame);
                }
                case RETURN -> {
                    SrValue result = stack.isEmpty() ? SrValue.VOID : stack.pop();
                    frames.pop();
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

    // --- Helpers ---

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

    public List<SrValue> getStackRoots() {
        List<SrValue> roots = new ArrayList<>(stack);
        for(CallFrame f : frames) {
            for(SrValue v : f.locals) if(v != null) roots.add(v);
        }
        return roots;
    }
}