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

/**
 * The abstract parser.
 *
 * @param <TokenType> the token type
 */
public interface AbstractReader<TokenType> {

    /**
     * @return system id associated with this parser
     */
    String getSystemId();

    /**
     * Move to next token in the stream.
     *
     * @return true if next token was parsed, false if end of file is reached an
     * no more tokens are available.
     * @throws net.sf.etl.parsers.ParserIOException if there is IO problem.
     */
    boolean advance();

    /**
     * Closes lexer and underlying stream if there is one.
     *
     * @throws net.sf.etl.parsers.ParserIOException if there is IO problem during close.
     */
    void close();

    /**
     * @return true if parser is valid and still open. If method advance or
     * current had thrown exception, than parser becomes invalid.
     */
    boolean isValid();

    /**
     * @return true if parser has been closed.
     */
    boolean isClosed();

    /**
     * @return current token
     * @see #advance()
     */
    TokenType current();

    /**
     * @return current token or null if token has not been read yet.
     * @see #advance()
     */
    TokenType currentOrNull();

}
