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

/**
 * The base class for the parsers.
 *
 * @param <T> the token type
 */
public abstract class AbstractReaderImpl<T> implements AbstractReader<T> {
    /**
     * The exception.
     */
    private ParserException exception;
    /**
     * True if the stream is closed.
     */
    private boolean closed;
    /**
     * The current token.
     */
    private T current;

    @Override
    public final boolean isValid() {
        return exception == null;
    }

    @Override
    public final boolean isClosed() {
        return closed;
    }

    @Override
    public final boolean advance() {
        ensureValid();
        try {
            return doAdvance();
        } catch (ParserException e) {
            exception = e;
            throw exception;
        } catch (Exception e) { // NOPMD
            exception = new ParserException("Exception during parsing", e);
            throw exception;
        }
    }

    /**
     * Do advancing using underlying resources.
     *
     * @return if moved to next token
     */
    protected abstract boolean doAdvance();

    /**
     * Ensure that the parser is in valid state.
     */
    protected final void ensureValid() {
        if (exception != null) {
            throw exception;
        }
        if (closed) {
            throw new ParserException("Parser is closed");
        }
    }

    /**
     * Set current token.
     *
     * @param current the current token
     */
    protected final void setCurrent(final T current) {
        this.current = current;
    }

    @Override
    public final T current() {
        ensureValid();
        if (current == null) {
            throw new ParserException("You need to advance first");
        }
        return current;
    }

    @Override
    public final void close() {
        try {
            if (closed) {
                closed = true;
                doClose();
            }
        } catch (Exception e) { // NOPMD
            throw new ParserIOException(e);
        }
    }

    /**
     * Perform close operation.
     *
     * @throws Exception if any problem
     */
    protected abstract void doClose() throws Exception;
}
