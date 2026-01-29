package org.pakicek.runtime.vm;

import org.junit.Test;
import java.math.BigInteger;
import static org.junit.Assert.*;

public class SrStructTest {

    @Test
    public void testStructCreation() {
        SrStruct point = new SrStruct("Point");
        assertEquals("Point", point.name);
        assertEquals(0, point.getSize());
    }

    @Test
    public void testFieldAccess() {
        SrStruct vector = new SrStruct("Vector");

        vector.fields.put("x", new SrValue(10.5));
        vector.fields.put("y", new SrValue(20.5));
        assertEquals(2, vector.getSize());
        assertEquals(10.5, vector.fields.get("x").asFloat(), 0.0001);
        assertEquals(20.5, vector.fields.get("y").asFloat(), 0.0001);
    }

    @Test
    public void testNestedStructs() {
        SrStruct line = new SrStruct("Line");
        SrStruct p1 = new SrStruct("Point");
        SrStruct p2 = new SrStruct("Point");

        p1.fields.put("x", new SrValue(BigInteger.ZERO));
        p2.fields.put("x", new SrValue(BigInteger.TEN));

        line.fields.put("start", new SrValue(p1));
        line.fields.put("end", new SrValue(p2));

        SrValue startVal = line.fields.get("start");
        assertEquals(SrValue.Type.OBJECT, startVal.type);
        assertTrue(startVal.asObject() instanceof SrStruct);

        SrStruct startPoint = (SrStruct) startVal.asObject();
        assertEquals(BigInteger.ZERO, startPoint.fields.get("x").asInt());
    }
}