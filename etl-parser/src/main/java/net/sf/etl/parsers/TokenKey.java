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
package net.sf.etl.parsers;

import net.sf.etl.parsers.characters.QuoteClass;

import java.io.Serializable;
import java.util.EnumMap;

/**
 * The key that specifies the extended token kind. If token has additional properties like modifiers and quotes,
 * they are also included into token key.
 *
 * @author const
 */
public final class TokenKey implements Serializable {
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = -882146831401926996L;
    /**
     * Map for tokens without modifiers.
     */
    private static final EnumMap<Tokens, TokenKey> KIND_MAP;
    /**
     * The map for the strings without prefix.
     */
    private static final EnumMap<QuoteClass, TokenKey> STRING_MAP;
    /**
     * The map for multiline strings without prefix.
     */
    private static final EnumMap<QuoteClass, TokenKey> MULTILINE_STRING_MAP;
    /**
     * Pre-calculated hash code, the token key is used in hash maps extensively, so hash code is pre-calculated
     * to speed up lookup and equality comparison.
     */
    private final int hashCode;
    /**
     * The token kind.
     */
    private final Tokens kind;
    /**
     * The modifier (like suffix for numbers or prefix for strings).
     */
    private final String modifier;
    /**
     * The quote class for strings.
     */
    private final QuoteClass quoteClass;

    static {
        final EnumMap<Tokens, TokenKey> tokenKindMap = new EnumMap<Tokens, TokenKey>(Tokens.class);
        for (final Tokens k : Tokens.values()) {
            tokenKindMap.put(k, new TokenKey(k, null, null)); // NOPMD
        }
        KIND_MAP = tokenKindMap;
        final EnumMap<QuoteClass, TokenKey> stringEnumMap = new EnumMap<QuoteClass, TokenKey>(QuoteClass.class);
        for (final QuoteClass quoteClass : QuoteClass.values()) {
            stringEnumMap.put(quoteClass, new TokenKey(Tokens.STRING, null, quoteClass)); // NOPMD
        }
        STRING_MAP = stringEnumMap;
        final EnumMap<QuoteClass, TokenKey> multiLineStringEnumMap
                = new EnumMap<QuoteClass, TokenKey>(QuoteClass.class);
        for (final QuoteClass quoteClass : QuoteClass.values()) {
            multiLineStringEnumMap.put(quoteClass, new TokenKey(Tokens.MULTILINE_STRING, null, quoteClass)); // NOPMD
        }
        MULTILINE_STRING_MAP = multiLineStringEnumMap;
    }

    /**
     * The private constructor from fields.
     *
     * @param kind       the token kind
     * @param modifier   the token modifier
     * @param quoteClass the quote class
     */
    private TokenKey(final Tokens kind, final String modifier, final QuoteClass quoteClass) {
        this.kind = kind;
        this.modifier = modifier;
        this.quoteClass = quoteClass;
        hashCode = calculateHashCode();
    }

    /**
     * Get string token key.
     *
     * @param kind   the token kind
     * @param prefix the string prefix (or null for not prefixed string)
     * @param start  the start quote
     * @param end    the end quote
     * @return the token key with specified parameters
     */
    public static TokenKey quoted(final Tokens kind, final String prefix, final int start,
                                  final int end) {
        if (start == -1 || end == -1) {
            throw new IllegalArgumentException(
                    "Start and end quoted should be defined: " + start + " : "
                            + end);
        }
        return quoted(kind, prefix, QuoteClass.classify(start));
    }

    /**
     * Get string token key.
     *
     * @param kind       the token kind
     * @param prefix     the string prefix (or null for not prefixed string)
     * @param quoteClass the quote class
     * @return the token key with specified parameters
     */
    public static TokenKey quoted(final Tokens kind, final String prefix, final QuoteClass quoteClass) {
        switch (kind) {
            case PREFIXED_STRING:
            case PREFIXED_MULTILINE_STRING:
                if (prefix == null) {
                    throw new IllegalArgumentException(
                            "There must be prefix for prefixed string: " + kind);
                }
                break;
            case STRING:
                if (prefix != null) {
                    throw new IllegalArgumentException(
                            "There must not be prefix for unprefixed string: "
                                    + kind);
                }
                return STRING_MAP.get(quoteClass);
            case MULTILINE_STRING:
                if (prefix != null) {
                    throw new IllegalArgumentException(
                            "There must not be prefix for unprefixed string: "
                                    + kind);
                }
                return MULTILINE_STRING_MAP.get(quoteClass);
            default:
                throw new IllegalArgumentException("Not a string token: " + kind);
        }
        return new TokenKey(kind, prefix, quoteClass);
    }

