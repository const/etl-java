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
package net.sf.etl.parsers.term;

import net.sf.etl.parsers.streams.TermParserReader;
import net.sf.etl.parsers.streams.beans.BeansTermParser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * An base for tests based on beans term parser
 *
 * @author const
 */
public abstract class BeansTermCase {
    /**
     * A parser to use
     */
    protected BeansTermParser parser;
    /**
     * A term parser to use
     */
    private TermParserReader termParser;

    /**
     * Start parsing resource
     *
     * @param resourceName a resource to parse
     */
    protected void startWithResource(String resourceName) {
        java.net.URL in = this.getClass().getResource(resourceName);
        assertNotNull(in);
        startWithURL(in);
    }

    /**
     * Start parsing with URL
     *
     * @param in an URL to parse
     */
    protected void startWithURL(java.net.URL in) {
        termParser = new TermParserReader(in);
        termParser.advance();
        parser = createBeansTermParser(termParser);
    }

    /**
     * Create and configure term parser
     *
     * @param termParser a term parser
     * @return a new bean parser
     */
    protected BeansTermParser createBeansTermParser(TermParserReader termParser) {
        return new BeansTermParser(termParser, null);
    }

    /**
     * End parsing resource
     *
     * @param errorExit true if error exit
     */
    protected void endParsing(boolean errorExit) {
        if (!errorExit) {
            assertFalse(parser.hadErrors());
        }
        termParser.close();
    }
}
