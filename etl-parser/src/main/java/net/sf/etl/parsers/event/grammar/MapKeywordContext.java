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

package net.sf.etl.parsers.event.grammar;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Map keyword context.
 */
public final class MapKeywordContext implements KeywordContext {
    /**
     * The backing map.
     */
    private final Map<String, Keyword> map;

    /**
     * Map keyword context that numerate keywords form zero, if keyword is mentioned several times, it numbered
     * with the latest occurrence.
     *
     * @param keywords the keywords
     */
    public MapKeywordContext(final Keyword... keywords) {
        this.map = new HashMap<>(keywords.length);
        for (final Keyword value : keywords) {
            map.put(value.text(), value);
        }
    }

    /**
     * Map keyword context that numerate keywords form zero, if keyword is mentioned several times, it numbered
     * with the latest occurrence.
     *
     * @param keywords the keywords
     */
    public MapKeywordContext(final Collection<Keyword> keywords) {
        this.map = new HashMap<>(keywords.size());
        for (final Keyword value : keywords) {
            map.put(value.text(), value);
        }
    }

    @Override
    public Keyword get(final String text) {
        return map.get(text);
    }
}
