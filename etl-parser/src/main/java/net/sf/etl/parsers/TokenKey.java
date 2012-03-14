/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2009 Constantine A Plotnikov
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

import java.util.HashMap;

/**
 * The key that uniquely specifies the token kind. Note that each token key is
 * unique and stored is forever. In future, a weak hash map should be used to clean up.
 *
 * @author const
 */
public final class TokenKey {
    /**
     * Map for string tokens
     */
    private final static HashMap<StringKey, TokenKey> stringMap = new HashMap<StringKey, TokenKey>();
    /**
     * Map for number tokens + modifier
     */
    private final static HashMap<ModifierKey, TokenKey> modifierMap = new HashMap<ModifierKey, TokenKey>();
    /**
     * Map for tokens without modifiers
     */
    private final static HashMap<Tokens, TokenKey> kindMap;

    static {
        HashMap<Tokens, TokenKey> map = new HashMap<Tokens, TokenKey>();
        for (Tokens k : Tokens.values()) {
            map.put(k, new TokenKey(k, null, -1, -1));
        }
        kindMap = map;
    }

    /**
     * The token kind
     */
    private final Tokens kind;
    /**
     * The modifier (like suffix for numbers or prefix for strings)
     */
    private final String modifier;
    /**
     * The first quote for strings
     */
    private final int start;
    /**
     * The second quote for strings
     */
    private final int end;

    /**
     * The private constructor from fields
     *
     * @param kind     the token kind
     * @param modifier the token modifier
     * @param start    the start of the token
     * @param end      the end of the token
     */
    private TokenKey(Tokens kind, String modifier, int start, int end) {
        this.kind = kind;
        this.modifier = modifier == null ? null : modifier.intern();
        this.start = start;
        this.end = end;
    }

    /**
     * @return the token kind
     */
    public Tokens kind() {
        return kind;
    }

    /**
     * @return true if the string has prefix
     */
    public boolean hasPrefix() {
        return modifier != null;
    }

    /**
     * @return the prefix for prefixed strings (string is
     *         {@link String#intern()}-ed)
     */
    public String prefix() {
        if (modifier == null) {
            throw new IllegalStateException(
                    "The token key does not have prefix: " + this);
        }
        return modifier;
    }

    /**
     * @return the suffix for numbers (string is {@link String#intern()}-ed)
     */
    public String suffix() {
        if (modifier == null) {
            throw new IllegalStateException(
                    "The token key does not have suffix: " + this);
        }
        return modifier;
    }

    /**
     * @return the start quote (string is {@link String#intern()}-ed)
     */
    public int startQuote() {
        if (start == -1) {
            throw new IllegalStateException("The token key is not string key: "
                    + this);
        }
        return start;
    }

    /**
     * @return the end quote (string is {@link String#intern()}-ed)
     */
    public int endQuote() {
        if (start == -1) {
            throw new IllegalStateException("The token key is not string key: "
                    + this);
        }
        return end;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder rc = new StringBuilder();
        rc.append(kind);
        if (modifier != null || start != -1 || end != -1) {
            rc.append('[');
            boolean printed = false;
            if (modifier != null) {
                rc.append(modifier);
                printed = true;
            }
            if (start != -1 && start == end) {
                if (printed) {
                    rc.append(", ");
                }
                rc.append("quote=");
                rc.append(Character.toChars(start));
            } else {
                if (start != -1) {
                    if (printed) {
                        rc.append(", ");
                    }
                    rc.append("start=");
                    rc.append(Character.toChars(start));
                }
                if (end != -1) {
                    if (printed) {
                        rc.append(", ");
                    }
                    rc.append("end=");
                    rc.append(Character.toChars(end));
                }
            }
            rc.append(']');
        }
        return rc.toString();
    }

