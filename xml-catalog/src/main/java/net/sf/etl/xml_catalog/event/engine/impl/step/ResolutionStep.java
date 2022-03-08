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

import net.sf.etl.xml_catalog.event.CatalogRequest;
import net.sf.etl.xml_catalog.event.engine.impl.util.ReverseLengthComparator;
import net.sf.etl.xml_catalog.event.entries.CatalogEntry;
import net.sf.etl.xml_catalog.event.entries.CatalogReferenceEntry;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * This is a base class for resolution processes in context of the single dom document. Such resolution processes
 * handle only local definition. Handling multiple catalogs is outside of scope of this class and its descendants.
 * However, some helpful information might be detected by this class.
 */
public abstract class ResolutionStep {
    /**
     * The delegates with which resolution process should continue with.
     */
    private List<CatalogRequest> resolvedDelegates;

    /**
     * The resolved element.
     */
    private CatalogEntry resolvedEntry;
    /**
     * The resolved URI.
     */
    private URI resolvedUri;

    /**
     * Process delegate element.
     *
     * @param delegates the delegates map (or null)
     * @param entry     the delegate entry
     * @param prefix    the prefix
     * @param value     the value to match
     * @return {@code delegates} argument or new map, if that element was null
     */
    public static NavigableMap<String, CatalogReferenceEntry> delegate(
            final NavigableMap<String, CatalogReferenceEntry> delegates,
            final CatalogReferenceEntry entry, final String prefix, final String value) {
        NavigableMap<String, CatalogReferenceEntry> result = delegates;
        if (prefix != null && value.startsWith(prefix)) {
            if (result == null) {
                result = new TreeMap<>(ReverseLengthComparator.INSTANCE);
            }
            if (!result.containsKey(prefix)) {
                result.put(prefix, entry);
            }
        }
        return result;
    }

    /**
     * Process max prefix element.
     *
     * @param current      the current maximum (might be null)
     * @param currentValue the current value
     * @param element      the processed element
     * @param elementValue the prefix or suffix value to match
     * @param value        the value to match
     * @param <T>          the type of entry
     * @return new maximum value
     */
    protected static <T extends CatalogEntry> T maxPrefix(final T current, final String currentValue,
                                                          final T element, final String elementValue,
                                                          final String value) {
        if (elementValue == null || !value.startsWith(elementValue)) {
            return current;
        }
        return maxLength(current, currentValue, element, elementValue);
    }

    /**
     * Process max suffix element.
     *
     * @param current      the current maximum (might be null)
     * @param currentValue the current value
     * @param element      the processed element
     * @param elementValue the prefix or suffix value to match
     * @param value        the value to match
     * @param <T>          the type of entry
     * @return new maximum value
     */
    protected static <T extends CatalogEntry> T maxSuffix(final T current, final String currentValue,
                                                          final T element, final String elementValue,
                                                          final String value) {
        if (elementValue == null || !value.endsWith(elementValue)) {
            return current;
        }
        return maxLength(current, currentValue, element, elementValue);
    }

    /**
     * Process max by length element.
     *
     * @param current      the current maximum (might be null)
     * @param currentValue the current value
     * @param element      the processed element
     * @param elementValue the prefix or suffix value to match
     * @param <T>          the type of entry
     * @return new maximum value
     */
    private static <T extends CatalogEntry> T maxLength(final T current, final String currentValue,
                                                        final T element, final String elementValue) {
        if (current != null && currentValue.length() >= elementValue.length()) {
            return current;
        }
        return element;
    }

    /**
     * Get delegates from map.
     *
     * @param delegates the collection of delegates
     * @return the array of delegate URIs
     */
    public static List<CatalogRequest> getDelegates(final NavigableMap<String, CatalogReferenceEntry> delegates) {
        if (delegates == null || delegates.isEmpty()) {
            return null;
        }
        final ArrayList<CatalogRequest> rc = new ArrayList<CatalogRequest>(delegates.size());
        for (final CatalogReferenceEntry e : delegates.values()) {
            rc.add(catalogRequest(e));
        }
        return rc;
    }

    /**
     * Get catalog request from element.
     *
     * @param element the element to examine
     * @return the catalog request of the catalog
     */
    private static CatalogRequest catalogRequest(final CatalogReferenceEntry element) {
        return element.toCatalogRequest();
    }


    /**
     * Process the catalog file starting with the specified entry.
     *
     * @param entry the entry to use.
     */
    public abstract void resolve(CatalogEntry entry);

    /**
     * @return the resolved delegates.
     */
    public final List<CatalogRequest> getResolvedDelegates() {
        return resolvedDelegates;
    }

    /**
     * Set resolved delegates.
     *
     * @param resolvedDelegates the resolved delegates
     */
    protected final void setResolvedDelegates(final List<CatalogRequest> resolvedDelegates) {
        this.resolvedDelegates = resolvedDelegates;
    }

    /**
     * @return the resolved element.
     */
    public final CatalogEntry getResolvedEntry() {
        return resolvedEntry;
    }

    /**
     * @return the resolved URI
     */
    public final URI getResolvedUri() {
        return resolvedUri;
    }

    /**
     * Set resolved element.
     *
     * @param entry the resolved entry
     * @param uri   the URI
     */
    protected final void setResolved(final CatalogEntry entry, final URI uri) {
        this.resolvedEntry = entry;
        this.resolvedUri = uri;
    }
}
