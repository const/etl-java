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
package net.sf.etl.utils;

import net.sf.etl.parsers.event.tree.ObjectFactory;

import java.util.ArrayList;

/**
 * This is an abstract class that helps to implement tools that convert AST to
 * other forms using abstract tree parser.
 *
 * @author const
 */
public abstract class ETL2AST extends AbstractFileConverter {

    /**
     * list of loaded packages
     */
    protected ArrayList<String> ignoredNamespaces = new ArrayList<String>();

    /**
     * If true, text position is expanded
     */
    boolean expandTextPos = true;

    @Override
    protected int handleCustomOption(String[] args, int i) throws Exception {
        if ("-i".equals(args[i])) {
            final String namespace = args[i + 1];
            i++;
            ignoredNamespaces.add(namespace);
        } else if ("-expand-position".equals(args[i])) {
            expandTextPos = Boolean.valueOf(args[i + 1]);
            i++;
        } else {
            return super.handleCustomOption(args, i);
        }
        return i;
    }

    /**
     * Configure parser with standard options
     *
     * @param p a parser to configure
     */
    @SuppressWarnings("unchecked")
    protected void configureStandardOptions(ObjectFactory<?, ?, ?, ?> p) {
        // ignore namespace
        for (String ignoredNamespace : ignoredNamespaces) {
            p.ignoreNamespace(ignoredNamespace);
        }
        // specify text position flag
        ((ObjectFactory<Object, Object, Object, Object>) p).setPosPolicy(expandTextPos
                ? ObjectFactory.PositionPolicyExpanded.get()
                : ObjectFactory.PositionPolicyPositions.get());
    }
}
