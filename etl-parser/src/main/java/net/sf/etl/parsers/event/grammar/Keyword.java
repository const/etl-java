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

import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.TokenKey;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * The keyword in the keyword context, the keyword context behaves like a symbol. The keyword uses default identity
 * {@link Object#hashCode()} and {@link Object#equals(Object)} implementations.
 */
public final class Keyword implements Serializable {
    /**
     * The weak hash map for keywords.
     */
    private static final WeakHashMap<String, WeakReference<Keyword>> KEYWORDS =
            new WeakHashMap<String, WeakReference<Keyword>>();
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 3643615947738378559L;
    /**
     * The keyword text.
     */
    private final String text;
    /**
     * The token key for keyword.
     */
    private final TokenKey tokenKey;

    /**
     * The private constructor.
     *
     * @param tokenKey the token key
     * @param text     the keyword text
     */
    private Keyword(final TokenKey tokenKey, final String text) {
        this.tokenKey = tokenKey;
        this.text = text;
    }

    /**
     * Get keyword for the specified token.
     *
     * @param token the text to use
     * @return the keyword for the text
     */
    public static Keyword forToken(final Token token) {
        return forText(token.text(), token.key());
    }

    /**
     * Get keyword for the text. This method is synchronized and slow, but it is called only during
     * grammar construction case. In runtime, the instances of {@link KeywordContext} are used.
     *
     * @param text the text
     * @param key  the token key
     * @return token for the text
     */
    public static Keyword forText(final String text, final TokenKey key) {
        synchronized (KEYWORDS) {
            final WeakReference<Keyword> keyword = KEYWORDS.get(text);
            Keyword rc = keyword == null ? null : keyword.get();
            if (rc == null) {
                rc = new Keyword(key, text);
                KEYWORDS.put(rc.text(), new WeakReference<Keyword>(rc));
            }
            return rc;
        }
    }

    /**
     * @return resolve object from serialization
     */
    private Object readResolve() {
        return forText(text, tokenKey);
    }

    /**
     * @return the keyword text
     */
    public String text() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }
}
