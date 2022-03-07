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
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The test for bootstrap grammars
 */
public class BootstrapGrammarsTest extends BasicTermTestCase {

    @Test
    public void doctypeTest() {
        final CompiledGrammar compiledGrammar = BootstrapGrammars.doctypeGrammar();
        final DefinitionContext defaultContext = compiledGrammar.getDefaultContext();
        assertEquals(StandardGrammars.VERSION, defaultContext.grammar().version());
        assertEquals(StandardGrammars.DOCTYPE_GRAMMAR_ID.name(), defaultContext.grammar().name());
        assertEquals("DoctypeContext", defaultContext.context());
        assertNull(compiledGrammar.getErrors());
        startCompiledGrammar(compiledGrammar, "doctype strict ETL.Grammar \"0.3.0\" context = Grammar;");
        read(Terms.STATEMENT_START);
        read(Terms.OBJECT_START);
        read(Terms.STRUCTURAL, "doctype");
        read(Terms.IGNORABLE);
        read(Terms.PROPERTY_START);
        read(Terms.VALUE, "strict");
        read(Terms.IGNORABLE);
        read(Terms.PROPERTY_END);
        read(Terms.LIST_PROPERTY_START);
        read(Terms.VALUE, "ETL");
        read(Terms.STRUCTURAL, ".");
        read(Terms.VALUE, "Grammar");
        read(Terms.IGNORABLE);
        read(Terms.LIST_PROPERTY_END);
        read(Terms.PROPERTY_START);
        read(Terms.VALUE, "\"0.3.0\"");
        read(Terms.IGNORABLE);
        read(Terms.PROPERTY_END);
        read(Terms.STRUCTURAL, "context");
        read(Terms.IGNORABLE);
        read(Terms.STRUCTURAL, "=");
        read(Terms.IGNORABLE);
        read(Terms.PROPERTY_START);
        read(Terms.VALUE, "Grammar");
        read(Terms.PROPERTY_END);
        read(Terms.OBJECT_END);
        read(Terms.STATEMENT_END);
        read(Terms.CONTROL);
        read(Terms.CONTROL, ";");
        read(Terms.EOF);
    }


    @Test
    public void doctypeTestRecovery() {
        final CompiledGrammar compiledGrammar = BootstrapGrammars.doctypeGrammar();
        final DefinitionContext defaultContext = compiledGrammar.getDefaultContext();
        assertEquals(StandardGrammars.VERSION, defaultContext.grammar().version());
        assertEquals(StandardGrammars.DOCTYPE_GRAMMAR_ID.name(), defaultContext.grammar().name());
        assertEquals("DoctypeContext", defaultContext.context());
        assertNull(compiledGrammar.getErrors());
        startCompiledGrammar(compiledGrammar, "doctype strict ETL.Grammar \"0.3.0\" context Test;");
        read(Terms.STATEMENT_START);
        read(Terms.OBJECT_START);
        read(Terms.STRUCTURAL, "doctype");
        read(Terms.IGNORABLE);
        read(Terms.PROPERTY_START);
        read(Terms.VALUE, "strict");
        read(Terms.IGNORABLE);
        read(Terms.PROPERTY_END);
        read(Terms.LIST_PROPERTY_START);
        read(Terms.VALUE, "ETL");
        read(Terms.STRUCTURAL, ".");
        read(Terms.VALUE, "Grammar");
        read(Terms.IGNORABLE);
        read(Terms.LIST_PROPERTY_END);
        read(Terms.PROPERTY_START);
        read(Terms.VALUE, "\"0.3.0\"");
        read(Terms.IGNORABLE);
        read(Terms.PROPERTY_END);
        read(Terms.STRUCTURAL, "context");
        read(Terms.IGNORABLE);
        read(Terms.SYNTAX_ERROR, true);
        read(Terms.PROPERTY_START);
        read(Terms.VALUE, "Test");
        read(Terms.PROPERTY_END);
        read(Terms.OBJECT_END);
        read(Terms.STATEMENT_END);
        read(Terms.CONTROL);
        read(Terms.CONTROL, ";");
        read(Terms.EOF);
    }


