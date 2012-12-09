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

package net.sf.etl.parsers.literals;

import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.Tokens;

/**
 * Information about number that is being parsed.
 */
public class NumberInfo {
    /**
     * A kind of number
     */
    public final Tokens kind;
    /**
     * a text of number with underscores removed.
     */
    public final String text;
    /**
     * a suffix attached to number
     */
    public final String suffix;
    /**
     * exponent (adjusted according the dot position)
     */
    public final int exponent;
    /**
     * The input string
     */
    public final String input;
    /**
     * a base of number
     */
    public final int base;
    /**
     * a sign of the number (1 for positive numbers and -1 for negative)
     */
    public final int sign;
    /**
     * the errors
     */
    public final ErrorInfo errors;

    /**
     * A constructor
     *
     * @param kind     the kind of token
     * @param sign     the sign of the number (1 for positive numbers and -1 for
     *                 negative, 0 for unspecified)
     * @param base     the base of number
     * @param text     the text of number with underscores removed.
     * @param exponent the exponent associated with the token
     * @param suffix   the suffix attached to number
     * @param errors   the errors collected during parsing
     */
    public NumberInfo(String input, Tokens kind, int sign, int base, String text,
                      int exponent, String suffix, ErrorInfo errors) {
        super();
        this.input = input;
        this.base = base;
        this.exponent = exponent;
        this.kind = kind;
        this.sign = sign;
        this.suffix = suffix;
        this.text = text;
        this.errors = errors;
    }

    /**
     * Check for errors and throw an exception if there are ones
     */
    public void checkErrors() {
        if (errors != null) {
            throw new LiteralParseException("number", input, errors);
        }
    }

}
