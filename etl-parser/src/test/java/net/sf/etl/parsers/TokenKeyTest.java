/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2012 Constantine A Plotnikov
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

import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 * A test for token kinds
 *
 * @author const
 */
public class TokenKeyTest {
    /**
     * Test simple keys
     */
    @Test
    public void testSimple() {
        assertSame(TokenKey.modified(Tokens.INTEGER, null), TokenKey
                .simple(Tokens.INTEGER));
        assertSame(Tokens.INTEGER, TokenKey.simple(Tokens.INTEGER).kind());
        assertNotSame(TokenKey.modified(Tokens.INTEGER_WITH_SUFFIX, "UL"),
                TokenKey.simple(Tokens.INTEGER));
        assertNotSame(TokenKey.simple(Tokens.LEFT_CURLY), TokenKey
                .simple(Tokens.INTEGER));
    }
}
