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

package net.sf.etl.parsers.term.bootstrap;

import net.sf.etl.parsers.DefinitionContext;
import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.event.grammar.BootstrapGrammars;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.event.term.BasicTermTestCase;
import net.sf.etl.parsers.event.unstable.model.grammar.Element;
import net.sf.etl.parsers.event.unstable.model.grammar.Grammar;
import net.sf.etl.parsers.event.unstable.model.grammar.GrammarLiteTermParser;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * The test for bootstrap grammars
 */
public class BootstrapGrammarsTest extends BasicTermTestCase {

    @Test
    public void doctypeTest() {
        final CompiledGrammar compiledGrammar = BootstrapGrammars.doctypeGrammar();
        final DefinitionContext defaultContext = compiledGrammar.getDefaultContext();
        assertEquals(StandardGrammars.VERSION, defaultContext.grammar().version());
        assertEquals(StandardGrammars.DOCTYPE_GRAMMAR_NAME, defaultContext.grammar().name());
        assertEquals("DoctypeContext", defaultContext.context());
        assertNull(compiledGrammar.getErrors());
        startCompiledGrammar(compiledGrammar, "doctype strict public \"-//IDN etl.sf.net//ETL//Grammar 0.3.0\";");
        read(Terms.STATEMENT_START);
        read(Terms.OBJECT_START);
        read(Terms.STRUCTURAL, "doctype");
        read(Terms.IGNORABLE);
        read(Terms.PROPERTY_START);
        read(Terms.VALUE, "strict");
        read(Terms.IGNORABLE);
        read(Terms.PROPERTY_END);
        read(Terms.STRUCTURAL, "public");
        read(Terms.IGNORABLE);
        read(Terms.PROPERTY_START);
        read(Terms.VALUE, "\"-//IDN etl.sf.net//ETL//Grammar 0.3.0\"");
        read(Terms.PROPERTY_END);
        read(Terms.OBJECT_END);
        read(Terms.STATEMENT_END);
        read(Terms.CONTROL);
        read(Terms.CONTROL, ";");
        read(Terms.EOF);
    }


    @Test
    public void defaultTest() {
        final CompiledGrammar compiledGrammar = BootstrapGrammars.defaultGrammar();
        final DefinitionContext defaultContext = compiledGrammar.getDefaultContext();
        assertEquals(StandardGrammars.VERSION, defaultContext.grammar().version());
        assertEquals(StandardGrammars.DEFAULT_GRAMMAR_NAME, defaultContext.grammar().name());
        assertEquals("DefaultContext", defaultContext.context());
        assertNull(compiledGrammar.getErrors());
        startCompiledGrammar(compiledGrammar, "/// test");
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

    @Test
    public void grammarTest() {
        final CompiledGrammar compiledGrammar = BootstrapGrammars.grammarGrammar();
        final DefinitionContext defaultContext = compiledGrammar.getDefaultContext();
        assertEquals(StandardGrammars.VERSION, defaultContext.grammar().version());
        assertEquals("net.sf.etl.grammars.Grammar", defaultContext.grammar().name());
        assertEquals("GrammarSource", defaultContext.context());
        assertNull(compiledGrammar.getErrors());
    }

    @Test
    public void grammarReadAllDoctypeTest() throws IOException {
        startSystemId(StandardGrammars.DOCTYPE_GRAMMAR_SYSTEM_ID);
        final GrammarLiteTermParser parser = new GrammarLiteTermParser(reader);
        assertTrue(parser.hasNext());
        final Element next = parser.next();
        assertTrue(next instanceof Grammar);
        assertEquals("Errors: " + parser.errors(), 0, parser.errors().size());
    }

    @Test
    public void grammarReadAllDefaultTest() throws IOException {
        startSystemId(StandardGrammars.DEFAULT_GRAMMAR_SYSTEM_ID);
        final GrammarLiteTermParser parser = new GrammarLiteTermParser(reader);
        assertTrue(parser.hasNext());
        final Element next = parser.next();
        assertTrue(next instanceof Grammar);
        assertEquals("Errors: " + parser.errors(), 0, parser.errors().size());
    }

    @Test
    public void grammarReadAllGrammarTest() throws IOException {
        startSystemId(StandardGrammars.ETL_GRAMMAR_SYSTEM_ID);
        final GrammarLiteTermParser parser = new GrammarLiteTermParser(reader);
        assertTrue(parser.hasNext());
        final Element next = parser.next();
        assertTrue(next instanceof Grammar);
        assertEquals("Errors: " + parser.errors(), 0, parser.errors().size());
    }
}
