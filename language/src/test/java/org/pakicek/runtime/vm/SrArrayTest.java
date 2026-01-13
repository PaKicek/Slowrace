package org.pakicek.runtime.vm;

import org.junit.Test;
import java.math.BigInteger;
import static org.junit.Assert.*;

public class SrArrayTest {

    @Test
    public void testArrayInitialization() {
        // Arrays should be initialized with default values (0)
        int size = 5;
        SrArray array = new SrArray(size);

        assertEquals(size, array.getSize());
        for (int i = 0; i < size; i++) {
            assertNotNull(array.elements[i]);
            assertEquals(SrValue.Type.INT, array.elements[i].type);
            assertEquals(BigInteger.ZERO, array.elements[i].asInt());
        }
    }

    @Test
    public void testArrayModification() {
        SrArray array = new SrArray(3);
        array.elements[0] = new SrValue("First");
        array.elements[1] = new SrValue(true);
        array.elements[2] = new SrValue(3.14);

        assertEquals("First", array.elements[0].asString());
        assertTrue(array.elements[1].asBool());
        assertEquals(3.14, array.elements[2].asFloat(), 0.0001);
    }
}