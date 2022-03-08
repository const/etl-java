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

package net.sf.etl.parsers.event.unstable.model.grammar;

import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.event.tree.SimpleObjectFactory;

/**
 * The object factory that implement typical scenarios for loading grammar objects.
 */
public final class GrammarLiteObjectFactory extends SimpleObjectFactory<Element> {

    /**
     * The object factory.
     */
    public GrammarLiteObjectFactory() {
        super(Element.class.getClassLoader());
        setPosPolicy(PositionPolicyLocation.get());
        mapNamespaceToPackage(StandardGrammars.ETL_GRAMMAR_NAMESPACE,
                "net.sf.etl.parsers.event.unstable.model.grammar");
        ignoreNamespace(StandardGrammars.DOCTYPE_NS);
        setAbortOnDefaultGrammar(true);
        ignoreObjects(StandardGrammars.ETL_GRAMMAR_NAMESPACE, "BlankTopLevel");
    }

    @Override
    public void valueEnlisted(final Element rc, final Property f, final Object v) {
        if (v instanceof Element) {
            final Element e = (Element) v;
            e.setOwnerObject(rc);
            e.setOwnerFeature(f.getName());
        }
        super.valueEnlisted(rc, f, v);
    }
}
