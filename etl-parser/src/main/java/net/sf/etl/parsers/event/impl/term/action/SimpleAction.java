/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2022 Konstantin Plotnikov
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

package net.sf.etl.parsers.event.impl.term.action;

import net.sf.etl.parsers.SourceLocation;

/**
 * The simple action just have a next element.
 */
public abstract class SimpleAction extends Action {
    /**
     * The next action.
     */
    private Action next;

    /**
     * The constructor.
     *
     * @param source the source location in the grammar that caused this node creation
     */
    protected SimpleAction(final SourceLocation source) {
        super(source);
    }

    /**
     * The constructor.
     *
     * @param source the source location in the grammar that caused this node creation
     * @param next   the next action
     */
    protected SimpleAction(final SourceLocation source, final Action next) {
        super(source);
        this.next = next;
    }

    /**
     * @return the next action
     */
    public final Action getNext() {
        return next;
    }

    /**
     * Set next action.
     *
     * @param next the next action
     */
    public final void setNext(final Action next) {
        this.next = next;
    }
}
