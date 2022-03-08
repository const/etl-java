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

package net.sf.etl.xml_catalog.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * URI normalization.
 */
public final class URIUtil {
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(URIUtil.class);

    /**
     * Private constructor for utility class.
     */
    private URIUtil() {
        // do nothing
    }

    /**
     * Normalize string for an url.
     *
     * @param input the  potentially bad string
     * @return normalized string
     */
    public static String normalizeURI(final String input) {
        if (input == null) {
            return null;
        }
        final byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        // CHECKSTYLE:OFF
        final StringBuilder rc = new StringBuilder(input.length() + 10);
        // CHECKSTYLE:ON
        for (final byte b : bytes) {
            switch (b) {
                case '"':
                case '<':
                case '>':
                case '\\':
                case '^':
                case '`':
                case '{':
                case '|':
                case '}':
                    rc.append('%');
                    HexUtil.appendHexByteUppercase(rc, b);
                    break;
                default:
                    if (' ' < b && b <= '~') {
                        rc.append((char) b);
                    } else {
                        rc.append('%');
                        HexUtil.appendHexByteUppercase(rc, b);
                    }
                    break;
            }
        }
        return rc.toString();
    }

    /**
     * Normalize potentially relative URI using base URI.
     *
     * @param baseUri the base URI
     * @param uri     the possibly relative relative URI
     * @return the normalized URI
     */
    public static String normalizeUri(final String baseUri, final String uri) {
        String result = normalizeURI(uri);
        try {
            final URI parsedBaseUri = URI.create(baseUri);
            if ("jar".equals(parsedBaseUri.getScheme())) {
                final int pos = baseUri.lastIndexOf('!');
                if (pos != -1) {
                    final String tempUriPrefix = "temp:";
                    final URI temp = URI.create(tempUriPrefix + baseUri.substring(pos + 1)).resolve(result);
                    result = baseUri.substring(0, pos + 1) + temp.toString().substring(tempUriPrefix.length());
                }
            } else {
                result = parsedBaseUri.resolve(result).toString();
            }
        } catch (Exception ex) { // NOPMD
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to normalize URI: '" + uri + "' relative to " + baseUri, ex);
            }
        }
        return result;
    }
}
