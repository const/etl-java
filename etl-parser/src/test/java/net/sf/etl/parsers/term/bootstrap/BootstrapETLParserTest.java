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

import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.characters.TextUtil;
import net.sf.etl.parsers.event.impl.bootstrap.BootstrapETLParserLite;
import net.sf.etl.parsers.event.unstable.model.grammar.Grammar;
import net.sf.etl.parsers.streams.DefaultTermReaderConfiguration;
import net.sf.etl.parsers.streams.LexerReader;
import net.sf.etl.parsers.streams.PhraseParserReader;
import org.junit.Test;

import java.io.InputStreamReader;

import static org.junit.Assert.assertNotNull;

/**
 * This is test for bootstrap GTL parser. The test uses two grammars doctype and
 * GTL. Doctype is used as simpler one.
 *
 * @author const
 */
public class BootstrapETLParserTest {

    /**
     * parse ResourceI
     *
     * @param resourceName name of ResourceI
     * @return parsed grammar
     */
    private Grammar parseResource(final String resourceName) {
        try {
            final PhraseParserReader phraseParser = new PhraseParserReader(new LexerReader(
                    DefaultTermReaderConfiguration.INSTANCE,
                    new InputStreamReader(
                            BootstrapETLParserTest.class.getResourceAsStream(resourceName), TextUtil.UTF8),
                    BootstrapETLParserTest.class.getResource(resourceName).toString(),
                    TextPos.START
            ));
            try {
                final BootstrapETLParserLite parser = new BootstrapETLParserLite(phraseParser);
                return parser.parse();
            } finally {
                phraseParser.close();
            }
        } catch (final Exception e) { // NOPMD
            throw new ParserException("io problem", e);
        }

    }

    /**
     * Test loading the grammar
     */
    @Test
    public void testDoctype() {
        final Grammar g = parseResource("/net/sf/etl/grammars/doctype-0_3_0.g.etl");
        assertNotNull(g);
    }


    /**
     * Test loading the grammar
     */
    @Test
    public void testDefault() {
        final Grammar g = parseResource("/net/sf/etl/grammars/default-0_3_0.g.etl");
        assertNotNull(g);
    }

    /**
     * Test loading the grammar
     */
    @Test
    public void testGrammar() {
        final Grammar g = parseResource("/net/sf/etl/grammars/grammar-0_3_0.g.etl");
        assertNotNull(g);
    }

}
