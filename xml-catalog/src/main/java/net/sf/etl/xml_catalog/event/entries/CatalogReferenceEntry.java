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

package net.sf.etl.xml_catalog.event.entries;

import net.sf.etl.xml_catalog.event.CatalogRequest;

import java.net.URI;

/**
 * Catalog reference entry.
 */
public abstract class CatalogReferenceEntry extends CatalogEntry {
    /**
     * The catalog reference.
     */
    private final URI catalog;

    /**
     * The constructor.
     *
     * @param id      the id
     * @param base    the base URI
     * @param catalog the catalog reference
     */
    public CatalogReferenceEntry(final String id, final URI base, final URI catalog) {
        super(id, base);
        this.catalog = catalog;
    }

    /**
     * @return the catalog URI
     */
    public final URI getCatalog() {
        return catalog;
    }

    /**
     * @return to catalog request
     */
    public final CatalogRequest toCatalogRequest() {
        return new CatalogRequest(getCatalog().toASCIIString(), getId());
    }
}
