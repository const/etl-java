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

package net.sf.etl.parsers.event.tree;

import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.event.Cell;
import net.sf.etl.parsers.event.ParserState;

/**
 * AST parser interface.
 *
 * @param <BaseClass> the base class
 */
public interface TreeParser<BaseClass> {
    /**
     * @return read current object from parser
     */
    BaseClass read();

    /**
     * Parse token.
     *
     * @param token the cell with the token. The element is removed if it is consumed and more date is needed.
     * @return the parsed state
     */
    ParserState parse(Cell<TermToken> token);

    /**
     * @return get system id
     */
    String getSystemId();

    /**
     * Set handler for error tokens.
     *
     * @param errorTokenHandler the handler
     */
    void setErrorTokenHandler(final TokenCollector errorTokenHandler);

    /**
     * Set handlers for unexpected tokens.
     *
     * @param unexpectedTokenHandler the handler
     */
    void setUnexpectedTokenHandler(final TokenCollector unexpectedTokenHandler);

    /**
     * Add token listener.
     *
     * @param listener the listener
     */
    void addTokenListener(final TokenCollector listener);

    /**
     * Remove token listener.
     *
     * @param listener the listener
     */
    void removeTokenListener(final TokenCollector listener);

    /**
     * @return true if there were errors
     */
    boolean hadErrors();
}
