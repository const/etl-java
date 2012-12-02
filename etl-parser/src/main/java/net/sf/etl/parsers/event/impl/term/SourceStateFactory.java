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

import net.sf.etl.parsers.*;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.event.grammar.TermParserContext;
import net.sf.etl.parsers.event.grammar.TermParserState;
import net.sf.etl.parsers.event.grammar.TermParserStateFactory;

/**
 * The source state factory
 */
public class SourceStateFactory implements TermParserStateFactory {
    /**
     * The factory instance
     */
    public final static SourceStateFactory INSTANCE = new SourceStateFactory();

    @Override
    public TermParserState start(TermParserContext context, TermParserState previous) {
        return new SourceState(context, previous);
    }

    /**
     * The state that parses source code including doctype and content
     */
    public static class SourceState extends TermParserState {
        private final static int BEFORE_STATEMENT = 0;
        private final static int PARSING_STATEMENTS = 1;
        private int state = BEFORE_STATEMENT;

        /**
         * The constructor
         *
         * @param context  context
         * @param previous previous state on the stack
         */
        protected SourceState(TermParserContext context, TermParserState previous) {
            super(context, previous);
        }

        @Override
        public boolean canRecover() {
            // recovery relies on statement sequence
            return false;
        }

        @Override
        public void forceFinish() {
            throw new IllegalStateException("The forcing finish should not happen for source level, " +
                    "statement sequence should take care of this.");
        }

        @Override
        public void startRecover() {
            throw new IllegalStateException("The recovering should not happen for source level, " +
                    "statement sequence should take care of this.");
        }

        @Override
        public void parseMore() {
            switch (state) {
                case BEFORE_STATEMENT:
                    CompiledGrammar grammar = context.parser().grammar();
                    if (grammar == null) {
                        throw new IllegalStateException("The statement could not be parsed");
                    }
                    DefinitionContext definitionContext = context.parser().initialContext();
                    if (definitionContext == null) {
                        throw new IllegalStateException("The default context must be defined");
                    }
                    context.call(grammar.statementSequenceParser(definitionContext));
                    state = PARSING_STATEMENTS;
                    break;
                case PARSING_STATEMENTS:
                    if (context.current().kind() == PhraseTokens.EOF) {
                        context.produce(new TermToken(Terms.EOF, SyntaxRole.CONTROL, null, context.current(), context.current().start(), context.current().end(), null));
                        context.consumePhraseToken();
                        context.exit(this);
                    } else {
                        throw new IllegalStateException("Invalid token for end of source: " + context.current());
                    }
            }
            // TODO check for doctype and default grammar and context
        }
    }
}
