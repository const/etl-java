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

import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.TextPos;

import javax.xml.stream.XMLStreamException;
import java.util.logging.Logger;

/**
 * Structural XMI2-like output. XMI2 specification is available at
 * http://www.omg.org/cgi-bin/doc?formal/03-05-02.
 *
 * @author const
 */
public class XMIOutput extends StructuralOutput {
    /**
     * a logger
     */
    private static final Logger log = Logger.getLogger(XMIOutput.class.getName());
    /**
     * XMI namespace
     */
    final static String XMI_NS = "http://www.omg.org/XMI";
    /**
     * schema instance namespace
     */
    final static String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
    /**
     * Avoid attributes property
     */
    final boolean avoidAttributes;
    /**
     * True if the first object is yet to be written
     */
    boolean firstObject = true;

    /**
     * a constructor
     *
     * @param avoidAttributes if true attributes are avoided
     */
    public XMIOutput(boolean avoidAttributes) {
        super();
        this.avoidAttributes = avoidAttributes;
    }

    @Override
    protected void process() throws Exception {
        // FIXME
        // out.setProperty(
        // "http://xmlpull.org/v1/doc/properties.html#serializer-indentation"
        // ,"\t");
        suggestPrefix("xmi", XMI_NS);
        suggestPrefix("xsi", XSI_NS);
        startElement(XMI_NS, "xmi");
        getPrefixForAuxiliaryNamespace(XSI_NS);
        attribute(XMI_NS, "version", "2.0");
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
        endElement();
        out.flush();
    }

    /**
     * process object
     *
     * @param property current property
     * @throws Exception in case of IO problem
     */
    void processObject(String property) throws Exception {
        if (property == null) {
            if (firstObject) {
                getPrefixForAuxiliaryNamespace(parser.current().objectName().namespace());
                firstObject = false;
            }
            startElement(parser.current().objectName().namespace(), parser.current().objectName().name());
        } else {
            startElement(property);
            final String pfx = getPrefixForAuxiliaryNamespace(parser.current().objectName().namespace());
            attribute(XSI_NS, "type", pfx + ":" + parser.current().objectName().name());
        }
        boolean hadObjects = avoidAttributes;
        final TextPos start = parser.current().start();
        value(hadObjects, "startLine", Integer.toString(start.line()));
        value(hadObjects, "startColumn", Integer.toString(start.column()));
        value(hadObjects, "startOffset", Long.toString(start.offset()));
        // write content
        parser.advance();
        int extraStarts = 0;
        loop:
        while (true) {
            checkErrors();
            switch (parser.current().kind()) {
                case OBJECT_START:
                    log.severe("Unexpected Object Start Event in "
                            + parser.getSystemId() + " (Grammar BUG): "
                            + parser.current());
                    extraStarts++;
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
                    hadObjects = processProperty(hadObjects);
                    break;
                default:
                    otherToken();
            }
        }
        final TextPos end = parser.current().end();
        value(hadObjects, "endLine", Integer.toString(end.line()));
        value(hadObjects, "endColumn", Integer.toString(end.column()));
        value(hadObjects, "endOffset", Long.toString(end.offset()));
        // end content
        endElement();
        parser.advance();
    }

    /**
     * Crate value or element depending on whether there were objects
     *
     * @param hadObjects objects flag
     * @param name       the name
     * @param value      the value
     * @throws XMLStreamException if there is a problem with writing stream
     */
    private void value(boolean hadObjects, String name, String value)
            throws XMLStreamException {
        if (!hadObjects) {
            attribute(name, value);
        } else {
            startElement(name);
            out.writeCharacters(value);
            endElement();
        }
    }

    /**
     * process property of the object
     *
     * @param hadElements if true, there had been already elements output for the
     *                    current object
     * @return true if property had been output as element rather then xml
     *         attribute.
     * @throws Exception in case of IO problem
     */
    boolean processProperty(boolean hadElements) throws Exception {
        final String prop = parser.current().propertyName().name();
        final boolean isList = parser.current().kind() == Terms.LIST_PROPERTY_START;
        // write content
        parser.advance();
        int extraStarts = 0;
        loop:
        while (true) {
            checkErrors();
            switch (parser.current().kind()) {
                case PROPERTY_START:
                case LIST_PROPERTY_START:
                    log.severe("Unexpected Property Start Event in "
                            + parser.getSystemId() + " (Grammar BUG): "
                            + parser.current());
                    extraStarts++;
                    parser.advance();
                    break;

                case EOF:
                    return hadElements;
                case OBJECT_START:
                    hadElements = true;
                    this.processObject(prop);
                    break;
                case VALUE:
                    if (isList) {
                        hadElements = true;
                    }
                    value(hadElements, prop, parser.current().token().token().text());
                    parser.advance();
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
        return hadElements;
    }
}
