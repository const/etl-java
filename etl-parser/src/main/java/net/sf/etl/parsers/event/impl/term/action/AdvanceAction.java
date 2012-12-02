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

package net.sf.etl.parsers.event.impl.term.action;

import net.sf.etl.parsers.*;
import net.sf.etl.parsers.event.grammar.TermParserContext;
import net.sf.etl.parsers.event.impl.term.TermParserContextUtil;

/**
 * The action that advances to the next significant token skipping whitespaces and comments
 */
public class AdvanceAction extends SimpleAction {
    /**
     * If true, doc comments are skipped
     */
    boolean skipDocumentation;

    public AdvanceAction(boolean skipDocumentation) {
        this.skipDocumentation = skipDocumentation;
    }

    public AdvanceAction() {
        this(true);
    }

    @Override
    public void parseMore(TermParserContext context, ActionState state) {
        if (context.isAdvanceNeeded()) {
            PhraseToken t = context.current();
            switch (t.kind()) {
                case SOFT_STATEMENT_END:
                    if (!context.isScriptMode() || !context.canSoftEndStatement()) {
                        context.produce(new TermToken(Terms.IGNORABLE, SyntaxRole.IGNORABLE, null, t, t.start(), t.end(), null));
                        context.consumePhraseToken();
                        return;
                    }
                    break;
                case IGNORABLE:
                    TermParserContextUtil.reportIgnorable(context, t);
                    context.consumePhraseToken();
                    return;
                case CONTROL:
                    TermParserContextUtil.reportControl(context, t);
                    context.consumePhraseToken();
                    return;
                case SIGNIFICANT:
                    if (skipDocumentation) {
                        if (t.hasToken() && t.token().kind() == Tokens.DOC_COMMENT) {
                            context.produce(new TermToken(Terms.IGNORABLE, SyntaxRole.DOCUMENTATION, null, t, t.start(), t.end(), null));
                            context.consumePhraseToken();
                            return;
                        }
                    }
                    break;
            }
        }
        context.advanced();
        state.nextAction(next);
    }

}