    /**
     * Get number token key.
     *
     * @param kind   the token kind
     * @param suffix the number suffix (or null if there is no suffix)
     * @return the token key with specified parameters
     */
    public static TokenKey modified(final Tokens kind, final String suffix) {
        switch (kind) {
            case INTEGER_WITH_SUFFIX:
            case FLOAT_WITH_SUFFIX:
                if (suffix == null) {
                    throw new IllegalArgumentException(
                            "There must be suffix for number with suffix: " + kind);
                }
                break;
            case FLOAT:
            case INTEGER:
                if (suffix != null) {
                    throw new IllegalArgumentException(
                            "There must not be suffix for plain number: " + kind);
                }
                return simple(kind);
            default:
                throw new IllegalArgumentException("Not a modified token: " + kind);
        }
        return modifierKey(kind, suffix);
    }

    /**
     * Get or create modified token key if does not exists yet.
     *
     * @param kind   a token kind
     * @param suffix a token suffix
     * @return the create key
     */
    private static TokenKey modifierKey(final Tokens kind, final String suffix) {
        return new TokenKey(kind, suffix, null);
    }

    /**
     * Get token kind that does not have any characteristic except kind.
     *
     * @param kind the token kind
     * @return token key by token kind
     */
    public static TokenKey simple(final Tokens kind) {
        switch (kind) {
            case PREFIXED_STRING:
            case PREFIXED_MULTILINE_STRING:
            case INTEGER_WITH_SUFFIX:
            case FLOAT_WITH_SUFFIX:
            case STRING:
            case MULTILINE_STRING:
                throw new IllegalArgumentException(
                        "Invalid token kind for the method: " + kind);
            default:
                return KIND_MAP.get(kind);
        }
    }

    /**
     * Get string token key with symmetric quotes and no prefix.
     *
     * @param kind  the token kind
     * @param quote the quote to use
     * @return the resulting key
     */
    public static TokenKey string(final Tokens kind, final int quote) {
        return quoted(kind, null, quote, quote);
    }

    /**
     * @return the token kind
     */
    public Tokens kind() {
        return kind;
    }

    /**
     * @return the modifier or null
     */
    public String modifier() {
        return modifier;
    }

    /**
     * @return the prefix for prefixed strings
     */
    public String prefix() {
        if (modifier == null) {
            throw new IllegalStateException("The token key does not have prefix: " + this);
        }
        return modifier;
    }

    /**
     * @return the suffix for numbers
     */
    public String suffix() {
        if (modifier == null) {
            throw new IllegalStateException("The token key does not have suffix: " + this);
        }
        return modifier;
    }

    /**
     * @return the quote class
     */
    public QuoteClass quoteClass() {
        return quoteClass;
    }

    /**
     * @return the object substituted instead of deserialized one
     */
    private Object readResolve() {
        if (quoteClass != null) {
            return quoted(kind, modifier, quoteClass);
        }
        if (modifier != null) {
            return modified(kind, modifier);
        }
        return simple(kind);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(kind);
        if (modifier != null) {
            sb.append('/').append(modifier);
        }
        if (quoteClass != null) {
            sb.append('!').append(quoteClass);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        // CHECKSTYLE:OFF
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TokenKey tokenKey = (TokenKey) o;
        if (hashCode != tokenKey.hashCode) {
            return false;
        }
        if (quoteClass != tokenKey.quoteClass) {
            return false;
        }
        if (kind != tokenKey.kind) {
            return false;
        }
        //noinspection RedundantIfStatement
        if (modifier != null ? !modifier.equals(tokenKey.modifier) : tokenKey.modifier != null) {
            return false;
        }

        return true;
        // CHECKSTYLE:ON
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * @return calculated hash code
     */
    private int calculateHashCode() {
        // CHECKSTYLE:OFF
        int result = kind != null ? kind.hashCode() : 0;
        result = 31 * result + (modifier != null ? modifier.hashCode() : 0);
        result = 31 * result + (quoteClass != null ? quoteClass.hashCode() : 0);
        return result;
        // CHECKSTYLE:ON
    }
}
