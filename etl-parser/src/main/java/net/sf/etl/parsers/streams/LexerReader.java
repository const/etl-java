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

import net.sf.etl.parsers.DefaultTermParserConfiguration;
import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.ParserIOException;
import net.sf.etl.parsers.TermParserConfiguration;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.event.Lexer;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.impl.LexerImpl;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * The reader for the lexer.
 */
public final class LexerReader extends AbstractReaderImpl<Token> {
    /**
     * The UTF8 charset.
     */
    public static final Charset UTF8 = Charset.forName("UTF-8");
    /**
     * Reader.
     */
    private final Reader input;
    /**
     * The lexer.
     */
    private final Lexer lexer;
    /**
     * The buffer to use for IO.
     */
    private final CharBuffer buffer = CharBuffer.allocate(1024);
    /**
     * System id.
     */
    private final String systemId;
    /**
     * True if EOF has been read.
     */
    private boolean eofRead = false;


    /**
     * The constructor.
     *
     * @param input    the input
     * @param systemId the system id
     * @param start    the start position for the lexer
     */
    public LexerReader(final Reader input, final String systemId, final TextPos start) {
        this(DefaultTermParserConfiguration.INSTANCE, input, systemId, start);
    }

    /**
     * The constructor.
     *
     * @param configuration the configuration
     * @param input         the input
     * @param systemId      the system id
     * @param start         the start position for the lexer
     */
    public LexerReader(final TermParserConfiguration configuration, final Reader input, final String systemId,
                       final TextPos start) {
        this.input = input;
        this.systemId = systemId;
        lexer = new LexerImpl(configuration);
        lexer.start(systemId, start);
        buffer.position(0).limit(0);
    }

    /**
     * The constructor from url, it opens resource and starts reading its content. The assumed encoding is UTF-8.
     *
     * @param configuration the configuration
     * @param url           the url of resource
     */
    public LexerReader(final TermParserConfiguration configuration, final URL url) {
        this(createReader(configuration, url), url.toString(), TextPos.START);
    }

    /**
     * The constructor from url, it opens resource and starts reading its content. The assumed encoding is UTF-8.
     *
     * @param url the url of resource
     */
    public LexerReader(final URL url) {
        this(DefaultTermParserConfiguration.INSTANCE, url);
    }

    /**
     * Open reader by URL.
     *
     * @param configuration the configuration
     * @param url           the URL to open
     * @return the corresponding reader
     */
    private static Reader createReader(final TermParserConfiguration configuration, final URL url) {
        try {
            return configuration.openReader(url.toString());
        } catch (IOException ex) {
            throw new ParserIOException("Unable to open resource: " + url, ex);
        }
    }


    @Override
    protected boolean doAdvance() {
        setCurrent(null);
        while (true) {
            final ParserState state = lexer.parse(buffer, eofRead);
            switch (state) {
                case OUTPUT_AVAILABLE:
                    setCurrent(lexer.read());
                    return true;
                case EOF:
                    return false;
                case INPUT_NEEDED:
                    buffer.compact();
                    final int n;
                    try {
                        n = input.read(buffer.array(), buffer.arrayOffset() + buffer.position(),
                                buffer.limit() - buffer.position());
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
