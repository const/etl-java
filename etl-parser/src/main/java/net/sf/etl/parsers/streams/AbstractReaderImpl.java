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

import net.sf.etl.parsers.AbstractToken;
import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.ParserIOException;

/**
 * The base class for the parsers
 */
public abstract class AbstractReaderImpl<T extends AbstractToken> implements AbstractReader<T> {
    /**
     * The exception
     */
    ParserException exception;
    /**
     * True if the stream is closed
     */
    boolean closed;
    /**
     * The current token
     */
    T current;

    @Override
    public boolean isValid() {
        return exception == null;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public boolean advance() {
        ensureValid();
        try {
            return doAdvance();
        } catch (ParserException e) {
            exception = e;
            throw exception;
        } catch (Exception e) {
            exception = new ParserException("Exception during parsing", e);
            throw exception;
        }
    }

    /**
     * Do advancing using underlying resources
     *
     * @return if moved to next token
     */
    protected abstract boolean doAdvance();

    /**
     * Ensure that the parser is in valid state
     */
    protected void ensureValid() {
        if (exception != null) {
            throw exception;
        }
        if (closed) {
            throw new ParserException("Parser is closed");
        }
    }

    @Override
    public T current() {
        ensureValid();
        if (current == null) {
            throw new ParserException("You need to advance first");
        }
        return current;
    }

    @Override
    public void close() {
        try {
            doClose();
            closed = true;
        } catch (Exception e) {
            throw new ParserIOException(e);
        }
    }

    protected abstract void doClose() throws Exception;
}
