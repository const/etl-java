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

package net.sf.etl.parsers.event.impl.term.action;

import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.event.grammar.TermParserContext;

/**
 * Token action.
 */
public final class StructuralTokenAction extends SimpleAction {
    /**
     * The term token type.
     */
    private final Terms kind;
    /**
     * Object type.
     */
    private final Object type;
    /**
     * If true, object is created at the specified mark.
     */
    private final boolean atMark;

    /**
     * The constructor.
     *
     * @param source the source location in the grammar that caused this node creation
     * @param next   the next action
     * @param kind   the token kind
     * @param type   the structure type
     * @param atMark if true, the token is reported after mark
     */
    public StructuralTokenAction(final SourceLocation source, final Action next, final Terms kind, final Object type,
                                 final boolean atMark) {
        super(source, next);
        this.kind = kind;
        this.type = type;
        this.atMark = atMark;
    }

    @Override
    public void parseMore(final TermParserContext context, final ActionState state) {
        if (atMark) {
            final TermToken termToken = context.peekObjectAtMark();
            final TextPos start = termToken != null ? termToken.start() : context.current().start();
            context.produceAfterMark(new TermToken(kind, null, type, null, start, start, getSource(), null));
        } else {
            final TextPos start = context.current().start();
            context.produce(new TermToken(kind, null, type, null, start, start, getSource(), null));
        }
        state.nextAction(getNext());
    }
}
