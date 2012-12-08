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

/**
 * Base parser for literal values
 */
public class BaseLiteralParser {
    /**
     * Input text
     */
    protected final String inputText;
    /**
     * Buffer used for consuming characters
     */
    protected final StringBuilder buffer = new StringBuilder();
    /**
     * position in input text
     */
    protected int pos = 0;

    /**
     * The constructor
     *
     * @param inputText the input text
     */
    public BaseLiteralParser(String inputText) {
        this.inputText = inputText;
    }

    /**
     * Look at codepoint relatively to the current
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
    protected int la() {
        return pos >= inputText.length() ? -1 : inputText.charAt(pos);
    }

    /**
     * check if next symbol match specified
     *
     * @param ch character to match
     * @return true if character is matched
     */
    protected boolean lach(char ch) {
        return la() == ch;
    }

    /**
     * Consume character and possibly add it to buffer.
     *
     * @param addToBuffer if true the character should be added to the buffer
     */
    protected void consume(boolean addToBuffer) {
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
    protected boolean laDigit(int n) {
        final int ch = la(n);
        return ('0' <= ch && ch <= '9');
    }

    /**
     * check if next symbol is digit
     *
     * @return true if next symbol is digit
     */
    protected boolean laDigit() {
        return laDigit(0);
    }

    /**
     * look ahead alpha
     *
     * @return true if letter
     */
    protected boolean laAlpha() {
        final int ch = la();
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z');
    }
}

