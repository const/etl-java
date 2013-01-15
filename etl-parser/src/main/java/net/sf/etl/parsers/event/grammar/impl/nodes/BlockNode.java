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

import net.sf.etl.parsers.DefinitionContext;
import net.sf.etl.parsers.PhraseTokens;
import net.sf.etl.parsers.event.grammar.Keyword;
import net.sf.etl.parsers.event.grammar.LookAheadSet;
import net.sf.etl.parsers.event.grammar.impl.ActionBuilder;
import net.sf.etl.parsers.event.impl.term.action.Action;
import net.sf.etl.parsers.event.impl.term.action.AdvanceAction;
import net.sf.etl.parsers.event.impl.term.action.CallAction;
import net.sf.etl.parsers.event.impl.term.action.RecoverySetupAction;
import net.sf.etl.parsers.event.impl.term.action.buildtime.UnreachableAction;

import java.util.Collections;
import java.util.Set;

/**
 * The block node
 */
public class BlockNode extends Node {
    /**
     * The definition success
     */
    private final DefinitionContext context;

    /**
     * The constructor
     *
     * @param context the context
     */
    public BlockNode(DefinitionContext context) {
        this.context = context;
    }

    @Override
    public void collectKeywords(Set<Keyword> keywords, Set<ActionBuilder> visited) {
        // do nothing
    }

    @Override
    protected boolean calcMatchesEmpty() {
        return false;
    }

    @Override
    public Action buildActions(ActionBuilder b, Action normalExit, Action errorExit, Action recoveryTest) {
        // TODO use statement sequence production, and report location for block
        // skip ignorable tokens after this token. Note that when token is
        // not a doc comment, doc comments are treated as ignorable tokens.
        normalExit = new AdvanceAction(source, normalExit);
        normalExit = new RecoverySetupAction(source, normalExit, recoveryTest);
        // report token
        final CallAction callAction = new CallAction(source);
        b.getLinker().linkBlock(callAction, context);
        callAction.success = normalExit;
        callAction.failure = new UnreachableAction(source, "The errors are unreachable after block!");
        return new ChoiceBuilder(source).
                setFallback(ActionUtil.createReportErrorAction(source, errorExit, "syntax.UnexpectedToken.expectingBlock", context)).
                add(buildLookAhead(Collections.<ActionBuilder>emptySet()), callAction).
                build(b);
    }

    @Override
    protected LookAheadSet createLookAhead(Set<ActionBuilder> visitedBuilders) {
        return LookAheadSet.get(source, PhraseTokens.START_BLOCK);
    }
}
