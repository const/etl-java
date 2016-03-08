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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The group entry.
 */
public final class GroupEntry extends CatalogEntry {
    /**
     * The list of entries.
     */
    private final List<CatalogEntry> entries;
    /**
     * The prefer public entry.
     */
    private final Boolean preferPublic;

    /**
     * The constructor.
     *
     * @param id           the id
     * @param base         the base URI
     * @param entries      the list of entries
     * @param preferPublic the value of prefer public attribute (null if not set)
     */
    public GroupEntry(final String id, final URI base, final List<CatalogEntry> entries, final Boolean preferPublic) {
        super(id, base);
        this.entries = Collections.unmodifiableList(Arrays.asList(entries.toArray(new CatalogEntry[entries.size()])));
        this.preferPublic = preferPublic;
    }

    /**
     * @return the entries
     */
    public List<CatalogEntry> getEntries() {
        return entries;
    }

    /**
     * @return the prefer public value
     */
    public Boolean getPreferPublic() {
        return preferPublic;
    }

    /**
     * Get adjusted prefer public value.
     *
     * @param previous the value from the previous context
     * @return the prefer public value
     */
    public boolean getPreferPublic(final boolean previous) {
        return preferPublic == null ? previous : getPreferPublic();
    }
}
