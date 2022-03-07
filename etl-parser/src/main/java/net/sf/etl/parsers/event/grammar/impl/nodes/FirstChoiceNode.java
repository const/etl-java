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

import net.sf.etl.parsers.event.grammar.LookAheadSet;
import net.sf.etl.parsers.event.grammar.impl.ActionBuilder;
import net.sf.etl.parsers.event.impl.term.action.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

/**
 * This node represents first choice construct. This construct tries to select
 * the first node, and than the second node.
 *
 * @author const
 */
public final class FirstChoiceNode extends GroupNode {
    @Override
    public Action buildActions(final ActionBuilder b, final Action normalExit, final Action errorExit,
                               final Action recoveryTest) {
        final HashSet<ActionBuilder> visitedSet = new HashSet<>();
        final ArrayList<Node> nodes = new ArrayList<>(nodes());
        Action current = null;
        if (matchesEmpty() && !nodes.get(nodes().size() - 1).matchesEmpty()) {
            final ListIterator<Node> i = nodes().listIterator(nodes().size());
            while (i.hasPrevious()) {
                final Node node = i.previous();
                if (node.matchesEmpty()) {
                    current = node.buildActions(b, normalExit, errorExit, recoveryTest);
                    break;
                }
            }
            assert current != null;
        } else {
            final Node node = nodes().remove(nodes().size() - 1);
            current = node.buildActions(b, normalExit, errorExit, recoveryTest);
        }
        Collections.reverse(nodes);
        for (final Node node : nodes) {
            final ChoiceBuilder choiceBuilder = new ChoiceBuilder(getSource()); // NOPMD
            choiceBuilder.setFallback(current);
            final LookAheadSet la = new LookAheadSet(node.buildLookAhead(visitedSet)); // NOPMD
            la.removeEmpty();
            choiceBuilder.add(la, node.buildActions(b, normalExit, errorExit, recoveryTest));
            current = choiceBuilder.build();
        }
        return current;
    }

    @Override
    protected boolean calcMatchesEmpty() {
        assert nodes().size() >= 2 : "First choice node must have at least two alternative";
        for (final Node node : nodes()) {
            if (node.matchesEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected LookAheadSet createLookAhead(final Set<ActionBuilder> visitedBuilders) {
        assert nodes().size() >= 2 : "First choice node must have at least two alternative";
        final LookAheadSet rc = new LookAheadSet();
        for (final Node node : nodes()) {
            rc.addAll(node.buildLookAhead(visitedBuilders));
        }
        return rc;
    }

    @Override
    public Node flatten() {
        final FirstChoiceNode node = (FirstChoiceNode) super.flatten();
        return node.nodes().size() == 1 ? nodes().get(0) : this;
    }
}
