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
import net.sf.etl.xml_catalog.event.entries.DelegatePublicEntry;
import net.sf.etl.xml_catalog.event.entries.DelegateSystemEntry;
import net.sf.etl.xml_catalog.event.entries.GroupEntry;
import net.sf.etl.xml_catalog.event.entries.PublicEntry;
import net.sf.etl.xml_catalog.event.entries.RewriteSystemEntry;
import net.sf.etl.xml_catalog.event.entries.SystemEntry;
import net.sf.etl.xml_catalog.event.entries.SystemSuffixEntry;

import java.util.NavigableMap;


/**
 * This class collects information used for entity resolution process for one catalog.
 */
public final class EntityResolutionStep extends ResolutionStep {
    /**
     * Default value of prefer public.
     */
    private final boolean defaultPreferPublic;
    /**
     * public id argument.
     */
    private String publicId;
    /**
     * system id argument.
     */
    private String systemId;
    /**
     * Resolved public element.
     */
    private PublicEntry resolvedPublic;
    /**
     * Resolved system element.
     */
    private SystemEntry resolvedSystem;
    /**
     * Resolved rewrite system.
     */
    private RewriteSystemEntry rewriteSystem;
    /**
     * Resolved system suffix.
     */
    private SystemSuffixEntry systemSuffix;
    /**
     * Matched delegate public.
     */
    private NavigableMap<String, CatalogReferenceEntry> delegatePublic;
    /**
     * Matched delegate system.
     */
    private NavigableMap<String, CatalogReferenceEntry> delegateSystem;

    /**
     * Constructor for the process.
     *
     * @param publicId     the public identifiers
     * @param systemId     the system identifiers
     * @param preferPublic the default value of prefer public
     */
    public EntityResolutionStep(final String publicId, final String systemId, final boolean preferPublic) {
        this.publicId = publicId;
        this.systemId = systemId;
        this.defaultPreferPublic = preferPublic;
    }

    @Override
    public void resolve(final CatalogEntry entry) {
        process(entry, defaultPreferPublic);
        if (resolvedSystem != null) {
            setResolved(resolvedSystem, resolvedSystem.getUri());
        } else if (rewriteSystem != null) {
            final String suffix = systemId.substring(rewriteSystem.getSystemIdStartString().length());
            setResolved(rewriteSystem, rewriteSystem.getBase().resolve(rewriteSystem.getRewritePrefix() + suffix));
        } else if (systemSuffix != null) {
            setResolved(systemSuffix, systemSuffix.getUri());
        } else if (resolvedPublic != null) {
            setResolved(resolvedPublic, resolvedPublic.getUri());
        }
        if (getResolvedUri() == null) {
            if (delegateSystem != null && !delegateSystem.isEmpty()) {
                publicId = null;
                setResolvedDelegates(getDelegates(delegateSystem));
            } else if (delegatePublic != null && !delegatePublic.isEmpty()) {
                systemId = null;
                setResolvedDelegates(getDelegates(delegatePublic));
            }
        }
    }

    /**
     * @return the public id
     */
    public String getPublicId() {
        return publicId;
    }

    /**
     * @return the system id
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * Process the element (one pass).
     *
     * @param entry        the entry to process
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
        if (systemId != null) {
            if (entry instanceof SystemEntry && systemId.equals(((SystemEntry) entry).getSystemId())) {
                resolvedSystem = (SystemEntry) entry;
                return true;
            } else if (entry instanceof RewriteSystemEntry) {
                final RewriteSystemEntry e = (RewriteSystemEntry) entry;
                rewriteSystem = maxPrefix(
                        rewriteSystem, rewriteSystem == null ? null : rewriteSystem.getSystemIdStartString(),
                        e, e.getSystemIdStartString(), systemId);
            } else if (entry instanceof SystemSuffixEntry) {
                final SystemSuffixEntry e = (SystemSuffixEntry) entry;
                systemSuffix = maxSuffix(systemSuffix, systemSuffix == null ? null : systemSuffix.getSystemIdSuffix(),
                        e, e.getSystemIdSuffix(), systemId);
            } else if (entry instanceof DelegateSystemEntry) {
                final DelegateSystemEntry e = (DelegateSystemEntry) entry;
                delegateSystem = delegate(delegateSystem, e, e.getSystemIdStartString(), systemId);
            }
        }
        if (publicId != null && (preferPublic || systemId == null)) {
            if (entry instanceof PublicEntry) {
                final PublicEntry e = (PublicEntry) entry;
                if (resolvedPublic == null && publicId.equals(e.getPublicId())) {
                    resolvedPublic = e;
                    if (systemId == null) {
                        return true;
                    }
                }
            } else if (entry instanceof DelegatePublicEntry) {
                final DelegatePublicEntry e = (DelegatePublicEntry) entry;
                delegatePublic = delegate(delegatePublic, e, e.getPublicIdStartString(), publicId);
            }
        }
        return false;
    }
}
