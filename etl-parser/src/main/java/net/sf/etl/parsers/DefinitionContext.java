/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2012 Constantine A Plotnikov
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

/**
 * Statement context. It allows identifying the statement context within the grammar.
 */
public class DefinitionContext {
    /**
     * The context value used when actual context is not known
     */
    public static final DefinitionContext UNKNOWN = new DefinitionContext(new GrammarInfo("unknown.Unknown", "unknown:grammar", null), "unknown");
    /**
     * Grammar
     */
    private final GrammarInfo grammar;
    /**
     * Context
     */
    private final String context;

    /**
     * The constructor
     *
     * @param grammar the grammar system id
     * @param context the context name
     */
    public DefinitionContext(GrammarInfo grammar, String context) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefinitionContext that = (DefinitionContext) o;

        if (context != null ? !context.equals(that.context) : that.context != null) return false;
        //noinspection RedundantIfStatement
        if (grammar != null ? !grammar.equals(that.grammar) : that.grammar != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = grammar != null ? grammar.hashCode() : 0;
        result = 31 * result + (context != null ? context.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DefinitionContext");
        sb.append("{grammar='").append(grammar).append('\'');
        sb.append(", context='").append(context).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
