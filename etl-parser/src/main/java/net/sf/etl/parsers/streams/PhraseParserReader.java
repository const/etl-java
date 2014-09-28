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

import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.event.Cell;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.PhraseParser;
import net.sf.etl.parsers.event.impl.PhraseParserImpl;

import java.net.URL;

/**
 * The reader for the phrase parser.
 */
public final class PhraseParserReader extends AbstractReaderImpl<PhraseToken> {
    /**
     * The underlying lexer.
     */
    private final Cell<Token> tokenCell = new Cell<Token>();
    /**
     * The lexer.
     */
    private final LexerReader lexer;
    /**
     * The phrase parser.
     */
    private final PhraseParser phraseParser = new PhraseParserImpl();

    /**
     * The constructor from lexer.
     *
     * @param lexer the base lexer
     */
    public PhraseParserReader(final LexerReader lexer) {
        this.lexer = lexer;
        this.phraseParser.start(lexer.getSystemId());
    }

    /**
     * The constructor from URL.
     *
     * @param configuration the configuration
     * @param url           the url to use
     */
    public PhraseParserReader(final TermReaderConfiguration configuration, final URL url) {
        this(new LexerReader(configuration, url));
    }

    @Override
    protected boolean doAdvance() {
        while (true) {
            final ParserState state = phraseParser.parse(tokenCell);
            switch (state) {
                case INPUT_NEEDED:
                    if (lexer.advance()) {
                        tokenCell.put(lexer.current());
                    } else {
                        throw new ParserException("Advancing should be possible before EOF: " + lexer);
                    }
                    break;
                case EOF:
                    return false;
                case OUTPUT_AVAILABLE:
                    setCurrent(phraseParser.read());
                    return true;
                default:
                    throw new ParserException("Invalid state from the phrase parser: " + state);
            }
        }
    }

    @Override
    protected void doClose() throws Exception {
        lexer.close();
    }

    @Override
    public String getSystemId() {
        return lexer.getSystemId();
    }
}
