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
import net.sf.etl.parsers.event.grammar.LookAheadSet;
import net.sf.etl.parsers.event.grammar.impl.ActionBuilder;
import net.sf.etl.parsers.event.impl.term.action.Action;
import net.sf.etl.parsers.event.impl.term.action.CallAction;

import java.util.Set;

/**
 * A call of state factory. Note that it is assumed that called production
 * is never matches empty sequence of tokens and always either consume something
 * or exits with error.
 *
 * @author const
 */
public final class CallNode extends Node {
    /**
     * The builder for the factory.
     */
    private final ActionBuilder factoryBuilder;

    /**
     * The constructor for call node.
     *
     * @param factoryBuilder the builder for invoked state factory
     */
    public CallNode(final ActionBuilder factoryBuilder) {
        this.factoryBuilder = factoryBuilder;
    }

    @Override
    public void collectKeywords(final Set<Keyword> keywords, final Set<ActionBuilder> visited) {
        factoryBuilder.collectKeywords(keywords, visited);
    }

    @Override
    public Action buildActions(final ActionBuilder b, final Action normalExit, final Action errorExit,
                               final Action recoveryTest) {
        final CallAction callAction = new CallAction(getSource());
        factoryBuilder.link(callAction);
        callAction.setSuccess(normalExit);
        callAction.setFailure(errorExit);
        return callAction;
    }


    @Override
    protected boolean calcMatchesEmpty() {
        // TODO currently all variants are false, but think more on it later
        return false;
    }

    @Override
    protected LookAheadSet createLookAhead(final Set<ActionBuilder> visitedBuilders) {
        return factoryBuilder.buildLookAhead(visitedBuilders);
    }
}
