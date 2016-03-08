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

package net.sf.etl.xml_catalog.event.engine.impl.step;

import net.sf.etl.xml_catalog.event.entries.CatalogEntry;
import net.sf.etl.xml_catalog.event.entries.CatalogReferenceEntry;
import net.sf.etl.xml_catalog.event.entries.DelegateUriEntry;
import net.sf.etl.xml_catalog.event.entries.GroupEntry;
import net.sf.etl.xml_catalog.event.entries.RewriteUriEntry;
import net.sf.etl.xml_catalog.event.entries.UriEntry;
import net.sf.etl.xml_catalog.event.entries.UriResolutionEntry;
import net.sf.etl.xml_catalog.event.entries.UriSuffixEntry;

import java.util.NavigableMap;

/**
 * This class collects information used for URI resolution process for one catalog.
 * It actually does almost the same as entity resolution process when systemId is specified.
 */
public final class URIResolutionStep extends ResolutionStep {
    /**
     * The URI to resolve.
     */
    private final String uri;
    /**
     * The URI nature.
     */
    private final String nature;
    /**
     * The URI purpose.
     */
    private final String purpose;
    /**
     * The resolved entry.
     */
    private UriEntry resolved;
    /**
     * Delegates.
     */
    private NavigableMap<String, CatalogReferenceEntry> delegates;
    /**
     * The rewrite URI entry.
     */
    private RewriteUriEntry rewrite;
    /**
     * Resolved suffix entry.
     */
    private UriSuffixEntry suffix;

    /**
     * The constructor.
     *
     * @param uri     the URI to resolve
     * @param nature  the nature
     * @param purpose the purpose
     */
    public URIResolutionStep(final String uri, final String nature, final String purpose) {
        this.uri = uri;
        this.nature = nature;
        this.purpose = purpose;
    }

    /**
     * Check if nature or purpose uri match the requested.
     *
     * @param requestedValue the value from the request
     * @param entryValue     the value from entry
     * @return true if match
     */
    private static boolean match(final String requestedValue, final String entryValue) {
        return requestedValue == null || requestedValue.equals(entryValue);
    }

    /**
     * Process the catalog file starting with the specified entry.
     *
     * @param entry the entry to use.
     */
    @Override
    public void resolve(final CatalogEntry entry) {
        process(entry);
        if (resolved != null) {
            setResolved(resolved, resolved.getUri());
            return;
        }
        if (rewrite != null) {
            final String uriSuffix = uri.substring(rewrite.getUriStartString().length());
            setResolved(rewrite, rewrite.getBase().resolve(rewrite.getRewritePrefix() + uriSuffix));
            return;
        }
        if (suffix != null) {
            setResolved(suffix, suffix.getUri());
            return;
        }
        if (delegates != null && !delegates.isEmpty()) {
            setResolvedDelegates(getDelegates(delegates));
        }
    }

    /**
     * Process the entry.
     *
     * @param entry the entry
     * @return the processed entry
     */
    private boolean process(final CatalogEntry entry) { // NOPMD
        if (entry instanceof GroupEntry) {
            for (final CatalogEntry catalogEntry : ((GroupEntry) entry).getEntries()) {
                if (process(catalogEntry)) {
                    return true;
                }
            }
            return false;
        }
        if (!(entry instanceof UriResolutionEntry)) {
            return false;
        }
        final UriResolutionEntry uriEntry = (UriResolutionEntry) entry;
        if (!match(nature, uriEntry.getNature()) || !match(purpose, uriEntry.getPurpose())) {
            return false;
        }
        if (entry instanceof UriEntry) {
            final UriEntry e = (UriEntry) entry;
            if (uri.equals(e.getName())) {
                resolved = e;
                return true;
            }
        } else if (entry instanceof DelegateUriEntry) {
            final DelegateUriEntry e = (DelegateUriEntry) entry;
            delegates = delegate(delegates, e, e.getUriStartString(), uri);
        } else if (entry instanceof RewriteUriEntry) {
            final RewriteUriEntry e = (RewriteUriEntry) entry;
            rewrite = maxPrefix(rewrite, rewrite == null ? null : rewrite.getUriStartString(),
                    e, e.getUriStartString(), uri);
        } else if (entry instanceof UriSuffixEntry) {
            final UriSuffixEntry e = (UriSuffixEntry) entry;
            suffix = maxSuffix(suffix, suffix == null ? null : suffix.getUriSuffix(), e, e.getUriSuffix(), uri);
        }
        return false;
    }
}
