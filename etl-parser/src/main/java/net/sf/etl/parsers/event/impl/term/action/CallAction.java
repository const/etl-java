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
import net.sf.etl.parsers.event.grammar.TermParserState;
import net.sf.etl.parsers.event.grammar.TermParserStateFactory;

/**
 * The call action.
 */
public final class CallAction extends Action {
    /**
     * The point where to go on success.
     */
    private Action success;
    /**
     * The point where to go on the failure of the call.
     */
    private Action failure;
    /**
     * The state factory to call. This state factory is usually set using
     * {@link net.sf.etl.parsers.event.impl.term.action.buildtime.ActionLinker}
     * rather than directly during construction.
     */
    private TermParserStateFactory stateFactory;

    /**
     * The constructor.
     *
     * @param source the source location in the grammar that caused this node creation
     */
    public CallAction(final SourceLocation source) {
        super(source);
    }

    @Override
    public void parseMore(final TermParserContext context, final ActionState state) {
        final TermParserState.CallStatus status = state.consumeCallStatus();
        switch (status) {
            case NONE:
                context.call(stateFactory);
                break;
            case SUCCESS:
                state.nextAction(success);
                break;
            case FAILURE:
                state.nextAction(failure);
                break;
            default:
                throw new IllegalStateException("Unexpected call status: " + status);
        }
    }

    /**
     * Set success path.
     *
     * @param success the success action.
     */
    public void setSuccess(final Action success) {
        this.success = success;
    }

    /**
     * Set failure path.
     *
     * @param failure the failure action
     */
    public void setFailure(final Action failure) {
        this.failure = failure;
    }

    /**
     * Set state factory.
     *
     * @param stateFactory the state factory
     */
    public void setStateFactory(final TermParserStateFactory stateFactory) {
        this.stateFactory = stateFactory;
    }
}
