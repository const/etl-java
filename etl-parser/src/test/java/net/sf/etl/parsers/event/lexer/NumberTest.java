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

package net.sf.etl.parsers.event.lexer;

import net.sf.etl.parsers.TokenKey;
import net.sf.etl.parsers.Tokens;
import org.junit.Test;

/**
 * Test for numbers
 */
public class NumberTest extends LexerTestCase {
    @Test
    public void integers() {
        single("1", Tokens.INTEGER);
        single("090", Tokens.INTEGER);
        single("0_9_0QA", Tokens.INTEGER_WITH_SUFFIX);
        single("2#101#", Tokens.INTEGER);
        single("8#777#", Tokens.INTEGER);
        single("16#7FFF_FFFF#", Tokens.INTEGER);
        single("36#_a_z_#", Tokens.INTEGER);
        single("16#7FFF_FFFF#U", Tokens.INTEGER_WITH_SUFFIX);
    }

    @Test
    public void shebang() {
        start("1#!test");
        read("1", Tokens.INTEGER);
        read("#!test", Tokens.LINE_COMMENT);
        start("1.0#!test");
        read("1.0", Tokens.FLOAT);
        read("#!test", Tokens.LINE_COMMENT);
    }

    @Test
    public void floats() {
        single("1.0l", TokenKey.modified(Tokens.FLOAT_WITH_SUFFIX, "l"));
        single("16#7F.FF_FFFF#DDDD", TokenKey.modified(Tokens.FLOAT_WITH_SUFFIX, "DDDD"));
        single("1e1", Tokens.FLOAT);
        single("0.1e+2", Tokens.FLOAT);
        single("3.1_4", Tokens.FLOAT);
        single("2#1.01#E+2", Tokens.FLOAT);
        single("8#0.777#", Tokens.FLOAT);
        single("16#7FFF_FFFF#e-4", Tokens.FLOAT);
        single("36#_a._z_#", Tokens.FLOAT);
        single("16#7F.FF_FFFF#DDDD", Tokens.FLOAT_WITH_SUFFIX);
    }

}
