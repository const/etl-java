/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2022 Konstantin Plotnikov
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
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.SyntaxRole;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.event.grammar.TermParserContext;

/**
 * The utilities related to term parser context.
 */
public final class TermParserContextUtil {
    /**
     * Private constructor for utility class.
     */
    private TermParserContextUtil() {
    }

    /**
     * Report control token.
     *
     * @param context the parser context
     * @param token   the reported token
     */
    public static void reportControl(final TermParserContext context, final PhraseToken token) {
        context.produce(new TermToken(Terms.CONTROL, SyntaxRole.CONTROL,
                null, token, token.start(), token.end(), null));
    }

    /**
     * Report ignorable token and auto-detect role.
     *
     * @param context the context
     * @param token   the reported token
     */
    public static void reportIgnorable(final TermParserContext context, final PhraseToken token) {
        SyntaxRole role;
        switch (token.token().kind()) {
            case DOC_COMMENT:
            case BLOCK_COMMENT:
            case LINE_COMMENT:
                role = SyntaxRole.DOCUMENTATION;
                break;
            default:
                role = SyntaxRole.IGNORABLE;
                break;
        }
        reportIgnorable(context, role, token);
    }

    /**
     * Report ignorable token with specified role.
     *
     * @param context the context
     * @param role    the role
     * @param token   the token to report
     */
    public static void reportIgnorable(final TermParserContext context, final SyntaxRole role,
                                       final PhraseToken token) {
        context.produce(new TermToken(Terms.IGNORABLE, role, null, token, token.start(), token.end(), null));
    }

    /**
     * Skip ignorable token.
     *
     * @param source            the source location
     * @param context           the context
     * @param skipDocumentation if true, the documentation tokens are skipped as well
     * @return true if current token was skipped and this method should be called again when
     * next phrase token become available
     */
    public static boolean skipIgnorable(final SourceLocation source, final TermParserContext context,
                                        final boolean skipDocumentation) {
        if (context.isAdvanceNeeded()) {
            final PhraseToken t = context.current();
            switch (t.kind()) {
                case SOFT_STATEMENT_END:
                    if (!context.isScriptMode() || !context.canSoftEndStatement()) {
                        context.produce(new TermToken(Terms.IGNORABLE, SyntaxRole.IGNORABLE, null, t,
                                t.start(), t.end(), source, null));
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
                    if (skipDocumentation && t.hasToken() && t.token().kind() == Tokens.DOC_COMMENT) {
                        context.produce(new TermToken(Terms.IGNORABLE, SyntaxRole.DOCUMENTATION, null, t,
                                t.start(), t.end(), source, null));
                        context.consumePhraseToken();
                        return true;
                    }
                    break;
                default:
                    break;
            }
            context.advanced();
        }
        return false;
    }
}
