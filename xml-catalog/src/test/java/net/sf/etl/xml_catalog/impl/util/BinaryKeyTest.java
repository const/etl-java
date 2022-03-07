/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2013 Constantine A Plotnikov
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.sf.etl.xml_catalog.impl.util;


import net.sf.etl.xml_catalog.util.BinaryKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The test for binary test
 */
public class BinaryKeyTest {

    @Test
    public void hexTest() {
        final BinaryKey k1 = BinaryKey.fromString("0ffe");
        final BinaryKey k2 = BinaryKey.fromString("0FFE");
        final BinaryKey k3 = BinaryKey.fromString("0F");
        assertEquals(k1, k2);
        assertEquals(k1.hashCode(), k2.hashCode());
        assert k1.toString().equalsIgnoreCase("0ffe");
        assertEquals(BinaryKey.fromString(""), BinaryKey.EMPTY);
        assert !k1.equals(k3);
        try {
            BinaryKey.fromString("0FE");
            fail("Wrong length"); // NOPMD
        } catch (IllegalArgumentException ex) { //NOPMD
            // expected
        }
        try {
            BinaryKey.fromString("0F_E");
            fail("Wrong chars"); // NOPMD
        } catch (IllegalArgumentException ex) { //NOPMD
            // expected
        }
        try {
            BinaryKey.fromString("-0FE");
            fail("Wrong chars"); // NOPMD
        } catch (IllegalArgumentException ex) { //NOPMD
            // expected
        }
    }

    @Test
    public void byteTest() {
        final BinaryKey k1 = BinaryKey.fromString("0ffe");
        final BinaryKey k2 = BinaryKey.fromBytes(new byte[]{1, 15, (byte) 0xfe, 4}, 1, 2);
        assertEquals(k1, k2);
        final BinaryKey k3 = BinaryKey.fromBytes(new byte[]{15, (byte) 0xfe});
        assertEquals(k1, k3);
    }

    @Test
    public void longTest() {
        final BinaryKey k1 = BinaryKey.fromString("1122334455667788");
        final BinaryKey k2 = BinaryKey.fromLong(0x1122334455667788L);
        assertEquals(k1, k2);
        final BinaryKey k3 = BinaryKey.fromString("0000000000000001");
        final BinaryKey k4 = BinaryKey.fromLong(0x01L);
        assertEquals(k3, k4);
    }

    @Test
    public void sha1() {
        final BinaryKey k1 = BinaryKey.sha1(new byte[0]);
        final BinaryKey k2 = BinaryKey.EMPTY.toSha1();
        assertEquals(k1, k2);
        final BinaryKey k3 = BinaryKey.sha1(new byte[1]);
        assert !k1.equals(k3);
    }
}
