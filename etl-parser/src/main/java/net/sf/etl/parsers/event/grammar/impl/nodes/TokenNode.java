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
package net.sf.etl.parsers.event.grammar.impl.nodes;

import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.SyntaxRole;
import net.sf.etl.parsers.Terms;
import net.sf.etl.parsers.TokenKey;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.event.grammar.Keyword;
import net.sf.etl.parsers.event.grammar.LookAheadSet;
import net.sf.etl.parsers.event.grammar.impl.ActionBuilder;
import net.sf.etl.parsers.event.impl.term.action.Action;
import net.sf.etl.parsers.event.impl.term.action.AdvanceAction;
import net.sf.etl.parsers.event.impl.term.action.RecoverySetupAction;
import net.sf.etl.parsers.event.impl.term.action.ReportTokenAction;

import java.util.Collections;
import java.util.Set;

/**
 * Token reporting node.
 *
 * @author const
 */
public final class TokenNode extends Node {
    /**
     * The kind of token.
     */
    private final Terms termKind;
    /**
     * The lexical kind of token.
     */
    private final TokenKey tokenKey;
    /**
     * The role of token.
     */
    private final SyntaxRole role;
    /**
     * The text.
     */
    private final String text;

    /**
     * A constructor from fields.
     *
     * @param role     the token role
     * @param termKind the term kind for the token
     * @param text     the text of token
     * @param tokenKey the token kind
     */
    public TokenNode(final Terms termKind, final SyntaxRole role, final TokenKey tokenKey,
                     final String text) {
        super();
        this.role = role;
        this.termKind = termKind;
        this.text = text;
        this.tokenKey = tokenKey;
    }

    @Override
    public void collectKeywords(final Set<Keyword> keywords, final Set<ActionBuilder> visited) {
        if (text != null) {
            // TODO allow non-keyword fixed values
            keywords.add(Keyword.forText(text, tokenKey));
        }
    }

    @Override
    public Action buildActions(final ActionBuilder b, final Action normalExit, final Action errorExit,
                               final Action recoveryTest) {
        // Note that building states for the node usually done starting from
        // the last state because next state must be specified.
        final SourceLocation source = getSource();
        Action last = normalExit;
        final String errorId;
        final String arg;
        if (text != null) {
            errorId = "syntax.UnexpectedToken.expectingText";
            arg = text;
        } else {
            errorId = "syntax.UnexpectedToken.expectingKind";
            arg = tokenKey == null ? "*" : tokenKey.toString();
        }
        // skip ignorable tokens after this token. Note that when token is
        // not a doc comment, doc comments are treated as ignorable tokens.
        last = new AdvanceAction(source, last, tokenKey == null || tokenKey.kind() != Tokens.DOC_COMMENT);
        last = new RecoverySetupAction(source, last, recoveryTest);
        // report token
        last = new ReportTokenAction(source, last, termKind, role);
        return new ChoiceBuilder(source).
                setFallback(ActionUtil.createReportErrorAction(source, errorExit, errorId, arg)).
                add(buildLookAhead(Collections.<ActionBuilder>emptySet()), last).
                build();
    }

    @Override
    protected boolean calcMatchesEmpty() {
        return false;
    }

    @Override
    protected LookAheadSet createLookAhead(final Set<ActionBuilder> visitedBuilders) {
        if (text == null) {
            return LookAheadSet.get(getSource(), tokenKey);
        } else {
            return LookAheadSet.getWithText(getSource(), tokenKey, text);
        }
    }
}
