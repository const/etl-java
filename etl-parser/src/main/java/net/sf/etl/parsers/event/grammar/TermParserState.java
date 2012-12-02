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

package net.sf.etl.parsers.event.grammar;

import net.sf.etl.parsers.SourceLocation;

/**
 * The state for term parser. The state represent runtime state of the parser. The states are organized in the stack.
 */
public abstract class TermParserState {
    /**
     * The context for term parser
     */
    protected final TermParserContext context;
    /**
     * The previous state
     */
    private final TermParserState previous;
    /**
     * Current location in the source code
     */
    private SourceLocation location;

    /**
     * The constructor
     *
     * @param context  context
     * @param previous previous state on the stack
     */
    protected TermParserState(TermParserContext context, TermParserState previous) {
        this.context = context;
        this.previous = previous;
    }

    /**
     * @return the source location
     */
    public SourceLocation getLocation() {
        return location;
    }

    /**
     * Set current location
     *
     * @param location the location
     */
    protected void setLocation(SourceLocation location) {
        this.location = location;
    }

    /**
     * @return the previous state
     */
    public TermParserState getPreviousState() {
        return previous;
    }

    /**
     * Attempt the recovery
     * <p/>
     * TODO three states RECOVER, UNKNOWN, SKIP
     *
     * @return true if this state can recover basing on the current token.
     */
    public abstract boolean canRecover();

    /**
     * If this state has returned false from {@link #canRecover()}, and some method returned true from recover,
     * the context is forcibly finished. It is up to implementation of the state, what should be done in this case.
     * Usually, the context will close all open objects/properties and exit. After this call, invocation of
     * {@link #parseMore()} should not call {@link net.sf.etl.parsers.event.grammar.TermParserContext#consumePhraseToken()}
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
     * Call other state, so the next request will be handled by new state
     *
     * @param factory the factory that creates the state
     */
    protected void call(TermParserStateFactory factory) {
        context.call(factory);
    }

    /**
     * Exit for this state, so the previous state will be in control
     */
    protected void exit() {
        context.exit(this);
    }
}
