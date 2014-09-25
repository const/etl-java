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
package net.sf.etl.utils.xml;

import net.sf.etl.parsers.ObjectName;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;

import javax.xml.stream.XMLStreamException;

/**
 * Tree variant of the output. The tree variant is intended to be easily
 * processable using XSLT tools. So it uses as simply structured output as
 * possible. And it does not tries to optimize output.
 *
 * @author const
 */
public class TreeOutput extends StructuralOutput {
    /**
     * Tree output namespace
     */
    final static String TREE_NS = "http://etl.sf.net/2008/xml/tree";
    /**
     * a logger
     */
    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(TreeOutput.class.getName());
    /**
     * the current indentation level
     */
    int indentationLevel;
    /**
     * the current indentation string
     */
    String indentationString = "  ";

    @Override
    protected void process() throws Exception {
        suggestPrefix("t", TREE_NS);
        startStructElement(TREE_NS, "source", null);
        parser.advance();
        loop:
        while (true) {
            checkErrors();
            switch (parser.current().kind()) {
                case OBJECT_START:
                    processObject(null);
                    break;
                case EOF:
                    break loop;
                default:
                    otherToken();
            }
        }
        endStructElement();
        out.flush();
    }

    /**
     * Process object
     *
     * @param parent the parent context
     * @throws XMLStreamException in case of IO problem
     */
    private void processObject(Context parent) throws XMLStreamException {
        changeProperty(parent);
        TermToken current = parser.current();
        ObjectName name = current.objectName();
        startStructElement(name.namespace(), name.name(), "object");
        writePositionElement("start", current.start());
        Context context = new Context();
        parser.advance();
        int extraStarts = 0;
        loop:
        while (true) {
            checkErrors();
            switch (parser.current().kind()) {
                case OBJECT_START:
                    LOG.severe("Unexpected Object Start Event in " + parser.getSystemId() + " (Grammar BUG): " + parser.current());
                    extraStarts++;
                    parser.advance();
                    break;
                case VALUE:
                    LOG.severe("Unexpected Value Event in " + parser.getSystemId() + " (Grammar BUG): " + parser.current());
                    parser.advance();
                    break;
                case EOF:
                    return;
                case OBJECT_END:
                    if (extraStarts > 0) {
                        parser.advance();
                        extraStarts--;
                        break;
                    } else {
                        break loop;
                    }
                case PROPERTY_START:
                case LIST_PROPERTY_START:
                    processProperty(context);
                    break;
                default:
                    otherToken();
            }
        }
        context.activeProperty = null;
        changeProperty(context);
        writePositionElement("end", current.end());
        endStructElement();
        parser.advance();
    }

    /**
     * Process property
     *
     * @param context the context to process
     * @throws XMLStreamException in case of IO problem
     */
    private void processProperty(Context context) throws XMLStreamException {
        context.activeProperty = parser.current().propertyName().name();
        context.isList = parser.current().kind() == Terms.LIST_PROPERTY_START;
        // write content
        parser.advance();
        int extraStarts = 0;
        loop:
        while (true) {
            checkErrors();
            switch (parser.current().kind()) {
                case PROPERTY_START:
                case LIST_PROPERTY_START:
                    LOG.severe("Unexpected Property Start Event in "
                            + parser.getSystemId() + " (Grammar BUG): "
                            + parser.current());
                    extraStarts++;
                    parser.advance();
                    break;
                case EOF:
                    LOG.severe("Unexpected EOF Event in " + parser.getSystemId()
                            + " (Grammar BUG): " + parser.current());
                    return;
                case OBJECT_START:
                    processObject(context);
                    break;
                case VALUE:
                    processValue(context);
                    break;
                case PROPERTY_END:
                case LIST_PROPERTY_END:
                    if (extraStarts > 0) {
                        extraStarts--;
                        parser.advance();
                        break;
                    } else {
                        break loop;
                    }
                default:
                    otherToken();
            }
        }
        // end content
        parser.advance();
    }

    /**
     * Process value element
     *
     * @param context the parent context
     * @throws XMLStreamException in case of IO problem
     */
    private void processValue(Context context) throws XMLStreamException {
        Token tk = parser.current().token().token();
        changeProperty(context);
        startValueElement(TREE_NS, "value");
        attribute("kind", tk.kind().toString());
        out.writeCharacters(tk.text());
        endValueElement();
        parser.advance();
    }

    /**
     * Change property so the active property actually becomes active one
     *
     * @param parent the parent context
     * @throws XMLStreamException if the is IO problem
     */
    private void changeProperty(Context parent) throws XMLStreamException {
        if (parent == null) {
            return;
        }
        String open = parent.openProperty;
        String active = parent.activeProperty;
        if (open == null || !open.equals(active)) {
            if (open != null) {
                endStructElement();
            }
            if (active != null) {
                startStructElement(active, parent.isList ? "list-property" : "property");
            }
            parent.openProperty = active;
        }
    }

    /**
     * Write start element
     *
     * @param elementName the element name
     * @param pos         the position
     * @throws XMLStreamException in case of IO problem
     */
    private void writePositionElement(String elementName, final TextPos pos)
            throws XMLStreamException {
        startValueElement(TREE_NS, elementName);
        attribute("line", Integer.toString(pos.line()));
        attribute("column", Integer.toString(pos.column()));
        attribute("offset", Long.toString(pos.offset()));
        endValueElement();
    }

    /**
     * End value element
     *
     * @throws XMLStreamException in case of IO problem
     */
    private void endValueElement() throws XMLStreamException {
        endElement();
        nextLine();
    }

    /**
     * Start an value element
     *
     * @param ns          the namespace to use
     * @param elementName the element name
     * @throws XMLStreamException in case of IO problem
     */
    private void startValueElement(String ns, String elementName)
            throws XMLStreamException {
        writeIdent();
        startElement(ns, elementName);
    }

    /**
     * Start structural element
     *
     * @param namespace a namespace
     * @param element   an element
     * @throws XMLStreamException in case of IO problem
     */
    protected void startStructElement(String namespace, String element,
                                      String type) throws XMLStreamException {
        indent();
        startElement(namespace, element);
        if (type != null) {
            attribute("type", type);
        }
        nextLine();
    }

    /**
     * Start structural element
     *
     * @param element an element
     * @throws XMLStreamException in case of IO problem
     */
    protected void startStructElement(String element, String type)
            throws XMLStreamException {
        indent();
        startElement(element);
        if (type != null) {
            attribute("type", type);
        }
        nextLine();
    }

    /**
     * Print new line
     *
     * @throws XMLStreamException in case of IO problem
     */
    private void nextLine() throws XMLStreamException {
        out.writeCharacters("\n");
    }

    /**
     * Write indentation and increase indentation level
     *
     * @throws XMLStreamException in case of IO problem
     */
    private void indent() throws XMLStreamException {
        writeIdent();
        indentationLevel++;
    }

    /**
     * Write the current indentation
     *
     * @throws XMLStreamException in case of IO problem
     */
    private void writeIdent() throws XMLStreamException {
        for (int i = 0; i < indentationLevel; i++) {
            out.writeCharacters(indentationString);
        }
    }

    /**
     * End structural element
     *
     * @throws XMLStreamException in case of IO problem
     */
    protected void endStructElement() throws XMLStreamException {
        indentationLevel--;
        writeIdent();
        endElement();
        nextLine();
    }

    /**
     * The object specific printing context
     */
    class Context {
        /**
         * The currently open and not closed property for the object
         */
        String openProperty;
        /**
         * The currently active property
         */
        String activeProperty;
        /**
         * If true, the active property is a list property
         */
        boolean isList;
    }

}
