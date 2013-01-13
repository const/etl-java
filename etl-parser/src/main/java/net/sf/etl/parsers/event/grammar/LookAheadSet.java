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
package net.sf.etl.parsers.event.grammar;

import net.sf.etl.parsers.PhraseTokens;
import net.sf.etl.parsers.TokenKey;
import net.sf.etl.parsers.characters.QuoteClass;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class encapsulate management of look ahead information. Each state factory
 * in the grammar has look-ahead declaration.
 * <p/>
 * The class is not thread safe.
 *
 * @author const
 */
public class LookAheadSet {
    /**
     * All tokens value
     */
    private static final String ALL_TOKENS = null;
    /**
     * Sample of empty entry
     */
    public static final EmptyEntry EMPTY_ENTRY_SAMPLE = new EmptyEntry();
    /**
     * Sample any token entry
     */
    public static final AnyTokenEntry ANY_TOKEN_ENTRY_SAMPLE = new AnyTokenEntry();
    /**
     * if true, the lookahead info is frozen
     */
    private boolean isFrozen;
    /**
     * Phrase tokens
     */
    private Set<Entry> entries = new LinkedHashSet<Entry>();

    /**
     * This constructor creates new lookahead info by copying data from previous
     * one
     *
     * @param other other lookahead info
     */
    public LookAheadSet(LookAheadSet other) {
        addAll(other);
    }

    /**
     * A constructor that starts with empty set.
     */
    public LookAheadSet() {
    }

    /**
     * Check if this lookahead info is still modifiable
     */
    private void checkModifiable() {
        if (isFrozen) {
            throw new IllegalStateException(
                    "Look ahead information has been frozen");
        }
    }

    /**
     * Freeze the set
     */
    public void freeze() {
        if (!isFrozen) {
            isFrozen = true;
            entries = Collections.unmodifiableSet(entries);
        }
    }

    /**
     * Get instance of lookahead info for phrase token
     *
     * @param token a phrase token for lookahead
     * @return lookahead information
     */
    public static LookAheadSet get(PhraseTokens token) {
        final LookAheadSet rc = new LookAheadSet();
        rc.add(token);
        return rc;
    }

    /**
     * @return true if set contains empty
     */
    public boolean containsEmpty() {
        return entries.contains(EMPTY_ENTRY_SAMPLE);
    }


    /**
     * @return true if set contains any token
     */
    public boolean containsAny() {
        return entries.contains(ANY_TOKEN_ENTRY_SAMPLE);
    }

    /**
     * Add empty sequence to lookahead
     */
    public void addEmpty() {
        checkModifiable();
        entries.add(EMPTY_ENTRY_SAMPLE);
    }

    /**
     * @return LookAheadInfo that contains only empty value
     */
    public static LookAheadSet getWithEmpty() {
        final LookAheadSet rc = new LookAheadSet();
        rc.addEmpty();
        return rc;
    }

    /**
     * Create look ahead set with key
     *
     * @param tokenKey the token kind
     * @return get look ahead info that matches token of the specified kind
     */
    public static LookAheadSet get(TokenKey tokenKey) {
        return getWithText(tokenKey, ALL_TOKENS);
    }

    /**
     * @param tokenKey token kind
     * @param text     a text to match
     * @return get lookahead info that matches specific special
     */
    public static LookAheadSet getWithText(TokenKey tokenKey, String text) {
        final LookAheadSet rc = new LookAheadSet();
        rc.addToken(tokenKey, text);
        return rc;
    }

    /**
     * Add token to match
     *
     * @param tokenKey a token kind to match
     * @param text     a text to match
     */
    private void addToken(TokenKey tokenKey, String text) {
        checkModifiable();
        if (text != null) {
            entries.add(new KeywordEntry(Keyword.forText(text, tokenKey)));
        } else if (tokenKey != null) {
            entries.add(new TokenKeyEntry(tokenKey));
        } else {
            entries.add(new AnyTokenEntry());
        }
    }

    /**
     * Add all values from additional lookahead info
     *
     * @param other other lookahead info
     */
    public void addAll(LookAheadSet other) {
        checkModifiable();
        entries.addAll(other.entries);
    }

