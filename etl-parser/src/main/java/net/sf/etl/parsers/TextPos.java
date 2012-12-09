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
package net.sf.etl.parsers;

import java.io.Serializable;

/**
 * <p> Text position. The position is represented as three numbers: line (numbered
 * from 1), column (numbered from 1), offset in 16-bit chars (numbered from 0). </p>
 * <p>
 * The object is immutable.
 * </p>
 * <p>
 * Three components are used because different values are needed for different
 * consumers. Compilers and command line tools usually need line and column.
 * Editors like Eclipse TextEditor need offset.
 * </p>
 *
 * @author const
 */
public final class TextPos implements Serializable {
    /**
     * generated serial version id
     */
    private static final long serialVersionUID = 7303432848120694096L;
    // NOTE consider using the type 'long' for lines and columns.
    /**
     * line
     */
    private final int line;
    /**
     * column
     */
    private final int column;
    /**
     * offset in characters
     */
    private final long offset;
    /**
     * start offset
     */
    public static final long START_OFFSET = 0;
    /**
     * start column
     */
    public static final int START_COLUMN = 1;
    /**
     * start line
     */
    public static final int START_LINE = 1;
    /**
     * Start text position of in text
     */
    public static final TextPos START = new TextPos(START_LINE, START_COLUMN,
            START_OFFSET);

    /**
     * A constructor
     *
     * @param line   a line
     * @param column a column
     * @param offset an offset in the file
     */
    public TextPos(int line, int column, long offset) {
        this.column = column;
        this.line = line;
        this.offset = offset;
    }

    /**
     * @return column in symbols
     */
    public int column() {
        return column;
    }

    /**
     * @return line
     */
    public int line() {
        return line;
    }

    /**
     * @return offset in 16-bit characters
     */
    public long offset() {
        return offset;
    }

    /**
     * @return string representation of position
     */
    @Override
    public String toString() {
        return "(" + line + "," + column + "," + offset + ")";
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TextPos textPos = (TextPos) o;

        if (column != textPos.column) return false;
        if (line != textPos.line) return false;
        if (offset != textPos.offset) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = line;
        result = 31 * result + column;
        result = 31 * result + (int) (offset ^ (offset >>> 32));
        return result;
    }
}
