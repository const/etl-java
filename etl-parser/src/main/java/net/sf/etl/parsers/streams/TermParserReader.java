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

package net.sf.etl.parsers.streams;

import net.sf.etl.parsers.*;
import net.sf.etl.parsers.event.Cell;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.TermParser;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.event.impl.term.TermParserImpl;
import net.sf.etl.parsers.streams.util.DefaultGrammarResolver;

import java.io.Reader;
import java.net.URL;

/**
 * The reader for term parser
 */
public class TermParserReader extends AbstractReaderImpl<TermToken> {
    /**
     * The parser configuration
     */
    private final TermParserConfiguration configuration;
    /**
     * The reader
     */
    private final PhraseParserReader phraseParserReader;
    /**
     * Term parser implementation
     */
    private final TermParser termParser = new TermParserImpl();
    /**
     * The phrase parser
     */
    private final Cell<PhraseToken> cell = new Cell<PhraseToken>();
    /**
     * The grammar resolver
     */
    private GrammarResolver resolver = DefaultGrammarResolver.INSTANCE;

    /**
     * The constructor that forces usage for the specific grammar
     *
     * @param phraseParserReader the phrase parser
     * @param forcedGrammar      the grammar that is forced for the parser
     * @param scriptMode         if true, the grammar is used in script mode
     */
    public TermParserReader(PhraseParserReader phraseParserReader, CompiledGrammar forcedGrammar, boolean scriptMode) {
        this(DefaultTermParserConfiguration.INSTANCE, phraseParserReader, forcedGrammar, scriptMode);
    }

    /**
     * The constructor that forces usage for the specific grammar
     *
     * @param phraseParserReader the phrase parser
     * @param forcedGrammar      the grammar that is forced for the parser
     * @param scriptMode         if true, the grammar is used in script mode
     */
    public TermParserReader(TermParserConfiguration configuration, PhraseParserReader phraseParserReader,
                            CompiledGrammar forcedGrammar, boolean scriptMode) {
        this.configuration = configuration;
        this.phraseParserReader = phraseParserReader;
        this.termParser.forceGrammar(forcedGrammar, scriptMode);
        this.termParser.start(phraseParserReader.getSystemId());
    }

    /**
     * The constructor
     *
     * @param phraseParserReader the phrase parser reader
     */
    public TermParserReader(PhraseParserReader phraseParserReader) {
        this(DefaultTermParserConfiguration.INSTANCE, phraseParserReader);
    }

    /**
     * The constructor
     *
     * @param phraseParserReader the phrase parser reader
     */
    public TermParserReader(TermParserConfiguration configuration, PhraseParserReader phraseParserReader) {
        this.configuration = configuration;
        this.phraseParserReader = phraseParserReader;
        this.termParser.start(phraseParserReader.getSystemId());
    }

    /**
     * Create parser basing on url
     *
     * @param url the url to use
     */
    public TermParserReader(URL url) {
        this(DefaultTermParserConfiguration.INSTANCE, url);
    }

    /**
     * Create parser basing on url
     *
     * @param url the url to use
     */
    public TermParserReader(TermParserConfiguration configuration, URL url) {
        this(configuration, new PhraseParserReader(configuration, url));
    }

    /**
     * Start from reader
     *
     * @param reader   the reader
     * @param systemId the system id
     * @param start    the start position
     */
    public TermParserReader(Reader reader, String systemId, TextPos start) {
        this(new PhraseParserReader(new LexerReader(reader, systemId, start)));
    }

    /**
     * Start from reader
     *
     * @param reader   the reader
     * @param systemId the system id
     */
    public TermParserReader(Reader reader, String systemId) {
        this(reader, systemId, TextPos.START);
    }

    @Override
    protected boolean doAdvance() {
        while (true) {
            ParserState state = termParser.parse(cell);
            switch (state) {
                case RESOURCE_NEEDED:
                    resolver.resolve(termParser);
                    break;
                case EOF:
                    return false;
                case OUTPUT_AVAILABLE:
                    current = termParser.read();
                    return true;
                case INPUT_NEEDED:
                    if (phraseParserReader.advance()) {
                        cell.put(phraseParserReader.current());
                    } else {
                        throw new IllegalStateException("No input from phrase parser before EOF");
                    }
                    break;
            }
        }
    }

    @Override
    protected void doClose() throws Exception {
        phraseParserReader.close();
    }

    @Override
    public String getSystemId() {
        return termParser.getSystemId();
    }

    /**
     * Change resolver for th parser
     *
     * @param resolver the resolver
     */
    public void setResolver(GrammarResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Set default grammar
     *
     * @param grammarPublicId the public id of the grammar
     * @param grammarSystemId the system id of th grammar
     * @param defaultContext  the default context
     * @param scriptMode      the script mode
     */
    public void setDefaultGrammar(String grammarPublicId, String grammarSystemId, String defaultContext, boolean scriptMode) {
        this.termParser.setDefaultGrammar(grammarPublicId, grammarSystemId, defaultContext, scriptMode);
    }
}
