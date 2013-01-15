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
import net.sf.etl.parsers.event.impl.term.action.*;

import java.util.ListIterator;
import java.util.Set;

/**
 * The node sequence node
 *
 * @author const
 */
public class SequenceNode extends GroupNode {

    @Override
    public Action buildActions(ActionBuilder b, Action normalExit, Action errorExit, Action recoveryTest) {
        // A sequence node is built by creating a sequence of states that
        // correspond to the nodes. Empty sequence results in normalExit node.
        Action head = normalExit;
        head = new RecoverySetupAction(source, head, recoveryTest);
        errorExit = new RecoverySetupAction(source, errorExit, recoveryTest);
        boolean wasNonEmpty = false;
        LookAheadSet previousLa = new LookAheadSet();
        for (final ListIterator<Node> i = nodes().listIterator(nodes().size()); i.hasPrevious(); ) {
            final Node node = i.previous();
            final LookAheadSet actualLa = node.buildLookAhead();
            final LookAheadSet currentLa = new LookAheadSet(actualLa);
            if (currentLa.containsEmpty()) {
                currentLa.addAll(previousLa);
                currentLa.removeEmpty();
            }
            previousLa = currentLa;
            if (currentLa.isEmpty()) {
                head = node.buildActions(b, head, errorExit, recoveryTest);
            } else {
                RecoveryChoiceAction recoveryChoiceAction = new RecoveryChoiceAction(node.source, errorExit);
                errorExit = recoveryChoiceAction;
                head = node.buildActions(b, head, errorExit, recoveryTest);
                recoveryChoiceAction.recoveryPath = head;
                recoveryTest = new ChoiceBuilder(node.source).
                        setFallback(recoveryTest).
                        add(currentLa, new RecoveryVoteAction(node.source, recoveryChoiceAction)).
                        build(b);
                head = new RecoverySetupAction(node.source, head, recoveryTest);
            }
            if (!wasNonEmpty && !node.matchesEmpty() && i.hasPrevious()) {
                wasNonEmpty = true;
                head = new EnableSoftEndAction(source, head);
            }
        }
        if (wasNonEmpty) {
            head = new DisableSoftEndAction(source, head);
        }
        return head;
    }

    @Override
    protected boolean calcMatchesEmpty() {
        for (final Node node : nodes()) {
            if (!node.matchesEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected LookAheadSet createLookAhead(Set<ActionBuilder> visitedBuilders) {
        if (nodes().isEmpty()) {
            return LookAheadSet.getWithEmpty(source);
        }
        if (nodes().size() == 1) {
            return (nodes().get(0)).buildLookAhead(visitedBuilders);
        }
        final LookAheadSet rc = new LookAheadSet();
        for (final Node node : nodes()) {
            rc.removeEmpty();
            rc.addAll(node.buildLookAhead(visitedBuilders));
            if (!rc.containsEmpty()) {
                return rc;
            }
        }
        return rc;
    }
}
