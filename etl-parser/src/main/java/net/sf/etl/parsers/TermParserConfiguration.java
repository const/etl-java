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

package net.sf.etl.parsers;

import net.sf.etl.parsers.event.grammar.CompiledGrammar;

import java.nio.charset.Charset;

/**
 * The configuration for the term parser, there is a default configuration,
 * but it is expected, that IDE will provide other configuration.
 */
public interface TermParserConfiguration {
    /**
     * Get tab size for the specified system id.
     *
     * @param systemId the system id to check
     * @return the tabulation size
     */
    int getTabSize(String systemId);

    /**
     * Get cached grammar.
     *
     * @param systemId the system of compiled grammar
     * @return the cached grammar
     */
    CompiledGrammar getCachedGrammar(String systemId);

    /**
     * Cache compiled grammar.
     *
     * @param grammar the grammar to cache
     */
    void cacheGrammar(CompiledGrammar grammar);

    /**
     * Get encoding by system id.
     *
     * @param systemId the system id
     * @return the charset
     */
    Charset getEncoding(String systemId);
}
