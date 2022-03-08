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

package net.sf.etl.xml_catalog.event.engine.impl.util;

import net.sf.etl.xml_catalog.event.CatalogRequest;
import net.sf.etl.xml_catalog.util.Namespaces;
import net.sf.etl.xml_catalog.util.URIUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * DOM utilities.
 */
public final class DomUtil {
    /**
     * Private constructor for utility class.
     */
    private DomUtil() {
        // do nothing
    }

    /**
     * Get relative UR from the attribute.
     *
     * @param element the element to examine
     * @param name    the name of attribute
     * @return the relative URI.
     */
    public static String relativeUri(final Element element, final String name) {
        final String a = attribute(element, name, true);
        if (a == null) {
            return null;
        }
        final String uriText = element.getBaseURI();
        return URIUtil.normalizeUri(uriText, a);
    }

    /**
     * Get catalog request from element.
     *
     * @param element the element to examine
     * @param name    the name of attribute
     * @return the catalog request of the catalog
     */
    public static CatalogRequest catalogRequest(final Element element, final String name) {
        final String id = attribute(element, name, false);
        final String resolved = relativeUri(element, name);
        return new CatalogRequest(resolved, id);
    }


    /**
     * Get attribute value.
     *
     * @param element the element to examine
     * @param name    the name of attribute
     * @return the value of attribute (it is trimmed, and empty value is the same as attribute is not specified)
     */
    public static String attribute(final Element element, final String name) {
        return attribute(element, name, false);
    }

    /**
     * Get attribute value.
     *
     * @param element    the element to examine
     * @param name       the name of attribute
     * @param allowEmpty allow empty attribute value (if true, and empty value is
     *                   the same as attribute is not specified)
     * @return the value of attribute
     */
    public static String attribute(final Element element, final String name, final boolean allowEmpty) {
        if (!element.hasAttribute(name)) {
            return null;
        }
        String v = element.getAttribute(name);
        v = v.trim();
        if (!allowEmpty && v.length() == 0) {
            return null;
        }
        return v;
    }

    /**
     * Get attribute value with namespace.
     *
     * @param element    the element to examine
     * @param namespace  the namespace of attribute
     * @param allowEmpty allow empty attribute value (if true, and empty value is the same
     *                   as attribute is not specified)
     * @param name       the name of attribute
     * @return the value of attribute (it is trimmed, and empty value is the same as attribute is not specified)
     */
    public static String attributeNS(final Element element, final String namespace, final String name,
                                     final boolean allowEmpty) {
        if (!element.hasAttributeNS(namespace, name)) {
            return null;
        }
        String v = element.getAttributeNS(namespace, name);
        v = v.trim();
        if (!allowEmpty && v.length() == 0) {
            return null;
        }
        return v;
    }

    /**
     * Check whether node is an XML element from oasis catalog or TR9401 namespaces. And if yes, return it,
     * otherwise return null. This method to could be used to find only elements relevant to resolution process.
     *
     * @param node        the node to check
     * @param allowTR9401 if elements from TR9401 namespace are allowed
     * @return if node is an Element and has catalog namespace, return it
     */
    public static Element asCatalogElement(final Node node, final boolean allowTR9401) {
        if (!(node instanceof Element)) {
            return null;
        }
        final Element rc = (Element) node;
        final String namespaceURI = rc.getNamespaceURI();
        if (isCatalogNamespace(namespaceURI)
                || allowTR9401 && isTR9401Namespace(namespaceURI)) {
            return rc;
        }
        return null;
    }

    /**
     * Check if namespace is TR9401.
     *
     * @param namespaceURI the namespace URI
     * @return true if namespace is TRI9401 or not specified
     */
    public static boolean isTR9401Namespace(final String namespaceURI) {
        return namespaceURI == null || Namespaces.TR9401.equals(namespaceURI);
    }

    /**
     * Check if namespace is OASIS catalog.
     *
     * @param namespaceURI the namespace URI
     * @return true if namespace is oasis catalog or not specified
     */
    public static boolean isCatalogNamespace(final String namespaceURI) {
        return namespaceURI == null || Namespaces.CATALOG.equals(namespaceURI);
    }
}
