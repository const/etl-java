/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2009 Constantine A Plotnikov
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
 * Context of the term. This object is immutable.
 *
 * @author const
 */
public final class TermContext {
    /**
     * a constant for unknown term context. It is used when skipping blocks and
     * segments in case of error
     */
    public static final TermContext UNKNOWN = new TermContext("net.sf.etl.grammars.Unknown", "unknown", "unknown:");
    /**
     * Name of the grammar
     */
    private final String grammarName;
    /**
     * Context for the grammar
     */
    private final String context;
    /**
     * Grammar systemId
     */
    private final String grammarSystemId;
    /**
     * A priority of the context
     */
    private final int priority;

    /**
     * A constructor of context
     *
     * @param grammarName     grammar system identifier
     * @param context         context in the grammar
     * @param grammarSystemId a systemId of the grammar
     */
    public TermContext(String grammarName, String context,
                       String grammarSystemId) {
        this(grammarName, context, grammarSystemId, Integer.MAX_VALUE);
    }

    /**
     * A constructor of context with previous context and new priority.
     *
     * @param c        a previous context
     * @param priority a priority value
     */
    public TermContext(TermContext c, int priority) {
        this(c.grammarName, c.context, c.grammarSystemId, priority);
    }

    /**
     * A constructor of context
     *
     * @param grammarName     the grammar system identifier
     * @param context         the context in the grammar
     * @param grammarSystemId the systemId of the grammar
     * @param priority        the start priority of the context
     */
    public TermContext(String grammarName, String context,
                       String grammarSystemId, int priority) {
        super();
        this.context = context;
        this.grammarName = grammarName;
        this.grammarSystemId = grammarSystemId;
        this.priority = priority;
    }

    /**
     * @return context of parser
     */
    public String context() {
        return context;
    }

    /**
     * @return grammar used to define current context
     */
    public String grammarName() {
        return grammarName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return grammarName + ":" + context + "@" + grammarSystemId();
    }

    /**
     * @return Returns the grammar system id.
     */
    public String grammarSystemId() {
        return grammarSystemId;
    }

    /**
     * @return start priority of the context. Integer.MAX_VALUE means that no
     *         priority for context has been specified.
     */
    public int priority() {
        return priority;
    }

}
