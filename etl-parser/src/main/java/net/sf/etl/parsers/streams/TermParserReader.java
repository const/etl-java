/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2012 Constantine A Plotnikov
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
import net.sf.etl.parsers.event.Cell;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.TermParser;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.event.impl.TermParserImpl;

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

    @Override
    protected boolean doAdvance() {
        while (true) {
            ParserState state = termParser.parse(cell);
            switch (state) {
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
}
