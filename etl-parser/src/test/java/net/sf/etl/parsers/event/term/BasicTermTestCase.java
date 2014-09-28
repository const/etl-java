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

package net.sf.etl.parsers.event.term;

import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.streams.DefaultTermReaderConfiguration;
import net.sf.etl.parsers.streams.LexerReader;
import net.sf.etl.parsers.streams.PhraseParserReader;
import net.sf.etl.parsers.streams.TermParserReader;
import org.junit.After;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Basic test case for term parsing
 */
public class BasicTermTestCase {
    /**
     * The reader used for test
     */
    protected TermParserReader reader;
    /**
     * Skip all non structural tokens
     */
    protected boolean skipIgnorable;

    /**
     * Term parser reader used for the test
     *
     * @param reader the reader
     */
    protected void start(final TermParserReader reader) {
        closeReader();
        this.reader = reader;
        reader.advance();
    }

    /**
     * Read single term and check for the kind
     *
     * @param term the term to parse
     */
    protected TermToken read(final Terms term) {
        return read(term, false);
    }

    /**
     * Read single term and check for the kind
     *
     * @param term       the term to parse
     * @param haveErrors if true, the token must have errors, if false it must not have
     */
    protected TermToken read(final Terms term, final boolean haveErrors) {
        skipIgnorable();
        final TermToken current = reader.current();
        assertEquals("Current token: " + current, haveErrors, current.hasAnyErrors());
        reader.advance();
        assertEquals("Current token: " + current, term, current.kind());
        return current;
    }

    private void skipIgnorable() {
        if (!skipIgnorable) {
            return;
        }
        final TermToken current = reader.current();
        assertFalse("Current token: " + current, current.hasAnyErrors());
    }

    /**
     * Read token and check the value
     *
     * @param token the token to read
     * @param text  the text
     */
    protected void read(final Terms token, final String text) {
        final TermToken read = read(token);
        assertTrue("Current token: " + read, read.hasLexicalToken());
        assertEquals("Current token: " + read, text, read.token().token().text());
    }


    /**
     * Start parsing the grammar
     *
     * @param grammar the grammar to parse
     * @param text    the text to parse
     */
    protected void startCompiledGrammar(final CompiledGrammar grammar, final String text) {
        start(new TermParserReader(new PhraseParserReader(new LexerReader(DefaultTermReaderConfiguration.INSTANCE,
                new StringReader(text), "t", TextPos.START)), grammar, false));
    }

    /**
     * Start parsing the grammar
     *
     * @param systemId the systemId name
     */
    protected void startSystemId(final String systemId) throws IOException {
        startSystemId(new URL(systemId));
    }

    /**
     * Start grammar at URL
     *
     * @param url start grammar at URL
     */
    protected void startSystemId(final URL url) {
        start(new TermParserReader(url));
    }

    /**
     * Start parsing the resource
     *
     * @param resource the resource
     */
    protected void startResource(final String resource) {
        final URL url = getClass().getResource(resource);
        assertNotNull(getClass().getName() + " => " + resource, url);
        startSystemId(url);
    }


    @After
    public void closeReader() {
        if (reader != null) {
            reader.close();
        }
    }
}
