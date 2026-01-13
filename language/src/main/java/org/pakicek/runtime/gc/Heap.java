package org.pakicek.runtime.gc;

import org.pakicek.runtime.vm.*;
import java.util.ArrayList;
import java.util.List;

public class Heap {
    private final List<SrObject> objects = new ArrayList<>();
    private static final int GC_THRESHOLD = 1000; // Trigger GC every 1000 allocations
    private int allocationCount = 0;

    public void register(SrObject obj) {
        objects.add(obj);
        allocationCount++;
    }

    public boolean shouldCollect() {
        return allocationCount >= GC_THRESHOLD;
    }

    public void sweep() {
        // Remove everything NOT marked
        objects.removeIf(obj -> {
            if (!obj.isMarked) {
                return true; // Delete
            }
            obj.isMarked = false; // Reset mark for next cycle
            return false;
        });
        allocationCount = 0;
    }
}