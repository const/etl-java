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
import net.sf.etl.parsers.ParserIOException;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.event.Lexer;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.impl.LexerImpl;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * The reader for the lexer
 */
public class LexerReader extends AbstractReaderImpl<Token> {
    /**
     * Reader
     */
    final Reader input;
    /**
     * System id
     */
    private final String systemId;
    /**
     * The lexer
     */
    final Lexer lexer = new LexerImpl();
    /**
     * The buffer to use for IO
     */
    final CharBuffer buffer = CharBuffer.allocate(1024);
    /**
     * True if EOF has been read
     */
    boolean eofRead = false;


    public LexerReader(Reader input, String systemId, TextPos start) {
        this.input = input;
        this.systemId = systemId;
        lexer.start(systemId, start);
        buffer.position(0).limit(0);
    }


    @Override
    protected boolean doAdvance() {
        current = null;
        while (true) {
            ParserState state = lexer.parse(buffer, eofRead);
            switch (state) {
                case OUTPUT_AVAILABLE:
                    current = lexer.read();
                    return true;
                case EOF:
                    return false;
                case INPUT_NEEDED:
                    buffer.compact();
                    int n;
                    try {
                        n = input.read(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.limit() - buffer.position());
                    } catch (IOException e) {
                        throw new ParserIOException(e);
                    }
                    if (n < 0) {
                        eofRead = true;
                    } else {
                        buffer.position(buffer.position() + n);
                    }
                    buffer.flip();
                    break;
                default:
                    throw new ParserException("Unexpected lexer state: " + state);
            }
        }
    }

    @Override
    protected void doClose() throws Exception {
        input.close();
    }

    @Override
    public String getSystemId() {
        return systemId;
    }
}
