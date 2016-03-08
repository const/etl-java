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

package net.sf.etl.xml_catalog.event.entries;

import java.net.URI;

/**
 * The rewrite URI entry.
 */
public final class RewriteUriEntry extends CatalogEntry implements UriResolutionEntry {
    /**
     * The purpose.
     */
    private final String purpose;
    /**
     * The nature.
     */
    private final String nature;
    /**
     * The uri start string.
     */
    private final String uriStartString;
    /**
     * The rewrite prefix.
     */
    private final String rewritePrefix;

    /**
     * The constructor.
     *
     * @param id             the id
     * @param base           the base URI
     * @param purpose        the URI purpose
     * @param nature         the URI reference
     * @param uriStartString the URI start string
     * @param rewritePrefix  the rewrite prefix
     */
    public RewriteUriEntry(final String id, final URI base, final String purpose, final String nature,
                           final String uriStartString, final String rewritePrefix) {
        super(id, base);
        this.purpose = purpose;
        this.nature = nature;
        this.uriStartString = uriStartString;
        this.rewritePrefix = rewritePrefix;
    }

    @Override
    public String getNature() {
        return nature;
    }

    @Override
    public String getPurpose() {
        return purpose;
    }

    /**
     * @return the URI start string
     */
    public String getUriStartString() {
        return uriStartString;
    }

    /**
     * @return the rewrite prefix
     */
    public String getRewritePrefix() {
        return rewritePrefix;
    }
}
