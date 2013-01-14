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
import net.sf.etl.parsers.event.impl.term.action.buildtime.NopAction;

import java.util.HashSet;
import java.util.Set;

/**
 * The repeat node. This node repeats zero or more times its content.
 *
 * @author const
 */
public class RepeatNode extends ScopeNode {
    @Override
    public Action buildActions(ActionBuilder b, Action normalExit, Action errorExit) {
        final NopAction loopExit = new NopAction(source);
        final Action inner = innerNode().buildActions(b, loopExit, errorExit);
        LookAheadSet la = innerNode().buildLookAhead(new HashSet<ActionBuilder>());
        final ChoiceBuilder choiceBuilder = new ChoiceBuilder(source).setFallback(normalExit);
        final Action rc;
        // if inner node matches empty, it is tried at least once because it
        // could contain object creation expressions.
        if (la.containsEmpty()) {
            la = new LookAheadSet(la);
            la.removeEmpty();
            loopExit.next = choiceBuilder.add(la, inner).build(b);
            rc = inner;
        } else {
            rc = choiceBuilder.add(la, inner).build(b);
            loopExit.next = rc;
        }
        return rc;
    }

    @Override
    protected LookAheadSet createLookAhead(Set<ActionBuilder> visitedBuilders) {
        final LookAheadSet inner = innerNode().buildLookAhead(visitedBuilders);
        LookAheadSet rc;
        if (inner.containsEmpty()) {
            rc = inner;
        } else {
            rc = new LookAheadSet(inner);
            rc.addEmpty();
        }
        return rc;
    }

    @Override
    protected boolean calcMatchesEmpty() {
        return true;
    }
}
