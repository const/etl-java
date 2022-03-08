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
package net.sf.etl.parsers.event.unstable.model.grammar;

import net.sf.etl.parsers.Token;

import java.util.LinkedList;
import java.util.List;

/**
 * The StringOp node class. This class is a part of the lightweight grammar
 * model.
 *
 * @author const
 */
public final class StringOp extends TokenRefOp {
    /**
     * the prefix for the string (identifier).
     */
    private final List<Token> prefix = new LinkedList<>();
    /**
     * the quote for the string (for symmetric case).
     */
    private Token quote;
    /**
     * the multiline flag.
     */
    private Token multiline;

    /**
     * @return the prefix for the string (identifier).
     */
    public List<Token> getPrefix() {
        return prefix;
    }

    /**
     * @return the quote for the string (for symmetric case).
     */
    public Token getQuote() {
        return quote;
    }

    /**
     * Set the quote.
     *
     * @param quote the quote.
     */
    public void setQuote(final Token quote) {
        this.quote = quote;
    }

    /**
     * @return the multiline flag.
     */
    public Token getMultiline() {
        return multiline;
    }

    /**
     * Set multiline.
     *
     * @param multiline the multiline flag.
     */
    public void setMultiline(final Token multiline) {
        this.multiline = multiline;
    }
}
