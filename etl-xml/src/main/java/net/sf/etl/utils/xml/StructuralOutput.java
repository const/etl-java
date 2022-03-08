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

import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.TermToken;

import javax.xml.stream.XMLStreamException;

/**
 * The structured output.
 */
public abstract class StructuralOutput extends XMLOutput {
    /**
     * The last token in error.
     */
    private TermToken lastErrors;

    /**
     * Handle token that might contain errors.
     *
     * @throws XMLStreamException the write exception
     */
    protected final void checkErrors() throws XMLStreamException {
        final TermToken t = parser().current();
        if (t != lastErrors) { // NOPMD
            lastErrors = t;
            printErrors(t.errorInfo());
            if (t.hasPhraseToken()) {
                printErrors(t.token().errorInfo());
            }
            if (t.hasLexicalToken()) {
                printErrors(t.token().token().errorInfo());
            }
        }
    }

    /**
     * Print errors.
     *
     * @param error the error
     * @throws XMLStreamException the write exception
     */
    private void printErrors(final ErrorInfo error) throws XMLStreamException {
        for (ErrorInfo errorInfo = error; errorInfo != null; errorInfo = errorInfo.cause()) {
            out().writeComment(errorInfo.errorId() + ": " + errorInfo.location().toShortString()
                    + ": " + errorInfo.message());
            out().writeCharacters("\n");
        }
    }

    /**
     * Process the token not handled by the structural output.
     *
     * @throws XMLStreamException in case of IO problem
     */
    public final void otherToken() throws XMLStreamException {
        checkErrors();
        parser().advance();
    }
}