    /**
     * Get string token key
     *
     * @param kind   the token kind
     * @param prefix the string prefix (or null for not prefixed string)
     * @param start  the start quote
     * @param end    the end quote
     * @return the token key with specified parameters
     */
    public static TokenKey quoted(Tokens kind, String prefix, int start,
                                  int end) {
        switch (kind) {
            case PREFIXED_STRING:
            case PREFIXED_MULTILINE_STRING:
                if (prefix == null) {
                    throw new IllegalArgumentException(
                            "There must be prefix for prefixed string: " + kind);
                }
                break;
            case STRING:
            case MULTILINE_STRING:
                if (prefix != null) {
                    throw new IllegalArgumentException(
                            "There must not be prefix for unprefixed string: "
                                    + kind);
                }
                break;
            default:
                throw new IllegalArgumentException("Not a string token: " + kind);
        }
        if (start == -1 || end == -1) {
            throw new IllegalArgumentException(
                    "Start and end quoted should be defined: " + start + " : "
                            + end);
        }
        StringKey key = new StringKey(kind, prefix, start, end);
        synchronized (stringMap) {
            TokenKey rc = stringMap.get(key);
            if (rc == null) {
                rc = new TokenKey(kind, prefix, start, end);
                stringMap.put(key, rc);
            }
            return rc;
        }
    }

    /**
     * Get number token key
     *
     * @param kind   the token kind
     * @param suffix the number suffix (or null if there is no suffix)
     * @return the token key with specified parameters
     */
    public static TokenKey modified(Tokens kind, String suffix) {
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
                throw new IllegalArgumentException("Not a string token: " + kind);
        }
        return modifierKey(kind, suffix);
    }

    /**
     * Get or create modified token key if does not exists yet
     *
     * @param kind   a token kind
     * @param suffix a token suffix
     * @return the create key
     */
    private static TokenKey modifierKey(Tokens kind, String suffix) {
        ModifierKey key = new ModifierKey(kind, suffix);
        synchronized (modifierMap) {
            TokenKey rc = modifierMap.get(key);
            if (rc == null) {
                rc = new TokenKey(kind, suffix, -1, -1);
                modifierMap.put(key, rc);
            }
            return rc;
        }
    }

    /**
     * Get token kind that does not have any characteristic except kind
     *
     * @param kind the token kind
     * @return token key by token kind
     */
    public static TokenKey simple(Tokens kind) {
        switch (kind) {
            case PREFIXED_STRING:
            case PREFIXED_MULTILINE_STRING:
            case INTEGER_WITH_SUFFIX:
            case FLOAT_WITH_SUFFIX:
            case STRING:
            case MULTILINE_STRING:
                throw new IllegalArgumentException(
                        "Invalid token kind for the method: " + kind);
        }
        return kindMap.get(kind);
    }

    /**
     * Get string token key with symmetric quotes and no prefix
     *
     * @param kind  the token kind
     * @param quote the quote to use
     * @return the resulting key
     */
    public static TokenKey string(Tokens kind, int quote) {
        return quoted(kind, null, quote, quote);
    }

    /**
     * The key for strings map
     */
    private static class StringKey {
        /**
         * hash code
         */
        final int hash;
        /**
         * kind of token
         */
        final Tokens kind;
        /**
         * suffix for token
         */
        final String prefix;
        /**
         * start quote for token
         */
        final int start;
        /**
         * end quote for token
         */
        final int end;

        /**
         * A constructor from fields
         *
         * @param kind   the token kind
         * @param prefix the token suffix
         * @param start  the start quote
         * @param end    the end quote
         */
        StringKey(Tokens kind, String prefix, int start, int end) {
            this.kind = kind;
            this.prefix = prefix;
            this.start = start;
            this.end = end;
            hash = ((kind.hashCode() + (prefix != null ? prefix.hashCode() : 0)) * 17 + start) * 17 + end;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return hash;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StringKey)) {
                return false;
            }
            StringKey k = (StringKey) obj;
            return kind == k.kind
                    && (prefix == null ? k.prefix == null : prefix
                    .equals(k.prefix)) && start == k.start
                    && end == k.end;
        }
    }

    /**
     * The key for token+modifier map
     */
    private static class ModifierKey {
        /**
         * hash code
         */
        final int hash;
        /**
         * kind of token
         */
        final Tokens kind;
        /**
         * suffix for token
         */
        final String modifier;

        /**
         * A constructor from fields
         *
         * @param kind     the token kind
         * @param modifier the token modifier
         */
        ModifierKey(Tokens kind, String modifier) {
            this.kind = kind;
            this.modifier = modifier;
            hash = kind.hashCode() + modifier.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return hash;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ModifierKey)) {
                return false;
            }
            ModifierKey k = (ModifierKey) obj;
            return kind == k.kind && modifier.equals(k.modifier);
        }
    }
}
