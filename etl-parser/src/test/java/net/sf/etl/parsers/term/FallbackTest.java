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

import net.sf.etl.parsers.Terms;
import org.junit.Test;

/**
 * This test checks handling of errors using fallback test
 *
 * @author const
 */
public class FallbackTest extends TermStructureTestCase {
    /**
     * Namespace to use
     */
    public static final String ns = "http://etl.sf.net/2006/tests/fallbacks";

    /**
     * Test context with empty fallback statement
     */
    @Test
    public void testEmptyFallback() {
        startWithResource("fallback/EmptyFallbacks.test.etl");
        boolean errorExit = true;
        try {
            readDocType("\"Fallbacks.g.etl\"", "EmptyFallbacks");
            objectStart(ns, "SomeStatement");
            readDocAndOkAttribute();
            propStart("value");
            value("test");
            propEnd("value");
            objectEnd(ns, "SomeStatement");

            objectStart(ns, "BlankStatement");
            readDocAndOkAttribute();
            objectEnd(ns, "BlankStatement");

            objectStart(ns, "SomeStatement");
            readDoc();
            listStart("attributes");
            readError(Terms.SYNTAX_ERROR);
            listEnd("attributes");
            propStart("value");
            value("test");
            propEnd("value");
            objectEnd(ns, "SomeStatement");

            objectStart(ns, "BlankStatement");
            readDocAndOkAttribute();
            objectEnd(ns, "BlankStatement");
            readError(Terms.SYNTAX_ERROR);

            errorExit = false;
        } finally {
            endParsing(errorExit);
        }
    }

    /**
     * Test context with empty fallback statement
     */
    @Test
    public void testNonEmptyFallback() {
        startWithResource("fallback/NonEmptyFallbacks.test.etl");
        boolean errorExit = true;
        try {
            readDocType("\"Fallbacks.g.etl\"", "NonEmptyFallbacks");
            objectStart(ns, "SomeStatement");
            readDocAndOkAttribute();
            propStart("value");
            value("test");
            propEnd("value");
            objectEnd(ns, "SomeStatement");

            objectStart(ns, "SomeStatement");
            readDocAndOkAttribute();
            readError(Terms.SYNTAX_ERROR);
            objectEnd(ns, "SomeStatement");

            objectStart(ns, "SomeStatement");
            readDoc();
            listStart("attributes");
            readError(Terms.SYNTAX_ERROR);
            listEnd("attributes");
            propStart("value");
            value("test");
            propEnd("value");
            objectEnd(ns, "SomeStatement");

            objectStart(ns, "SomeStatement");
            readDocAndOkAttribute();
            readError(Terms.SYNTAX_ERROR);
            objectEnd(ns, "SomeStatement");

            errorExit = false;
        } finally {
            endParsing(errorExit);
        }
    }

    /**
     * read documentation and ok attribute
     */
    private void readDocAndOkAttribute() {
        readDoc();
        listStart("attributes");
        value("ok");
        listEnd("attributes");
    }

    /**
     *
     */
    private void readDoc() {
        listStart("documentation");
        value("/// a documentation");
        listEnd("documentation");
    }

}
