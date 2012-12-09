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

import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Tokens;

import java.math.BigInteger;

/**
 * This class contains utilities useful for examining string token contents.
 *
 * @author const
 */
public final class LiteralUtils {

    /**
     * a private constructor to prevent creation of class instances.
     */
    private LiteralUtils() {
    }

    /**
     * Parse number
     *
     * @param input an input token
     * @return information about number.
     */
    public static NumberInfo parseNumber(String input) {
        return new NumberParser(input).parse();
    }

    /**
     * Parse text of integer token to integer value.
     *
     * @param intToken a integer token to parse
     * @return parsed value
     */
    public static int parseInt(String intToken) {
        final NumberInfo n = parseNumber(intToken);
        n.checkErrors();
        if (n.kind != Tokens.INTEGER && n.kind != Tokens.INTEGER_WITH_SUFFIX) {
            throw new NumberFormatException("wrong token kind: " + n.kind);
        }
        String textToParse = n.text;
        if (n.sign == -1) {
            textToParse = "-" + textToParse;
        }
        return Integer.parseInt(textToParse, n.base);
    }

    /**
     * Parse text of floating point or integer token to double.
     *
     * @param doubleToken a floating point or integer token to parse
     * @return parsed double
     */
    public static double parseDouble(String doubleToken) {
        final NumberInfo n = parseNumber(doubleToken);
        n.checkErrors();
        BigInteger digits = new BigInteger((n.sign >= 0 ? "" : "-") + n.text,
                n.base);
        double exp = 1;
        int a = Math.abs(n.exponent);
        for (int i = 0; i < a; i++) {
            exp *= n.base;
        }
        double rc = digits.doubleValue();
        return n.exponent < 0 ? rc / exp : rc * exp;
    }

    /**
     * Parse text of string token to unicode characters. The string prefix is
     * ignored. Note it is assumed that the token has been already parsed by the
     * lexer, so minimal additional validation is performed.
     *
     * @param stringToken a string token to parse or null
     * @return parsed string or null if null has been passed as argument
     */
    public static String parseString(String stringToken) {
        final StringInfo parseResult = new StringParser(stringToken, TextPos.START, "unknown:").parse();
        parseResult.checkErrors();
        return parseResult.text;
    }

}
