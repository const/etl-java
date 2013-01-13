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

package net.sf.etl.parsers.event;

/**
 * Single element cell to pass tokens to the parser
 */
public class Cell<T> {
    /**
     * The element in the cell
     */
    private T element;

    /**
     * @return true, the element presents in the cell
     */
    public boolean hasElement() {
        return element != null;
    }

    /**
     * Take element from the cell
     *
     * @return a consumed element
     * @throws IllegalStateException if there is no an element in the cell
     */
    public T take() {
        if (element == null) {
            throw new IllegalStateException("There is no element in the cell");
        }
        T rc = element;
        element = null;
        return rc;
    }

    /**
     * Peek element in the cell
     *
     * @return a present element
     * @throws IllegalStateException if there is no an element in the cell
     */
    public T peek() {
        if (element == null) {
            throw new IllegalStateException("There is no element in the cell");
        }
        return element;
    }

    /**
     * Put element to the cell
     *
     * @param element the element to put
     * @throws IllegalStateException if there is already an element in the cell
     */
    public void put(T element) {
        if (element == null) {
            throw new NullPointerException("The supplied element could not be null.");
        }
        if (this.element != null) {
            throw new IllegalStateException("The cell already has element: " + this.element +
                    " and new element is supplied: " + element);
        }
        this.element = element;
    }

    /**
     * @return true if the cell is empty
     */
    public boolean isEmpty() {
        return element == null;
    }
}
