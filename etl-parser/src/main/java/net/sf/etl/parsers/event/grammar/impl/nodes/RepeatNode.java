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
import net.sf.etl.parsers.event.impl.term.action.RecoveryChoiceAction;
import net.sf.etl.parsers.event.impl.term.action.RecoverySetupAction;
import net.sf.etl.parsers.event.impl.term.action.RecoveryVoteAction;
import net.sf.etl.parsers.event.impl.term.action.buildtime.NopAction;

import java.util.Set;

/**
 * The repeat node. This node repeats zero or more times its content.
 *
 * @author const
 */
public final class RepeatNode extends ScopeNode {
    @Override
    public Action buildActions(final ActionBuilder b, final Action normalExitAction, final Action errorExitAction,
                               final Action recoveryTestAction) {
        Action normalExit = normalExitAction;
        Action errorExit = errorExitAction;
        Action recoveryTest = recoveryTestAction;
        final SourceLocation source = getSource();
        final NopAction loopEntry = new NopAction(source);
        LookAheadSet la = innerNode().buildLookAhead();
        // if inner node matches empty, it is tried at least once because it
        // could contain object creation expressions.
        if (la.containsEmpty()) {
            la = new LookAheadSet(la);
            la.removeEmpty();
        }
        normalExit = new RecoverySetupAction(source, normalExit, recoveryTest);
        errorExit = new RecoverySetupAction(source, errorExit, recoveryTest);
        RecoveryChoiceAction recoveryChoiceAction = new RecoveryChoiceAction(source, errorExit);
        recoveryChoiceAction.setRecoveryPath(loopEntry);
        errorExit = recoveryChoiceAction;
        recoveryTest = new ChoiceBuilder(source).
                setFallback(recoveryTest).
                add(la, new RecoveryVoteAction(source, recoveryChoiceAction)).build();
        final Action inner = innerNode().buildActions(b, loopEntry, errorExit, recoveryTest);
        loopEntry.setNext(new ChoiceBuilder(source).setFallback(normalExit).add(la, inner).build());
        return loopEntry.getNext();
    }

    @Override
    protected LookAheadSet createLookAhead(final Set<ActionBuilder> visitedBuilders) {
        final LookAheadSet inner = innerNode().buildLookAhead(visitedBuilders);
        LookAheadSet rc;
        if (inner.containsEmpty()) {
            rc = inner;
        } else {
            rc = new LookAheadSet(inner);
            rc.addEmpty(getSource());
        }
        return rc;
    }

    @Override
    protected boolean calcMatchesEmpty() {
        return true;
    }
}
