package org.pakicek.runtime.vm;

public abstract class SrObject {
    public boolean isMarked = false;

    public abstract int getSize();
}