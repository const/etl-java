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

import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.streams.LexerReader;
import org.apache_extras.xml_catalog.event.Catalog;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;

/**
 * The default term parser configuration (intended for command line tools), that does not distinguish
 * files and return the same value for all system ids.
 */
public class DefaultTermParserConfiguration implements TermParserConfiguration {
    /**
     * The default configuration instance
     */
    public static final DefaultTermParserConfiguration INSTANCE = new DefaultTermParserConfiguration();
    /**
     * The property that specifies file encoding
     */
    public static final String ETL_FILE_ENCODING_PROPERTY = "etl.file.encoding";
    /**
     * The property that specifies tab size
     */
    public static final String ETL_TAB_SIZE_PROPERTY = "etl.tab.size";
    /**
     * The catalog
     */
    private final Catalog catalog;
    /**
     * The tabulation size
     */
    private final int tabSize;
    /**
     * The charset encoding
     */
    private final Charset encoding;
    /**
     * The grammar cache
     */
    private final HashMap<String, CompiledGrammar> grammarCache = new HashMap<String, CompiledGrammar>();

    /**
     * The constructor from fields
     *
     * @param catalog  the catalog
     * @param tabSize  the tab size
     * @param encoding the encoding
     */
    public DefaultTermParserConfiguration(Catalog catalog, int tabSize, Charset encoding) {
        this.catalog = catalog;
        this.tabSize = tabSize;
        this.encoding = encoding;
    }

    /**
     * The default constructor that takes default values from system properties
     */
    public DefaultTermParserConfiguration() {
        this(getDefaultCatalog(), getDefaultTabSize(), getDefaultEncoding());
    }

    /**
     * @return the default catalog
     */
    private static Catalog getDefaultCatalog() {
        return Catalog.getDefaultCatalog(DefaultTermParserConfiguration.class);
    }

    /**
     * @return the default encoding
     */
    private static Charset getDefaultEncoding() {
        try {
            final String property = System.getProperty(ETL_FILE_ENCODING_PROPERTY, "UTF-8");
            return Charset.forName(property);
        } catch (Exception ex) {
            return LexerReader.UTF8;
        }
    }

    /**
     * @return the default tab size (if not specified = 8)
     */
    private static int getDefaultTabSize() {
        try {
            return Integer.parseInt(System.getProperty(ETL_TAB_SIZE_PROPERTY, "8"));
        } catch (Exception ex) {
            return 8;
        }
    }


    @Override
    public Catalog getCatalog(String systemId) {
        return catalog;
    }

    @Override
    public int getTabSize(String systemId) {
        return tabSize;
    }

    @Override
    public Reader openReader(String systemId) throws IOException {
        if (!systemId.startsWith("file:") && !systemId.startsWith("jar:file:")) {
            throw new IllegalArgumentException("Only local system ids are supported!");
        }
        return new InputStreamReader(new URL(systemId).openStream(), encoding);
    }

    @Override
    public CompiledGrammar getCachedGrammar(String systemId) {
        synchronized (grammarCache) {
            return grammarCache.get(systemId);
        }
    }

    @Override
    public void cacheGrammar(CompiledGrammar grammar) {
        synchronized (grammarCache) {
            HashSet<String> cachedGrammars = new HashSet<String>();
            cacheGrammar(cachedGrammars, grammar);
        }
    }

    /**
     * Cache grammar and all related grammars
     *
     * @param cachedGrammars the grammars cached in this pass
     * @param grammar        the cached grammar
     */
    private void cacheGrammar(HashSet<String> cachedGrammars, CompiledGrammar grammar) {
        final String systemId = grammar.getDescriptor().getSystemId();
        if (!cachedGrammars.add(systemId)) {
            grammarCache.put(systemId, grammar);
            for (CompiledGrammar other : grammar.getOtherGrammars()) {
                cacheGrammar(cachedGrammars, other);
            }
        }
    }
}