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

import net.sf.etl.parsers.TermParserConfiguration;

import java.io.IOException;
import java.io.Reader;

/**
 * The configuration for the blocking parsers.
 */
public interface TermReaderConfiguration {
    /**
     * @return the generic parser configuration
     */
    TermParserConfiguration getParserConfiguration();

    /**
     * Identify default grammar resolver for the source.
     *
     * @param systemId the system id of source being parsed
     * @return get default synchronous grammar resolver for the term reader
     */
    GrammarResolver getGrammarResolver(String systemId);

    /**
     * Get the appropriate reader for the specified system id. The reader is configured with needed encoding.
     * Many IDE allow specify a particular encoding for the file, so it needs to be checked for each file separately.
     *
     * @param systemId the system id
     * @return the reader for the specified system id
     * @throws java.io.IOException in case if opening failed.
     */
    Reader openReader(String systemId) throws IOException;
}
