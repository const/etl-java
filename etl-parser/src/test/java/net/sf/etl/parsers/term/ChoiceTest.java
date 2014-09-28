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
public class ChoiceTest extends TermStructureTestCase {
    /**
     * Namespace to use
     */
    public static final String NS = "urn:test.Choice";

    @Test
    public void testA() {
        startWithResource("choice/ChoiceTestA.c.etl");
        boolean errorExit = true;
        try {
            readDocType("script", "\"Choice.g.etl\"", "TestContext");
            objectStart(NS, "TestStatement");
            propStart("Value");
            value("a");
            propEnd("Value");
            objectEnd(NS, "TestStatement");
            objectStart(NS, "TestStatement");
            readError(Terms.SYNTAX_ERROR);
            objectEnd(NS, "TestStatement");
            errorExit = false;
        } finally {
            endParsing(errorExit);
        }
    }

    @Test
    public void testB() {
        startWithResource("choice/ChoiceTestB.c.etl");
        boolean errorExit = true;
        try {
            readDocType("script", "\"Choice.g.etl\"", null);
            objectStart(NS, "TestStatement");
            propStart("Value");
            value("a");
            propEnd("Value");
            objectEnd(NS, "TestStatement");
            objectStart(NS, "TestStatement");
            propStart("Value");
            value("b");
            propEnd("Value");
            objectEnd(NS, "TestStatement");
            objectStart(NS, "TestStatement");
            readError(Terms.SYNTAX_ERROR);
            objectEnd(NS, "TestStatement");
            errorExit = false;
        } finally {
            endParsing(errorExit);
        }
    }

    @Test
    public void testC() {
        startWithResource("choice/ChoiceTestC.c.etl");
        boolean errorExit = true;
        try {
            readDocType(null, "\"ChoiceExt.g.etl\"", null);
            objectStart(NS, "TestStatement");
            propStart("Value");
            value("a");
            propEnd("Value");
            objectEnd(NS, "TestStatement");
            objectStart(NS, "TestStatement");
            readError(Terms.SYNTAX_ERROR);
            objectEnd(NS, "TestStatement");
            objectStart(NS, "TestStatement");
            propStart("Value");
            value("B");
            propEnd("Value");
            objectEnd(NS, "TestStatement");
            objectStart(NS, "TestStatement");
            propStart("Value");
            value("c");
            propEnd("Value");
            objectEnd(NS, "TestStatement");
            errorExit = false;
        } finally {
            endParsing(errorExit);
        }
    }
}
