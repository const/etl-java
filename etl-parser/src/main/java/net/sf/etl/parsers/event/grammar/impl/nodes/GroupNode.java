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
import net.sf.etl.parsers.event.grammar.impl.ActionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This is a base utility class that holds list of nodes.
 *
 * @author const
 */
public abstract class GroupNode extends Node {
    /**
     * The list of nodes.
     */
    private final List<Node> nodes = new ArrayList<>();

    /**
     * @return the list of nodes
     */
    public final List<Node> nodes() {
        return nodes;
    }

    @Override
    public final void collectKeywords(final Set<Keyword> keywords, final Set<ActionBuilder> visited) {
        for (final Node node : nodes) {
            node.collectKeywords(keywords, visited);
        }
    }

    // CHECKSTYLE:OFF
    @Override
    public Node flatten() {
        boolean needsFlatten = false;
        final Class<?> type = getClass();
        for (final Node node : nodes) {
            if (node.getClass() == type) {
                needsFlatten = true;
                break;
            }
        }
        if (needsFlatten) {
            final ArrayList<Node> copy = new ArrayList<>(nodes);
            nodes.clear();
            flattenTo(type, copy, nodes);
        }
        return this;
    }
    // CHECKSTYLE:ON


    @Override
    public Node inferSoftBreaks() {
        for (int i = 0; i < nodes().size() ; i++) {
            nodes.set(i, nodes.get(i).flatten());
        }
        return this;
    }

    /**
     * Flatten nodes.
     *
     * @param type      the node type
     * @param toFlatten the non flat nodes.
     * @param result    the nodes to flatten
     */
    private void flattenTo(final Class<?> type, final List<Node> toFlatten, final List<Node> result) {
        for (final Node node : toFlatten) {
            if (node.getClass() == type) {
                flattenTo(type, ((GroupNode) node).nodes(), result);
            } else {
                result.add(node);
            }
        }
    }
}
