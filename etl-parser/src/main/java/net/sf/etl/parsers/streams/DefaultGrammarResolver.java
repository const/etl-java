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

package net.sf.etl.parsers.streams;

import net.sf.etl.parsers.event.TermParser;

import java.util.Collections;
import java.util.Set;

/**
 * The caching grammar resolver, it is based on system ids.
 */
public final class DefaultGrammarResolver implements GrammarResolver {
    /**
     * The resolver instance.
     */
    public static final DefaultGrammarResolver INSTANCE = new DefaultGrammarResolver();
    /**
     * The configuration.
     */
    private final TermReaderCatalogConfiguration configuration;
    /**
     * The grammars that are already loaded.
     */
    private final Set<String> loadedGrammars;

    /**
     * The constructor from default configuration.
     */
    public DefaultGrammarResolver() {
        this(DefaultTermReaderConfiguration.INSTANCE, Collections.<String>emptySet());
    }

    /**
     * The caching resolver.
     *
     * @param configuration  the configuration
     * @param loadedGrammars the loaded grammars
     */
    public DefaultGrammarResolver(final TermReaderCatalogConfiguration configuration,
                                  final Set<String> loadedGrammars) {
        this.configuration = configuration;
        this.loadedGrammars = loadedGrammars;
    }

    /**
     * The resolve the grammar and finish when it is done.
     *
     * @param termParser the request
     */
    @Override
    public void resolve(final TermParser termParser) {
        new BlockingCatalogSession(
                configuration,
                configuration.getCatalog(termParser.getSystemId()),
                termParser,
                loadedGrammars
        ).resolve();
    }
}
