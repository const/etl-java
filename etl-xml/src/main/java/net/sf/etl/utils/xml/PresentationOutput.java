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

import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.characters.Whitespaces;

import javax.xml.stream.XMLStreamException;
import java.util.List;

/**
 * This class generates presentation output basing on incoming events.
 * Presentation output is mostly dump of all events in the parser.
 *
 * @author const
 */
public final class PresentationOutput extends XMLOutput {
    /**
     * The namespace of stylesheet.
     */
    private static final String PNS = "http://etl.sf.net/2006/etl/presentation";

    /**
     * The style file name.
     */
    private final String styleFile;

    /**
     * The type of stylesheet.
     */
    private final String styleType;
    /**
     * The last token for which errors were written.
     */
    private TermToken lastErrorToken;

    /**
     * The constructor.
     *
     * @param sf the style file.
     * @param st the style type.
     */
    public PresentationOutput(final String sf, final String st) {
        super();
        this.styleFile = sf;
        this.styleType = st;
    }

    @Override
    protected void process() throws Exception { // NOPMD
        if (styleFile != null) {
            out().writeProcessingInstruction("xml-stylesheet href=\"" + styleFile + "\" type=\"" + styleType + "\"");
        }
        suggestPrefix("p", PNS);
        try {
            startElement(PNS, "source");
            attribute("xml:space", "preserve");
            do {
                final TermToken tk = parser().current();
                checkErrors(tk);
                final Terms kind = tk.kind();
                switch (kind) {
                    case GRAMMAR_IS_LOADED:
                        startElement(PNS, "grammarIsLoaded");
                        attribute("grammarURI", tk.loadedGrammar().usedGrammar().getDescriptor().getSystemId());
                        attribute("initialContext", tk.loadedGrammar().usedContext().context());
                        endElement();
                        break;
                    case OBJECT_START:
                        startElement(tk.objectName().namespace(), tk.objectName().name());
                        attribute("type", "object");
                        break;
                    case OBJECT_END:
                        endElement();
                        break;
                    case ATTRIBUTES_START:
                        startElement(PNS, "attributes");
                        attribute("context", tk.definitionInfo().getContext().context());
                        break;
                    case ATTRIBUTES_END:
                        endElement();
                        break;
                    case DOC_COMMENT_START:
                        startElement(PNS, "documentation");
                        attribute("context", tk.definitionInfo().getContext().context());
                        break;
                    case DOC_COMMENT_END:
                        endElement();
                        break;
                    case LIST_PROPERTY_START:
                    case PROPERTY_START:
                        startElement(tk.propertyName().name());
                        attribute("type", kind == Terms.PROPERTY_START ? "property" : "list-property");
                        break;
                    case LIST_PROPERTY_END:
                    case PROPERTY_END:
                        endElement();
                        break;
                    case VALUE:
                        startElement(PNS, "value");
                        writeTokenData();
                        endElement();
                        break;
                    case CONTROL:
                        startElement(PNS, "control");
                        writeTokenData();
                        endElement();
                        break;
                    case IGNORABLE:
                        startElement(PNS, "ignorable");
                        writeTokenData();
                        endElement();
                        break;
                    case STRUCTURAL:
                        startElement(PNS, "structural");
                        writeTokenData();
                        endElement();
                        break;
                    case EXPRESSION_START:
                        startElement(PNS, "expression");
                        attribute("context", tk.expressionContext().context());
                        break;
                    case EXPRESSION_END:
                        endElement();
                        break;
                    case MODIFIERS_START:
                        startElement(PNS, "modifiers");
                        break;
                    case MODIFIERS_END:
                        endElement();
                        break;
                    case EOF:
                        break;
                    case STATEMENT_START:
                        startElement(PNS, "statement");
                        attribute("context", tk.definitionInfo().getContext().context());
                        break;
                    case STATEMENT_END:
                        endElement();
                        break;
                    case BLOCK_START:
                        startElement(PNS, "block");
                        attribute("context", tk.statementContext().context());
                        break;
                    case BLOCK_END:
                        endElement();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown termKind: " + kind);
                }
            } while (parser().advance());
            endElement();
        } finally {
            out().flush();
        }
    }

    /**
     * Check errors for the token wne write them.
     *
     * @param tk the token to check
     * @throws XMLStreamException in case of XML problem
     */
    private void checkErrors(final TermToken tk) throws XMLStreamException {
        if (tk != lastErrorToken) { // NOPMD
            lastErrorToken = tk;
            writeError(tk.errorInfo());
            if (tk.hasPhraseToken()) {
                writeError(tk.token().errorInfo());
                if (tk.hasLexicalToken()) { // NOPMD
                    writeError(tk.token().token().errorInfo());
                }
            }
        }
    }

    /**
     * Write information about error.
     *
     * @param startError the error to write about (might be null)
     * @throws XMLStreamException if there is IO problem
     */
    private void writeError(final ErrorInfo startError) throws XMLStreamException {
        ErrorInfo error = startError;
        while (error != null) {
            startElement(PNS, "error");
            attribute("errorId", error.errorId());
            attribute("file", error.location().systemId());
            attribute("message", error.message());
            attribute("shortLocation", error.location().toShortString());
            attribute("startLine", Integer.toString(error.location().start().line()));
            attribute("startColumn", Integer.toString(error.location().start().column()));
            attribute("endLine", Integer.toString(error.location().end().line()));
            attribute("endColumn", Integer.toString(error.location().end().column()));
            for (final Object o : error.errorArgs()) {
                startElement(PNS, "arg");
                attribute("value", String.valueOf(o));
                endElement();
            }
            endElement();
            error = error.cause();
        }
    }


    /**
     * Write token data.
     *
     * @throws XMLStreamException if there is IO problem
     */
    private void writeTokenData() throws XMLStreamException {
        final TermToken current = parser().current();
        attribute("role", current.role().name());
        attribute("control", current.token().kind().name());
        if (current.hasLexicalToken()) {
            attribute("token", current.token().token().kind().name());
            final List<String> lines = Whitespaces.splitNewLines(current.token().token().text());
            for (int i = 0; i < lines.size(); i++) {
                final String line = lines.get(i);
                if (i > 0) {
                    startElement(PNS, "newline");
                    attribute("newline", Integer.toString(current.start().line() + i + 1));
                    out().writeCharacters("\n");
                    endElement();
                }
                out().writeCharacters(line);
            }
        }
    }
}
