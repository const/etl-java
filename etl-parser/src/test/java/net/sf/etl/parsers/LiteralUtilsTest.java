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
package net.sf.etl.parsers;

import net.sf.etl.parsers.literals.LiteralUtils;
import net.sf.etl.parsers.literals.NumberInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A test for literal utilities
 *
 * @author const
 */
public class LiteralUtilsTest {

    /**
     * THis test checks parsing numbers
     */
    @Test
    public void testParseIntegerNumber() {
        NumberInfo n = LiteralUtils.parseNumber(Integer.toString(Integer.MAX_VALUE) + "qwe");
        assertEquals(Tokens.INTEGER_WITH_SUFFIX, n.getKind());
        assertEquals(10, n.getBase());
        assertEquals(0, n.getSign());
        assertEquals(0, n.getExponent());
        assertEquals(Integer.MAX_VALUE, Integer.parseInt(n.getText()));
        assertEquals("qwe", n.getSuffix());
        n = LiteralUtils.parseNumber(Integer.toString(Integer.MIN_VALUE));
        assertEquals(10, n.getBase());
        assertEquals(-1, n.getSign());
        assertEquals(0, n.getExponent());
        assertEquals(Integer.MIN_VALUE, Integer.parseInt("-" + n.getText()));
    }

    /**
     * THis test checks parsing numbers
     */
    @Test
    public void testParseFloatNumber() {
        String text = "+" + Integer.MAX_VALUE + ".0e3qwe";
        NumberInfo n = LiteralUtils.parseNumber(text);
        assertEquals(Tokens.FLOAT_WITH_SUFFIX, n.getKind());
        assertEquals(10, n.getBase());
        assertEquals(1, n.getSign());
        assertEquals(2, n.getExponent());
        assertEquals(Integer.MAX_VALUE * 1000.0, LiteralUtils.parseDouble(text), 0.1);
        assertEquals("qwe", n.getSuffix());
        text = "-0Xda.daP+8";
        n = LiteralUtils.parseNumber(text);
        assertEquals(16, n.getBase());
        assertEquals(-1, n.getSign());
        assertEquals(0, n.getExponent());
        assertEquals((double) -0xdada, LiteralUtils.parseDouble(text), 0.1);
    }

    /**
     * Test parsing integers
     */
    @Test
    public void testParseInt() {
        assertEquals(Integer.MAX_VALUE, LiteralUtils.parseInt(Integer.toString(Integer.MAX_VALUE)));
        assertEquals(Integer.MIN_VALUE, LiteralUtils.parseInt(Integer.toString(Integer.MIN_VALUE)));
        assertEquals(Integer.MAX_VALUE, LiteralUtils.parseInt("0x7FFF_FFFF"));
        assertEquals(-Integer.MAX_VALUE, LiteralUtils.parseInt("-0x7fff_ffff"));
        assertEquals(Integer.MIN_VALUE, LiteralUtils.parseInt("-0x8000_0000"));
        assertEquals(0, LiteralUtils.parseInt("-0x0000_0000_0000_0000"));
        assertEquals(10, LiteralUtils.parseInt("0b1010"));
    }

    /**
     * Test parse strings
     */
    @Test
    public void testParseString() {
        assertEquals("a\bc", LiteralUtils.parseString("'\\a\\b\\c'"));
        assertEquals("a\bc", LiteralUtils.parseString("PFX12'\\a\\b\\c'"));
        assertEquals("a''\b\nc", LiteralUtils
                .parseString("PFX12'''\\a''\\b\\n\\c'''"));
        assertEquals("a\bc\f\r\n", LiteralUtils
                .parseString("'\\a\\b\\c\\f\\r\\n'"));
        assertEquals("\u0001\u00FF\u0403\u0404\u0405", LiteralUtils
                .parseString("'\\x{1}\\xFF\\x{00403}\\x{404}\\u0405'"));
    }
}
