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

package net.sf.etl.parsers.streams;

import net.sf.etl.parsers.DefaultTermParserConfiguration;
import net.sf.etl.parsers.TermParserConfiguration;
import net.sf.etl.xml_catalog.blocking.BlockingCatalog;
import net.sf.etl.xml_catalog.blocking.provider.CatalogProviders;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collections;

/**
 * The configuration for the blocking parsers.
 */
public final class DefaultTermReaderConfiguration implements TermReaderCatalogConfiguration {
    /**
     * The default configuration instance.
     */
    public static final DefaultTermReaderConfiguration INSTANCE = new DefaultTermReaderConfiguration(
            DefaultTermParserConfiguration.INSTANCE, getDefaultCatalog());
    /**
     * The catalog.
     */
    private final BlockingCatalog catalog;
    /**
     * The parser configuration.
     */
    private final TermParserConfiguration termParserConfiguration;
    /**
     * The grammar resolver.
     */
    private final GrammarResolver grammarResolver;

    /**
     * The constructor.
     *
     * @param termParserConfiguration the configuration
     * @param catalog                 the catalog
     */
    public DefaultTermReaderConfiguration(final TermParserConfiguration termParserConfiguration,
                                          final BlockingCatalog catalog) {
        this.catalog = catalog;
        this.termParserConfiguration = termParserConfiguration;
        this.grammarResolver = new DefaultGrammarResolver(this, Collections.<String>emptySet());
    }

    /**
     * The constructor.
     *
     * @param termParserConfiguration the configuration
     * @param classLoader the class loader to use
     */
    public DefaultTermReaderConfiguration(final TermParserConfiguration termParserConfiguration,
                                          final ClassLoader classLoader) {
        this(termParserConfiguration, getDefaultCatalog(classLoader));
    }

    /**
     * The constructor.
     *
     * @param classLoader the class loader to use
     */
    public DefaultTermReaderConfiguration(final ClassLoader classLoader) {
        this(DefaultTermParserConfiguration.INSTANCE, classLoader);
    }


    /**
     * @return the default catalog
     */
    private static BlockingCatalog getDefaultCatalog() {
        return getDefaultCatalog(DefaultTermReaderConfiguration.class.getClassLoader());
    }

    /**
     * Get default catalog with class loader.
     *
     * @param classLoader the class loader to use
     * @return the default catalog
     */
    private static BlockingCatalog getDefaultCatalog(final ClassLoader classLoader) {
        final BlockingCatalog defaultCatalog = BlockingCatalog.getDefaultCatalog(classLoader);
        return defaultCatalog.withOtherProvider(
                CatalogProviders.createCachedCatalog(defaultCatalog.getCatalogProvider()));
    }


    @Override
    public BlockingCatalog getCatalog(final String systemId) {
        return catalog;
    }

    @Override
    public TermParserConfiguration getParserConfiguration() {
        return termParserConfiguration;
    }

    @Override
    public Reader openReader(final String systemId) throws IOException {
        if (!systemId.startsWith("file:") && !systemId.startsWith("jar:file:")) {
            throw new IllegalArgumentException("Only local system ids are supported!");
        }
        return new InputStreamReader(new URL(systemId).openStream(), termParserConfiguration.getEncoding(systemId));
    }


    @Override
    public GrammarResolver getGrammarResolver(final String systemId) {
        return grammarResolver;
    }
}
