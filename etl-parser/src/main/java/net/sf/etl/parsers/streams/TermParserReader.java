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
import net.sf.etl.parsers.event.grammar.BootstrapGrammars;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.event.impl.term.TermParserImpl;
import net.sf.etl.parsers.resource.ResolvedObject;
import net.sf.etl.parsers.resource.ResourceDescriptor;
import net.sf.etl.parsers.resource.ResourceRequest;
import net.sf.etl.parsers.resource.ResourceUsage;
import net.sf.etl.parsers.streams.util.CachingGrammarResolver;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

/**
 * The reader for term parser
 */
public class TermParserReader extends AbstractReaderImpl<TermToken> {
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
    private GrammarResolver resolver = CachingGrammarResolver.INSTANCE;

    /**
     * The constructor that forces usage for the specific grammar
     *
     * @param phraseParserReader the phrase parser
     * @param forcedGrammar      the grammar that is forced for the parser
     * @param scriptMode         if true, the grammar is used in script mode
     */
    public TermParserReader(PhraseParserReader phraseParserReader, CompiledGrammar forcedGrammar, boolean scriptMode) {
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
        this.phraseParserReader = phraseParserReader;
        this.termParser.start(phraseParserReader.getSystemId());
    }

    /**
     * Create parser basing on url
     *
     * @param url the url to use
     */
    public TermParserReader(URL url) {
        this(new PhraseParserReader(url));
    }

    @Override
    protected boolean doAdvance() {
        while (true) {
            ParserState state = termParser.parse(cell);
            switch (state) {
                case RESOURCE_NEEDED:
                    Cell<ErrorInfo> errors = new Cell<ErrorInfo>();
                    ResolvedObject<CompiledGrammar> resolve;
                    final ResourceRequest request = termParser.grammarRequest();
                    try {
                        resolve = resolver.resolve(request, errors);
                    } catch (Throwable t) {
                        // TODO LOG it
                        t.printStackTrace();
                        resolve = new ResolvedObject<CompiledGrammar>(request,
                                Collections.<ResourceUsage>emptyList(),
                                new ResourceDescriptor(SourceLocation.UNKNOWN.systemId()),
                                BootstrapGrammars.defaultGrammar());
                        if (errors.isEmpty()) {
                            errors.put(new ErrorInfo("syntax.FailedToResolve",
                                    Collections.<Object>unmodifiableList(
                                            Arrays.asList(request.getReference().getSystemId(),
                                                    request.getReference().getPublicId(), t.toString())),
                                    new SourceLocation(TextPos.START, TextPos.START, termParser.getSystemId()), null));
                        }
                    }
                    termParser.provideGrammar(resolve, errors.isEmpty() ? null : errors.take());
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
}
