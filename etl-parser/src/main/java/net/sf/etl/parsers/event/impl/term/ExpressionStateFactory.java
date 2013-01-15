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

import net.sf.etl.parsers.event.grammar.KeywordContext;
import net.sf.etl.parsers.event.grammar.TermParserContext;
import net.sf.etl.parsers.event.grammar.TermParserState;
import net.sf.etl.parsers.event.grammar.TermParserStateFactory;

/**
 * The factory for expression context, it sets keywords and starts parsing expression in the range.
 * The factory is used to parse a specific text as an expressions. It does not support recovery
 * and it expects to be nested into {@link StatementSequenceStateFactory} (single statement variant)
 */
public class ExpressionStateFactory implements TermParserStateFactory {
    /**
     * The expression factory
     */
    private final TermParserStateFactory expressionFactory;
    /**
     * The keyword context
     */
    private final KeywordContext keywordContext;

    /**
     * The constructor
     *
     * @param expressionFactory the expression factory
     * @param keywordContext    the keyword context
     */
    public ExpressionStateFactory(TermParserStateFactory expressionFactory, KeywordContext keywordContext) {
        this.expressionFactory = expressionFactory;
        this.keywordContext = keywordContext;
    }

    @Override
    public TermParserState start(TermParserContext context, TermParserState previous) {
        return new TermParserState(context, previous) {
            /**
             * The initial state
             */
            private final static int STARTING = 0;
            /**
             * The after call state
             */
            private final static int FINISHING = 1;
            /**
             * The current state
             */
            private int state = STARTING;

            @Override
            public RecoverableStatus canRecover() {
                return RecoverableStatus.UNKNOWN;
            }

            @Override
            public void forceFinish() {
                // do nothing
            }

            @Override
            public void startRecover() {
                throw new UnsupportedOperationException("The method should be never called.");
            }

            @Override
            public void parseMore() {
                switch (state) {
                    case STARTING:
                        state = FINISHING;
                        context.pushKeywordContext(keywordContext);
                        context.call(expressionFactory);
                        return;
                    case FINISHING:
                        context.popKeywordContext(keywordContext);
                        context.exit(this, consumeCallStatus() == CallStatus.SUCCESS);
                        return;
                    default:
                        throw new IllegalStateException("Invalid state: " + state);
                }
            }
        };
    }
}
