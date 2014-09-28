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
import net.sf.etl.parsers.event.grammar.LookAheadSet;
import net.sf.etl.parsers.event.grammar.impl.ActionBuilder;
import net.sf.etl.parsers.event.impl.term.action.Action;
import net.sf.etl.parsers.event.impl.term.action.DisableSoftEndAction;
import net.sf.etl.parsers.event.impl.term.action.EnableSoftEndAction;
import net.sf.etl.parsers.event.impl.term.action.RecoveryChoiceAction;
import net.sf.etl.parsers.event.impl.term.action.RecoverySetupAction;
import net.sf.etl.parsers.event.impl.term.action.RecoveryVoteAction;

import java.util.ListIterator;
import java.util.Set;

/**
 * The node sequence node.
 *
 * @author const
 */
public final class SequenceNode extends GroupNode {

    @Override
    public Action buildActions(final ActionBuilder b, final Action normalExit, final Action errorExitAction,
                               final Action recoveryTestAction) {
        // A sequence node is built by creating a sequence of states that
        // correspond to the nodes. Empty sequence results in normalExit node.
        final SourceLocation source = getSource();
        Action head = normalExit;
        Action errorExit = errorExitAction;
        Action recoveryTest = recoveryTestAction;
        head = new RecoverySetupAction(source, head, recoveryTest);
        errorExit = new RecoverySetupAction(source, errorExit, recoveryTest);
        boolean wasNonEmpty = false;
        LookAheadSet previousLa = new LookAheadSet();
        final ListIterator<Node> i = nodes().listIterator(nodes().size());
        while (i.hasPrevious()) {
            final Node node = i.previous();
            final LookAheadSet actualLa = node.buildLookAhead();
            final LookAheadSet currentLa = new LookAheadSet(actualLa); // NOPMD
            if (currentLa.containsEmpty()) {
                currentLa.addAll(previousLa);
                currentLa.removeEmpty();
            }
            previousLa = currentLa;
            if (currentLa.isEmpty()) {
                head = node.buildActions(b, head, errorExit, recoveryTest);
            } else {
                final RecoveryChoiceAction recoveryChoiceAction =
                        new RecoveryChoiceAction(node.getSource(), errorExit); //NOPMD
                errorExit = recoveryChoiceAction;
                head = node.buildActions(b, head, errorExit, recoveryTest);
                recoveryChoiceAction.setRecoveryPath(head);
                recoveryTest = new ChoiceBuilder(node.getSource())  // NOPMD
                        .setFallback(recoveryTest)
                        .add(currentLa, new RecoveryVoteAction(node.getSource(), recoveryChoiceAction)) // NOPMD
                        .build();
                head = new RecoverySetupAction(node.getSource(), head, recoveryTest); // NOPMD
            }
            if (!wasNonEmpty && !node.matchesEmpty() && i.hasPrevious()) {
                wasNonEmpty = true;
                head = new EnableSoftEndAction(source, head); // NOPMD
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
    protected LookAheadSet createLookAhead(final Set<ActionBuilder> visitedBuilders) {
        if (nodes().isEmpty()) {
            return LookAheadSet.getWithEmpty(getSource());
        }
        if (nodes().size() == 1) {
            return nodes().get(0).buildLookAhead(visitedBuilders);
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
