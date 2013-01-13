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
package net.sf.etl.parsers.event.grammar.impl;

/**
 * This exception is used to interrupt compilation process in case of errors
 *
 * @author const
 */
public class CompilationException extends RuntimeException {
    /**
     * make compiler happy
     */
    private static final long serialVersionUID = 3618985563861168436L;
    /**
     * The id of the exception
     */
    public final String errorId;
    /**
     * The arguments of the exception
     */
    public final String errorArgs[];

    /**
     * The constructor
     *
     * @param errorId   the id of the exception
     * @param errorArgs the args of the exception
     */
    public CompilationException(String errorId, String[] errorArgs) {
        super();
        this.errorId = errorId;
        this.errorArgs = errorArgs;
    }

}
