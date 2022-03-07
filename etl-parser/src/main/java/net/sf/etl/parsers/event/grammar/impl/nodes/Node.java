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

import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.event.grammar.Keyword;
import net.sf.etl.parsers.event.grammar.LookAheadSet;
import net.sf.etl.parsers.event.grammar.impl.ActionBuilder;
import net.sf.etl.parsers.event.grammar.impl.flattened.DefinitionView;
import net.sf.etl.parsers.event.impl.term.action.Action;

import java.util.HashSet;
import java.util.Set;

/**
 * This is a base class for all nodes. Note that nodes are not thread safe.
 *
 * @author const
 */
public abstract class Node {
    /**
     * The source for the node.
     */
    private SourceLocation source;
    /**
     * The definition view that has caused creation of this node.
     */
    private DefinitionView definition;
    /**
     * The builder that owns this node.
     */
    private ActionBuilder builder;
    /**
     * If true, node matches an empty node.
     */
    private Boolean matchesEmpty;
    /**
     * The look ahead information.
     */
    private LookAheadSet cachedLookAhead;

    /**
     * Collect keywords in the nodes.
     *
     * @param keywords the keywords
     * @param visited  the visited builders
     */
    public abstract void collectKeywords(final Set<Keyword> keywords, final Set<ActionBuilder> visited);

    /**
     * Check if this node matches an empty sequence of tokens. Note that this
     * method is called before lookahead of the node can be calculated. The
     * grammar is incomplete state at this moment of time.
     *
     * @return true if this node matches empty sequence of tokens.
     */
    public final boolean matchesEmpty() {
        if (matchesEmpty == null) {
            matchesEmpty = calcMatchesEmpty();
        }
        return matchesEmpty;
    }

    /**
     * @return determine if this node matches empty
     */
    protected abstract boolean calcMatchesEmpty();

    /**
     * Build state machine for the node.
     *
     * @param b            the builder for state machine
     * @param normalExit   the normal exit for the node
     * @param errorExit    the error exit for the node
     * @param recoveryTest the current recovery test action
     * @return the first state of state machine described by this node
     */
    public abstract Action buildActions(final ActionBuilder b, final Action normalExit, final Action errorExit,
                                        final Action recoveryTest);

    /**
     * Build lookahead if does not exists.
     *
     * @param visitedBuilders visited builders set, it is used to avoid infinite recursion
     * @return created or cached lookahead
     */
    public final LookAheadSet buildLookAhead(final Set<ActionBuilder> visitedBuilders) {
        if (cachedLookAhead == null) {
            cachedLookAhead = createLookAhead(visitedBuilders);
        }
        return cachedLookAhead;
    }

    /**
     * @return build look ahead starting from the current node
     */
    public final LookAheadSet buildLookAhead() {
        return buildLookAhead(new HashSet<ActionBuilder>());
    }

    /**
     * Create lookahead object.
     *
     * @param visitedBuilders visited builders
     * @return the look ahead object
     */
    protected abstract LookAheadSet createLookAhead(final Set<ActionBuilder> visitedBuilders);

    /**
     * @return the definition.
     */
    public final DefinitionView getDefinition() {
        return definition;
    }

    /**
     * @param definition The definition to set.
     */
    public final void setDefinition(final DefinitionView definition) {
        this.definition = definition;
    }

    /**
     * @return the builder that have created this node.
     */
    public final ActionBuilder getBuilder() {
        return builder;
    }

    /**
     * @param builder the builder that have created this node.
     */
    public final void setBuilder(final ActionBuilder builder) {
        this.builder = builder;
    }

    // CHECKSTYLE:OFF

    /**
     * @return flattened node with the sub nodes of the same kind merged
     */
    public Node flatten() {
        return this;
    }

    /**
     * @return the node with inferred soft breaks.
     */
    public Node inferSoftBreaks() {
        return this;
    }
    // CHECKSTYLE:ON


    /**
     * @return the source location
     */
    public final SourceLocation getSource() {
        return source;
    }

    /**
     * Set source location.
     *
     * @param source the location
     */
    public final void setSource(final SourceLocation source) {
        this.source = source;
    }
}
