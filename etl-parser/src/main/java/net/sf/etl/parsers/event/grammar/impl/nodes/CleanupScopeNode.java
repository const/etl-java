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

import net.sf.etl.parsers.event.grammar.impl.ActionBuilder;
import net.sf.etl.parsers.event.impl.term.action.Action;

/**
 * This utility class is base class for scopes that must perform some cleanup
 * during exit.
 *
 * @author const
 */
public abstract class CleanupScopeNode extends ScopeNode {

    @Override
    public Action buildActions(ActionBuilder b, Action normalExit, Action errorExit, Action recoveryTest) {
        // This is a common implementation of scope node that needs cleanup that
        // works in the way to try/finally.
        // Build state that is invoked in the end of the scope in case of error
        final Action errorCloseState = buildEndState(b, errorExit, errorExit);
        // Build state that is invoked in the end of the scope in normal case
        final Action normalCloseState = buildEndState(b, normalExit, errorExit);
        // Build states for the body
        final Action bodyStates = innerNode().buildActions(b, normalCloseState, errorCloseState, recoveryTest);
        // finally build start state. Note that both exit points are provided.
        return buildStartState(b, bodyStates, errorExit, errorCloseState);
    }

    /**
     * Build scope open state
     *
     * @param b               the builder for state machine
     * @param bodyStates      the entry point of body states
     * @param errorExit       the exit point that should be used before clean up is required
     * @param errorCloseState the exit point that should be used after cleanup is required
     * @return the entry point into the state
     */
    protected abstract Action buildStartState(ActionBuilder b, Action bodyStates, Action errorExit,
                                              Action errorCloseState);

    /**
     * Build end state. This method create scope close elements. It is assumed
     * that closing is the same in case of error and normal exit. It is like
     * try/finally pattern in Java.
     *
     * @param b          the builder for state machine
     * @param normalExit the normal exit
     * @param errorExit  the error exit
     * @return the entry point into cleanup
     */
    protected abstract Action buildEndState(ActionBuilder b, Action normalExit, Action errorExit);
}
