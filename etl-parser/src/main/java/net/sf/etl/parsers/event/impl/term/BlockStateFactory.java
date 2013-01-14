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

import net.sf.etl.parsers.*;
import net.sf.etl.parsers.event.grammar.TermParserContext;
import net.sf.etl.parsers.event.grammar.TermParserState;
import net.sf.etl.parsers.event.grammar.TermParserStateFactory;

/**
 * Block state factory
 */
public class BlockStateFactory implements TermParserStateFactory {
    /**
     * The context for the block
     */
    private final DefinitionContext statementContext;
    /**
     * The statement sequence state factory
     */
    private final TermParserStateFactory statementSequenceFactory;

    /**
     * The constructor
     *
     * @param statementContext         the statement context information for the block
     * @param statementSequenceFactory the statement sequence factory for the block
     */
    public BlockStateFactory(DefinitionContext statementContext, TermParserStateFactory statementSequenceFactory) {
        this.statementContext = statementContext;
        this.statementSequenceFactory = statementSequenceFactory;
    }

    @Override
    public TermParserState start(TermParserContext context, TermParserState previous) {
        return new BlockState(context, previous, statementContext, statementSequenceFactory);
    }

    /**
     * The block state
     */
    private static class BlockState extends TermParserState {
        /**
         * Before block start is reported
         */
        private static final int BEFORE_BLOCK = 0;
        /**
         * Inside the block
         */
        private static final int IN_BLOCK = 1;
        /**
         * Inside the block
         */
        private static final int AFTER_BLOCK = 2;
        /**
         * The current mode of the state
         */
        private int mode = BEFORE_BLOCK;
        /**
         * The context for the block
         */
        private final DefinitionContext statementContext;
        /**
         * The factory for statement sequence
         */
        private final TermParserStateFactory statementSequenceFactory;

        /**
         * The constructor
         *
         * @param context  context
         * @param previous previous state on the stack
         */
        protected BlockState(TermParserContext context, TermParserState previous, DefinitionContext statementContext, TermParserStateFactory statementSequenceFactory) {
            super(context, previous);
            this.statementContext = statementContext;
            this.statementSequenceFactory = statementSequenceFactory;
        }

        @Override
        public RecoverableStatus canRecover() {
            throw new IllegalStateException("Recovery is not handled here: " + mode);
        }

        @Override
        public void forceFinish() {
            throw new IllegalStateException("Finishing block should be never forced, because statement sequence forces everything: " + mode);
        }

        @Override
        public void startRecover() {
            throw new IllegalStateException("Recovery is not handled here: " + mode);
        }

        @Override
        public void parseMore() {
            PhraseToken current = context.current();
            switch (mode) {
                case BEFORE_BLOCK:
                    if (current.kind() != PhraseTokens.START_BLOCK) {
                        throw new IllegalStateException("Invalid state for the call" + mode + " : " + current);
                    }
                    context.produce(new TermToken(Terms.BLOCK_START, null, statementContext, current, current.start(), current.end(), null));
                    context.startSoftEndContext();
                    context.consumePhraseToken();
                    context.call(statementSequenceFactory);
                    mode = IN_BLOCK;
                    break;
                case IN_BLOCK:
                    if (current.kind() != PhraseTokens.END_BLOCK) {
                        throw new IllegalStateException("Invalid state for the call" + mode + " : " + current);
                    }
                    final CallStatus callStatus = consumeCallStatus();
                    if (callStatus != CallStatus.SUCCESS) {
                        throw new IllegalStateException("Unexpected call status: " + callStatus);
                    }
                    context.endSoftEndContext();
                    context.produce(new TermToken(Terms.BLOCK_END, null, statementContext, current, current.start(), current.end(), null));
                    context.consumePhraseToken();
                    mode = AFTER_BLOCK;
                    break;
                case AFTER_BLOCK:
                    if (!TermParserContextUtil.skipIgnorable(null, context, true)) {
                        context.exit(this, true);
                    }
                    break;
                default:
                    throw new IllegalStateException("Invalid state for the call: " + mode);
            }
        }
    }
}
