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
import net.sf.etl.parsers.SourceLocation;
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
public final class LookAheadSet {
    /**
     * Sample of empty entry.
     */
    public static final EmptyEntry EMPTY_ENTRY_SAMPLE = new EmptyEntry(null);
    /**
     * Sample any token entry.
     */
    public static final AnyTokenEntry ANY_TOKEN_ENTRY_SAMPLE = new AnyTokenEntry(null);
    /**
     * All tokens value.
     */
    private static final String ALL_TOKENS = null;
    /**
     * if true, the lookahead info is frozen.
     */
    private boolean isFrozen;
    /**
     * Phrase tokens.
     */
    private Set<Entry> entries = new LinkedHashSet<Entry>();

    /**
     * This constructor creates new lookahead info by copying data from previous
     * one.
     *
     * @param other other lookahead info
     */
    public LookAheadSet(final LookAheadSet other) {
        addAll(other);
    }

    /**
     * A constructor that starts with empty set.
     */
    public LookAheadSet() {
        // do nothing
    }

    /**
     * Get instance of lookahead info for phrase token.
     *
     * @param location the location
     * @param token    the phrase token for lookahead
     * @return lookahead information
     */
    public static LookAheadSet get(final SourceLocation location, final PhraseTokens token) {
        final LookAheadSet rc = new LookAheadSet();
        rc.add(location, token);
        return rc;
    }

    /**
     * Get LA-set only with empty value.
     *
     * @param location the cause location for this node
     * @return LookAheadInfo that contains only empty value
     */
    public static LookAheadSet getWithEmpty(final SourceLocation location) {
        final LookAheadSet rc = new LookAheadSet();
        rc.addEmpty(location);
        return rc;
    }

    /**
     * Create look ahead set with key.
     *
     * @param location the cause location for this entry
     * @param tokenKey the token kind
     * @return get look ahead info that matches token of the specified kind
     */
    public static LookAheadSet get(final SourceLocation location, final TokenKey tokenKey) {
        return getWithText(location, tokenKey, ALL_TOKENS);
    }

    /**
     * Create a node with single text.
     *
     * @param location the cause location
     * @param tokenKey the token kind
     * @param text     the text to match
     * @return get lookahead info that matches specific special
     */
    public static LookAheadSet getWithText(final SourceLocation location, final TokenKey tokenKey, final String text) {
        final LookAheadSet rc = new LookAheadSet();
        rc.addToken(tokenKey, text, location);
        return rc;
    }

    /**
     * Check if this lookahead info is still modifiable.
     */
    private void checkModifiable() {
        if (isFrozen) {
            throw new IllegalStateException(
                    "Look ahead information has been frozen");
        }
    }

