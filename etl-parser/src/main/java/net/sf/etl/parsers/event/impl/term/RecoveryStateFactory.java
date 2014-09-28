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

package net.sf.etl.parsers.event.impl.term;

import net.sf.etl.parsers.DefinitionContext;
import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.SyntaxRole;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.event.grammar.TermParserContext;
import net.sf.etl.parsers.event.grammar.TermParserState;
import net.sf.etl.parsers.event.grammar.TermParserStateFactory;

/**
 * The recovery state factory.
 */
public final class RecoveryStateFactory implements TermParserStateFactory {
    /**
     * The static instance for the factory.
     */
    public static final TermParserStateFactory INSTANCE = new RecoveryStateFactory();


    @Override
    public TermParserState start(final TermParserContext context, final TermParserState previous) {
        return new RecoveryParserState(context, previous);
    }

    /**
     * The parser state that is activated in the case of recovery.
     */
    private static class RecoveryParserState extends TermParserState {
        /**
         * Nesting level of block skipping.
         */
        private int blockSkip;

        /**
         * The constructor.
         *
         * @param context  context
         * @param previous previous state on the stack
         */
        protected RecoveryParserState(final TermParserContext context, final TermParserState previous) {
            super(context, previous);
        }

        @Override
        public RecoverableStatus canRecover() {
            throw new IllegalStateException("The method should be never called for this state");
        }

        @Override
        public void forceFinish() {
            throw new IllegalStateException("The method should be never called for this state");
        }

        @Override
        public void startRecover() {
            throw new IllegalStateException("The method should be never called for this state");
        }

        @Override
        public void parseMore() {
            final TermParserContext context = getContext();
            if (TermParserContextUtil.skipIgnorable(null, context, true)) {
                return;
            }
            if (blockSkip > 0) {
                skipToken();
                return;
            }
            for (final TermParserState state : previousStates()) {
                final RecoverableStatus recoverableStatus = state.canRecover();
                switch (recoverableStatus) {
                    case SKIP:
                        skipToken();
                        return;
                    case RECOVER:
                        for (final TermParserState forcedState : previousStates()) {
                            if (state == forcedState) { // NOPMD
                                break;
                            }
                            forcedState.forceFinish();
                        }
                        state.startRecover();
                        context.exit(this, state == getPreviousState());
                        return;
                    case UNKNOWN:
                        // continue loop
                        break;
                    default:
                        throw new IllegalStateException("Unknown recovery strategy");
                }
            }
        }

        /**
         * Skip token during recovery.
         */
        private void skipToken() {
            final TermParserContext context = getContext();
            final PhraseToken current = context.current();
            switch (current.kind()) {
                case START_BLOCK:
                    blockSkip++;
                    context.produce(new TermToken(Terms.BLOCK_START, SyntaxRole.CONTROL, DefinitionContext.UNKNOWN,
                            current, current.start(), current.end(), null));
                    context.consumePhraseToken();
                    return;
                case END_BLOCK:
                    blockSkip--;
                    context.produce(new TermToken(Terms.BLOCK_END, SyntaxRole.CONTROL, DefinitionContext.UNKNOWN,
                            current, current.start(), current.end(), null));
                    context.consumePhraseToken();
                    return;
                case STATEMENT_END:
                case SOFT_STATEMENT_END:
                    TermParserContextUtil.reportControl(context, current);
                    context.consumePhraseToken();
                    return;
                case SIGNIFICANT:
                    TermParserContextUtil.reportIgnorable(context, SyntaxRole.UNKNOWN, current);
                    context.consumePhraseToken();
                    return;
                default:
                    throw new IllegalStateException("The token should not be here: " + current);
            }
        }
    }

}
