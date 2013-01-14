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

/**
 * The generic choice action that does an universal choice over current phrase token.
 */
public class ChoiceAction extends Action {
    // TODO play with different Map implementations to see which is actually faster
    /**
     * This is used to select an option based on phrase token kind except for the case of
     * {@link PhraseTokens#SIGNIFICANT}
     */
    public final EnumMap<PhraseTokens, Action> phrase = new EnumMap<PhraseTokens, Action>(PhraseTokens.class);
    /**
     * If token phrase is not matched, of phrase token is not matched, this alternative is chosen
     */
    public Action unmatchedPhrase;
    /**
     * The choice over keywords, these are tried next in the case of {@link PhraseTokens#SIGNIFICANT}.
     */
    public final IdentityHashMap<Keyword, Action> keywords = new IdentityHashMap<Keyword, Action>();
    /**
     * For non-keyword tokens, a token key based match is tried
     */
    public final HashMap<TokenKey, Action> tokens = new HashMap<TokenKey, Action>();
    /**
     * This alternative is chosen if the token does not match.
     */
    public Action unmatchedToken;

    /**
     * The action
     *
     * @param source the source location in the grammar that caused this node creation
     */
    public ChoiceAction(SourceLocation source) {
        super(source);
    }

    /**
     * Parse more elements
     *
     * @param context the context of the parser
     * @param state   the context state
     */
    @Override
    public void parseMore(TermParserContext context, ActionState state) {
        final PhraseToken current = context.current();
        if (current.kind() == PhraseTokens.SIGNIFICANT) {
            Keyword keyword = context.classify();
            final Action action = keyword != null ? keywords.get(keyword) : tokens.get(current.token().key());
            state.nextAction(action != null ? action : unmatchedToken);
        } else {
            final Action action = phrase.get(current.kind());
            state.nextAction(action != null ? action : unmatchedPhrase);
        }
    }
}
