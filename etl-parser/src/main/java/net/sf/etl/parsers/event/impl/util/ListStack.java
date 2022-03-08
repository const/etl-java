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

package net.sf.etl.parsers.event.impl.util;

import java.util.ArrayList;

/**
 * The simple stack semantics.
 *
 * @param <E> the element type
 */
public final class ListStack<E> extends ArrayList<E> {
    /**
     * The cached stack head.
     */
    private E head;

    /**
     * @return peek item on the stack
     */
    public E peek() {
        if (isEmpty()) {
            throw new IllegalStateException("The stack is empty");
        }
        return head;
    }

    /**
     * @return popped item
     */
    public E pop() {
        final E remove = remove(size() - 1);
        head = isEmpty() ? null : get(size() - 1);
        return remove;
    }

    /**
     * Push item on the stack.
     *
     * @param item the item
     */
    public void push(final E item) {
        head = item;
        add(item);
    }
}
