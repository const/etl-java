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

package net.sf.etl.xml_catalog.event;

import net.sf.etl.xml_catalog.event.entries.CatalogEntry;
import net.sf.etl.xml_catalog.event.entries.DomParser;
import net.sf.etl.xml_catalog.event.entries.GroupEntry;
import net.sf.etl.xml_catalog.event.entries.NextCatalogEntry;
import org.w3c.dom.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The snapshot of catalog file at some moment of time. The data returned from this object should never change
 * over time.
 */
public final class CatalogFile {
    /**
     * The system id of the catalog file.
     */
    private final String systemId;
    /**
     * The version of the catalog file.
     */
    private final Object version;
    /**
     * The resources used for the catalog file.
     */
    private final List<CatalogResourceUsage> usedResources;
    /**
     * The root catalog entry.
     */
    private final GroupEntry rootEntry;
    /**
     * The next catalogs.
     */
    private final List<CatalogRequest> nextCatalogs;


    /**
     * The constructor.
     *
     * @param systemId      the system id of catalog file
     * @param version       the version of catalog file
     * @param usedResources the resources used by the catalog
     * @param rootEntry     the root entry
     */
    public CatalogFile(final String systemId, final Object version, final List<CatalogResourceUsage> usedResources,
                       final GroupEntry rootEntry) {
        this.systemId = systemId;
        this.version = version;
        this.rootEntry = rootEntry;
        this.usedResources = Collections.unmodifiableList(new ArrayList<CatalogResourceUsage>(usedResources));
        this.nextCatalogs = Collections.unmodifiableList(getNextCatalogs(rootEntry, new ArrayList<CatalogRequest>()));
    }

    /**
     * Collect next catalogs.
     *
     * @param rootEntry    the root entry
     * @param nextCatalogs the next catalogs
     * @return the next catalogs parameter
     */
    private static List<CatalogRequest> getNextCatalogs(final GroupEntry rootEntry,
                                                        final List<CatalogRequest> nextCatalogs) {
        for (final CatalogEntry entry : rootEntry.getEntries()) {
            if (entry instanceof GroupEntry) {
                getNextCatalogs((GroupEntry) entry, nextCatalogs);
            } else if (entry instanceof NextCatalogEntry) {
                nextCatalogs.add(((NextCatalogEntry) entry).toCatalogRequest());
            }
        }
        return nextCatalogs;
    }

    /**
     * Create catalog file from from DOM.
     *
     * @param systemId      the system id of catalog file
     * @param version       the version of catalog file
     * @param usedResources the resources used by the catalog
     * @param document      the document
     * @return the created catalog file
     */
    public static CatalogFile fromDom(final String systemId, final Object version,
                                      final List<CatalogResourceUsage> usedResources,
                                      final Document document) {
        return new CatalogFile(systemId, version, usedResources, DomParser.parse(document));
    }


    /**
     * Create a catalog file from URI list.
     *
     * @param systemId      the system id of catalog file
     * @param version       the version of catalog file
     * @param usedResources the resources used by the catalog (the list is copied)
     * @param nextCatalogs  the next catalogs (the list is copied)
     * @return the created catalog file
     */
    public static CatalogFile fromNextCatalogs(final String systemId, final Object version,
                                               final List<CatalogResourceUsage> usedResources,
                                               final List<URI> nextCatalogs) {
        final URI base;
        try {
            base = new URI(systemId);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Catalog systemId could not be parsed: " + systemId, e);
        }
        final List<CatalogEntry> nextEntries = new ArrayList<CatalogEntry>();
        for (final URI nextCatalog : nextCatalogs) {
            nextEntries.add(new NextCatalogEntry(null, base, nextCatalog)); // NOPMD
        }
        return new CatalogFile(systemId, version, usedResources, new GroupEntry(null, base, nextEntries, null));
    }

    /**
     * Create a catalog file from URI list.
     *
     * @param systemId     the system id of catalog file
     * @param version      the version of catalog file
     * @param nextCatalogs the next catalogs (the list is copied)
     * @return the created catalog file
     */
    public static CatalogFile fromNextCatalogs(final String systemId, final Object version,
                                               final List<URI> nextCatalogs) {
        return fromNextCatalogs(systemId, version, CatalogResourceUsage.NONE, nextCatalogs);
    }

    /**
     * Create a catalog file from URI list.
     *
     * @param systemId     the system id of catalog file
     * @param nextCatalogs the next catalogs (the list is copied)
     * @return the created catalog file
     */
    public static CatalogFile fromNextCatalogs(final String systemId, final List<URI> nextCatalogs) {
        return fromNextCatalogs(systemId, System.currentTimeMillis(), CatalogResourceUsage.NONE, nextCatalogs);
    }

    /**
     * The system id of the catalog.
     *
     * @return the system id
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * @return the catalog file version, it is some immutable object that somehow mark the version of catalog,
     * it could be timestamp of the file or the time when catalog was created.
     */
    public Object getVersion() {
        return version;
    }


    /**
     * @return the resources used by this catalog file, changes in these resources might cause a
     * need in re-evaluation the catalog.
     */
    public List<CatalogResourceUsage> getUsedResources() {
        return usedResources;
    }

    /**
     * @return the root entry for the catalog.
     */
    public GroupEntry getRootEntry() {
        return rootEntry;
    }

    /**
     * @return the next catalogs for this catalog, this is either evaluated basing on DOM tree or even hardcoded
     */
    public List<CatalogRequest> getNextCatalogs() {
        return nextCatalogs;
    }
}
