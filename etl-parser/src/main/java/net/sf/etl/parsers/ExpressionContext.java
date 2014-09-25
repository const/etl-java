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

/**
 * The expression context that identifies the expression context within some statement context.
 * Grammars for expression context and statement context could be different. For example in
 * the case when sql-like language is hosted within java like language.
 */
public final class ExpressionContext {
    /**
     * Context that includes this statement context.
     */
    private final DefinitionContext hostContext;
    /**
     * The expression grammar.
     */
    private final GrammarInfo expressionGrammar;
    /**
     * The expression context name.
     */
    private final String context;
    /**
     * The initial precedence level.
     */
    private final int precedenceLevel;

    /**
     * The constructor.
     *
     * @param hostContext       the host statement context
     * @param expressionGrammar the expression grammar
     * @param context           the expression context name
     * @param precedenceLevel   the precedence level
     */
    public ExpressionContext(final DefinitionContext hostContext, final GrammarInfo expressionGrammar,
                             final String context, final Integer precedenceLevel) {
        this.hostContext = hostContext;
        this.expressionGrammar = expressionGrammar;
        this.context = context;
        this.precedenceLevel = precedenceLevel == null ? Integer.MAX_VALUE : 0;
    }


    /**
     * The constructor.
     *
     * @param hostContext       the host statement context
     * @param referencedContext the expression context name
     * @param precedenceLevel   the precedence level
     */
    public ExpressionContext(final DefinitionContext hostContext, final DefinitionContext referencedContext,
                             final Integer precedenceLevel) {
        this.hostContext = hostContext;
        this.expressionGrammar = referencedContext.grammar();
        this.context = referencedContext.context();
        this.precedenceLevel = precedenceLevel == null ? Integer.MAX_VALUE : 0;
    }

    /**
     * @return the host statement context
     */
    public DefinitionContext hostContext() {
        return hostContext;
    }

    /**
     * @return the expression grammar system id
     */
    public GrammarInfo expressionGrammar() {
        return expressionGrammar;
    }

    /**
     * @return the context name
     */
    public String context() {
        return context;
    }

    /**
     * @return the initial precedence level for the context
     */
    public int precedenceLevel() {
        return precedenceLevel;
    }

    @Override
    public boolean equals(final Object o) {
        //CHECKSTYLE:OFF
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpressionContext that = (ExpressionContext) o;

        if (precedenceLevel != that.precedenceLevel) return false;
        if (context != null ? !context.equals(that.context) : that.context != null) return false;
        if (expressionGrammar != null ? !expressionGrammar.equals(that.expressionGrammar)
                : that.expressionGrammar != null)
            return false;
        //noinspection RedundantIfStatement
        if (hostContext != null ? !hostContext.equals(that.hostContext) : that.hostContext != null) return false;

        return true;
        //CHECKSTYLE:ON
    }

    @Override
    public int hashCode() {
        //CHECKSTYLE:OFF
        int result = hostContext != null ? hostContext.hashCode() : 0;
        result = 31 * result + (expressionGrammar != null ? expressionGrammar.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        result = 31 * result + precedenceLevel;
        return result;
        //CHECKSTYLE:ON
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ExpressionContext");
        sb.append("{hostContext=").append(hostContext);
        sb.append(", expressionGrammar=").append(expressionGrammar);
        sb.append(", context='").append(context).append('\'');
        sb.append(", precedenceLevel=").append(precedenceLevel);
        sb.append('}');
        return sb.toString();
    }
}
