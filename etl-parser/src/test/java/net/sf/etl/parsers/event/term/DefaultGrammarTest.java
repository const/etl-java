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

import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.event.impl.term.DefaultCompiledGrammar;
import org.junit.Test;

/**
 * The test for default grammar
 */
public class DefaultGrammarTest extends BasicTermTestCase {
    @Test
    public void simple() {
        String text = "/// test";
        startDefaultGrammar(text);
        read(Terms.STATEMENT_START);
        read(Terms.OBJECT_START);
        read(Terms.DOC_COMMENT_START);
        read(Terms.LIST_PROPERTY_START);
        read(Terms.OBJECT_START);
        read(Terms.PROPERTY_START);
        read(Terms.VALUE);
        read(Terms.PROPERTY_END);
        read(Terms.OBJECT_END);
        read(Terms.LIST_PROPERTY_END);
        read(Terms.DOC_COMMENT_END);
        read(Terms.LIST_PROPERTY_START);
        read(Terms.LIST_PROPERTY_END);
        read(Terms.OBJECT_END);
        read(Terms.STATEMENT_END);
        read(Terms.CONTROL);
        read(Terms.EOF);
    }

    private void startDefaultGrammar(String text) {
        startCompiledGrammar(DefaultCompiledGrammar.INSTANCE, text);
    }
}
