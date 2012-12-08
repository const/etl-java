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
import net.sf.etl.parsers.characters.Identifiers;
import net.sf.etl.parsers.characters.Numbers;
import net.sf.etl.parsers.characters.QuoteClass;

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
     * // TODO StringInfo
     *
     * @param stringToken a string token to parse or null
     * @return parsed string or null if null has been passed as argument
     */
    public static String parseString(String stringToken) {
        if (stringToken == null) {
            return null;
        }
        final StringBuilder rc = new StringBuilder();
        int n = stringToken.length();
        if (n < 2) {
            throw new IllegalArgumentException("Unexpected end of the token "
                    + n + " in " + stringToken);
        }
        int i = 0;
        int codepoint;
        while (Identifiers.isIdentifierPart(codepoint = stringToken.codePointAt(i))) {
            i += Character.charCount(codepoint);
        }
        QuoteClass quoteClass = QuoteClass.classify(codepoint);
        if (quoteClass == null) {
            throw new IllegalArgumentException("Invalid quote codepoint: " + codepoint + " in string: " + stringToken);
        }
        final int quote = codepoint;
        final int quoteSize = Character.charCount(codepoint);
        boolean multiline = stringToken.length() > quoteSize * 3 + i
                && stringToken.codePointAt(i + quoteSize) == quote
                && stringToken.codePointAt(i + 2 * quoteSize) == quote;
        // ignore last and first characters
        i += (multiline ? 3 : 1) * quoteSize;
        final int endQuote = stringToken.codePointBefore(n);
        final QuoteClass endQuoteClass = QuoteClass.classify(endQuote);
        if (endQuoteClass == null) {
            throw new IllegalArgumentException("The terminating code point is not a quote: " + codepoint + " in " + stringToken);
        }
        if (endQuoteClass != quoteClass) {
            throw new IllegalArgumentException("The startQuote(" + quote + ":" + quoteClass +
                    ") and endQuote(" + endQuote + ":" + endQuoteClass + ") belong to different quote classes: " + stringToken);
        }
        final int endQuoteSize = Character.charCount(endQuote);
        if (multiline && (endQuote != stringToken.codePointAt(n - 2 * endQuoteSize) ||
                endQuote != stringToken.codePointAt(n - 3 * endQuoteSize) ||
                n - i < quoteSize * 3)) {
            throw new IllegalArgumentException("The multiline string should end with three quotes: " + stringToken);
        }
        n -= (multiline ? 3 : 1) * endQuoteSize;
        if (i > n) {
            throw new IllegalArgumentException("The string is in invalid format: " + stringToken);
        }
        while (i < n) {
            codepoint = stringToken.codePointAt(i);
            i += Character.charCount(codepoint);
            switch (codepoint) {
                case '\\':
                    if (i >= n) {
                        throw new IllegalArgumentException(
                                "Unexpected end of the token " + i);
                    }
                    codepoint = stringToken.codePointAt(i);
                    i += Character.charCount(codepoint);
                    switch (codepoint) {
                        // TODO \U8d
                        case 'U':
                            final int start = i;
                            do {
                                codepoint = stringToken.codePointAt(i);
                                i += Character.charCount(codepoint);
                                if (codepoint == ';') {
                                    break;
                                }
                                if (!Numbers.isHex(codepoint)) {
                                    throw new IllegalArgumentException(
                                            "Invalid symbol in escape sequence " + codepoint + " in string " + stringToken);
                                }
                            }
                            while (i < n);
                            if (i == start || stringToken.codePointBefore(i) != ';') {
                                throw new IllegalArgumentException(
                                        "Unexpected end of the escape sequence " + i + " in " + stringToken);
                            }
                            final int cp = Integer.parseInt(stringToken
                                    .substring(start, i - 1), 16);
                            rc.appendCodePoint(cp);
                            break;
                        case 'u':
                            final int ch16 = Integer.parseInt(stringToken.substring(i, i + 4), 16);
                            rc.append((char) ch16);
                            i += 4;
                            break;
                        // TODO \x{CP}
                        case 'x':
                            final int ch8 = Integer.parseInt(stringToken.substring(i,
                                    i + 2), 16) & 0xFF;
                            rc.append((char) ch8);
                            i += 2;
                            break;
                        case 'n':
                            rc.append('\n');
                            break;
                        case 'r':
                            rc.append('\r');
                            break;
                        case 't':
                            rc.append('\t');
                            break;
                        case 'f':
                            rc.append('\f');
                            break;
                        case 'b':
                            rc.append('\b');
                            break;
                        default:
                            rc.appendCodePoint(codepoint);
                    }
                    break;
                default:
                    rc.appendCodePoint(codepoint);
            }
        }
        return rc.toString();
    }

}
