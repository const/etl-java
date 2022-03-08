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
import net.sf.etl.parsers.event.grammar.KeywordContext;
import net.sf.etl.parsers.event.grammar.MapKeywordContext;
import net.sf.etl.parsers.event.grammar.impl.ActionBuilder;
import net.sf.etl.parsers.event.impl.term.action.Action;
import net.sf.etl.parsers.event.impl.term.action.PopKeywordsAction;
import net.sf.etl.parsers.event.impl.term.action.PushKeywordsAction;

import java.util.HashSet;

/**
 * The keyword scope node.
 */
public final class KeywordScopeNode extends CleanupScopeNode {
    /**
     * The keyword context to use.
     */
    private KeywordContext context;

    @Override
    protected Action buildStartState(final ActionBuilder b, final Action bodyStates, final Action errorExit,
                                     final Action errorCloseState) {
        ensureKeywordsGathered(b);
        return new PushKeywordsAction(getSource(), context, bodyStates);
    }

    @Override
    protected Action buildEndState(final ActionBuilder b, final Action normalExit, final Action errorExit) {
        ensureKeywordsGathered(b);
        return new PopKeywordsAction(getSource(), context, normalExit);
    }

    /**
     * Ensure that context keywords are gathered.
     *
     * @param b the builder for the context
     */
    private void ensureKeywordsGathered(final ActionBuilder b) {
        if (context == null) {
            final HashSet<Keyword> keywords = new HashSet<>();
            innerNode().collectKeywords(keywords, new HashSet<>());
            context = new MapKeywordContext(keywords);
            b.contextBuilder().setKeywordContext(context);
        }
    }
}
