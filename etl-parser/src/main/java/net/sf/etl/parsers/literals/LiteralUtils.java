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

import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;

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
     * Parse number.
     *
     * @param input the input token
     * @return information about number.
     */
    public static NumberInfo parseNumber(final String input) {
        return parseNumber(input, TextPos.START, "unknown:");
    }

    /**
     * Parse number.
     *
     * @param input    the input token
     * @param start    the start position
     * @param systemId the system id
     * @return information about number.
     */
    public static NumberInfo parseNumber(final String input, final TextPos start, final String systemId) {
        return new NumberParser(input, start, systemId).parse();
    }


    /**
     * Parse text of integer token to integer value.
     *
     * @param intToken the integer token to parse
     * @return parsed value
     */
    public static int parseInt(final String intToken) {
        final NumberInfo n = parseNumber(intToken);
        n.checkErrors();
        return n.parseInt();
    }

    /**
     * Parse text of floating point or integer token to double.
     *
     * @param doubleToken the floating point or integer token to parse
     * @return parsed double
     */
    public static double parseDouble(final String doubleToken) {
        final NumberInfo n = parseNumber(doubleToken);
        n.checkErrors();
        return n.parseDouble();
    }

    /**
     * Parse text of string token to unicode characters. The string prefix is
     * ignored. Note it is assumed that the token has been already parsed by the
     * lexer, so minimal additional validation is performed.
     *
     * @param stringToken the string token to parse or null
     * @return parsed string or null if null has been passed as argument
     */
    public static String parseString(final String stringToken) {
        if (stringToken == null) {
            return null;
        }
        final StringInfo parseResult = new StringParser(stringToken, TextPos.START, "unknown:").parse();
        parseResult.checkErrors();
        return parseResult.getText();
    }

    /**
     * Parse text of string token to unicode characters. The string prefix is
     * ignored. Note it is assumed that the token has been already parsed by the
     * lexer, so minimal additional validation is performed.
     *
     * @param stringToken the string token to parse or null
     * @param systemId    the  report system id
     * @return parsed string or null if null has been passed as argument
     */
    public static String parseString(final Token stringToken, final String systemId) {
        if (stringToken == null) {
            return null;
        }
        final StringInfo parseResult = new StringParser(stringToken.text(), stringToken.start(), systemId).parse();
        parseResult.checkErrors();
        return parseResult.getText();
    }
}
