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

package net.sf.etl.parsers.event.grammar.impl.nodes;

import net.sf.etl.parsers.PhraseTokens;
import net.sf.etl.parsers.event.grammar.LookAheadSet;
import net.sf.etl.parsers.event.grammar.impl.ActionBuilder;
import net.sf.etl.parsers.event.impl.term.action.Action;
import net.sf.etl.parsers.event.impl.term.action.KeywordChoiceAction;
import net.sf.etl.parsers.event.impl.term.action.PhraseTokenChoiceAction;
import net.sf.etl.parsers.event.impl.term.action.TokenKeyChoiceAction;

import java.util.ArrayList;

/**
 * The builder for the choice node
 */
public class ChoiceBuilder {
    // TODO add support for assumed options (it is used reduce choices) private final LookAheadSet assuming;
    /**
     * The fallback action
     */
    private Action fallback;
    /**
     * The choice options
     */
    private final ArrayList<ChoiceOption> options = new ArrayList<ChoiceOption>();

    /**
     * Set fallback node that is executed when no choice are available
     *
     * @param fallback the fallback node
     * @return this choice builder
     */
    public ChoiceBuilder setFallback(Action fallback) {
        this.fallback = fallback;
        return this;
    }

    /**
     * Add new alternative
     *
     * @param matches the matched tokens
     * @param action  the action
     * @return this choice builder
     */
    public ChoiceBuilder add(LookAheadSet matches, Action action) {
        options.add(new ChoiceOption(matches, action));
        return this;
    }

    /**
     * Build choice nodes
     *
     * @return the choice node
     */
    public Action build(ActionBuilder b) {
        PhraseTokenChoiceAction phraseChoice = new PhraseTokenChoiceAction();
        KeywordChoiceAction keywords = new KeywordChoiceAction();
        TokenKeyChoiceAction tokens = new TokenKeyChoiceAction();
        phraseChoice.next.put(PhraseTokens.SIGNIFICANT, keywords);
        LookAheadSet la = new LookAheadSet();
        // do sanity check
        Action emptyFallback = fallback;
        Action anyFallback = null;
        for (ChoiceOption option : options) {
            if (option.lookAhead.containsEmpty()) {
                emptyFallback = option.action;
            }
            if (option.lookAhead.containsAny()) {
                anyFallback = option.action;
            }
            final String test = la.conflictsWith(option.lookAhead);
            if (test != null) {
                // TODO report error here
                throw new IllegalStateException("Look ahead conflict: " + test);
            }
            la.addAll(option.lookAhead);
        }
        if (anyFallback == null) {
            anyFallback = emptyFallback;
        }
        phraseChoice.fallback = emptyFallback;
        keywords.otherKeyword = anyFallback;
        keywords.nonKeyword = tokens;
        tokens.fallback = anyFallback;
        // TODO optimize it later, it creates a lot of unneeded actions
        for (ChoiceOption option : options) {
            for (LookAheadSet.Entry entry : option.lookAhead.entries()) {
                if (entry instanceof LookAheadSet.KeywordEntry) {
                    keywords.next.put(((LookAheadSet.KeywordEntry) entry).keyword, option.action);
                } else if (entry instanceof LookAheadSet.TokenKeyEntry) {
                    tokens.next.put(((LookAheadSet.TokenKeyEntry) entry).key, option.action);
                } else if (entry instanceof LookAheadSet.PhraseEntry) {
                    phraseChoice.next.put(((LookAheadSet.PhraseEntry) entry).kind, option.action);
                } else if (entry instanceof LookAheadSet.EmptyEntry) {
                    // do nothing, it was processed as fallback
                } else if (entry instanceof LookAheadSet.AnyTokenEntry) {
                    // to nothing, it was processed as fallback
                }
            }
        }
        return phraseChoice;
    }


    /**
     * The choice options
     */
    private class ChoiceOption {
        /**
         * The look ahead set
         */
        final LookAheadSet lookAhead;
        /**
         * The action associated with set
         */
        final Action action;

        /**
         * The constructor
         *
         * @param lookAhead the look ahead set
         * @param action    the action associated with the set
         */
        private ChoiceOption(LookAheadSet lookAhead, Action action) {
            this.lookAhead = lookAhead;
            this.action = action;
        }
    }
}
