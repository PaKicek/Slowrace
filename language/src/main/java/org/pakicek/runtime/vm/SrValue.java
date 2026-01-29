package org.pakicek.runtime.vm;

import java.math.BigInteger;

public class SrValue {
    public enum Type { INT, FLOAT, BOOL, STRING, OBJECT, VOID }

    public final Type type;
    private final Object value;

    public SrValue(BigInteger val) { type = Type.INT; value = val; }
    public SrValue(double val) { type = Type.FLOAT; value = val; }
    public SrValue(boolean val) { type = Type.BOOL; value = val; }
    public SrValue(String val) { type = Type.STRING; value = val; }
    public SrValue(SrObject val) { type = Type.OBJECT; value = val; }
    private SrValue() { type = Type.VOID; value = null; }

    public static final SrValue VOID = new SrValue();

    public BigInteger asInt() {
        return (BigInteger) value;
    }

    public double asFloat() {
        return value != null ? (Double) value : 0.0;
    }

    public boolean asBool() {
        return value != null ? (Boolean) value : false;
    }

    public String asString() {
        return value == null ? "void" : value.toString();
    }

    public SrObject asObject() {
        return (SrObject) value;
    }

    @Override
    public String toString() {
        return value == null ? "void" : value.toString();
    }
}