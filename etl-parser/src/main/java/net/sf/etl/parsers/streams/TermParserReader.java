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

import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.event.Cell;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.TermParser;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.event.impl.term.TermParserImpl;

import java.io.Reader;
import java.net.URL;

/**
 * The reader for term parser.
 */
public final class TermParserReader extends AbstractReaderImpl<TermToken> {
    /**
     * The parser configuration.
     */
    private final TermReaderConfiguration configuration;
    /**
     * The reader.
     */
    private final PhraseParserReader phraseParserReader;
    /**
     * Term parser implementation.
     */
    private final TermParser termParser;
    /**
     * The phrase parser.
     */
    private final Cell<PhraseToken> cell = new Cell<PhraseToken>();
    /**
     * The grammar resolver.
     */
    private GrammarResolver resolver;

    /**
     * The constructor from fields.
     *
     * @param configuration      the configuration
     * @param phraseParserReader the phrase parser reader
     * @param termParser         the term parser
     * @param resolver           the resolver
     */
    public TermParserReader(final TermReaderConfiguration configuration, final PhraseParserReader phraseParserReader,
                            final TermParser termParser, final GrammarResolver resolver) {
        this.configuration = configuration;
        this.phraseParserReader = phraseParserReader;
        this.termParser = termParser;
        this.resolver = resolver;
    }

    /**
     * The constructor that forces usage for the specific grammar.
     *
     * @param phraseParserReader the phrase parser
     * @param forcedGrammar      the grammar that is forced for the parser
     * @param scriptMode         if true, the grammar is used in script mode
     */
    public TermParserReader(final PhraseParserReader phraseParserReader, final CompiledGrammar forcedGrammar,
                            final boolean scriptMode) {
        this(DefaultTermReaderConfiguration.INSTANCE, phraseParserReader, forcedGrammar, scriptMode);
    }

    /**
     * The constructor that forces usage for the specific grammar.
     *
     * @param configuration      the configuration
     * @param phraseParserReader the phrase parser
     * @param forcedGrammar      the grammar that is forced for the parser
     * @param scriptMode         if true, the grammar is used in script mode
     */
    public TermParserReader(final TermReaderConfiguration configuration, final PhraseParserReader phraseParserReader,
                            final CompiledGrammar forcedGrammar, final boolean scriptMode) {
        this(configuration, phraseParserReader, new TermParserImpl(),
                configuration.getGrammarResolver(phraseParserReader.getSystemId()));
        this.termParser.forceGrammar(forcedGrammar, scriptMode);
        this.termParser.start(phraseParserReader.getSystemId());
    }

    /**
     * The constructor.
     *
     * @param phraseParserReader the phrase parser reader
     */
    public TermParserReader(final PhraseParserReader phraseParserReader) {
        this(DefaultTermReaderConfiguration.INSTANCE, phraseParserReader);
    }

    /**
     * The constructor.
     *
     * @param configuration      the configuration
     * @param phraseParserReader the phrase parser reader
     */
    public TermParserReader(final TermReaderConfiguration configuration, final PhraseParserReader phraseParserReader) {
        this(configuration, phraseParserReader, new TermParserImpl(),
                configuration.getGrammarResolver(phraseParserReader.getSystemId()));
        this.termParser.start(phraseParserReader.getSystemId());
    }

    /**
     * Create parser basing on url.
     *
     * @param url the url to use
     */
    public TermParserReader(final URL url) {
        this(DefaultTermReaderConfiguration.INSTANCE, url);
    }

    /**
     * Create parser basing on url.
     *
     * @param configuration the configuration
     * @param url           the url to use
     */
    public TermParserReader(final TermReaderConfiguration configuration, final URL url) {
        this(configuration, new PhraseParserReader(configuration, url));
    }

    /**
     * The constructor from reader and system id.
     *
     * @param configuration the configuration
     * @param reader        the reader
     * @param systemId      the system id
     */
    public TermParserReader(final TermReaderConfiguration configuration, final Reader reader, final String systemId) {
        this(configuration, new PhraseParserReader(new LexerReader(configuration, reader, systemId, TextPos.START)));
    }

    @Override
    protected boolean doAdvance() {
        while (true) {
            final ParserState state = termParser.parse(cell);
            switch (state) {
                case RESOURCE_NEEDED:
                    resolver.resolve(termParser);
                    break;
                case EOF:
                    return false;
                case OUTPUT_AVAILABLE:
                    final TermToken token = termParser.read();
                    setCurrent(token);
                    return true;
                case INPUT_NEEDED:
                    if (phraseParserReader.advance()) {
                        cell.put(phraseParserReader.current());
                    } else {
                        throw new IllegalStateException("No input from phrase parser before EOF");
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown parser state: " + state);
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
     * @return the current parser configuration
     */
    public TermReaderConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Change resolver for the parser.
     *
     * @param resolver the resolver
     */
    public void setResolver(final GrammarResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Set default grammar.
     *
     * @param grammarPublicId the public id of the grammar
     * @param grammarSystemId the system id of th grammar
     * @param defaultContext  the default context
     * @param scriptMode      the script mode
     */
    public void setDefaultGrammar(final String grammarPublicId, final String grammarSystemId,
                                  final String defaultContext, final boolean scriptMode) {
        this.termParser.setDefaultGrammar(grammarPublicId, grammarSystemId, defaultContext, scriptMode);
    }
}
