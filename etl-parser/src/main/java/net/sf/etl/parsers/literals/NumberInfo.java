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

import java.math.BigInteger;

/**
 * Information about number that is being parsed.
 */
public class NumberInfo {
    /**
     * The kind of number
     */
    public final Tokens kind;
    /**
     * The text of number with underscores removed.
     */
    public final String text;
    /**
     * The suffix attached to number
     */
    public final String suffix;
    /**
     * The exponent (adjusted according the dot position)
     */
    public final int exponent;
    /**
     * The input string
     */
    public final String input;
    /**
     * The base of number
     */
    public final int base;
    /**
     * The sign of the number (1 for positive numbers and -1 for negative)
     */
    public final int sign;
    /**
     * The errors
     */
    public final ErrorInfo errors;

    /**
     * The constructor
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

    /**
     * Parse as integer value
     *
     * @return the parsed value
     */
    public int parseInt() {
        if (kind != Tokens.INTEGER && kind != Tokens.INTEGER_WITH_SUFFIX) {
            throw new NumberFormatException("wrong token kind: " + kind);
        }
        String textToParse = text;
        if (sign == -1) {
            textToParse = "-" + textToParse;
        }
        return Integer.parseInt(textToParse, base);
    }


    /**
     * Parse as double value
     *
     * @return the parsed value
     */
    public double parseDouble() {
        BigInteger digits = new BigInteger((sign >= 0 ? "" : "-") + text, base);
        double exp = 1;
        int a = Math.abs(exponent);
        for (int i = 0; i < a; i++) {
            exp *= base;
        }
        double rc = digits.doubleValue();
        return exponent < 0 ? rc / exp : rc * exp;
    }
}
