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
public class PresentationOutput extends XMLOutput {
    // TODO split comments
    /**
     * namespace of stylesheet
     */
    static final String pns = "http://etl.sf.net/2006/etl/presentation";

    /**
     * style file name
     */
    String styleFile;

    /**
     * type of stylesheet
     */
    String styleType;
    /**
     * The last token for which errors were written
     */
    TermToken lastErrorToken;

    /**
     * a constructor
     *
     * @param sf style file
     * @param st style type
     */
    public PresentationOutput(String sf, String st) {
        super();
        this.styleFile = sf;
        this.styleType = st;
    }

    @Override
    protected void process() throws Exception {
        if (styleFile != null) {
            out.writeProcessingInstruction("xml-stylesheet href=\"" + styleFile + "\" type=\"" + styleType + "\"");
        }
        suggestPrefix("p", pns);
        try {
            startElement(pns, "source");
            attribute("xml:space", "preserve");
            do {
                TermToken tk = parser.current();
                checkErrors(tk);
                Terms kind = tk.kind();
                switch (kind) {
                    case GRAMMAR_IS_LOADED:
                        startElement(pns, "grammarIsLoaded");
                        attribute("grammarURI", tk.loadedGrammar().getUsedGrammar().getDescriptor().getSystemId());
                        attribute("initialContext", tk.loadedGrammar().getUsedContext().context());
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
                        startElement(pns, "attributes");
                        attribute("context", tk.definitionInfo().getContext().context());
                        break;
                    case ATTRIBUTES_END:
                        endElement();
                        break;
                    case DOC_COMMENT_START:
                        startElement(pns, "documentation");
                        attribute("context", tk.definitionInfo().getContext().context());
                        break;
                    case DOC_COMMENT_END:
                        endElement();
                        break;
                    case LIST_PROPERTY_START:
                    case PROPERTY_START:
                        startElement(tk.propertyName().name());
                        attribute("type", kind == Terms.PROPERTY_START ? "property"
                                : "list-property");
                        break;
                    case LIST_PROPERTY_END:
                    case PROPERTY_END:
                        endElement();
                        break;
                    case VALUE:
                        startElement(pns, "value");
                        writeTokenData("value");
                        endElement();
                        break;
                    case CONTROL:
                        startElement(pns, "control");
                        writeTokenData("control");
                        endElement();
                        break;
                    case IGNORABLE:
                        startElement(pns, "ignorable");
                        writeTokenData("ignorable");
                        endElement();
                        break;
                    case STRUCTURAL:
                        startElement(pns, "structural");
                        writeTokenData("structural");
                        endElement();
                        break;
                    case EXPRESSION_START:
                        startElement(pns, "expression");
                        attribute("context", tk.expressionContext().context());
                        break;
                    case EXPRESSION_END:
                        endElement();
                        break;
                    case MODIFIERS_START:
                        startElement(pns, "modifiers");
                        break;
                    case MODIFIERS_END:
                        endElement();
                        break;
                    case EOF:
                        break;
                    case STATEMENT_START:
                        startElement(pns, "statement");
                        attribute("context", tk.definitionInfo().getContext().context());
                        break;
                    case STATEMENT_END:
                        endElement();
                        break;
                    case BLOCK_START:
                        startElement(pns, "block");
                        attribute("context", tk.statementContext().context());
                        break;
                    case BLOCK_END:
                        endElement();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown termKind: " + kind);
                }
            } while (parser.advance());
            endElement();
        } finally {
            out.flush();
        }
    }

    /**
     * Check errors for the token wne write them
     *
     * @param tk the token to check
     */
    private void checkErrors(TermToken tk) throws XMLStreamException {
        if (tk != lastErrorToken) {
            lastErrorToken = tk;
            writeError(tk.errorInfo());
            if (tk.hasPhraseToken()) {
                writeError(tk.token().errorInfo());
                if (tk.hasLexicalToken()) {
                    writeError(tk.token().token().errorInfo());
                }
            }
        }
    }

    /**
     * Write information about error
     *
     * @param error the error to write about (might be null)
     * @throws XMLStreamException if there is IO problem
     */
    private void writeError(ErrorInfo error) throws XMLStreamException {
        while (error != null) {
            startElement(pns, "error");
            attribute("errorId", error.errorId());
            attribute("file", error.location().systemId());
            attribute("message", error.message());
            attribute("shortLocation", error.location().toShortString());
            attribute("startLine", Integer.toString(error.location().start().line()));
            attribute("startColumn", Integer.toString(error.location().start().column()));
            attribute("endLine", Integer.toString(error.location().end().line()));
            attribute("endColumn", Integer.toString(error.location().end().column()));
            for (Object o : error.errorArgs()) {
                startElement(pns, "arg");
                attribute("value", "" + o);
                endElement();
            }
            endElement();
            error = error.cause();
        }
    }


    /**
     * Write token data
     *
     * @throws XMLStreamException if there is IO problem
     */
    private void writeTokenData(String token) throws XMLStreamException {
        final TermToken current = parser.current();
        attribute("role", current.role().name());
        attribute("control", current.token().kind().name());
        if (current.hasLexicalToken()) {
            attribute("token", current.token().token().kind().name());
            final List<String> lines = Whitespaces.splitNewLines(current.token().token().text());
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (i > 0) {
                    startElement(pns, "newline");
                    attribute("line", Integer.toString(current.start().line() + i + 1));
                    out.writeCharacters("\n");
                    endElement();
                }
                out.writeCharacters(line);
            }
        }
    }
}
