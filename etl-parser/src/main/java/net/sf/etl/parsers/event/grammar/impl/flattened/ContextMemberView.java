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

/**
 * A base class for named context members. This class is special.
 *
 * @author const
 */
public abstract class ContextMemberView {
    /**
     * the context that included definition from other grammar
     */
    private final ContextView includingContext;

    /**
     * the context that defines this definition
     */
    private final ContextView definingContext;

    /**
     * A constructor
     *
     * @param definingContext  a context that defines this member
     * @param includingContext a context that includes this member using grammar include
     */
    public ContextMemberView(ContextView definingContext,
                             ContextView includingContext) {
        super();
        this.definingContext = definingContext;
        this.includingContext = includingContext;
    }

    /**
     * @return Returns the definingContext.
     */
    public ContextView definingContext() {
        return definingContext;
    }

    /**
     * @return including context is a context that contains definition because
     *         of grammar include.
     */
    public ContextView includingContext() {
        return includingContext;
    }
}
