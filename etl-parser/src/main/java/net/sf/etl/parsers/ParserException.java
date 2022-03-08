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
package net.sf.etl.parsers;

import java.io.Serial;

/**
 * Base class for all parser exceptions.
 *
 * @author const
 */
public class ParserException extends RuntimeException {
    /**
     * serial version id to make compiler happy.
     */
    @Serial
    private static final long serialVersionUID = 3834306233053427507L;

    /**
     * Constructor for the ParserException object.
     */
    public ParserException() {
        // do nothing
    }

    /**
     * Constructor for the ParserException object.
     *
     * @param msg message for exception
     */
    public ParserException(final String msg) {
        super(msg);
    }

    /**
     * Constructor for the ParserException object.
     *
     * @param msg message for exception
     * @param ex  exception
     */
    public ParserException(final String msg, final Throwable ex) {
        super(msg, ex);
    }
}
