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
package net.sf.etl.parsers.event.unstable.model.grammar;

/**
 * The FirstChoiceOp node class. This class is a part of the lightweight grammar
 * model.
 *
 * @author const
 */
public final class FirstChoiceOp extends Syntax {
    /**
     * the alternative that is tried first.
     */
    private Syntax first;
    /**
     * the alternative that is tried second.
     */
    private Syntax second;

    /**
     * @return the alternative that is tried first.
     */
    public Syntax getFirst() {
        return first;
    }

    /**
     * Set first alternative.
     *
     * @param first the first
     */
    public void setFirst(final Syntax first) {
        this.first = first;
    }

    /**
     * @return the alternative that is tried second.
     */
    public Syntax getSecond() {
        return second;
    }

    /**
     * Set second alternative.
     *
     * @param second the second
     */
    public void setSecond(final Syntax second) {
        this.second = second;
    }
}
