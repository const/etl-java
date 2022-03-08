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

import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.SyntaxRole;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.event.grammar.TermParserContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The action that reports error and goes to the next token.
 */
public final class ReportErrorAction extends SimpleAction {
    /**
     * The error id.
     */
    private final String errorId;
    /**
     * The objects.
     */
    private final List<Object> objects;

    /**
     * The report tokens action.
     *
     * @param source  the source location in the grammar that caused this node creation
     * @param next    the next action
     * @param errorId the error id
     * @param objects the error arguments
     */
    public ReportErrorAction(final SourceLocation source, final Action next, final String errorId,
                             final Object... objects) {
        super(source, next);
        this.errorId = errorId;
        this.objects = objects == null || objects.length == 0
                ? Collections.emptyList()
                : Collections.unmodifiableList(Arrays.asList(objects));
    }

    @Override
    public void parseMore(final TermParserContext context, final ActionState state) {
        final PhraseToken current = context.current();
        final TextPos pos = current.start();
        context.produce(new TermToken(Terms.SYNTAX_ERROR, SyntaxRole.UNKNOWN, null, null,
                pos, pos,
                getSource(),
                new ErrorInfo(errorId, objects,
                        new SourceLocation(pos, pos, context.parser().getSystemId()), null)));
        state.nextAction(getNext());
    }
}
