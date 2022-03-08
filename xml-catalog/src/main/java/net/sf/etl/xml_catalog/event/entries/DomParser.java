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

import net.sf.etl.xml_catalog.event.engine.impl.util.DomUtil;
import net.sf.etl.xml_catalog.event.entries.tr9401.DTDDeclEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.DoctypeEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.DocumentEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.EntityEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.LinkTypeEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.NotationEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.SGMLDeclEntry;
import net.sf.etl.xml_catalog.util.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URI;
import java.util.ArrayList;

/**
 * The DOM parser for the catalog entries.
 */
public final class DomParser {
    /**
     * Private constructor for utility class.
     */
    private DomParser() {
        // do nothing
    }

    /**
     * Parse the document.
     *
     * @param document the document
     * @return the parsed entries
     */
    public static GroupEntry parse(final Document document) {
        final Element root = document.getDocumentElement();
        if (!DomUtil.isCatalogNamespace(root.getNamespaceURI()) || !"catalog".equals(root.getLocalName())) {
            throw new IllegalArgumentException("Wrong root element " + root.getTagName());
        }
        return (GroupEntry) parseElement(root);
    }

    /**
     * Parse the element.
     *
     * @param element the element to parse
     * @return the catalog entry
     */
    private static CatalogEntry parseElement(final Element element) { // NOPMD
        final String name = element.getLocalName();
        final String namespace = element.getNamespaceURI();
        final URI base = URI.create(element.getBaseURI());
        final String id = DomUtil.attribute(element, "id");
        if (DomUtil.isCatalogNamespace(namespace)) {
            if ("catalog".equals(name) || "group".equals(name)) {
                final String prefer = DomUtil.attribute(element, "prefer");
                final Boolean preferPublic = prefer == null ? null : "public".equals(prefer);
                final ArrayList<CatalogEntry> entries = new ArrayList<CatalogEntry>();
                final NodeList childNodes = element.getChildNodes();
                final int n = childNodes.getLength();
                for (int i = 0; i < n; i++) {
                    final Node item = childNodes.item(i);
                    if (item instanceof Element) {
                        final CatalogEntry entry = parseElement((Element) item);
                        if (entry != null) { // NOPMD
                            entries.add(entry);
                        }
                    }
                }
                return new GroupEntry(id, base, entries, preferPublic);
            } else if ("public".equals(name)) {
                return new PublicEntry(id, base, uri(element, base), DomUtil.attribute(element, "publicId", true));
            } else if ("system".equals(name)) {
                return new SystemEntry(id, base, uri(element, base), DomUtil.attribute(element, "systemId", true));
            } else if ("rewriteSystem".equals(name)) {
                return new RewriteSystemEntry(id, base, DomUtil.attribute(element, "systemIdStartString", true),
                        DomUtil.attribute(element, "rewritePrefix", true));
            } else if ("systemSuffix".equals(name)) {
                return new SystemSuffixEntry(id, base, uri(element, base),
                        DomUtil.attribute(element, "systemIdSuffix", true));
            } else if ("delegatePublic".equals(name)) {
                return new DelegatePublicEntry(id, base, catalog(element, base),
                        DomUtil.attribute(element, "publicIdStartString", true));
            } else if ("delegateSystem".equals(name)) {
                return new DelegateSystemEntry(id, base, catalog(element, base),
                        DomUtil.attribute(element, "systemIdStartString", true));
            } else if ("uri".equals(name)) {
                return new UriEntry(id, base, uri(element, base), purpose(element), nature(element),
                        name(element));
            } else if ("rewriteURI".equals(name)) {
                return new RewriteUriEntry(id, base, purpose(element), nature(element),
                        DomUtil.attribute(element, "uriStartString", true),
                        DomUtil.attribute(element, "rewritePrefix", true));
            } else if ("uriSuffix".equals(name)) {
                return new UriSuffixEntry(id, base, uri(element, base), purpose(element), nature(element),
                        DomUtil.attribute(element, "uriSuffix", true));
            } else if ("delegateURI".equals(name)) {
                return new DelegateUriEntry(id, base, catalog(element, base), purpose(element), nature(element),
                        DomUtil.attribute(element, "uriStartString", true));
            } else if ("nextCatalog".equals(name)) {
                return new NextCatalogEntry(id, base, catalog(element, base));
            }
        }
        if (DomUtil.isTR9401Namespace(namespace)) {
            if ("doctype".equals(name)) {
                return new DoctypeEntry(id, base, uri(element, base), name(element));
            } else if ("document".equals(name)) {
                return new DocumentEntry(id, base, uri(element, base));
            } else if ("dtddecl".equals(name)) {
                return new DTDDeclEntry(id, base, uri(element, base), DomUtil.attribute(element, "publicId", true));
            } else if ("entity".equals(name)) {
                return new EntityEntry(id, base, uri(element, base), name(element));
            } else if ("linktype".equals(name)) {
                return new LinkTypeEntry(id, base, uri(element, base), name(element));
            } else if ("notation".equals(name)) {
                return new NotationEntry(id, base, uri(element, base), name(element));
            } else if ("sgmldecl".equals(name)) {
                return new SGMLDeclEntry(id, base, uri(element, base));
            }
        }
        return null;
    }

    /**
     * Get name from the element.
     *
     * @param element the element
     * @return the name
     */
    private static String name(final Element element) {
        return DomUtil.attribute(element, "name");
    }

    /**
     * Get purpose of the element.
     *
     * @param element the element
     * @return the purpose
     */
    private static String purpose(final Element element) {
        return DomUtil.attributeNS(element, Namespaces.RDDL, "purpose", true);
    }

    /**
     * Get nature of element.
     *
     * @param element the element
     * @return the nature
     */
    private static String nature(final Element element) {
        return DomUtil.attributeNS(element, Namespaces.RDDL, "nature", true);
    }

    /**
     * Get uri to which this element is pointing.
     *
     * @param element the element
     * @param base    the base
     * @return the URI
     */
    private static URI uri(final Element element, final URI base) {
        final String uri = DomUtil.attribute(element, "uri", true);
        return base.resolve(uri);
    }

    /**
     * Get catalog to which this element is pointing.
     *
     * @param element the element
     * @param base    the base
     * @return the result URI
     */
    private static URI catalog(final Element element, final URI base) {
        final String uri = DomUtil.attribute(element, "catalog", true);
        return base.resolve(uri);
    }
}
