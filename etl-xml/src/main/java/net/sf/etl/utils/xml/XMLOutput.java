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
package net.sf.etl.utils.xml;

import net.sf.etl.parsers.streams.TermParserReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for different kinds of outputs supported by application.
 *
 * @author const
 */
public abstract class XMLOutput {
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(XMLOutput.class);
    /**
     * <p>
     * Tested StAX parsers are hopelessly broken in situation when root element
     * does not declare all prefixes for child elements. The namespace is
     * generated for the first element, but is ignored for the second element.
     * </p>
     * <p/>
     * <p>
     * So workaround is needed to manually maintain declarations. This class
     * provides such workaround.
     * </p>
     * <p/>
     * <p>
     * Stack of namespace contexts. The stack might contain one of the
     * following:
     * </p>
     * <ul>
     * <li>null - this context did not defined any prefix</li>
     * <li>a string - a single prefix has been defined in the context</li>
     * <li>a list of strings - multiple prefixes has been defined in this
     * context</li>
     * </ul>
     */
    private final List<ArrayList<String>> contextStack = new ArrayList<ArrayList<String>>();
    /**
     * The current mapping from prefix to namespace.
     */
    private final Map<String, String> prefixToNamespaceMap = new HashMap<String, String>(); // NOPMD
    /**
     * The current mapping namespace to prefix.
     */
    private final Map<String, String> namespaceToPrefixMap = new HashMap<String, String>(); // NOPMD
    /**
     * The parser.
     */
    private TermParserReader parser;
    /**
     * The output writer.
     */
    private XMLStreamWriter out;
    /**
     * The map from namespace to prefix name.
     */
    private Map<String, String> prefixes = new HashMap<String, String>(); // NOPMD

    /**
     * Start processing input and generating output.
     *
     * @param newParser the parser to use
     * @param output    the stream for output file
     * @throws IOException if there is an IO problem
     */
    public final void process(final TermParserReader newParser, final OutputStream output) throws IOException {
        // TODO refactor to use constructors
        try {
            this.parser = newParser;
            final XMLOutputFactory factory = XMLOutputFactory.newInstance();
            final Writer w = new OutputStreamWriter(output, "UTF-8");
            try {
                this.out = factory.createXMLStreamWriter(w);
                process();
            } finally {
                w.close();
            }
        } catch (final Exception ex) { // NOPMD
            LOG.error("Processing failed: " + parser.getSystemId(), ex);
        }
    }

    /**
     * Start processing input and generating output.
     *
     * @param newParser parser to use
     * @param writer    writer for output file
     * @throws IOException if there is an IO problem
     */
    public final void process(final TermParserReader newParser, final Writer writer) throws IOException {
        try {
            this.parser = newParser;
            final XMLOutputFactory factory = XMLOutputFactory.newInstance();
            this.out = factory.createXMLStreamWriter(writer);
            process();
        } catch (final Exception ex) { // NOPMD
            LOG.error("Processing failed: " + parser.getSystemId(), ex);
        }
    }

    /**
     * Generate prefix by namespace.
     *
     * @param ns the namespace to use
     * @return the prefix for that namespace
     */
    protected final String generatePrefix(final String ns) {
        String rc = prefixes.get(ns);
        if (rc == null) {
            rc = "n" + prefixes.size();
            prefixes.put(ns, rc);
        }
        return rc;
    }

    /**
     * Process file.
     *
     * @throws Exception the exception
     */
    protected abstract void process() throws Exception;

    /**
     * Start element in default namespace that might contain an auxiliary
     * namespace declaration.
     *
     * @param element the element to start
     * @throws XMLStreamException in case of problem with xml output
     */
    protected final void startElement(final String element) throws XMLStreamException {
        contextStack.add(null);
        out.writeStartElement(element);
    }

    /**
     * This method suggests some prefix to outputter. Suggestions starting with
     * letter "n" are ignored.
     *
     * @param prefix    the prefix to suggest
     * @param namespace a namespace for that prefix
     */
    protected final void suggestPrefix(final String prefix, final String namespace) {
        if (!prefix.startsWith("n")) { // NOPMD
            prefixes.put(namespace, prefix);
        }
    }

    /**
     * Start element with specified namespace and name.
     *
     * @param namespace the namespace
     * @param element   the local name of element
     * @throws XMLStreamException in case of problem with writer
     */
    protected final void startElement(final String namespace, final String element)
            throws XMLStreamException {
        String prefix = namespaceToPrefixMap.get(namespace);
        boolean needNsDeclaration;
        contextStack.add(null);
        if (prefix == null) {
            prefix = registerNewPrefix(namespace);
            needNsDeclaration = true;
        } else {
            needNsDeclaration = false;
        }
        out.writeStartElement(prefix, element, namespace);
        if (needNsDeclaration) {
            out.writeNamespace(prefix, namespace);
        }
    }

    /**
     * Write the attribute.
     *
     * @param name  the name of attribute
     * @param value the value
     * @throws XMLStreamException in case of writer problem
     */
    protected final void attribute(final String name, final String value)
            throws XMLStreamException {
        out.writeAttribute(name, value);
    }

    /**
     * Write attribute for namespace and value.
     *
     * @param namespace the namespace of attribute
     * @param name      the name of attribute
     * @param value     the value
     * @throws XMLStreamException in case of writer problem
     */
    protected final void attribute(final String namespace, final String name, final String value)
            throws XMLStreamException {
        final String prefix = getPrefixForAuxiliaryNamespace(namespace);
        out.writeAttribute(prefix, namespace, name, value);
    }

    /**
     * Get prefix for auxiliary namespace, one that is used in attribute value.
     *
     * @param namespace the namespace for which prefix is needed
     * @return a prefix for auxiliary namespace
     * @throws XMLStreamException in case of writer problem
     */
    protected final String getPrefixForAuxiliaryNamespace(final String namespace)
            throws XMLStreamException {
        String prefix = namespaceToPrefixMap.get(namespace);
        if (prefix == null) {
            prefix = registerNewPrefix(namespace);
            out.writeNamespace(prefix, namespace);
        }
        return prefix;
    }

    /**
     * Registers new prefix for namespace.
     *
     * @param namespace the namespace
     * @return a new prefix for namespace
     */
    private String registerNewPrefix(final String namespace) {
        final String prefix = generatePrefix(namespace);
        final int stackTop = contextStack.size() - 1;
        final ArrayList<String> previousTop = contextStack.get(stackTop);
        if (previousTop == null) {
            final ArrayList<String> l = new ArrayList<String>(1);
            l.add(prefix);
            contextStack.set(stackTop, l);
        } else {
            previousTop.add(prefix);
        }
        namespaceToPrefixMap.put(namespace, prefix);
        prefixToNamespaceMap.put(prefix, namespace);
        return prefix;
    }

    /**
     * Write end element and clean a context stack.
     *
     * @throws XMLStreamException in case of writer problem
     */
    protected final void endElement() throws XMLStreamException {
        final int stackTop = contextStack.size() - 1;
        final ArrayList<String> top = contextStack.get(stackTop);
        if (top != null) {
            for (final String p : top) {
                final String ns = prefixToNamespaceMap.remove(p);
                namespaceToPrefixMap.remove(ns);
            }
        }
        contextStack.remove(stackTop);
        out.writeEndElement();
    }

    /**
     * @return the parser.
     */
    protected final TermParserReader parser() {
        return parser;
    }

    /**
     * @return the output writer.
     */
    public final XMLStreamWriter out() {
        return out;
    }
}
