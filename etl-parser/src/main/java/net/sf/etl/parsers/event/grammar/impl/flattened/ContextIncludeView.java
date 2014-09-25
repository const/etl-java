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
package net.sf.etl.parsers.event.grammar.impl.flattened;

import net.sf.etl.parsers.event.unstable.model.grammar.ContextInclude;

/**
 * The view of context include. This is used to hold information about included context.
 *
 * @author const
 */
public final class ContextIncludeView extends ContextMemberView {
    /**
     * The context referenced from this definition.
     */
    private final ContextView referencedContext;
    /**
     * The wrapper for all definitions from that context.
     */
    private final WrapperLink wrappers;
    /**
     * The original definition.
     */
    private final ContextIncludeView originalDefinition;
    /**
     * The wrapped definition.
     */
    private final ContextIncludeView wrappedDefinition;
    /**
     * The context include element.
     */
    private final ContextInclude contextIncludeElement;

    /**
     * The constructor for original place.
     *
     * @param contextIncludeElement the element that have caused inclusion of the context
     * @param definingContext       the defining context
     * @param referencedContext     the referenced context
     * @param wrappers              the wrapper chain to be used for statements. It can be null to
     *                              represent that fact that no wrapping is required.
     */
    public ContextIncludeView(final ContextInclude contextIncludeElement,
                              final ContextView definingContext, final ContextView referencedContext,
                              final WrapperLink wrappers) {
        super(definingContext, definingContext);
        this.referencedContext = referencedContext;
        this.wrappers = wrappers;
        this.originalDefinition = this;
        this.wrappedDefinition = this;
        this.contextIncludeElement = contextIncludeElement;
    }

    /**
     * The copy constructor for including the definition.
     *
     * @param includingContext the context in new grammar that includes the definition
     * @param includeView      the view in the parent grammar
     */
    public ContextIncludeView(final ContextView includingContext,
                              final ContextIncludeView includeView) {
        super(includeView.definingContext(), includingContext);
        this.referencedContext = includingContext.grammar().context(
                includeView.referencedContext().name());
        if (referencedContext() == null) {
            throw new IllegalArgumentException(
                    "Assumption failed: non existing context is referenced! "
                            + includeView.referencedContext().name());
        }
        this.wrappers = includeView.wrappers();
        this.originalDefinition = includeView.originalDefinition;
        this.wrappedDefinition = this;
        this.contextIncludeElement = includeView.contextIncludeElement;
    }

    /**
     * The constructor for transitive include view.
     *
     * @param includingView the context in new grammar that includes the definition
     * @param includedView  the view in other context
     */
    public ContextIncludeView(final ContextIncludeView includingView,
                              final ContextIncludeView includedView) {
        super(includedView.definingContext(), includingView.includingContext());
        this.referencedContext = includedView.referencedContext;
        this.wrappers = WrapperLink.concatenate(includingView.wrappers(),
                includedView.wrappers());
        this.originalDefinition = includedView.originalDefinition;
        this.contextIncludeElement = includedView.contextIncludeElement;
        this.wrappedDefinition = includedView.wrappedDefinition();
    }

    /**
     * @return the original definition.
     */
    public ContextIncludeView originalDefinition() {
        return originalDefinition;
    }

    /**
     * @return the referenced context.
     */
    public ContextView referencedContext() {
        return referencedContext;
    }

    /**
     * @return the wrappers.
     */
    public WrapperLink wrappers() {
        return wrappers;
    }

    /**
     * @return Returns the wrappedDefinition.
     */
    public ContextIncludeView wrappedDefinition() {
        return wrappedDefinition;
    }

    /**
     * @return element of this context include
     */
    public ContextInclude contextIncludeElement() {
        return contextIncludeElement;
    }
}
