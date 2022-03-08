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

package net.sf.etl.xml_catalog.event.engine.impl.step;

import net.sf.etl.xml_catalog.event.entries.CatalogEntry;
import net.sf.etl.xml_catalog.event.entries.GroupEntry;
import net.sf.etl.xml_catalog.event.entries.UriReferenceEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.DTDDeclEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.NamedEntry;

/**
 * Resolve TR9401 catalog entries.
 */
public final class TR9401ResolutionStep extends ResolutionStep {
    /**
     * The entry type.
     */
    private final Class<?> type;
    /**
     * The entry name.
     */
    private final String name;
    /**
     * If true, only prefer public dtddecl entries will be matched.
     */
    private final boolean mustPreferPublic;
    /**
     * Start value for each file.
     */
    private final boolean defaultPreferPublic;
    /**
     * Resolved entity.
     */
    private UriReferenceEntry resolved;

    /**
     * The constructor for search process.
     *
     * @param type                the type of element being searched
     * @param name                the name of element (publicId for dtddecl)
     * @param mustPreferPublic    if true, dtddecl element matches only when prefer public is active.
     * @param defaultPreferPublic default prefer public value
     */
    public TR9401ResolutionStep(final Class<?> type, final String name, final boolean mustPreferPublic,
                                final boolean defaultPreferPublic) {
        this.type = type;
        this.name = name;
        this.mustPreferPublic = mustPreferPublic;
        this.defaultPreferPublic = defaultPreferPublic;
    }

    /**
     * Process the element (one pass).
     *
     * @param entry        the entry to process.
     * @param preferPublic the value of prefer attribute
     * @return true, if further processing should be stopped because of success
     */
    private boolean process(final CatalogEntry entry, final boolean preferPublic) { // NOPMD
        if (entry instanceof GroupEntry) {
            final GroupEntry e = (GroupEntry) entry;
            final boolean childPreferPublic = e.getPreferPublic(preferPublic);
            for (final CatalogEntry catalogEntry : e.getEntries()) {
                if (process(catalogEntry, childPreferPublic)) {
                    return true;
                }
            }
            return false;
        }
        if (entry.getClass() != type) {
            return false;
        }
        if (entry instanceof DTDDeclEntry) {
            if (!mustPreferPublic || preferPublic) {
                final DTDDeclEntry e = (DTDDeclEntry) entry;
                if (name.equals(e.getPublicId())) {
                    resolved = e;
                    return true;
                }
            }
        } else if (entry instanceof NamedEntry) {
            final NamedEntry e = (NamedEntry) entry;
            if (name.equals(e.getName())) {
                resolved = e;
                return true;
            }
        } else {
            resolved = (UriReferenceEntry) entry;
            return true;
        }
        return false;
    }


    @Override
    public void resolve(final CatalogEntry entry) {
        process(entry, defaultPreferPublic);
        if (resolved != null) {
            setResolved(resolved, resolved.getUri());
        }
    }
}
