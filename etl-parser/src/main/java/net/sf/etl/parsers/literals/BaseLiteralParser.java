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
import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.TextPos;

import java.util.Collections;

/**
 * Base parser for literal values
 */
public class BaseLiteralParser {
    /**
     * Input text
     */
    protected final String inputText;
    /**
     * The start position
     */
    private final TextPos start;
    /**
     * The system id
     */
    private final String systemId;
    /**
     * Buffer used for consuming characters
     */
    protected final StringBuilder buffer = new StringBuilder();
    /**
     * position in input text
     */
    protected int pos = 0;
    /**
     * The line
     */
    protected int line;
    /**
     * The column
     */
    protected int column;
    /**
     * The offset
     */
    protected long offset;
    /**
     * The errors to use
     */
    protected ErrorInfo errors;

    /**
     * The constructor
     *
     * @param inputText the input text
     */
    public BaseLiteralParser(String inputText, TextPos start, String systemId) {
        this.inputText = inputText;
        this.start = start;
        this.systemId = systemId;
        this.line = start.line();
        this.column = start.column();
        this.offset = start.offset();
    }

    /**
     * The constructor
     *
     * @param inputText the input text
     */
    public BaseLiteralParser(String inputText) {
        this(inputText, TextPos.START, "unknown");
    }


    /**
     * Look at codepoint relatively to the current, note that the method is doing a linear detection of
     * code points.
     *
     * @param n position relatively to current.
     * @return -1 if end of string or character at the current position.
     */
    protected int la(int n) {
        int p = pos;
        for (int i = 0; i < n; i++) {
            p = p + Character.charCount(inputText.codePointAt(p));
        }
        return p >= inputText.length() ? -1 : inputText.codePointAt(p);
    }

    /**
     * Look at character
     *
     * @return -1 if end of string or character at the current position.
     */
    protected int la() {
        return pos >= inputText.length() ? -1 : inputText.codePointAt(pos);
    }

    /**
     * Consume character and possibly add it to buffer.
     *
     * @param addToBuffer if true the character should be added to the buffer
     */
    protected void consume(boolean addToBuffer) {
        if (pos >= inputText.length()) {
            throw new ParserException();
        }
        int c = inputText.codePointAt(pos);
        int count = Character.charCount(c);
        pos += count;
        offset += count;
        column++;
        if (addToBuffer) {
            buffer.appendCodePoint(c);
        }
    }

    /**
     * Consume current codepoint and get next codepoint
     *
     * @param addToBuffer the buffer to add to
     * @return the next codepoint
     */
    protected int next(boolean addToBuffer) {
        consume(addToBuffer);
        return la();
    }

    /**
     * Create error
     *
     * @param key the error key
     */
    protected void error(String key) {
        errors = new ErrorInfo(key, Collections.emptyList(), new SourceLocation(start, new TextPos(line, column, offset), systemId), errors);
    }
}

