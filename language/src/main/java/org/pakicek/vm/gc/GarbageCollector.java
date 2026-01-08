package org.pakicek.vm.gc;

import org.pakicek.vm.VirtualMachine;
import org.pakicek.vm.runtime.*;

import java.util.Stack;

public class GarbageCollector {
    private final Heap heap;
    private final VirtualMachine vm;

    public GarbageCollector(Heap heap, VirtualMachine vm) {
        this.heap = heap;
        this.vm = vm;
    }

    public void collect() {
        mark();
        heap.sweep();
    }

    private void mark() {
        // Roots: Operand stack and Local variables in all frames
        Stack<SrObject> workList = new Stack<>();

        // Collect roots from VM
        for (SrValue val : vm.getStackRoots()) {
            if (val.type == SrValue.Type.OBJECT && val.asObject() != null) {
                workList.push(val.asObject());
            }
        }

        // Traverse graph
        while (!workList.isEmpty()) {
            SrObject obj = workList.pop();
            if (obj.isMarked) continue;

            obj.isMarked = true;

            // Add child objects to workList
            if (obj instanceof SrArray) {
                for (SrValue el : ((SrArray) obj).elements) {
                    if (el.type == SrValue.Type.OBJECT) workList.push(el.asObject());
                }
            } else if (obj instanceof SrStruct) {
                for (SrValue val : ((SrStruct) obj).fields.values()) {
                    if (val.type == SrValue.Type.OBJECT) workList.push(val.asObject());
                }
            }
        }
    }
}