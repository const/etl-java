/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2022 Konstantin Plotnikov
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
import net.sf.etl.parsers.event.impl.term.action.Action;
import net.sf.etl.parsers.event.impl.term.action.CommitMarkAction;

import java.util.Set;

/**
 * A commit mark node. This node invokes commit mark operation on parser.
 *
 * @author const
 */
public final class CommitMarkNode extends Node {
    @Override
    public void collectKeywords(final Set<Keyword> keywords, final Set<ActionBuilder> visited) {
        // do nothing
    }

    @Override
    protected boolean calcMatchesEmpty() {
        return true;
    }

    @Override
    public Action buildActions(final ActionBuilder b, final Action normalExit, final Action errorExit,
                               final Action recoveryTest) {
        return new CommitMarkAction(getSource(), normalExit);
    }

    @Override
    protected LookAheadSet createLookAhead(final Set<ActionBuilder> visitedBuilders) {
        return LookAheadSet.getWithEmpty(getSource());
    }
}
