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
     * a base of number
     */
    public final int base;
    /**
     * a sign of the number (1 for positive numbers and -1 for negative)
     */
    public final int sign;

    /**
     * A constructor
     *
     * @param kind     A kind of token
     * @param base     a base of number
     * @param sign     a sign of the number (1 for positive numbers and -1 for
     *                 negative)
     * @param suffix   a suffix attached to number
     * @param text     a text of number with underscores removed.
     * @param exponent Exponent associated with the token
     */
    public NumberInfo(Tokens kind, int sign, int base, String text,
                      int exponent, String suffix) {
        super();
        this.base = base;
        this.exponent = exponent;
        this.kind = kind;
        this.sign = sign;
        this.suffix = suffix;
        this.text = text;
    }
}
