/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2011 Constantine A Plotnikov
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
package net.sf.etl.parsers;

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
     * Information about number that is being parsed.
     */
    public static class NumberInfo {
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
                    + n);
        }
        int i = 0;
        while (Character.isUnicodeIdentifierPart(stringToken.charAt(i))) {
            i++;
        }
        final char quote = stringToken.charAt(i);
        switch (quote) {
            case '\'':
            case '"':
                break;
            default:
                throw new IllegalArgumentException("Invalid quote character "
                        + stringToken.charAt(0));
        }
        boolean multiline = stringToken.length() > 6 + i
                && stringToken.charAt(i + 1) == quote
                && stringToken.charAt(i + 2) == quote;
        // ignore last and first characters
        n -= multiline ? 3 : 1;
        i += multiline ? 3 : 1;
        if (i > n
                || stringToken.charAt(n) != quote
                || !(!multiline || stringToken.charAt(n + 1) == quote
                && stringToken.charAt(n + 2) == quote)) {
            throw new IllegalArgumentException(
                    "The string is in invalid format: " + stringToken);
        }
        while (i < n) {
            char ch = stringToken.charAt(i++);
            if ((ch >= '\uD800' && ch <= '\uDBFF')
                    || (ch >= '\uDC00' && ch <= '\uDFFF')) {
                // NOTE POST 0.2: fix it
                throw new IllegalArgumentException(
                        "Large codepoints are not yet handled: " + ((int) ch));
            }
            switch (ch) {
                case '\\':
                    if (i >= n) {
                        throw new IllegalArgumentException(
                                "Unexpected end of the token " + i);
                    }
                    ch = stringToken.charAt(i++);
                    switch (ch) {
                        case 'U':
                            final int start = i;
                            while (i < n && (ch = stringToken.charAt(i++)) != ';') {
                                if (('0' > ch || ch > '9') && ('a' > ch || ch > 'f')
                                        && ('A' > ch || ch > 'F')) {
                                    throw new IllegalArgumentException(
                                            "Invalid symbol in escape sequence " + ch);
                                }
                            }
                            if (i == start || stringToken.charAt(i - 1) != ';') {
                                throw new IllegalArgumentException(
                                        "Unexpected end of the token " + i);
                            }
                            final int codepoint = Integer.parseInt(stringToken
                                    .substring(start, i - 1), 16);
                            rc.appendCodePoint(codepoint);
                            break;
                        case 'u':
                            final int ch16 = Integer.parseInt(stringToken.substring(i,
                                    i + 4), 16);
                            rc.append((char) ch16);
                            i += 4;
                            break;
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
                            rc.append(ch);
                    }
                    break;
                default:
                    rc.append(ch);
            }
        }
        return rc.toString();
    }

    /**
     * This is a parser of number. It is loosely based on lexer code.
     */
    private static class NumberParser {
        /**
         * Buffer used for consuming characters
         */
        StringBuffer buffer = new StringBuffer();
        /**
         * Input text
         */
        final String inputText;
        /**
         * position in input text
         */
        int pos = 0;
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
            this.inputText = inputText;
        }

        /**
         * Look at character
         *
         * @param n position relatively to current.
         * @return -1 if end of string or character at the current position.
         */
        private int la(int n) {
            return (pos + n) >= inputText.length() ? -1 : inputText.charAt(pos
                    + n);
        }

        /**
         * Look at character
         *
         * @return -1 if end of string or character at the current position.
         */
        private int la() {
            return pos >= inputText.length() ? -1 : inputText.charAt(pos);
        }

        /**
         * check if next symbol match specified
         *
         * @param ch character to match
         * @return true if character is matched
         */
        private boolean lach(char ch) {
            return la() == ch;
        }

        /**
         * Consume character and possibly add it to buffer.
         *
         * @param addToBuffer if true the character should be added to the buffer
         */
        private void consume(boolean addToBuffer) {
            if (pos > inputText.length()) {
                throw new NumberFormatException();
            }
            if (addToBuffer) {
                buffer.append(inputText.charAt(pos));
            }
            pos++;
        }

        /**
         * check if next symbol is digit
         *
         * @param n look ahead position
         * @return true if next symbol is digit
         */
        private boolean laDigit(int n) {
            final int ch = la(n);
            return ('0' <= ch && ch <= '9');
        }

        /**
         * check if next symbol is digit
         *
         * @return true if next symbol is digit
         */
        private boolean laDigit() {
            return laDigit(0);
        }

        /**
         * look ahead alpha
         *
         * @return true if letter
         */
        private boolean laAlpha() {
            final int ch = la();
            return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z');
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
}
