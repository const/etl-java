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

package net.sf.etl.parsers;

import java.util.Objects;

/**
 * Statement context. It allows identifying the statement context within the grammar.
 */
public final class DefinitionContext {
    /**
     * The context value used when actual context is not known.
     */
    public static final DefinitionContext UNKNOWN = new DefinitionContext(
            new GrammarInfo("unknown.Unknown", "unknown:grammar", null), "unknown");
    /**
     * Grammar.
     */
    private final GrammarInfo grammar;
    /**
     * Context.
     */
    private final String context;

    /**
     * The constructor.
     *
     * @param grammar the grammar system id
     * @param context the context name
     */
    public DefinitionContext(final GrammarInfo grammar, final String context) {
        this.grammar = grammar;
        this.context = context;
    }

    /**
     * @return the grammar system id
     */
    public GrammarInfo grammar() {
        return grammar;
    }

    /**
     * @return the context
     */
    public String context() {
        return context;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DefinitionContext that = (DefinitionContext) o;
        return Objects.equals(grammar, that.grammar) && Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(grammar, context);
    }

    @Override
    public String toString() {
        return "DefinitionContext{grammar='" + grammar + "\', context='" + context + "\'}";
    }
}
