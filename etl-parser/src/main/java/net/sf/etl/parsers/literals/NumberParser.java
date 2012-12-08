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
 * This is a parser of number. It is loosely based on lexer code.
 */
class NumberParser extends BaseLiteralParser {
    /**
     * number base
     */
    int base = 10;
    /**
     * A sign of the number
     */
    int sign = 1;
    /**
     * Exponent
     */
    int exponent = 0;

    /**
     * a suffix attached to number
     */
    String suffix;
    /**
     * digits of the number without dot and underscore
     */
    String text;

    /**
     * A constructor for parser
     *
     * @param inputText the input text
     */
    NumberParser(String inputText) {
        super(inputText);
    }

    /**
     * @return parsed number
     */
    NumberInfo parse() {
        Tokens kind = Tokens.INTEGER;
        int beforeDot = -1;
        // parsing integer of decimal, or floating point number
        if (lach('+')) {
            consume(false);
        } else if (lach('-')) {
            sign = -1;
            consume(false);
        }
        while (laDigit() || lach('_')) {
            consume(!lach('_')); // '0'
        }
        if (lach('#')) {
            // based number
            try {
                base = Integer.parseInt(buffer.toString());
            } catch (final Exception ex) {
                throw new NumberFormatException();
            }
            if (2 > base || base > 36) {
                throw new NumberFormatException();
            }
            buffer.setLength(0);
            consume(false); // '\#'
            while (laDigit() || laAlpha() || lach('.') || lach('_')) {
                final int ch = la();
                if (ch != '.' && ch != '_') {
                    // check if digit conform to the base
                    if (base <= 10) {
                        if (!('0' <= ch && ch < '0' + base)) {
                            throw new NumberFormatException();
                        }
                    } else {
                        if (!(('0' <= ch && ch <= '9')
                                || ('a' <= ch && ch < 'a' + base - 10) || ('A' <= ch && ch < 'A' + base - 10))) {
                            throw new NumberFormatException();
                        }
                    }
                } else if (ch == '.') {
                    beforeDot = buffer.length();
                    if (kind == Tokens.FLOAT) {
                        throw new NumberFormatException();
                    }
                    kind = Tokens.FLOAT;
                }
                consume(!lach('_') && !lach('.'));
            } // end while
            if (lach('#')) {
                consume(false);
                text = buffer.toString();
                buffer.setLength(0);
            } else {
                throw new NumberFormatException();
            }
        } else {
            // parse non based integer
            if (lach('.') && laDigit(1)) {
                // floating point number
                kind = Tokens.FLOAT;
                beforeDot = buffer.length();
                consume(false); // '.'
                consume(true);
                while (laDigit()) {
                    consume(true); // '0'
                }
            }
            text = buffer.toString();
            buffer.setLength(0);
        }
        if (lach('e') || lach('E')) {
            kind = Tokens.FLOAT;
            consume(false); // 'e'
            if (lach('+') || lach('-')) {
                consume(lach('-'));
            }
            if (!laDigit()) {
                throw new NumberFormatException();
            } else {
                while (laDigit() || lach('_')) {
                    consume(!lach('_')); // digit
                }
            }
            exponent = Integer.parseInt(buffer.toString());
            buffer.setLength(0);
        }
        exponent -= beforeDot == -1 ? 0 : text.length() - beforeDot;
        if (laAlpha() && !lach('E') && !lach('e')) {
            if (kind == Tokens.FLOAT) {
                kind = Tokens.FLOAT_WITH_SUFFIX;
            } else {
                kind = Tokens.INTEGER_WITH_SUFFIX;
            }
            do {
                consume(true);
            } while (laAlpha() || lach('_') || laDigit());
            suffix = buffer.toString();
            buffer.setLength(0);
        }
        if (pos != inputText.length()) {
            throw new NumberFormatException(
                    "Some characters left in the string "
                            + (inputText.length() - pos));
        }
        return new NumberInfo(kind, sign, base, text, exponent, suffix);
    }
}
