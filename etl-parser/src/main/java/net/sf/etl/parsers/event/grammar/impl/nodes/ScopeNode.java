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
package net.sf.etl.parsers.event.grammar.impl.nodes;

import net.sf.etl.parsers.event.grammar.Keyword;
import net.sf.etl.parsers.event.grammar.LookAheadSet;
import net.sf.etl.parsers.event.grammar.impl.ActionBuilder;

import java.util.Set;

/**
 * The scope node.
 *
 * @author const
 */
public abstract class ScopeNode extends Node {
    /**
     * The node that belongs to the scope.
     */
    private Node innerNode = new SequenceNode();

    /**
     * @return Returns the node.
     */
    public final Node innerNode() {
        return innerNode;
    }

    // CHECKSTYLE:OFF
    @Override
    protected boolean calcMatchesEmpty() {
        return innerNode.matchesEmpty();
    }
    // CHECKSTYLE:ON

    // CHECKSTYLE:OFF
    @Override
    protected LookAheadSet createLookAhead(final Set<ActionBuilder> visitedBuilders) {
        return innerNode.buildLookAhead(visitedBuilders);
    }
    // CHECKSTYLE:ON


    @Override
    public final void collectKeywords(final Set<Keyword> keywords, final Set<ActionBuilder> visited) {
        innerNode.collectKeywords(keywords, visited);
    }

    @Override
    public final Node flatten() {
        innerNode = innerNode.flatten();
        return this;
    }

    @Override
    public Node inferSoftBreaks() {
        innerNode = innerNode.inferSoftBreaks();
        return super.inferSoftBreaks();
    }
}
