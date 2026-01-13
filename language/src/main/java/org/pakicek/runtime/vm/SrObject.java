package org.pakicek.runtime.vm;

public abstract class SrObject {
    public boolean isMarked = false; // For Mark-and-Sweep

    // Returns object size for memory accounting (optional)
    public abstract int getSize();
}