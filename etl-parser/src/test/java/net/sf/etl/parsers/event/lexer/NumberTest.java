/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2022 Konstantin Plotnikov
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
import org.junit.jupiter.api.Test;

/**
 * Test for numbers
 */
public class NumberTest extends LexerTestCase {
    @Test
    public void integers() {
        single("1", Tokens.INTEGER);
        single("090", Tokens.INTEGER);
        single("0_9_0QA", Tokens.INTEGER_WITH_SUFFIX);
        single("0b101", Tokens.INTEGER);
        single("0x7FFF_FFFF", Tokens.INTEGER);
        single("0x7FFF_FFFFu", Tokens.INTEGER_WITH_SUFFIX);
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
        single("0x7F.FF_FFFFp0DDDD", TokenKey.modified(Tokens.FLOAT_WITH_SUFFIX, "DDDD"));
        single("1e1", Tokens.FLOAT);
        single("0.1e+2", Tokens.FLOAT);
        single("3.1_4", Tokens.FLOAT);
        single("0b1.01p+2", Tokens.FLOAT);
        single("0x7FFF_FFFFp-4", Tokens.FLOAT);
        single("0x7F.FF_FFFF\u20820DDDD", Tokens.FLOAT_WITH_SUFFIX);
    }

}
