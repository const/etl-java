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
package net.sf.etl.parsers.term.beans;

import net.sf.etl.parsers.TextPos;

import java.io.Serializable;

/**
 * Base class for all other beans
 *
 * @author const
 */
public class Element implements Serializable {
    /**
     * default serial version id
     */
    private static final long serialVersionUID = 1L;
    /**
     * start of element
     */
    TextPos start;
    /**
     * end of element
     */
    TextPos end;

    /**
     * @return Returns the end.
     */
    public TextPos getEnd() {
        return end;
    }

    /**
     * @param end The end to set.
     */
    public void setEnd(TextPos end) {
        this.end = end;
    }

    /**
     * @return Returns the start.
     */
    public TextPos getStart() {
        return start;
    }

    /**
     * @param start The start to set.
     */
    public void setStart(TextPos start) {
        this.start = start;
    }
}
