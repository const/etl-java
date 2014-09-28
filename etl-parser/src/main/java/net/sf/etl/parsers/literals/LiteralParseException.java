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
import net.sf.etl.parsers.ParserException;

/**
 * The exception related to literal parsing.
 */
public class LiteralParseException extends ParserException {
    /**
     * uid.
     */
    private static final long serialVersionUID = -6558961969795377492L;

    /**
     * The exception constructor.
     *
     * @param type   the type of object
     * @param text   the text of the token
     * @param errors the errors related to token
     */
    public LiteralParseException(final String type, final String text, final ErrorInfo errors) {
        super(message(type, text, errors));
    }

    /**
     * Create error message.
     *
     * @param type   the type of object to be parsed
     * @param text   the text of the token
     * @param errors the list of errors
     * @return the message for the exception
     */
    private static String message(final String type, final String text, final ErrorInfo errors) {
        final StringBuilder b = new StringBuilder(); // NOPMD
        b.append("Error parsing ").append(type).append(" '").append(text).append("':");
        for (ErrorInfo e = errors; e != null; e = e.cause()) {
            b.append("\n  ").append(errors.message()).append(' ').append(errors.end());
        }
        return b.toString();
    }
}
