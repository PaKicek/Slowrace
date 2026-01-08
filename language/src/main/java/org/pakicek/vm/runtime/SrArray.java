package org.pakicek.vm.runtime;

public class SrArray extends SrObject {
    public final SrValue[] elements;

    public SrArray(int size) {
        this.elements = new SrValue[size];
        // Initialize with default values (important for GC to avoid nulls)
        for(int i=0; i<size; i++) elements[i] = new SrValue(java.math.BigInteger.ZERO);
    }

    @Override
    public int getSize() { return elements.length; }
}