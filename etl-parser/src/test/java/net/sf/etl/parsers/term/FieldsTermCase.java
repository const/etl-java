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
package net.sf.etl.parsers.term;

import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.event.tree.FieldObjectFactory;
import net.sf.etl.parsers.event.tree.TokenCollector;
import net.sf.etl.parsers.streams.TermParserReader;
import net.sf.etl.parsers.streams.TreeParserReader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * An base for tests based on beans term parser
 *
 * @param <ObjectType> a type of objects in term parser
 * @author const
 */
public abstract class FieldsTermCase<ObjectType> { // NOPMD
    /**
     * A parser to use
     */
    protected TreeParserReader<ObjectType> parser;
    /**
     * A term parser to use
     */
    private TermParserReader termParser;

    /**
     * Start parsing resource
     *
     * @param resourceName a resource to parse
     */
    protected void startWithResource(final String resourceName) {
        final java.net.URL in = this.getClass().getResource(resourceName);
        assertNotNull(in);
        startWithURL(in);
    }

    /**
     * Start parsing with URL
     *
     * @param url an URL to parse
     */
    protected void startWithURL(final java.net.URL url) {
        termParser = new TermParserReader(url);
        termParser.advance();
        parser = new TreeParserReader<ObjectType>(termParser, createFieldTermParser());
        parser.setErrorTokenHandler(new TokenCollector() {
            @Override
            public void collect(final TermToken token) {
                fail("Errors from parser are not expected: " + token);
            }
        });

    }

    /**
     * Create and configure object factory
     *
     * @return a object factory
     */
    protected FieldObjectFactory<ObjectType> createFieldTermParser() {
        return new FieldObjectFactory<ObjectType>(this.getClass().getClassLoader());
    }

    /**
     * End parsing resource
     *
     * @param errorExit true if error exit
     */
    protected void endParsing(final boolean errorExit) {
        if (!errorExit) {
            assertFalse(parser.hadErrors());
        }
        termParser.close();
    }
}
