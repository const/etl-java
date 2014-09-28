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

import net.sf.etl.parsers.characters.TextUtil;
import net.sf.etl.parsers.characters.Whitespaces;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The default term parser configuration (intended for command line tools), that does not distinguish
 * files and return the same value for all system ids.
 */
public final class DefaultTermParserConfiguration implements TermParserConfiguration {
    /**
     * The default configuration instance.
     */
    public static final DefaultTermParserConfiguration INSTANCE = new DefaultTermParserConfiguration();
    /**
     * The property that specifies file encoding.
     */
    public static final String ETL_FILE_ENCODING_PROPERTY = "etl.file.encoding";
    /**
     * The property that specifies tab size.
     */
    public static final String ETL_TAB_SIZE_PROPERTY = "etl.tab.size";
    /**
     * The tabulation size.
     */
    private final int tabSize;
    /**
     * The charset encoding.
     */
    private final Charset encoding;
    /**
     * The grammar cache.
     */
    private final Map<String, CompiledGrammar> grammarCache = new HashMap<String, CompiledGrammar>(); //NOPMD

    /**
     * The constructor from fields.
     *
     * @param tabSize  the tab size
     * @param encoding the encoding
     */
    public DefaultTermParserConfiguration(final int tabSize, final Charset encoding) {
        this.tabSize = tabSize;
        this.encoding = encoding;
    }

    /**
     * The default constructor that takes default values from system properties.
     */
    public DefaultTermParserConfiguration() {
        this(getDefaultTabSize(), getDefaultEncoding());
    }


    /**
     * @return the default encoding
     */
    private static Charset getDefaultEncoding() {
        try {
            final String property = System.getProperty(ETL_FILE_ENCODING_PROPERTY, TextUtil.UTF8.name());
            return Charset.forName(property);
        } catch (Exception ex) { // NOPMD
            return TextUtil.UTF8;
        }
    }

    /**
     * @return the default tab size (if not specified = 8)
     */
    private static int getDefaultTabSize() {
        try {
            return Integer.parseInt(System.getProperty(ETL_TAB_SIZE_PROPERTY, "8"));
        } catch (Exception ex) { // NOPMD
            return Whitespaces.DEFAULT_TAB_SIZE;
        }
    }


    @Override
    public int getTabSize(final String systemId) {
        return tabSize;
    }


    @Override
    public CompiledGrammar getCachedGrammar(final String systemId) {
        synchronized (grammarCache) {
            return grammarCache.get(systemId);
        }
    }

    @Override
    public void cacheGrammar(final CompiledGrammar grammar) {
        synchronized (grammarCache) {
            final HashSet<String> cachedGrammars = new HashSet<String>();
            cacheGrammar(cachedGrammars, grammar);
        }
    }


    /**
     * Cache grammar and all related grammars.
     *
     * @param cachedGrammars the grammars cached in this pass
     * @param grammar        the cached grammar
     */
    private void cacheGrammar(final Set<String> cachedGrammars, final CompiledGrammar grammar) {
        final String systemId = grammar.getDescriptor().getSystemId();
        if (!cachedGrammars.add(systemId)) {
            grammarCache.put(systemId, grammar);
            for (final CompiledGrammar other : grammar.getOtherGrammars()) {
                cacheGrammar(cachedGrammars, other);
            }
        }
    }

    @Override
    public Charset getEncoding(final String systemId) {
        return encoding;
    }
}
