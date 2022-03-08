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

import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.event.grammar.impl.ActionBuilder;
import net.sf.etl.parsers.event.impl.term.action.Action;
import net.sf.etl.parsers.event.impl.term.action.ReportBeforeMarkAction;
import net.sf.etl.parsers.event.impl.term.action.StructuralTokenAction;

/**
 * An simple term context scope node.
 *
 * @author const
 */
public final class TermContextScope extends CleanupScopeNode {
    /**
     * The start event for context.
     */
    private final Terms startEvent;
    /**
     * The end event for context.
     */
    private final Terms endEvent;
    /**
     * the context.
     */
    private final Object definition;
    /**
     * If mark mode.
     */
    private final MarkMode atMark;

    /**
     * A constructor.
     *
     * @param startEvent the start event for context
     * @param endEvent   the end event
     * @param definition the definition
     */
    public TermContextScope(final Terms startEvent, final Terms endEvent, final Object definition) {
        this(startEvent, endEvent, definition, false);
    }

    /**
     * A constructor.
     *
     * @param startEvent the start event for context
     * @param endEvent   the end event
     * @param definition the definition
     * @param atMark     if true, scope is started at the mark
     */
    public TermContextScope(final Terms startEvent, final Terms endEvent, final Object definition,
                            final boolean atMark) {
        this(startEvent, endEvent, definition, atMark ? MarkMode.AFTER_MARK : MarkMode.NORMAL);
    }

    /**
     * A constructor.
     *
     * @param startEvent the start event for context
     * @param endEvent   the end event
     * @param definition the definition
     * @param markMode   if true, scope is started with the specified mark mode
     */
    public TermContextScope(final Terms startEvent, final Terms endEvent, final Object definition,
                            final MarkMode markMode) {
        this.startEvent = startEvent;
        this.endEvent = endEvent;
        this.definition = definition;
        this.atMark = markMode;
    }

    @Override
    protected Action buildStartState(final ActionBuilder b, final Action bodyStates, final Action errorExit,
                                     final Action errorCloseState) {
        if (atMark == MarkMode.BEFORE_MARK) {
            return new ReportBeforeMarkAction(bodyStates, startEvent, definition, getSource());
        } else {
            return new StructuralTokenAction(getSource(), bodyStates, startEvent, definition,
                    atMark == MarkMode.AFTER_MARK);
        }
    }

    @Override
    protected Action buildEndState(final ActionBuilder b, final Action normalExit, final Action errorExit) {
        return new StructuralTokenAction(getSource(), normalExit, endEvent, definition, false);
    }

    /**
     * The mark mode.
     */
    public enum MarkMode {
        /**
         * Produce token at current location.
         */
        NORMAL,
        /**
         * Start after mark.
         */
        AFTER_MARK,
        /**
         * Start before mark.
         */
        BEFORE_MARK
    }
}