    /**
     * Check if two look ahead info are conflicts.
     *
     * @param other info object to compare with
     * @return a string that enumerates some found problems.
     */
    public String conflictsWith(LookAheadSet other) {
        // Compare phrase tokens. There should be no intersections.
        final HashSet<Entry> phraseTokensTmp = new HashSet<Entry>(this.entries);
        phraseTokensTmp.retainAll(other.entries);
        if (phraseTokensTmp.isEmpty()) {
            return null;
        } else {
            return phraseTokensTmp.toString();
        }
    }


    /**
     * Add phrase token
     *
     * @param token a token to add
     */
    public void add(PhraseTokens token) {
        checkModifiable();
        entries.add(new PhraseEntry(token));
    }

    /**
     * Remove empty from set
     */
    public void removeEmpty() {
        checkModifiable();
        entries.remove(EMPTY_ENTRY_SAMPLE);
    }

    /**
     * Check if set contains phrase token
     *
     * @param token a token to check
     * @return true if look ahead contains token
     */
    public boolean contains(PhraseTokens token) {
        return entries.contains(new PhraseEntry(token));
    }

    /**
     * @return get all entries
     */
    public Set<Entry> entries() {
        freeze();
        return entries;
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // NOTE POST 0.2: this value is used in syntax error reporting. Make it
        // more friendly.
        return "LA" + entries;
    }

    /**
     * The entry in look ahead set
     */
    public abstract static class Entry {
        // TODO private final SourceLocation sourceLocation; // in order to report errors better

    }

    /**
     * The empty entry
     */
    public static final class EmptyEntry extends Entry {
        /**
         * The hash code to use for all instances of this entry
         */
        private static final int HASH = 2222;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            //noinspection RedundantIfStatement
            if (o == null || getClass() != o.getClass()) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return HASH;
        }

        @Override
        public String toString() {
            return "*Empty*";
        }
    }

    /**
     * The entry that matches any token whether it is keyword or not
     */
    public static final class AnyTokenEntry extends Entry {
        /**
         * The hash code to use for all instances of this entry
         */
        private static final int HASH = 3333;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            //noinspection RedundantIfStatement
            if (o == null || getClass() != o.getClass()) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return HASH;
        }

        @Override
        public String toString() {
            return "*Any*";
        }
    }

    /**
     * The entry that matches token that correspond to selected token key
     */
    public static final class TokenKeyEntry extends Entry {
        /**
         * The token key
         */
        public final TokenKey key;

        /**
         * The constructor
         *
         * @param key the token key
         */
        public TokenKeyEntry(TokenKey key) {
            if (key == null) {
                throw new IllegalArgumentException("Key cannot be null");
            }
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TokenKeyEntry that = (TokenKeyEntry) o;

            //noinspection RedundantIfStatement
            if (!key.equals(that.key)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(key.kind());
            final String modifier = key.modifier();
            final QuoteClass quoteClass = key.quoteClass();
            if (modifier != null || quoteClass != null) {
                sb.append('[');
                if (modifier != null) {
                    sb.append(modifier);
                }
                if (quoteClass != null) {
                    if (modifier != null) {
                        sb.append(",");
                    }
                    sb.append("\'\\");
                    sb.appendCodePoint(quoteClass.sample());
                    sb.append('\'');
                }
                sb.append(']');
            }
            return sb.toString();
        }
    }

    /**
     * Phrase token entry
     */
    public static final class PhraseEntry extends Entry {
        /**
         * The token kind
         */
        public final PhraseTokens kind;

        /**
         * The constructor
         *
         * @param kind the token kind
         */
        public PhraseEntry(PhraseTokens kind) {
            if (kind == null) {
                throw new IllegalArgumentException("Kind must not be null");
            }
            this.kind = kind;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PhraseEntry that = (PhraseEntry) o;

            //noinspection RedundantIfStatement
            if (kind != that.kind) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return kind.hashCode();
        }

        @Override
        public String toString() {
            return kind.toString();
        }
    }

    /**
     * Keyword entry
     */
    public static final class KeywordEntry extends Entry {
        /**
         * The keyword element
         */
        public final Keyword keyword;

        /**
         * The constructor
         *
         * @param keyword the keyword
         */
        public KeywordEntry(Keyword keyword) {
            if (keyword == null) {
                throw new IllegalArgumentException("Keyword must not be null");
            }
            this.keyword = keyword;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            KeywordEntry that = (KeywordEntry) o;

            //noinspection RedundantIfStatement
            if (!keyword.equals(that.keyword)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return keyword.hashCode();
        }

        @Override
        public String toString() {
            return keyword.toString();
        }
    }
}
