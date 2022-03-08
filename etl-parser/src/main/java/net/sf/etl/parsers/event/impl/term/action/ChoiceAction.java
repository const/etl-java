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

package net.sf.etl.parsers.event.impl.term.action;

import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.PhraseTokens;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.TokenKey;
import net.sf.etl.parsers.event.grammar.Keyword;
import net.sf.etl.parsers.event.grammar.TermParserContext;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * The generic choice action that does an universal choice over current phrase token.
 */
public final class ChoiceAction extends Action {
    // TODO play with different Map implementations to see which is actually faster
    /**
     * This is used to select an option based on phrase token kind except for the case of
     * {@link PhraseTokens#SIGNIFICANT}.
     */
    private final EnumMap<PhraseTokens, Action> phrase = new EnumMap<>(PhraseTokens.class);
    /**
     * The choice over keywords, these are tried next in the case of {@link PhraseTokens#SIGNIFICANT}.
     */
    private final IdentityHashMap<Keyword, Action> keywords = new IdentityHashMap<>();
    /**
     * For non-keyword tokens, a token key based match is tried.
     */
    private final HashMap<TokenKey, Action> tokens = new HashMap<>(); // NOPMD
    /**
     * If token phrase is not matched, of phrase token is not matched, this alternative is chosen.
     */
    private Action unmatchedPhrase;
    /**
     * This alternative is chosen if the token does not match.
     */
    private Action unmatchedToken;

    /**
     * The action.
     *
     * @param source the source location in the grammar that caused this node creation
     */
    public ChoiceAction(final SourceLocation source) {
        super(source);
    }

    /**
     * Parse more elements.
     *
     * @param context the context of the parser
     * @param state   the context state
     */
    @Override
    public void parseMore(final TermParserContext context, final ActionState state) {
        final PhraseToken current = context.current();
        if (current.kind() == PhraseTokens.SIGNIFICANT) {
            final Keyword keyword = context.classify();
            final Action action = keyword != null ? keywords.get(keyword) : tokens.get(current.token().key());
            state.nextAction(action != null ? action : unmatchedToken);
        } else {
            final Action action = phrase.get(current.kind());
            state.nextAction(action != null ? action : unmatchedPhrase);
        }
    }

    /**
     * @return the map for phrase tokens.
     */
    public Map<PhraseTokens, Action> getPhrase() {
        return phrase;
    }

    /**
     * @return the map for keywords.
     */
    public Map<Keyword, Action> getKeywords() {
        return keywords;
    }

    /**
     * @return the map for token keys.
     */
    public Map<TokenKey, Action> getTokens() {
        return tokens;
    }

    /**
     * Set action on unmatched phrase token.
     *
     * @param unmatchedPhrase the action.
     */
    public void setUnmatchedPhrase(final Action unmatchedPhrase) {
        this.unmatchedPhrase = unmatchedPhrase;
    }

    /**
     * Set action on unmatched token.
     *
     * @param unmatchedToken the action
     */
    public void setUnmatchedToken(final Action unmatchedToken) {
        this.unmatchedToken = unmatchedToken;
    }
}
