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

package net.sf.etl.xml_catalog.event;

import java.net.URI;
import java.net.URL;
import java.util.Objects;

/**
 * The request for the catalog file. The catalog requests could be invalid, in that case they might be ignored.
 */
public final class CatalogRequest {
    /**
     * The system id for the request.
     */
    private final String systemId;
    /**
     * The request location that requested the catalog.
     */
    private final String requestLocation;

    /**
     * The constructor.
     *
     * @param systemId        the system id
     * @param requestLocation the request location is some free form, that helps to find out where
     *                        this catalog is requested
     */
    public CatalogRequest(final String systemId, final String requestLocation) {
        this.systemId = systemId;
        this.requestLocation = requestLocation;
    }

    /**
     * The constructor.
     *
     * @param systemId the system id
     */
    public CatalogRequest(final String systemId) {
        this(systemId, null);
    }

    /**
     * The constructor from URI.
     *
     * @param uri the URI
     */
    public CatalogRequest(final URI uri) {
        this(uri.toString());
    }

    /**
     * Constructor from URL.
     *
     * @param url the url
     */
    public CatalogRequest(final URL url) {
        this(url.toString());
    }

    /**
     * @return the requested system id
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * @return the request location in the catalog file that requested this catalog (might be null)
     */
    public String getRequestLocation() {
        return requestLocation;
    }

    @Override
    public boolean equals(final Object o) {
        // CHECKSTYLE:OFF
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CatalogRequest that = (CatalogRequest) o;
        return Objects.equals(systemId, that.systemId);
        // CHECKSTYLE:ON
    }

    @Override
    public int hashCode() {
        return systemId != null ? systemId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CatalogRequest" + "{systemId='" + systemId + '\''
                + ", requestLocation='" + requestLocation + '\'' + '}';
    }
}