    @Test
    public void grammarTestRecovery() {
        final CompiledGrammar compiledGrammar = BootstrapGrammars.grammarGrammar();
        final DefinitionContext defaultContext = compiledGrammar.getDefaultContext();
        assertEquals(StandardGrammars.VERSION, defaultContext.grammar().version());
        assertEquals("ETL.Grammar", defaultContext.grammar().name());
        assertEquals("GrammarSource", defaultContext.context());
        assertNull(compiledGrammar.getErrors());
        startCompiledGrammar(compiledGrammar, "grammar test1. .test2.\'test\' q{}");
        read(Terms.STATEMENT_START);
        read(Terms.OBJECT_START);
        read(Terms.STRUCTURAL, "grammar");
        read(Terms.IGNORABLE);
        read(Terms.MODIFIERS_START);
        read(Terms.MODIFIERS_END);
        read(Terms.LIST_PROPERTY_START);
        read(Terms.VALUE, "test1");
        read(Terms.STRUCTURAL, ".");
        read(Terms.IGNORABLE);
        read(Terms.SYNTAX_ERROR, true);
        read(Terms.STRUCTURAL, ".");
        read(Terms.VALUE, "test2");
        read(Terms.STRUCTURAL, ".");
        read(Terms.SYNTAX_ERROR, true);
        read(Terms.LIST_PROPERTY_END);
        read(Terms.PROPERTY_START);
        read(Terms.VALUE, "'test'");
        read(Terms.IGNORABLE);
        read(Terms.PROPERTY_END);
        read(Terms.LIST_PROPERTY_START);
        read(Terms.SYNTAX_ERROR, true);
        read(Terms.IGNORABLE, "q");
        read(Terms.BLOCK_START);
        read(Terms.CONTROL, "{");
        read(Terms.CONTROL, "}");
        read(Terms.BLOCK_END);
        read(Terms.LIST_PROPERTY_END);
        read(Terms.OBJECT_END);
        read(Terms.STATEMENT_END);
        read(Terms.CONTROL);
        read(Terms.EOF);
    }


    @Test
    public void defaultTest() {
        final CompiledGrammar compiledGrammar = BootstrapGrammars.defaultGrammar();
        final DefinitionContext defaultContext = compiledGrammar.getDefaultContext();
        assertEquals(StandardGrammars.VERSION, defaultContext.grammar().version());
        assertEquals(StandardGrammars.DEFAULT_GRAMMAR_ID.name(), defaultContext.grammar().name());
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
        assertEquals("ETL.Grammar", defaultContext.grammar().name());
        assertEquals("GrammarSource", defaultContext.context());
        assertNull(compiledGrammar.getErrors());
    }

    @Test
    public void grammarReadAllDoctypeTest() throws IOException {
        startSystemId(StandardGrammars.DOCTYPE_GRAMMAR_URL.toString());
        final GrammarLiteTermParser parser = new GrammarLiteTermParser(reader);
        assertTrue(parser.advance());
        final Element next = parser.current();
        assertTrue(next instanceof Grammar);
        assertEquals(0, parser.errors().size(), "Errors: " + parser.errors());
    }

    @Test
    public void grammarReadAllDefaultTest() throws IOException {
        startSystemId(StandardGrammars.DEFAULT_GRAMMAR_URL.toString());
        final GrammarLiteTermParser parser = new GrammarLiteTermParser(reader);
        assertTrue(parser.advance());
        final Element next = parser.current();
        assertTrue(next instanceof Grammar);
        assertEquals(0, parser.errors().size(), "Errors: " + parser.errors());
    }

    @Test
    public void grammarReadAllGrammarTest() throws IOException {
        startSystemId(StandardGrammars.ETL_GRAMMAR_URL.toString());
        final GrammarLiteTermParser parser = new GrammarLiteTermParser(reader);
        assertTrue(parser.advance());
        final Element next = parser.current();
        assertTrue(next instanceof Grammar);
        assertEquals(0, parser.errors().size(), "Errors: " + parser.errors());
    }
}
