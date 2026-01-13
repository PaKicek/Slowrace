package org.pakicek.runtime.vm;

import java.util.HashMap;
import java.util.Map;

public class SrStruct extends SrObject {
    public final String name;
    // Map is simpler for name-based lookup
    public final Map<String, SrValue> fields = new HashMap<>();

    public SrStruct(String name) {
        this.name = name;
    }

    @Override
    public int getSize() { return fields.size(); }
}