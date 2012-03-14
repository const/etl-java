/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2009 Constantine A Plotnikov
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

import net.sf.etl.parsers.LiteralUtils;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.LiteralUtils.NumberInfo;
import junit.framework.TestCase;

/**
 * A test for literal utilities
 * 
 * @author const
 */
public class LiteralUtilsTest extends TestCase {

	/**
	 * THis test checks parsing numbers
	 */
	public void testParseIntegerNumber() {
		NumberInfo n = LiteralUtils.parseNumber("" + Integer.MAX_VALUE + "qwe");
		assertEquals(Tokens.INTEGER_WITH_SUFFIX, n.kind);
		assertEquals(10, n.base);
		assertEquals(1, n.sign);
		assertEquals(0, n.exponent);
		assertEquals(Integer.MAX_VALUE, Integer.parseInt(n.text));
		assertEquals("qwe", n.suffix);
		n = LiteralUtils.parseNumber("" + Integer.MIN_VALUE);
		assertEquals(10, n.base);
		assertEquals(-1, n.sign);
		assertEquals(0, n.exponent);
		assertEquals(Integer.MIN_VALUE, Integer.parseInt("-" + n.text));
	}

	/**
	 * THis test checks parsing numbers
	 */
	public void testParseFloatNumber() {
		String text = "" + Integer.MAX_VALUE + ".0e3qwe";
		NumberInfo n = LiteralUtils.parseNumber(text);
		assertEquals(Tokens.FLOAT_WITH_SUFFIX, n.kind);
		assertEquals(10, n.base);
		assertEquals(1, n.sign);
		assertEquals(2, n.exponent);
		assertEquals(Integer.MAX_VALUE * 1000.0, LiteralUtils.parseDouble(text));
		assertEquals("qwe", n.suffix);
		text = "-16#da.da#e+2";
		n = LiteralUtils.parseNumber(text);
		assertEquals(16, n.base);
		assertEquals(-1, n.sign);
		assertEquals(0, n.exponent);
		assertEquals((double) -0xdada, LiteralUtils.parseDouble(text));
	}

	/**
	 * Test parsing integers
	 */
	public void testParseInt() {
		assertEquals(Integer.MAX_VALUE, LiteralUtils.parseInt(""
				+ Integer.MAX_VALUE));
		assertEquals(Integer.MIN_VALUE, LiteralUtils.parseInt(""
				+ Integer.MIN_VALUE));
		assertEquals(Integer.MAX_VALUE, LiteralUtils.parseInt("16#7FFF_FFFF#"));
		assertEquals(-Integer.MAX_VALUE, LiteralUtils
				.parseInt("-16#7fff_ffff#"));
		assertEquals(Integer.MIN_VALUE, LiteralUtils.parseInt("-16#8000_0000#"));
		assertEquals(0, LiteralUtils.parseInt("-16#0000_0000_0000_0000#"));
		assertEquals(10, LiteralUtils.parseInt("2#1010#"));
	}

	/**
	 * Test parse strings
	 */
	public void testParseString() {
		assertEquals("a\bc", LiteralUtils.parseString("'\\a\\b\\c'"));
		assertEquals("a\bc", LiteralUtils.parseString("PFX12'\\a\\b\\c'"));
		assertEquals("a''\b\nc", LiteralUtils
				.parseString("PFX12'''\\a''\\b\\n\\c'''"));
		assertEquals("a\bc\f\r\n", LiteralUtils
				.parseString("'\\a\\b\\c\\f\\r\\n'"));
		assertEquals("\u0001\u00FF\u0403\u0404\u0405", LiteralUtils
				.parseString("'\\U1;\\xFF\\U00403;\\U404;\\u0405'"));
	}
}
