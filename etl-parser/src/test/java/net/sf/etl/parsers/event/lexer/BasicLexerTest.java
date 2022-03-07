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

import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.event.Lexer;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.impl.LexerImpl;
import org.junit.jupiter.api.Test;

import java.nio.CharBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The lexer test
 */
public class BasicLexerTest {
    @Test
    public void testEof() {
        final Lexer l = new LexerImpl();
        l.start("test:test", TextPos.START);
        ParserState r = l.parse(CharBuffer.wrap(""), false);
        assertEquals(ParserState.INPUT_NEEDED, r);
        r = l.parse(CharBuffer.wrap(""), true);
        assertEquals(ParserState.OUTPUT_AVAILABLE, r);
        final Token t = l.read();
        assertEquals(Tokens.EOF, t.kind());
        assertEquals(TextPos.START, t.start());
        assertEquals(TextPos.START, t.end());
    }
}
