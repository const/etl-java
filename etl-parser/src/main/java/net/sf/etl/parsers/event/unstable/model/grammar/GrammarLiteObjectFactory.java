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
import net.sf.etl.parsers.LoadedGrammarInfo;
import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.event.tree.FieldObjectFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The object factory that implement typical scenarios for loading grammar objects
 */
public class GrammarLiteObjectFactory extends FieldObjectFactory<Element> {
    /**
     * The errors
     */
    private final ArrayList<ErrorInfo> errors = new ArrayList<ErrorInfo>();
    /**
     * The loaded grammar
     */
    private LoadedGrammarInfo loadedGrammar;

    public GrammarLiteObjectFactory() {
        super(Element.class.getClassLoader());
        setPosPolicy(PositionPolicy.SOURCE_LOCATION);
        mapNamespaceToPackage(StandardGrammars.ETL_GRAMMAR_NAMESPACE,
                "net.sf.etl.parsers.event.unstable.model.grammar");
        ignoreNamespace(StandardGrammars.DOCTYPE_NS);
        setAbortOnDefaultGrammar(true);
        ignoreObjects(StandardGrammars.ETL_GRAMMAR_NAMESPACE, "BlankTopLevel");
    }

    @Override
    public void handleErrorFromParser(TermToken errorToken) {
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
    public void valueEnlisted(Element rc, Field f, Object v) {
        if (v instanceof Element) {
            Element e = (Element) v;
            e.ownerObject = rc;
            e.ownerFeature = f;
        }
        super.valueEnlisted(rc, f, v);
    }

    @Override
    public void handleLoadedGrammar(TermToken token, LoadedGrammarInfo loadedGrammarInfo) {
        loadedGrammar = loadedGrammarInfo;
    }

    /**
     * @return all errors gathered during parsing
     */
    public Collection<ErrorInfo> errors() {
        return errors;
    }


    /**
     * @return the loaded grammar
     */
    public LoadedGrammarInfo getLoadedGrammar() {
        return loadedGrammar;
    }
}
