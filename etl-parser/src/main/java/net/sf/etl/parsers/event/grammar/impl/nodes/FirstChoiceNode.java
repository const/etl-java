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

import java.util.*;

/**
 * This node represents first choice construct. This construct tries to select
 * the first node, and than the second node.
 *
 * @author const
 */
public class FirstChoiceNode extends GroupNode {
    @Override
    public Action buildActions(ActionBuilder b, Action normalExit, Action errorExit) {
        final HashSet<ActionBuilder> visitedSet = new HashSet<ActionBuilder>();
        ArrayList<Node> nodes = new ArrayList<Node>(nodes());
        Action current = null;
        if (matchesEmpty() && !nodes.get(nodes().size() - 1).matchesEmpty()) {
            for (ListIterator<Node> i = nodes().listIterator(nodes().size()); i.hasPrevious(); ) {
                Node node = i.previous();
                if (node.matchesEmpty()) {
                    current = node.buildActions(b, normalExit, errorExit);
                    break;
                }
            }
            assert current != null;
        } else {
            Node node = nodes().remove(nodes().size() - 1);
            current = node.buildActions(b, normalExit, errorExit);
        }
        Collections.reverse(nodes);
        for (Node node : nodes) {
            ChoiceBuilder choiceBuilder = new ChoiceBuilder();
            choiceBuilder.setFallback(current);
            LookAheadSet la = new LookAheadSet(node.buildLookAhead(visitedSet));
            la.removeEmpty();
            choiceBuilder.add(la, node.buildActions(b, normalExit, errorExit));
            current = choiceBuilder.build(b);
        }
        return current;
    }

    @Override
    protected boolean calcMatchesEmpty() {
        assert nodes().size() < 2 : "First choice node must have at least two alternative";
        for (final Node node : nodes()) {
            if (node.matchesEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected LookAheadSet createLookAhead(Set<ActionBuilder> visitedBuilders) {
        assert nodes().size() < 2 : "First choice node must have at least two alternative";
        final LookAheadSet rc = new LookAheadSet();
        for (final Node node : nodes()) {
            rc.addAll(node.buildLookAhead(visitedBuilders));
        }
        return rc;
    }
}