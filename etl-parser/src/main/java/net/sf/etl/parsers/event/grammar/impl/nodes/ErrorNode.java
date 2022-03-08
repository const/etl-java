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

import java.util.Set;

/**
 * This node report error to parser and rises error.
 *
 * @author const
 */
public final class ErrorNode extends Node {
    /**
     * The id of the error.
     */
    private final String errorId;
    /**
     * The arguments of the error.
     */
    private final Object[] errorArgs;

    /**
     * A constructor from fields.
     *
     * @param errorId   the error id
     * @param errorArgs the error args
     */
    public ErrorNode(final String errorId, final Object... errorArgs) {
        this.errorId = errorId;
        this.errorArgs = errorArgs;
    }

    @Override
    protected boolean calcMatchesEmpty() {
        return true;
    }


    @Override
    public void collectKeywords(final Set<Keyword> keywords, final Set<ActionBuilder> visited) {
        // do nothing
    }

    @Override
    public Action buildActions(final ActionBuilder b, final Action normalExit, final Action errorExit,
                               final Action recoveryTest) {
        return ActionUtil.createReportErrorAction(getSource(), errorExit, errorId, errorArgs);
    }

    @Override
    protected LookAheadSet createLookAhead(final Set<ActionBuilder> visitedBuilders) {
        return LookAheadSet.getWithEmpty(getSource());
    }

}
