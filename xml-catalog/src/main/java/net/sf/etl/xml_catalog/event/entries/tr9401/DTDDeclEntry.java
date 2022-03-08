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

package net.sf.etl.xml_catalog.event.entries.tr9401;

import net.sf.etl.xml_catalog.event.entries.UriReferenceEntry;

import java.net.URI;

/**
 * The DTD declaration entry.
 */
public final class DTDDeclEntry extends UriReferenceEntry {
    /**
     * The public id.
     */
    private final String publicId;

    /**
     * The constructor.
     *
     * @param id       the id
     * @param base     the base
     * @param uri      the URI
     * @param publicId the public id
     */
    public DTDDeclEntry(final String id, final URI base, final URI uri, final String publicId) {
        super(id, base, uri);
        this.publicId = publicId;
    }

    /**
     * @return the public id
     */
    public String getPublicId() {
        return publicId;
    }
}
