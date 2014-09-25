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

package net.sf.etl.parsers.event.impl.term;

import net.sf.etl.parsers.PropertyName;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.event.unstable.model.doctype.Doctype;

/**
 * This listener is used to gather doctype information, and it produces a doctype object to process it further.
 * <p/>
 * The listener assumes that parser works correctly and relies on parser error recovery. The listener skips
 * all tokens, but collects all error information from them.
 */
final class DoctypeTokenListener implements TermTokenListener {
    /**
     * The parser to use.
     */
    private final TermParserImpl parser;
    /**
     * The document type object.
     */
    private final Doctype doctype = new Doctype();
    /**
     * The last property.
     */
    private PropertyName lastProperty;
    /**
     * The start position.
     */
    private TextPos start;

    /**
     * The constructor that installs object to the parser, the listener will be removed when end of
     * the statement is encountered on the method {@link #observe(TermToken)}.
     *
     * @param parser the parser
     */
    DoctypeTokenListener(final TermParserImpl parser) {
        this.parser = parser;
        parser.addListener(this);
    }

    @Override
    public void observe(final TermToken token) {
        if (start == null) {
            start = token.start();
        }
        collectErrors(token);
        switch (token.kind()) {
            case PROPERTY_START:
                lastProperty = token.propertyName();
                break;
            case VALUE:
                assert lastProperty != null;
                if (StandardGrammars.DOCTYPE_GRAMMAR_DOCTYPE_TYPE.equals(lastProperty)) {
                    doctype.setType(token.token().token());
                } else if (StandardGrammars.DOCTYPE_GRAMMAR_DOCTYPE_SYSTEM_ID.equals(lastProperty)) {
                    doctype.setSystemId(token.token().token());
                } else if (StandardGrammars.DOCTYPE_GRAMMAR_DOCTYPE_PUBLIC_ID.equals(lastProperty)) {
                    doctype.setPublicId(token.token().token());
                } else if (StandardGrammars.DOCTYPE_GRAMMAR_DOCTYPE_CONTEXT.equals(lastProperty)) {
                    doctype.setContext(token.token().token());
                } else {
                    assert false : "Unknown property name: " + lastProperty;
                }
                break;
            case STATEMENT_END:
                doctype.setLocation(new SourceLocation(start, token.start(), parser.getSystemId()));
                parser.setDoctype(doctype);
                parser.removeListener(this);
                break;
            default:
                break;
        }
    }

    /**
     * Collect errors related to document type.
     *
     * @param token the token from which errors are collected
     */
    private void collectErrors(final TermToken token) {
        if (token.hasErrors()) {
            doctype.getErrors().add(token.errorInfo());
        }
        if (token.hasPhraseToken() && token.token().hasErrors()) {
            doctype.getErrors().add(token.token().errorInfo());
        }
        if (token.hasLexicalToken() && token.token().hasErrors()) {
            doctype.getErrors().add(token.token().errorInfo());
        }
    }
}
