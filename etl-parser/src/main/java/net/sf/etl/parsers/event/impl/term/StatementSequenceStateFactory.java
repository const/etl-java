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

package net.sf.etl.parsers.event.impl.term;

import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.SyntaxRole;
import net.sf.etl.parsers.event.grammar.TermParserContext;
import net.sf.etl.parsers.event.grammar.TermParserState;
import net.sf.etl.parsers.event.grammar.TermParserStateFactory;

/**
 * The state factory for statement sequence
 */
public class StatementSequenceStateFactory implements TermParserStateFactory {
    /**
     * The factory for the statement
     */
    private final TermParserStateFactory statementStateFactory;

    /**
     * The state factory
     *
     * @param statementStateFactory the statement state factory
     */
    public StatementSequenceStateFactory(TermParserStateFactory statementStateFactory) {
        this.statementStateFactory = statementStateFactory;
    }

    @Override
    public TermParserState start(TermParserContext context, TermParserState previous) {
        return new StatementSequenceState(context, previous, statementStateFactory);
    }

    /**
     * The statement sequence state
     */
    private static class StatementSequenceState extends TermParserState {
        /**
         * The state before statement
         */
        private static final int BEFORE_STATEMENT = 0;
        /**
         * The state (in recovery)
         */
        private static final int RECOVERING = 1;
        /**
         * Statement state factory
         */
        private final TermParserStateFactory statementStateFactory;
        /**
         * The current mode
         */
        private int mode = BEFORE_STATEMENT;

        /**
         * The constructor
         *
         * @param context               context
         * @param previous              previous state on the stack
         * @param statementStateFactory the constructor
         */
        public StatementSequenceState(TermParserContext context, TermParserState previous, TermParserStateFactory statementStateFactory) {
            super(context, previous);
            this.statementStateFactory = statementStateFactory;
        }


        @Override
        public boolean canRecover() {
            return true;
        }

        @Override
        public void forceFinish() {
            // do nothing, since it could always recover
        }

        @Override
        public void startRecover() {
            mode = RECOVERING;
        }

        @Override
        public void parseMore() {
            PhraseToken t = context.current();
            switch (t.kind()) {
                case STATEMENT_END:
                case SOFT_STATEMENT_END:
                    // soft statement end is treated as statement end, since it would have been ignored
                    // calling statement otherwise
                    mode = BEFORE_STATEMENT;
                    TermParserContextUtil.reportControl(context, t);
                    context.consumePhraseToken();
                    break;
                case IGNORABLE:
                    TermParserContextUtil.reportIgnorable(context, t);
                    context.consumePhraseToken();
                    break;
                case CONTROL:
                    TermParserContextUtil.reportControl(context, t);
                    context.consumePhraseToken();
                    break;
                case SIGNIFICANT:
                case START_BLOCK:
                    if (mode == RECOVERING) {
                        TermParserContextUtil.reportIgnorable(context, SyntaxRole.UNKNOWN, t);
                        context.consumePhraseToken();
                    } else {
                        context.call(statementStateFactory);
                    }
                    break;
                case END_BLOCK:
                case EOF:
                    context.exit(this);
                    break;
            }
        }
    }
}
