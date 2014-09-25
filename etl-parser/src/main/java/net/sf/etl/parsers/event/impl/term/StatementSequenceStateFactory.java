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

import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.event.grammar.TermParserContext;
import net.sf.etl.parsers.event.grammar.TermParserState;
import net.sf.etl.parsers.event.grammar.TermParserStateFactory;

/**
 * The state factory for statement sequence.
 */
public final class StatementSequenceStateFactory implements TermParserStateFactory {
    /**
     * The factory for the statement.
     */
    private final TermParserStateFactory statementStateFactory;
    /**
     * The single statement mode.
     */
    private final boolean singleStatement;

    /**
     * The state factory.
     *
     * @param statementStateFactory the statement state factory
     */
    public StatementSequenceStateFactory(final TermParserStateFactory statementStateFactory) {
        this(statementStateFactory, false);
    }

    /**
     * The state factory.
     *
     * @param statementStateFactory the statement state factory
     * @param singleStatement       if true, the parser is in single statement mode
     */
    public StatementSequenceStateFactory(final TermParserStateFactory statementStateFactory,
                                         final boolean singleStatement) {
        this.statementStateFactory = statementStateFactory;
        this.singleStatement = singleStatement;
    }

    @Override
    public TermParserState start(final TermParserContext context, final TermParserState previous) {
        return new StatementSequenceState(context, previous, statementStateFactory, singleStatement);
    }

    /**
     * The statement sequence state.
     */
    private static class StatementSequenceState extends TermParserState {
        /**
         * Statement state factory.
         */
        private final TermParserStateFactory statementStateFactory;
        /**
         * If true, the factory is working in single statement mode.
         */
        private final boolean singleStatement;
        /**
         * If true, the factory just called statement factory and have not yet processed the result.
         */
        private boolean afterCall;

        /**
         * The constructor.
         *
         * @param context               the context
         * @param previous              the previous state on the stack
         * @param statementStateFactory the constructor
         * @param singleStatement       the single statement indicator
         */
        public StatementSequenceState(final TermParserContext context, final TermParserState previous,
                                      final TermParserStateFactory statementStateFactory,
                                      final boolean singleStatement) {
            super(context, previous);
            this.statementStateFactory = statementStateFactory;
            this.singleStatement = singleStatement;
        }


        @Override
        public RecoverableStatus canRecover() {
            PhraseToken t = getContext().current();
            switch (t.kind()) {
                case END_BLOCK:
                case EOF:
                case STATEMENT_END:
                case SOFT_STATEMENT_END:
                    return RecoverableStatus.RECOVER;
                default:
                    // always use skip recover strategy
                    return RecoverableStatus.SKIP;
            }
        }

        @Override
        public void forceFinish() {
            // do nothing, since it could always recover
        }

        @Override
        public void startRecover() {
            // nothing special hast to be done here
        }

        @Override
        public void parseMore() {
            final TermParserContext context = getContext();
            PhraseToken t = context.current();
            switch (t.kind()) {
                case STATEMENT_END:
                case SOFT_STATEMENT_END:
                    // soft statement end is treated as statement end, since it would have been ignored
                    // calling statement otherwise
                    afterCall = false;
                    TermParserContextUtil.reportControl(context, t);
                    context.consumePhraseToken();
                    if (singleStatement) {
                        exit();
                    }
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
                    if (afterCall) {
                        context.produce(new TermToken(Terms.SYNTAX_ERROR, null, null, null, t.start(), t.end(),
                                new ErrorInfo("syntax.UnexpectedToken.expectingEndOfSegment", new Object[]{t},
                                        t.start(), t.end(), context.parser().getSystemId())));
                        context.call(RecoveryStateFactory.INSTANCE);
                        afterCall = false;
                    } else {
                        context.call(statementStateFactory);
                        afterCall = true;
                    }
                    break;
                case END_BLOCK:
                case EOF:
                    exit();
                    break;
                default:
                    throw new IllegalStateException("Unknown token kind: " + t.kind());
            }
        }
    }
}
