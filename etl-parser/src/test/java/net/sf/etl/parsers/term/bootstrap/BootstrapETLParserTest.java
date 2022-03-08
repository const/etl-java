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
package net.sf.etl.parsers.term.bootstrap;

import net.sf.etl.parsers.GrammarId;
import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.event.impl.bootstrap.BootstrapETLParserLite;
import net.sf.etl.parsers.event.unstable.model.grammar.Grammar;
import net.sf.etl.parsers.streams.DefaultTermReaderConfiguration;
import net.sf.etl.parsers.streams.LexerReader;
import net.sf.etl.parsers.streams.PhraseParserReader;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
     * @param grammarId name of grammar
     * @return parsed grammar
     */
    private Grammar parseResource(final GrammarId grammarId) {
        try {
            URL resource = StandardGrammars.getGrammarResource(BootstrapETLParserTest.class.getClassLoader(), grammarId);
            final PhraseParserReader phraseParser = new PhraseParserReader(new LexerReader(
                    DefaultTermReaderConfiguration.INSTANCE,
                    new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8),
                    resource.toString(),
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
        final Grammar g = parseResource(StandardGrammars.DOCTYPE_GRAMMAR_ID);
        assertNotNull(g);
    }


    /**
     * Test loading the grammar
     */
    @Test
    public void testDefault() {
        final Grammar g = parseResource(StandardGrammars.DEFAULT_GRAMMAR_ID);
        assertNotNull(g);
    }

    /**
     * Test loading the grammar
     */
    @Test
    public void testGrammar() {
        final Grammar g = parseResource(StandardGrammars.ETL_GRAMMAR_ID);
        assertNotNull(g);
    }

}
