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
import net.sf.etl.parsers.PhraseTokens;
import net.sf.etl.parsers.SyntaxRole;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.event.grammar.BootstrapGrammars;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.event.grammar.TermParserContext;
import net.sf.etl.parsers.event.grammar.TermParserState;
import net.sf.etl.parsers.event.grammar.TermParserStateFactory;

/**
 * The source state factory.
 */
public final class SourceStateFactory implements TermParserStateFactory {
    /**
     * The factory instance.
     */
    public static final SourceStateFactory INSTANCE = new SourceStateFactory();
    /**
     * The factory for doctype statements.
     */
    private static final TermParserStateFactory DOCTYPE_STATE_FACTORY = makeDoctypeFactory();

    /**
     * Make single statement parser state factory for a document type instruction.
     *
     * @return the state factory
     */
    private static TermParserStateFactory makeDoctypeFactory() {
        final CompiledGrammar doctype = BootstrapGrammars.doctypeGrammar();
        return new StatementSequenceStateFactory(doctype.statementParser(doctype.getDefaultContext()), true);
    }

    @Override
    public TermParserState start(final TermParserContext context, final TermParserState previous) {
        return new SourceState(context, previous);
    }

    /**
     * The state that parses source code including doctype and content.
     */
    public static final class SourceState extends TermParserState {
        /**
         * The state when document type is not yet known.
         */
        private static final int BEFORE_DOCTYPE = 0;
        /**
         * The state after doctype, but before first statement.
         */
        private static final int BEFORE_STATEMENT = 1;
        /**
         * The state while parsing statements using statement sequence parser.
         */
        private static final int PARSING_STATEMENTS = 2;
        /**
         * The the current state.
         */
        private int state;

        /**
         * The constructor.
         *
         * @param context  context
         * @param previous previous state on the stack
         */
        protected SourceState(final TermParserContext context, final TermParserState previous) {
            super(context, previous);
            if (context.parser().grammar() != null) {
                state = BEFORE_STATEMENT;
            } else {
                state = BEFORE_DOCTYPE;
            }
        }

        @Override
        public RecoverableStatus canRecover() {
            throw new IllegalStateException("The recovery is not supported here.");
        }

        @Override
        public void forceFinish() {
            throw new IllegalStateException("The forcing finish should not happen for source level, "
                    + "statement sequence should take care of this.");
        }

        @Override
        public void startRecover() {
            throw new IllegalStateException("The recovering should not happen for source level, "
                    + "statement sequence should take care of this.");
        }

        @Override
        public void parseMore() {
            final TermParserContext context = getContext();
            if (TermParserContextUtil.skipIgnorable(null, context, false)) {
                return;
            }
            final PhraseToken current = context.current();
            switch (state) {
                case BEFORE_DOCTYPE:
                    if (current.hasToken() && current.token().text().equals("doctype")) {
                        context.call(DOCTYPE_STATE_FACTORY);
                        new DoctypeTokenListener((TermParserImpl) context.parser());
                        state = BEFORE_STATEMENT;
                        return;
                    } else {
                        ((TermParserImpl) context.parser()).setDoctype(null);
                        state = BEFORE_STATEMENT;
                        return;
                    }
                case BEFORE_STATEMENT:
                    CompiledGrammar grammar = context.parser().grammar();
                    if (grammar == null) {
                        throw new IllegalStateException("The grammar should be already defined here");
                    }
                    DefinitionContext definitionContext = context.parser().initialContext();
                    if (definitionContext == null) {
                        throw new IllegalStateException("The default context must be defined");
                    }
                    context.call(grammar.statementSequenceParser(definitionContext));
                    state = PARSING_STATEMENTS;
                    break;
                case PARSING_STATEMENTS:
                    if (current.kind() == PhraseTokens.EOF) {
                        context.produce(new TermToken(Terms.EOF, SyntaxRole.CONTROL, null, current,
                                current.start(), current.end(), null));
                        context.consumePhraseToken();
                        context.exit(this, true);
                        break;
                    } else {
                        throw new IllegalStateException("Invalid token for end of source: " + current);
                    }
                default:
                    throw new IllegalStateException("Invalid state: " + state);
            }
        }
    }
}
