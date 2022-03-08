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

package net.sf.etl.parsers.event.impl.term.action;

import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.event.grammar.TermParserContext;
import net.sf.etl.parsers.event.impl.term.TermParserContextUtil;

/**
 * The action that advances to the next significant token skipping whitespaces and comments.
 */
public final class AdvanceAction extends SimpleAction {
    /**
     * If true, doc comments are skipped.
     */
    private final boolean skipDocumentation;

    /**
     * The constructor.
     *
     * @param source            the source location in the grammar that caused this node creation
     * @param next              the next action
     * @param skipDocumentation if true doc comments are skipped
     */
    public AdvanceAction(final SourceLocation source, final Action next, final boolean skipDocumentation) {
        super(source, next);
        this.skipDocumentation = skipDocumentation;
    }

    /**
     * The constructor.
     *
     * @param source the source location in the grammar that caused this node creation
     * @param next   the next action
     */
    public AdvanceAction(final SourceLocation source, final Action next) {
        this(source, next, true);
    }

    @Override
    public void parseMore(final TermParserContext context, final ActionState state) {
        if (!TermParserContextUtil.skipIgnorable(getSource(), context, skipDocumentation)) {
            state.nextAction(getNext());
        }
    }

}
