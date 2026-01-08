package org.pakicek.vm.runtime;

import org.junit.Test;
import java.math.BigInteger;
import static org.junit.Assert.*;

public class RuntimeTest {

    @Test
    public void testSrValueTypes() {
        SrValue intVal = new SrValue(new BigInteger("12345678901234567890"));
        assertEquals(SrValue.Type.INT, intVal.type);
        assertEquals(new BigInteger("12345678901234567890"), intVal.asInt());

        SrValue floatVal = new SrValue(3.14159);
        assertEquals(SrValue.Type.FLOAT, floatVal.type);
        assertEquals(3.14159, floatVal.asFloat(), 0.0001);

        SrValue boolVal = new SrValue(true);
        assertTrue(boolVal.asBool());

        SrValue strVal = new SrValue("Hello");
        assertEquals("Hello", strVal.asString());
    }

    @Test
    public void testSafeUnboxing() {
        SrValue voidVal = SrValue.VOID;
        // Should not throw NPE (NullPointerException)
        assertEquals(0.0, voidVal.asFloat(), 0.0001);
        assertFalse(voidVal.asBool());
    }
}