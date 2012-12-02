/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2012 Constantine A Plotnikov
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
 * The action state
 */
public class ActionState extends TermParserState {
    /**
     * The current action
     */
    private Action current;
    /**
     * The recovery state for the action state
     */
    private ActionStateRecovery recovery;


    /**
     * The constructor
     *
     * @param context  context
     * @param previous previous state on the stack
     * @param start    the start state
     */
    public ActionState(TermParserContext context, TermParserState previous, Action start) {
        super(context, previous);
        current = start;
    }

    @Override
    public boolean canRecover() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void forceFinish() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void startRecover() {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public void parseMore() {
        current.parseMore(context, this);
    }

    /**
     * Set next action to execute
     *
     * @param next the next action
     */
    public void nextAction(Action next) {
        current = next;
    }
}
