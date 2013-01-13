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

package net.sf.etl.parsers.literals;

import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.characters.QuoteClass;

/**
 * Parsed string information
 */
public class StringInfo {
    /**
     * The input text
     */
    public final String input;
    /**
     * The token kind
     */
    public final Tokens kind;
    /**
     * The parsed text for the literal
     */
    public final String text;
    /**
     * The prefix for the string literal
     */
    public final String prefix;
    /**
     * The quote class
     */
    public final QuoteClass quoteClass;
    /**
     * The start quote codepoint
     */
    public final int startQuote;
    /**
     * The end quote codepoint
     */
    public final int endQuote;
    /**
     * The errors detected during literal parsing
     */
    public final ErrorInfo errors;

    /**
     * The constructor
     *
     * @param input      the input text
     * @param kind       the token kind for this string
     * @param text       the token text
     * @param prefix     the token prefix
     * @param quoteClass the quote class
     * @param startQuote the start quote codepoint
     * @param endQuote   the end quote codepoint
     * @param errors     the list of errors (if present)
     */
    public StringInfo(String input, Tokens kind, String text, String prefix, QuoteClass quoteClass,
                      int startQuote, int endQuote, ErrorInfo errors) {
        this.input = input;
        this.kind = kind;
        this.text = text;
        this.prefix = prefix;
        this.quoteClass = quoteClass;
        this.startQuote = startQuote;
        this.endQuote = endQuote;
        this.errors = errors;
    }

    /**
     * Check for errors and throw an exception if there are ones
     */
    public void checkErrors() {
        if (errors != null) {
            throw new LiteralParseException("string", input, errors);
        }
    }
}
