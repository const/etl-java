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
}
