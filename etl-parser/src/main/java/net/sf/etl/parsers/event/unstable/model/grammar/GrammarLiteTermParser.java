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
package net.sf.etl.parsers.event.unstable.model.grammar;

import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.event.tree.TokenCollector;
import net.sf.etl.parsers.streams.TermParserReader;
import net.sf.etl.parsers.streams.TreeParserReader;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The AST parser that is used by lightweight grammar model.
 *
 * @author const
 */
public final class GrammarLiteTermParser extends TreeParserReader<Element> {
    /**
     * The errors.
     */
    private final ArrayList<ErrorInfo> errors = new ArrayList<ErrorInfo>();

    /**
     * The constructor.
     *
     * @param parser an underlying parser
     */
    public GrammarLiteTermParser(final TermParserReader parser) {
        super(parser, new GrammarLiteObjectFactory());
        setErrorTokenHandler(new TokenCollector() {
            @Override
            public void collect(final TermToken errorToken) {
                if (errorToken.hasErrors()) {
                    errors.add(errorToken.errorInfo());
                }
                if (errorToken.hasPhraseToken() && errorToken.token().hasErrors()) {
                    errors.add(errorToken.token().errorInfo());
                }
                if (errorToken.hasLexicalToken() && errorToken.token().token().hasErrors()) {
                    errors.add(errorToken.token().token().errorInfo());
                }
            }
        });
    }


    /**
     * @return all errors gathered during parsing
     */
    public Collection<ErrorInfo> errors() {
        return errors;
    }
}
