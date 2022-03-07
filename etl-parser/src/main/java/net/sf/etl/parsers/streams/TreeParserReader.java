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

import net.sf.etl.parsers.LoadedGrammarInfo;
import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.event.Cell;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.tree.ObjectFactory;
import net.sf.etl.parsers.event.tree.ObjectFactoryTreeParser;
import net.sf.etl.parsers.event.tree.TokenCollector;
import net.sf.etl.parsers.event.tree.TreeParser;

/**
 * The tree reader.
 *
 * @param <Element> the base class for parsed objects.
 */
public class TreeParserReader<Element> extends AbstractReaderImpl<Element> {
    /**
     * The reader.
     */
    private final TermParserReader termParserReader;
    /**
     * The tree parser.
     */
    private final TreeParser<Element> treeParser;
    /**
     * The underlying lexer.
     */
    private final Cell<TermToken> tokenCell = new Cell<>();
    /**
     * The loaded grammar.
     */
    private LoadedGrammarInfo loadedGrammar;

    /**
     * The constructor.
     *
     * @param termParserReader the term parser reader
     * @param objectFactory    the object factory
     */
    public TreeParserReader(final TermParserReader termParserReader,
                            final ObjectFactory<Element, ?, ?, ?> objectFactory) {
        this.termParserReader = termParserReader;
        this.treeParser = ObjectFactoryTreeParser.make(objectFactory, termParserReader.getSystemId());
    }

    @Override
    protected final boolean doAdvance() {
        while (true) {
            final ParserState state = treeParser.parse(tokenCell);
            switch (state) {
                case INPUT_NEEDED:
                    if (termParserReader.advance()) {
                        final TermToken current = termParserReader.current();
                        if (current.kind() == Terms.GRAMMAR_IS_LOADED) {
                            loadedGrammar = current.loadedGrammar();
                        }
                        tokenCell.put(current);
                    } else {
                        throw new ParserException("Advancing should be possible before EOF: " + termParserReader);
                    }
                    break;
                case EOF:
                    return false;
                case OUTPUT_AVAILABLE:
                    setCurrent(treeParser.read());
                    return true;
                default:
                    throw new ParserException("Invalid state from the phrase parser: " + state);
            }
        }
    }

    /**
     * @return the loaded grammar, if grammar loading already happened.
     */
    public final LoadedGrammarInfo getLoadedGrammar() {
        return loadedGrammar;
    }

    @Override
    protected final void doClose() throws Exception {
        termParserReader.close();
    }

    @Override
    public final String getSystemId() {
        return termParserReader.getSystemId();
    }

    /**
     * Set handler for error tokens.
     *
     * @param errorTokenHandler the handler
     */
    public final void setErrorTokenHandler(final TokenCollector errorTokenHandler) {
        treeParser.setErrorTokenHandler(errorTokenHandler);
    }

    /**
     * Set handlers for unexpected tokens.
     *
     * @param unexpectedTokenHandler the handler
     */
    public final void setUnexpectedTokenHandler(final TokenCollector unexpectedTokenHandler) {
        treeParser.setUnexpectedTokenHandler(unexpectedTokenHandler);
    }

    /**
     * Add token listener.
     *
     * @param listener the listener
     */
    public final void addTokenListener(final TokenCollector listener) {
        treeParser.addTokenListener(listener);
    }

    /**
     * Remove token listener.
     *
     * @param listener the listener
     */
    public final void removeTokenListener(final TokenCollector listener) {
        treeParser.removeTokenListener(listener);
    }

    /**
     * @return true if there were errors
     */
    public final boolean hadErrors() {
        return treeParser.hadErrors();
    }
}