    /**
     * Freeze the set.
     */
    public void freeze() {
        if (!isFrozen) {
            isFrozen = true;
            entries = Collections.unmodifiableSet(entries);
        }
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
     * Add empty sequence to lookahead.
     *
     * @param location the cause location for this entry
     */
    public void addEmpty(final SourceLocation location) {
        checkModifiable();
        entries.add(new EmptyEntry(location));
    }

    /**
     * Add token to match.
     *
     * @param tokenKey the token kind to match
     * @param text     the text to match
     * @param location the location that needs this token
     */
    private void addToken(final TokenKey tokenKey, final String text, final SourceLocation location) {
        checkModifiable();
        if (text != null) {
            entries.add(new KeywordEntry(location, Keyword.forText(text, tokenKey)));
        } else if (tokenKey != null) {
            entries.add(new TokenKeyEntry(location, tokenKey));
        } else {
            entries.add(new AnyTokenEntry(location));
        }
    }

    /**
     * Add all values from additional lookahead info.
     *
     * @param other other lookahead info
     */
    public void addAll(final LookAheadSet other) {
        checkModifiable();
        entries.addAll(other.entries);
    }

    /**
     * Check if two look ahead info are conflicts.
     *
     * @param other info object to compare with
     * @return a string that enumerates some found problems.
     */
    public String conflictsWith(final LookAheadSet other) {
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
     * Add phrase token.
     *
     * @param location the location that needs this token
     * @param token    the token to add
     */
    public void add(final SourceLocation location, final PhraseTokens token) {
        checkModifiable();
        entries.add(new PhraseEntry(location, token));
    }

    /**
     * Remove empty from set.
     *
     * @return this set
     */
    public LookAheadSet removeEmpty() {
        checkModifiable();
        entries.remove(EMPTY_ENTRY_SAMPLE);
        return this;
    }

    /**
     * Check if set contains phrase token.
     *
     * @param token a token to check
     * @return true if look ahead contains token
     */
    public boolean contains(final PhraseTokens token) {
        return entries.contains(new PhraseEntry(null, token));
    }

    /**
     * @return get all entries
     */
    public Set<Entry> entries() {
        freeze();
        return entries;
    }


    @Override
    public String toString() {
        // NOTE POST 0.2: this value is used in syntax error reporting. Make it
        // more friendly.
        return "LA" + entries;
    }

    /**
     * @return true if the set is empty
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * The entry in look ahead set.
     */
    public abstract static class Entry { // NOPMD
        /**
         * The source location.
         */
        private final SourceLocation location;

        /**
         * The source location.
         *
         * @param location the location
         */
        protected Entry(final SourceLocation location) {
            this.location = location;
        }
    }

    /**
     * The empty entry.
     */
    public static final class EmptyEntry extends Entry {
        /**
         * The hash code to use for all instances of this entry.
         */
        private static final int HASH = 2222;

        /**
         * The location that caused empty entry.
         *
         * @param location the location
         */
        public EmptyEntry(final SourceLocation location) {
            super(location);
        }

        @Override
        public boolean equals(final Object o) {
            //CHECKSTYLE:OFF
            if (this == o) {
                return true;
            }
            //noinspection RedundantIfStatement
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            return true;
            //CHECKSTYLE:ON
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
     * The entry that matches any token whether it is keyword or not.
     */
    public static final class AnyTokenEntry extends Entry {
        /**
         * The hash code to use for all instances of this entry.
         */
        private static final int HASH = 3333;

        /**
         * The constructor from location.
         *
         * @param location the location
         */
        public AnyTokenEntry(final SourceLocation location) {
            super(location);
        }

        @Override
        public boolean equals(final Object o) {
            //CHECKSTYLE:OFF
            if (this == o) {
                return true;
            }
            //noinspection RedundantIfStatement
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            return true;
            //CHECKSTYLE:ON
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
     * The entry that matches token that correspond to selected token key.
     */
    public static final class TokenKeyEntry extends Entry {
        /**
         * The token key.
         */
        private final TokenKey key;

        /**
         * The constructor.
         *
         * @param location the location
         * @param key      the token key
         */
        public TokenKeyEntry(final SourceLocation location, final TokenKey key) {
            super(location);
            if (key == null) {
                throw new IllegalArgumentException("Key cannot be null");
            }
            this.key = key;
        }

        /**
         * @return the token key
         */
        public TokenKey getKey() {
            return key;
        }

        @Override
        public boolean equals(final Object o) {
            //CHECKSTYLE:OFF
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final TokenKeyEntry that = (TokenKeyEntry) o;

            //noinspection RedundantIfStatement
            if (!key.equals(that.key)) {
                return false;
            }

            return true;
            //CHECKSTYLE:ON
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
                        sb.append(',');
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
     * Phrase token entry.
     */
    public static final class PhraseEntry extends Entry {
        /**
         * The token kind.
         */
        private final PhraseTokens kind;

        /**
         * The constructor.
         *
         * @param location the location that caused this entry to appear
         * @param kind     the token kind
         */
        public PhraseEntry(final SourceLocation location, final PhraseTokens kind) {
            super(location);
            if (kind == null) {
                throw new IllegalArgumentException("Kind must not be null");
            }
            this.kind = kind;
        }

        /**
         * @return the phrase kind
         */
        public PhraseTokens getKind() {
            return kind;
        }

        @Override
        public boolean equals(final Object o) {
            //CHECKSTYLE:OFF
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final PhraseEntry that = (PhraseEntry) o;

            //noinspection RedundantIfStatement
            if (kind != that.kind) {
                return false;
            }

            return true;
            //CHECKSTYLE:ON
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
     * Keyword entry.
     */
    public static final class KeywordEntry extends Entry {
        /**
         * The keyword element.
         */
        private final Keyword keyword;

        /**
         * The constructor.
         *
         * @param location the location that caused this entry to appear
         * @param keyword  the keyword
         */
        public KeywordEntry(final SourceLocation location, final Keyword keyword) {
            super(location);
            if (keyword == null) {
                throw new IllegalArgumentException("Keyword must not be null");
            }
            this.keyword = keyword;
        }

        /**
         * @return the keyword
         */
        public Keyword getKeyword() {
            return keyword;
        }

        @Override
        public boolean equals(final Object o) {
            //CHECKSTYLE:OFF
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final KeywordEntry that = (KeywordEntry) o;

            //noinspection RedundantIfStatement
            if (!keyword.equals(that.keyword)) {
                return false;
            }

            return true;
            //CHECKSTYLE:ON
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
