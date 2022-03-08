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

/**
 * The vote action for the recovery.
 */
public final class RecoveryVoteAction extends Action {
    /**
     * The recovery vote action.
     */
    public static final RecoveryVoteAction NO_RECOVERY = new RecoveryVoteAction(null, null);

    /**
     * The recovery choice action.
     */
    private final RecoveryChoiceAction recoveryChoiceAction;

    /**
     * The action.
     *
     * @param source               the source location in the grammar that caused this node creation
     * @param recoveryChoiceAction the recovery choice action
     */
    public RecoveryVoteAction(final SourceLocation source, final RecoveryChoiceAction recoveryChoiceAction) {
        super(source);
        this.recoveryChoiceAction = recoveryChoiceAction;
    }

    @Override
    public void parseMore(final TermParserContext context, final ActionState state) {
        state.setRecoveryPoint(recoveryChoiceAction);
    }
}
