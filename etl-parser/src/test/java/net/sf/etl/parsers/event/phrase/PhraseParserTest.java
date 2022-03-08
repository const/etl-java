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

package net.sf.etl.parsers.event.phrase;

import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.PhraseTokens;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.event.Cell;
import net.sf.etl.parsers.event.Lexer;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.PhraseParser;
import net.sf.etl.parsers.event.impl.LexerImpl;
import net.sf.etl.parsers.event.impl.PhraseParserImpl;
import org.junit.jupiter.api.Test;

import java.nio.CharBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The test for phrase parser
 */
public class PhraseParserTest {
    private Cell<Token> cell = new Cell<Token>();
    private Lexer lexer = new LexerImpl();
    private CharBuffer buffer;
    private PhraseParser phraseParser = new PhraseParserImpl();
    private PhraseToken current;

    @Test
    public void simple() {
        start("");
        read(PhraseTokens.EOF);
        start("{}");
        read(PhraseTokens.START_BLOCK);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.END_BLOCK);
        read(PhraseTokens.STATEMENT_END);
        read(PhraseTokens.EOF);
        start("{{;};}");
        read(PhraseTokens.START_BLOCK);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.START_BLOCK);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.END_BLOCK);
        read(PhraseTokens.STATEMENT_END);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.END_BLOCK);
        read(PhraseTokens.STATEMENT_END);
        read(PhraseTokens.EOF);
    }

    @Test
    public void simpleError() {
        start("b\n }{a");
        read(PhraseTokens.SIGNIFICANT);
        read(PhraseTokens.SOFT_STATEMENT_END);
        read(PhraseTokens.IGNORABLE);
        read(PhraseTokens.IGNORABLE);
        read(PhraseTokens.STATEMENT_END);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.START_BLOCK);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.SIGNIFICANT);
        read(PhraseTokens.STATEMENT_END);
        read(PhraseTokens.END_BLOCK);
        read(PhraseTokens.STATEMENT_END);
        read(PhraseTokens.EOF);
    }

    private void next() {
        final ParserState r = phraseParser.parse(cell);
        switch (r) {
            case OUTPUT_AVAILABLE:
                current = phraseParser.read();
                return;
            case INPUT_NEEDED:
                final ParserState state = lexer.parse(buffer, true);
                assertEquals(ParserState.OUTPUT_AVAILABLE, state);
                cell.put(lexer.read());
                next();
                return;
            default:
                throw new IllegalStateException("Unknown state: " + r);
        }
    }

    private void read(final PhraseTokens kind) {
        next();
        assertEquals(kind, current.kind());
    }

    private void start(final String text) {
        buffer = CharBuffer.wrap(text);
        cell = new Cell<Token>();
        lexer = new LexerImpl();
        lexer.start("test:test", TextPos.START);
        phraseParser = new PhraseParserImpl();
        current = null;
    }


}
