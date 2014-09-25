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

package net.sf.etl.parsers.event.grammar;

import java.util.Iterator;

/**
 * The state for term parser. The state represent runtime state of the parser. The states are organized in the stack.
 */
public abstract class TermParserState {
    /**
     * The context for term parser.
     */
    private final TermParserContext context;
    /**
     * The previous state.
     */
    private final TermParserState previous;
    /**
     * The call status.
     */
    private CallStatus callStatus = CallStatus.NONE;

    /**
     * The constructor.
     *
     * @param context  context
     * @param previous previous state on the stack
     */
    protected TermParserState(final TermParserContext context, final TermParserState previous) {
        this.context = context;
        this.previous = previous;
    }

    /**
     * @return the previous state
     */
    public final TermParserState getPreviousState() {
        return previous;
    }

    /**
     * @return previous states for iterator
     */
    public final Iterable<TermParserState> previousStates() {
        return new Iterable<TermParserState>() {
            @Override
            public Iterator<TermParserState> iterator() {
                return new Iterator<TermParserState>() {
                    private TermParserState state = previous;

                    @Override
                    public boolean hasNext() {
                        return state != null;
                    }

                    @Override
                    public TermParserState next() {
                        final TermParserState rc = state;
                        state = state.getPreviousState();
                        return rc;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("The operation is not supported");
                    }
                };
            }
        };
    }

    /**
     * Attempt the recovery.
     *
     * @return true if this state can recover basing on the current token.
     */
    public abstract RecoverableStatus canRecover();

    /**
     * If this state has returned false from {@link #canRecover()}, and some method returned true from recover,
     * the context is forcibly finished. It is up to implementation of the state, what should be done in this case.
     * Usually, the context will close all open objects/properties and exit. After this call, invocation of
     * {@link #parseMore()} should not call
     * {@link net.sf.etl.parsers.event.grammar.TermParserContext#consumePhraseToken()}
     */
    public abstract void forceFinish();

    /**
     * This method is invoked when state could recover from error (the invocation of {@link #canRecover()}
     * returned true on the current phrase token. Recovery action depends on the current state of the state object.
     */
    public abstract void startRecover();

    /**
     * This method should invoke some action method on {@link net.sf.etl.parsers.event.grammar.TermParserContext}
     * and exit, thus ensuring parsing progress.
     */
    public abstract void parseMore();

    /**
     * Exit for this state, so the previous state will be in control.
     */
    protected final void exit() {
        context.exit(this, true);
    }

    /**
     * @return the context
     */
    public final TermParserContext getContext() {
        return context;
    }

    /**
     * Set call status from the call.
     *
     * @param callStatus the call status
     */
    public final void setCallStatus(final boolean callStatus) {
        setCallStatus(callStatus ? CallStatus.SUCCESS : CallStatus.FAILURE);
    }

    /**
     * Set the call status.
     *
     * @param callStatus the call status
     */
    private void setCallStatus(final CallStatus callStatus) {
        this.callStatus = callStatus;
    }

    /**
     * Get call status and set it none.
     *
     * @return the consumed call status
     */
    public final CallStatus consumeCallStatus() {
        final CallStatus status = callStatus;
        callStatus = CallStatus.NONE;
        return status;
    }

    /**
     * The call status.
     */
    public static enum CallStatus {
        /**
         * No was made.
         */
        NONE,
        /**
         * The call was successful.
         */
        SUCCESS,
        /**
         * The call failed.
         */
        FAILURE
    }

    /**
     * The recoverable status.
     */
    public static enum RecoverableStatus {
        /**
         * This state could not recover at the moment.
         */
        UNKNOWN,
        /**
         * This state can recover here.
         */
        RECOVER,
        /**
         * The current token or block should be skipped.
         */
        SKIP,
    }
}
