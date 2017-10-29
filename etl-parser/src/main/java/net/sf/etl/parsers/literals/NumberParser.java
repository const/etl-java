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
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.characters.Identifiers;
import net.sf.etl.parsers.characters.Numbers;

/**
 * A parser of number. It is loosely based on lexer code.
 */
final class NumberParser extends BaseLiteralParser {
    /**
     * the number base.
     */
    private int base = Numbers.DECIMAL;
    /**
     * The sign of the number, 0 - unspecified.
     */
    private int sign;
    /**
     * THe exponent.
     */
    private int exponent;
    /**
     * The suffix attached to number.
     */
    private String suffix;
    /**
     * digits of the number without dot and underscore.
     */
    private String text;

    /**
     * The constructor for parser.
     *
     * @param inputText the input text
     * @param start     the start position
     * @param systemId  the system id
     */
    NumberParser(final String inputText, final TextPos start, final String systemId) {
        super(inputText, start, systemId);
    }

    /**
     * @return the parsed number
     */
    public NumberInfo parse() { // NOPMD
        Tokens kind = Tokens.INTEGER; // NOPMD
        // parsing integer of decimal, or floating point number
        int ch = la();
        if (Numbers.isPlus(la())) {
            sign = 1;
            ch = next(false);
        } else if (Numbers.isMinus(la())) {
            sign = -1;
            ch = next(false);
        }
        if (!Numbers.isDecimal(ch)) {
            error("lexical.InvalidCharacter");
            return produce(Tokens.WHITESPACE);
        }
        if (Character.digit(ch, Numbers.DECIMAL) == 0) {
            final int ch1 = la(1);
            final int ch2 = la(2);
            if (Numbers.isHexIndicator(ch1) && Numbers.isValidDigit(ch2, Numbers.HEX)) {
                base = Numbers.HEX;
                next(false);
                ch = next(false);
            } else if (Numbers.isBinaryIndicator(ch1) && Numbers.isValidDigit(ch2, Numbers.BINARY)) {
                base = Numbers.BINARY;
                next(false);
                ch = next(false);
            } else {
                ch = next(true);
            }
        }
        int beforeDot = -1;
        ch = parseDigits(ch);
        if (Numbers.isDecimalDot(ch) && Numbers.isValidDigit(la(1), base)) {
            beforeDot = buffer().length();
            ch = next(false);
            kind = Tokens.FLOAT;
            ch = parseDigits(ch);
        }
        text = buffer().toString();
        buffer().setLength(0);
        if (base == Numbers.DECIMAL) {
            final int ch1 = la(1);
            final int ch2 = la(2);
            if (Numbers.isDecimalExponentChar(ch) && isExponentStart(ch1, ch2)) {
                kind = Tokens.FLOAT;
                ch = parseExponent();
            }
        } else {
            final int ch1 = la(1);
            final int ch2 = la(2);
            if (Numbers.isBinaryExponentChar(ch) && isExponentStart(ch1, ch2)) {
                kind = Tokens.FLOAT;
                ch = parseExponent();
            } else if (kind == Tokens.FLOAT) {
                error("lexical.BinaryExponentRequired");
            }
        }
        if (kind == Tokens.FLOAT) {
            if (base == Numbers.DECIMAL || base == Numbers.BINARY) {
                exponent -= beforeDot < 0 ? 0 : text.length() - beforeDot;
            } else {
                // CHECKSTYLE:OFF
                exponent -= beforeDot < 0 ? 0 : (text.length() - beforeDot) * 4;
                // CHECKSTYLE:ON
            }
        }
        if (Numbers.isValidNumberSuffixStart(ch)) {
            if (kind == Tokens.FLOAT) {
                kind = Tokens.FLOAT_WITH_SUFFIX;
            } else {
                kind = Tokens.INTEGER_WITH_SUFFIX;
            }
            do {
                ch = next(true);
            } while (Identifiers.isIdentifierPart(ch));
            suffix = buffer().toString();
            buffer().setLength(0);
        }
        if (position() != inputText().length()) {
            error("lexical.TooManyCharactersInToken");
        }
        return produce(kind);
    }

    /**
     * Parse digits.
     *
     * @param startChar the start character
     * @return the digits
     */
    private int parseDigits(final int startChar) {
        int ch = startChar;
        while (Numbers.isValidDigit(ch, base) || Identifiers.isConnectorChar(ch)) {
            ch = next(!Identifiers.isConnectorChar(ch));
        }
        return ch;
    }

    /**
     * Parse exponent part.
     *
     * @return character after exponent
     */
    private int parseExponent() {
        int ch;
        ch = next(false);
        if (Numbers.isMinus(ch)) {
            buffer().append('-');
            ch = next(false);
        }
        if (Numbers.isPlus(ch)) {
            ch = next(false);
        }
        while (Numbers.isValidDigit(ch, Numbers.DECIMAL)) {
            ch = next(true);
        }
        exponent = Integer.parseInt(buffer().toString());
        buffer().setLength(0);
        return ch;
    }

    /**
     * Check characters after exponent to check if they are valid start of exponent number.
     *
     * @param ch1 the fist character after exponent
     * @param ch2 the second character after exponent
     * @return true if exponent
     */
    private boolean isExponentStart(final int ch1, final int ch2) {
        return Numbers.isDecimal(ch1) || Numbers.isSign(ch1) && Numbers.isDecimal(ch2);
    }

    /**
     * Produce result.
     *
     * @param kind the token kind
     * @return the number information
     */
    private NumberInfo produce(final Tokens kind) {
        return new NumberInfo(inputText(), kind, sign, base, text, exponent, suffix, errors());
    }
}
