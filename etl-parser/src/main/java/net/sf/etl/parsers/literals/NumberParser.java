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
class NumberParser extends BaseLiteralParser {
    /**
     * the number base
     */
    int base = 10;
    /**
     * The sign of the number, 0 - unspecified
     */
    int sign = 0;
    /**
     * THe exponent
     */
    int exponent = 0;
    /**
     * The suffix attached to number
     */
    String suffix;
    /**
     * digits of the number without dot and underscore
     */
    String text;

    /**
     * The constructor for parser
     *
     * @param inputText the input text
     */
    NumberParser(String inputText, TextPos start, String systemId) {
        super(inputText, start, systemId);
    }

    /**
     * @return the parsed number
     */
    NumberInfo parse() {
        Tokens kind = Tokens.INTEGER;
        int beforeDot = -1;
        // parsing integer of decimal, or floating point number
        int ch = la();
        if (Numbers.isPlus(la())) {
            sign = 1;
            ch = next(false);
        } else if (Numbers.isMinus(la())) {
            sign = -1;
            ch = next(false);
        }
        while (Numbers.isDecimal(ch) || Identifiers.isConnectorChar(ch)) {
            ch = next(!Identifiers.isConnectorChar(ch)); // '0'
        }
        if (Numbers.isBasedNumberChar(ch)) {
            // based number
            try {
                base = Integer.parseInt(buffer.toString());
            } catch (final Exception ex) {
                error("lexical.NumberBaseIsOutOfRange");
                base = Character.MAX_RADIX;
            }
            if (2 > base || base > Character.MAX_RADIX) {
                error("lexical.NumberBaseIsOutOfRange");
                base = Character.MAX_RADIX;
            }
            buffer.setLength(0);
            ch = next(false); // '\#'
            while (true) {
                if (Identifiers.isConnectorChar(ch)) {
                    ch = next(false);
                } else if (Numbers.isDecimalDot(ch)) {
                    beforeDot = buffer.length();
                    if (kind == Tokens.FLOAT) {
                        error("lexical.FloatTooManyDots");
                    }
                    kind = Tokens.FLOAT;
                    ch = next(false);
                } else if (Numbers.isValidDigit(ch, base)) {
                    ch = next(true);
                } else if (Numbers.isAnyDigit(ch)) {
                    error("lexical.SomeDigitAreOutOfBase");
                    ch = next(true);
                } else if (Numbers.isBasedNumberChar(ch)) {
                    ch = next(false);
                    text = buffer.toString();
                    buffer.setLength(0);
                    break;
                } else {
                    error("lexical.UnterminatedBasedNumber");
                    return new NumberInfo(inputText, kind, sign, base, text, exponent, suffix, errors);
                }
            } // end while
        } else {
            // parse non based integer
            if (Numbers.isDecimalDot(ch) && Numbers.isDecimal(la(1))) {
                // floating point number
                kind = Tokens.FLOAT;
                beforeDot = buffer.length();
                consume(false); // '.'
                ch = next(true);
                while (Numbers.isDecimal(ch) || Identifiers.isConnectorChar(ch)) {
                    ch = next(!Identifiers.isConnectorChar(ch)); // '0'
                }
            }
            text = buffer.toString();
            buffer.setLength(0);
        }
        if (Numbers.isExponentChar(ch)) {
            kind = Tokens.FLOAT;
            ch = next(false); // 'e'
            if (Numbers.isPlus(ch) || Numbers.isMinus(ch)) {
                ch = next(Numbers.isMinus(ch));
            }
            if (!Numbers.isDecimal(ch)) {
                error("lexical.UnterminatedNumberExponent");
                return new NumberInfo(inputText, kind, sign, base, text, exponent, suffix, errors);
            } else {
                ch = next(true);
                while (Numbers.isDecimal(ch) || Identifiers.isConnectorChar(ch)) {
                    ch = next(!Identifiers.isConnectorChar(ch)); // '0'
                }
            }
            exponent = Integer.parseInt(buffer.toString());
            buffer.setLength(0);
        }
        exponent -= beforeDot == -1 ? 0 : text.length() - beforeDot;
        if (Identifiers.isIdentifierStart(ch) && !Numbers.isExponentChar(ch)) {
            if (kind == Tokens.FLOAT) {
                kind = Tokens.FLOAT_WITH_SUFFIX;
            } else {
                kind = Tokens.INTEGER_WITH_SUFFIX;
            }
            do {
                ch = next(true);
            } while (Identifiers.isIdentifierPart(ch));
            suffix = buffer.toString();
            buffer.setLength(0);
        }
        if (pos != inputText.length()) {
            error("lexical.TooManyCharactersInToken");
        }
        return new NumberInfo(inputText, kind, sign, base, text, exponent, suffix, errors);
    }
}
