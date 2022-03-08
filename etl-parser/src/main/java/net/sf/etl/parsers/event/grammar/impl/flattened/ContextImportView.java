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
package net.sf.etl.parsers.event.grammar.impl.flattened;

import net.sf.etl.parsers.event.unstable.model.grammar.ContextImport;

/**
 * This is a view of context import.
 *
 * @author const
 */
public final class ContextImportView extends ContextMemberView {
    /**
     * The local name of imported context.
     */
    private final String localName;
    /**
     * The context import definition.
     */
    private final ContextImport definition;
    /**
     * The referenced context.
     */
    private final ContextView referencedContext;
    /**
     * The original definition.
     */
    private final ContextImportView originalDefinition;

    /**
     * The constructor.
     *
     * @param definition        the context import to be wrapped
     * @param definingContext   the context that defined this context import
     * @param referencedContext the context referenced by this import
     */
    public ContextImportView(final ContextImport definition, final ContextView definingContext,
                             final ContextView referencedContext) {
        super(definingContext, definingContext);
        this.definition = definition;
        this.localName = definition.getLocalName();
        this.referencedContext = referencedContext;
        this.originalDefinition = this;
    }

    /**
     * The constructor from other definition.
     *
     * @param importingContext the context that imports definition
     * @param definition       the context import to be wrapped
     */
    public ContextImportView(final ContextView importingContext, final ContextImportView definition) {
        super(definition.definingContext(), importingContext);
        this.definition = definition.definition;
        this.localName = definition.localName;
        if (definition.definition.getGrammarName() != null) {
            this.referencedContext = definition.referencedContext;
        } else {
            this.referencedContext = importingContext.grammar().context(definition.referencedContext.name());
            if (referencedContext == null) {
                throw new IllegalArgumentException("Assumption failed: non existing context is referenced! "
                        + definition.referencedContext.name());
            }
        }
        this.originalDefinition = definition.originalDefinition;
    }

    /**
     * @return local name of view
     */
    public String localName() {
        return localName;
    }

    /**
     * @return Context referenced by wrapped import.
     */
    public ContextView referencedContext() {
        return referencedContext;
    }

    /**
     * @return this method returns original definition for this import
     */
    public ContextImportView originalDefinition() {
        return originalDefinition;
    }

    /**
     * @return an grammar object for this import
     */
    public ContextImport definition() {
        return definition;
    }
}
