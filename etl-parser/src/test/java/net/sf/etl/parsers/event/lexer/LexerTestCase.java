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

import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.TokenKey;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.impl.LexerImpl;

import java.nio.CharBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The basic lexer test case
 */
public abstract class LexerTestCase { // NOPMD
    // TODO unicode tests
    // TODO errors and error recovery tests
    /**
     * The current lexer to use
     */
    protected LexerImpl lexer;
    /**
     * The used character buffer
     */
    protected CharBuffer buffer;
    /**
     * The input string
     */
    protected String input;
    /**
     * The consumed characters
     */
    protected int consumed;
    /**
     * The current token
     */
    protected Token current;

    /**
     * Start parsing
     *
     * @param text the text to parse
     */
    protected void start(final String text) {
        lexer = new LexerImpl();
        lexer.start("unknown:text", TextPos.START);
        input = text;
        consumed = input.length();
        buffer = CharBuffer.wrap(text);
    }

    /**
     * Start parsing
     *
     * @param text the text to parse
     */
    protected void startOneChar(final String text) {
        lexer = new LexerImpl();
        lexer.start("unknown:text", TextPos.START);
        input = text;
        consumed = 0;
        buffer = CharBuffer.allocate(4);
        buffer.position(0);
        buffer.limit(0);
    }


    /**
     * @return parse next token
     */
    protected Token next() {
        while (true) {
            final ParserState status = lexer.parse(buffer, consumed == input.length());
            if (status == ParserState.OUTPUT_AVAILABLE) {
                current = lexer.read();
                return current;
            } else if (status == ParserState.EOF) {
                current = null;
                return null;
            } else if (status == ParserState.INPUT_NEEDED) {
                buffer.compact();
                buffer.put(input.charAt(consumed++));
                buffer.flip();
            } else {
                throw new IllegalStateException("Not yet supported status: " + status);
            }
        }
    }

    /**
     * Test string that contains a single token of the specified kind
     *
     * @param text the text parse
     * @param kind the expected token kind
     * @return the parsed token
     */
    protected Token single(final String text, final Tokens kind) {
        // one char node
        startOneChar(text);
        read(text, kind);
        readEof();
        // single token mode
        start(text);
        final Token rc = read(text, kind);
        if (rc.hasErrors()) {
            fail("The token has errors: " + rc);
        }
        readEof();
        return rc;
    }

    /**
     * Check single token using token key
     *
     * @param text     the text to check
     * @param tokenKey the token key to use
     */
    protected void single(final String text, final TokenKey tokenKey) {
        final Token t = single(text, tokenKey.kind());
        assertEquals(tokenKey, t.key());
    }


    /**
     * Test uniform sequence of the tokens
     *
     * @param text   the text to check
     * @param kind   the kind of the token
     * @param tokens the token text
     */
    protected void sequence(final String text, final Tokens kind, final String... tokens) {
        // one char mode
        startOneChar(text);
        for (final String t : tokens) {
            read(t, kind);
        }
        readEof();
        // many chars mode
        start(text);
        for (final String t : tokens) {
            read(t, kind);
        }
        readEof();
    }

    /**
     * Test uniform sequence of the tokens
     *
     * @param text   the text to check
     * @param tokens the token text
     */
    protected void sequenceText(final String text, final String... tokens) {
        // one char mode
        startOneChar(text);
        for (final String t : tokens) {
            next();
            assertEquals(t, current.text());
        }
        readEof();
        // many chars mode
        start(text);
        for (final String t : tokens) {
            next();
            assertEquals(t, current.text());
        }
        readEof();
    }

    /**
     * Read end of stream
     */
    protected void readEof() {
        read("", Tokens.EOF);
        next();
        assertNull(current);
    }


    /**
     * Read single token and check it
     *
     * @param text the text to parse
     * @param kind the expected token kind
     * @return the parsed token
     */
    protected Token read(final String text, final Tokens kind) {
        next();
        checkCurrent(text, kind);
        return current;
    }

    /**
     * Read single token and check it
     *
     * @param text the text to parse
     * @param kind the expected token kind
     * @return the parsed token
     */
    protected Token read(final String text, final TokenKey kind) {
        next();
        checkCurrent(text, kind);
        return current;
    }


    protected void checkCurrent(final String text, final TokenKey tokenKey) {
        assertEquals(text, current.text());
        assertEquals(tokenKey, current.key());
    }


    protected void checkCurrent(final String text, final Tokens kind) {
        assertEquals(text, current.text());
        assertEquals(kind, current.kind());
    }

}
