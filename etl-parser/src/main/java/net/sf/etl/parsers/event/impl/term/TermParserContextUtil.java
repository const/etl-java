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

/**
 * The utilities related to term parser context
 */
public class TermParserContextUtil {
    /**
     * Report control token
     *
     * @param context the parser context
     * @param token   the reported token
     */
    public static void reportControl(TermParserContext context, PhraseToken token) {
        context.produce(new TermToken(Terms.CONTROL, SyntaxRole.CONTROL, null, token, token.start(), token.end(), null));
    }

    /**
     * Report ignorable token and auto-detect role
     *
     * @param context the context
     * @param token   the reported token
     */
    public static void reportIgnorable(TermParserContext context, PhraseToken token) {
        SyntaxRole role;
        switch (token.token().kind()) {
            case DOC_COMMENT:
            case BLOCK_COMMENT:
            case LINE_COMMENT:
                role = SyntaxRole.DOCUMENTATION;
                break;
            default:
                role = SyntaxRole.IGNORABLE;
        }
        reportIgnorable(context, role, token);
    }

    /**
     * Report ignorable token with specified role
     *
     * @param context the context
     * @param role    the role
     * @param token   the token to report
     */
    public static void reportIgnorable(TermParserContext context, SyntaxRole role, PhraseToken token) {
        context.produce(new TermToken(Terms.IGNORABLE, role, null, token, token.start(), token.end(), null));
    }

    /**
     * Skip ignorable token
     *
     * @param context           the context
     * @param skipDocumentation if true, the documentation tokens are skipped as well
     * @return true if current token was skipped and this method should be called again when
     *         next phrase token become available
     */
    public static boolean skipIgnorable(TermParserContext context, boolean skipDocumentation) {
        if (context.isAdvanceNeeded()) {
            PhraseToken t = context.current();
            switch (t.kind()) {
                case SOFT_STATEMENT_END:
                    if (!context.isScriptMode() || !context.canSoftEndStatement()) {
                        context.produce(new TermToken(Terms.IGNORABLE, SyntaxRole.IGNORABLE, null, t, t.start(), t.end(), null));
                        context.consumePhraseToken();
                        return true;
                    }
                    break;
                case IGNORABLE:
                    reportIgnorable(context, t);
                    context.consumePhraseToken();
                    return true;
                case CONTROL:
                    reportControl(context, t);
                    context.consumePhraseToken();
                    return true;
                case SIGNIFICANT:
                    if (skipDocumentation) {
                        if (t.hasToken() && t.token().kind() == Tokens.DOC_COMMENT) {
                            context.produce(new TermToken(Terms.IGNORABLE, SyntaxRole.DOCUMENTATION, null, t, t.start(), t.end(), null));
                            context.consumePhraseToken();
                            return true;
                        }
                    }
                    break;
            }
            context.advanced();
        }
        return false;
    }
}
