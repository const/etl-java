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
import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.streams.TermParserReader;
import net.sf.etl.parsers.streams.beans.FieldTermParser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

/**
 * An AST parser that is used by lightweight grammar model
 *
 * @author const
 */
public class GrammarLiteTermParser extends FieldTermParser<Element> {
    /**
     * The errors
     */
    private final ArrayList<ErrorInfo> errors = new ArrayList<ErrorInfo>();

    /**
     * A constructor
     *
     * @param parser an underlying parser
     */
    public GrammarLiteTermParser(TermParserReader parser) {
        super(parser, Element.class.getClassLoader());
        mapNamespaceToPackage(StandardGrammars.ETL_GRAMMAR_NAMESPACE, "net.sf.etl.parsers.event.unstable.model.grammar");
        ignoreNamespace(StandardGrammars.DOCTYPE_NS);
        setAbortOnDefaultGrammar(true);
        ignoreObjects(StandardGrammars.ETL_GRAMMAR_NAMESPACE, "BlankTopLevel");
    }

    /**
     * @return all errors gathered during parsing
     */
    public Collection<ErrorInfo> errors() {
        return errors;
    }

    @Override
    protected void handleErrorFromParser(TermToken errorToken) {
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

    @Override
    protected void valueEnlisted(Element rc, Field f, Object v) {
        if (v instanceof Element) {
            Element e = (Element) v;
            e.ownerObject = rc;
            e.ownerFeature = f;
        }
        super.valueEnlisted(rc, f, v);
    }

    @Override
    protected Object setObjectStartPos(Element rc, Class<?> metaObject, TermToken token) {
        rc.start = token.start();
        return rc.start;
    }

    @Override
    protected void setObjectEndPos(Element rc, Class<?> metaObject, Object starValue, TermToken token) {
        rc.end = token.start();
    }
}
