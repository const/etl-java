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

import net.sf.etl.parsers.event.grammar.TermParserContext;
import net.sf.etl.parsers.event.grammar.TermParserState;

/**
 * The action state.
 */
public final class ActionState extends TermParserState {
    /**
     * The current action.
     */
    private Action current;
    /**
     * The recovery state for the action state.
     */
    private Action recoveryTest;
    /**
     * The recovery choice action.
     */
    private RecoveryChoiceAction recoveryChoiceAction;

    /**
     * The constructor.
     *
     * @param context  the context
     * @param previous the previous state on the stack
     * @param start    the start state
     */
    public ActionState(final TermParserContext context, final TermParserState previous, final Action start) {
        super(context, previous);
        current = start;
    }

    @Override
    public RecoverableStatus canRecover() {
        recoveryChoiceAction = null;
        Action suspended = current;
        current = recoveryTest;
        final TermParserContext context = getContext();
        while (current != null) {
            current.parseMore(context, this);
        }
        current = suspended;
        return recoveryChoiceAction == null ? RecoverableStatus.UNKNOWN : RecoverableStatus.RECOVER;
    }


    @Override
    public void forceFinish() {
        // do nothing
    }

    @Override
    public void startRecover() {
        // do nothing
    }


    @Override
    public void parseMore() {
        current.parseMore(getContext(), this);
    }

    /**
     * Set next action to execute.
     *
     * @param next the next action
     */
    public void nextAction(final Action next) {
        current = next;
    }

    /**
     * Check if the recovery choice point is set to the specified action.
     *
     * @param action the action
     * @return check if the current point is a recovery point
     */
    boolean isRecoveryPoint(final RecoveryChoiceAction action) {
        return this.recoveryChoiceAction == action;
    }

    /**
     * Set recovery point, it aborts recovery loop.
     *
     * @param action the recovery choice point
     */
    void setRecoveryPoint(final RecoveryChoiceAction action) {
        this.recoveryChoiceAction = action;
        current = null;
    }

    /**
     * The action.
     *
     * @param action the recovery test action (might be null, if no recovery possible)
     */
    void setRecoveryTest(final Action action) {
        recoveryTest = action;
    }
}
