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

package net.sf.etl.xml_catalog.impl;

import net.sf.etl.xml_catalog.blocking.BlockingCatalog;
import net.sf.etl.xml_catalog.event.CatalogRequest;
import net.sf.etl.xml_catalog.event.engine.impl.step.EntityResolutionStep;
import net.sf.etl.xml_catalog.event.engine.impl.step.TR9401ResolutionStep;
import net.sf.etl.xml_catalog.event.engine.impl.step.URIResolutionStep;
import net.sf.etl.xml_catalog.event.entries.DomParser;
import net.sf.etl.xml_catalog.event.entries.tr9401.DTDDeclEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.DoctypeEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.DocumentEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.EntityEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.LinkTypeEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.NotationEntry;
import net.sf.etl.xml_catalog.event.entries.tr9401.SGMLDeclEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.net.URI;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test entity resolution process. Note it checks only operations that are in scope of single catalog file.
 */
public class ResolutionStepTest {
    /**
     * Test catalog URI
     */
    private static URI catalog;
    /**
     * Test catalog document
     */
    private static Document document;

    /**
     * Initialize catalog
     *
     * @throws Exception if parsing failed
     */
    @BeforeAll
    public static void startUp() throws Exception {
        catalog = ResolutionStepTest.class.getResource("test_data/entities-catalog.xml").toURI();
        document = parseCatalog(catalog);
    }

    /**
     * Check URI
     *
     * @param document the document to use
     * @param expected the expected URI
     * @param uri      the URI to resolve
     * @param nature   the nature to use
     * @param purpose  the purpose to use
     */
    private static void checkUri(final Document document, final URI expected, final String uri,
                                 final String nature, final String purpose) {
        final URIResolutionStep resolution = doResolve(document, uri, nature, purpose);
        assertEquals(expected, resolution.getResolvedUri());
    }

    /**
     * Do resolution
     *
     * @param document the document to use
     * @param uri      the URI to resolve
     * @param nature   the nature to use
     * @param purpose  the purpose to use
     * @return resolution result
     */
    private static URIResolutionStep doResolve(final Document document, final String uri, final String nature,
                                               final String purpose) {
        final URIResolutionStep resolution = new URIResolutionStep(uri, nature, purpose);
        resolution.resolve(DomParser.parse(document));
        return resolution;
    }

    /**
     * Parse catalog
     *
     * @param catalogUri the URI of catalog parse
     * @return the catalog document
     * @throws Exception if parsing fails
     */
    private static Document parseCatalog(final URI catalogUri) throws Exception {
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = schemaFactory.newSchema(BlockingCatalog.class.getResource(
                "/net/sf/etl/xml_catalog/grammars/oasis_xml_catalogs_1_1/catalog.xsd"));
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setSchema(schema);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(catalogUri.toString());
    }

    /**
     * Test bootstrap catalog with catalog grammars
     *
     * @throws Exception other way to fail it
     */
    @Test
    public void testBootstrapCatalogs() throws Exception { // NOPMD
        final URI bootStrapUri = BlockingCatalog.class.getResource(
                "/net/sf/etl/xml_catalog/grammars/catalog.xml").toURI();
        final Document bootStrapDocument = parseCatalog(bootStrapUri);
        checkSystemPublic(bootStrapDocument,
                null,
                "http://www.oasis-open.org/committees/entity/release/1.1/catalog.dtd",
                bootStrapUri.resolve("oasis_xml_catalogs_1_1/catalog.dtd"));
        checkSystemPublic(bootStrapDocument,
                "-//OASIS//DTD XML Catalogs V1.1//EN",
                "http://www.oasis-open.org/committees/entity/release/1.1/catalog.dtd",
                bootStrapUri.resolve("oasis_xml_catalogs_1_1/catalog.dtd"));
        checkSystemPublic(bootStrapDocument,
                "-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN",
                "http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd",
                bootStrapUri.resolve("oasis_xml_catalogs_1_0/catalog.dtd"));
        checkEntity(bootStrapUri.resolve("oasis_xml_catalogs_1_1/catalog.xsd"), bootStrapDocument, null,
                "http://www.oasis-open.org/committees/entity/release/1.1/catalog.xsd");
        checkUri(bootStrapDocument,
                bootStrapUri.resolve("oasis_xml_catalogs_1_1/catalog.xsd"),
                "urn:oasis:names:tc:entity:xmlns:xml:catalog",
                null,
                null);
        checkUri(bootStrapDocument,
                bootStrapUri.resolve("oasis_xml_catalogs_1_1/catalog.xsd"),
                "urn:oasis:names:tc:entity:xmlns:xml:catalog",
                null,
                "http://www.rddl.org/purposes#schema-validation");
        checkUri(bootStrapDocument,
                bootStrapUri.resolve("oasis_xml_catalogs_1_1/catalog.xsd"),
                "urn:oasis:names:tc:entity:xmlns:xml:catalog",
                "http://www.w3.org/2001/XMLSchema",
                null);
        checkUri(bootStrapDocument,
                bootStrapUri.resolve("oasis_xml_catalogs_1_1/catalog.xsd"),
                "urn:oasis:names:tc:entity:xmlns:xml:catalog",
                "http://www.w3.org/2001/XMLSchema",
                "http://www.rddl.org/purposes#schema-validation");
        checkUri(bootStrapDocument,
                bootStrapUri.resolve("oasis_xml_catalogs_1_1/catalog.dtd"),
                "urn:oasis:names:tc:entity:xmlns:xml:catalog",
                "http://www.isi.edu/in-notes/iana/assignments/media-types/application/xml-dtd",
                "http://www.rddl.org/purposes#validation");
        checkUri(bootStrapDocument,
                bootStrapUri.resolve("oasis_xml_catalogs_1_1/catalog.dtd"),
                "urn:oasis:names:tc:entity:xmlns:xml:catalog",
                "http://www.isi.edu/in-notes/iana/assignments/media-types/application/xml-dtd",
                null);
        checkUri(bootStrapDocument,
                bootStrapUri.resolve("oasis_xml_catalogs_1_1/catalog.dtd"),
                "urn:oasis:names:tc:entity:xmlns:xml:catalog",
                null,
                "http://www.rddl.org/purposes#validation");
        checkUri(bootStrapDocument,
                bootStrapUri.resolve("oasis_xml_catalogs_1_1/catalog.rng"),
                "urn:oasis:names:tc:entity:xmlns:xml:catalog",
                "http://relaxng.org/ns/structure/1.0",
                null);
        checkUri(bootStrapDocument,
                bootStrapUri.resolve("oasis_xml_catalogs_1_1/catalog.rng"),
                "urn:oasis:names:tc:entity:xmlns:xml:catalog",
                "http://relaxng.org/ns/structure/1.0",
                "http://www.rddl.org/purposes#schema-validation");
    }

    /**
     * Test delegate public
     *
     * @throws Exception other way to fail it
     */
    @Test
    public void testDelegatePublic() throws Exception {
        // resolution should find a some public delegates
        EntityResolutionStep resolution = doResolve(document,
                "-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN", null, true);
        checkPdResolve(resolution);
        // resolution when prefer=system should fail
        resolution = doResolve(document, "-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN", "someId.xml", true);
        checkPdResolve(resolution);
        // resolution when prefer=system should fail
        resolution = doResolve(document, "-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN", null, false);
        checkPdResolve(resolution);
        // resolution when prefer=system should fail
        resolution = doResolve(document, "-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN", "someId.xml", false);
        assertNull(resolution.getResolvedUri());
        assertNull(resolution.getResolvedDelegates());
    }

    /**
     * Test delegate system
     *
     * @throws Exception if there is problem with URI
     */
    @Test
    public void testDelegateSystem() throws Exception {
        // resolution should find a some public delegates
        EntityResolutionStep resolution = doResolve(document, null,
                "http://www.oasis-open.org/committees/entity/release/1.1/catalog.xsd", true);
        assertNull(resolution.getResolvedUri());
        assertEquals(Arrays.asList(
                catalogRequest("pd-catalog1.xml"),
                catalogRequest("pd-catalog5.xml"),
                catalogRequest("pd-catalog2.xml")
        ), resolution.getResolvedDelegates());
        // resolution should find a some public delegates
        resolution = doResolve(document, null, "http://www.oasis-open.org/committees/index.html", true);
        assertNull(resolution.getResolvedUri());
        assertEquals(Arrays.asList(catalogRequest("pd-catalog2.xml")), resolution.getResolvedDelegates());
    }

    private CatalogRequest catalogRequest(final String relative) {
        return new CatalogRequest(relative(relative));
    }

    /**
     * Test delegate system
     *
     * @throws Exception if there is problem with URI
     */
    @Test
    public void testDelegateUri() throws Exception { // NOPMD
        // resolution should find a some public delegates
        checkUriDelegate("https://www.oasis-open.org/committees/entity/release/1.1/catalog.xsd", null, null, new CatalogRequest[]{
                catalogRequest("pd-catalog1.xml"),
                catalogRequest("pd-catalog5.xml"),
                catalogRequest("pd-catalog2.xml"),
        });
        checkUriDelegate("https://www.oasis-open.org/committees/entity/release/1.1/catalog.xsd", "n:1", null, new CatalogRequest[]{
                catalogRequest("pd-catalog2.xml"),
        });
        checkUriDelegate("https://www.oasis-open.org/committees/entity/release/1.1/catalog.xsd", null, "p:1", new CatalogRequest[]{
                catalogRequest("pd-catalog5.xml"),
                catalogRequest("pd-catalog2.xml"),
        });
        // resolution should find a some public delegates
        checkUriDelegate("https://www.oasis-open.org/committees/index.html", null, null, new CatalogRequest[]{
                catalogRequest("pd-catalog2.xml"),
        });
        checkUriDelegate("https://www.oasis-open.org/committees/index.html", "n:1", "p:1", new CatalogRequest[]{
                catalogRequest("pd-catalog2.xml"),
        });
        checkUriDelegate("https://www.oasis-open.org/committees/index.html", "n:1", null, new CatalogRequest[]{
                catalogRequest("pd-catalog2.xml"),
        });
        checkUriDelegate("https://www.oasis-open.org/committees/index.html", null, "p:2", new CatalogRequest[]{
                catalogRequest("pd-catalog4.xml"),
        });
    }

    /**
     * Test public and system identifiers
     *
     * @throws Exception if there URI problem
     */
    @Test
    public void testPublicSystem() throws Exception { // NOPMD
        final String pi = "-//Example//Future 1.0//EN";
        final String si = "http://example.com/1.0/future.xsd";
        final URI s = relative("base-s/future-s.xsd");
        final URI p = relative("base-p/future-p.xsd");
        checkEntity(s, document, pi, si, false);
        checkEntity(s, document, pi, si, true);
        checkEntity(p, document, pi, null, true);
        checkEntity(p, document, pi, null, false);
        checkEntity(p, document, pi, "someId.xsd", true);
        checkEntity(null, document, pi, "someId.xsd", false);
        checkEntity(s, document, null, si, false);
        checkEntity(s, document, null, si, true);
        checkEntity(null, document, null, "someId.xsd", true);
        checkEntity(null, document, null, "someId.xsd", false);
    }

    /**
     * Test suffix/prefix rewrites
     */
    @Test
    public void testRewrite() { // NOPMD
        checkEntity(relative("1/t.xml"), document, "-//Example//Future 1.0//EN", "http://example.com/1.0/r/t.xml", true);
        checkEntity(null, document, "-//Example//Future 1.0//EN", "http://example.com/1.0/x/t.xml", false);
        checkEntity(relative("base-p/future-p.xsd"), document,
                "-//Example//Future 1.0//EN", "http://example.com/1.0/x/t.xml", true);
        checkEntity(relative("1/t.xml"), document, null, "http://example.com/1.0/r/t.xml", false);
        checkEntity(relative("2/t.xml"), document, null, "http://example.com/1.0/r/x/t.xml", false);
        checkEntity(relative("4/t.xml"), document, null, "http://example.com/1.0/r/x/y/t.xml", false);
        checkEntity(relative("2/tx.xml"), document, null, "http://example.com/1.0/r/x/tx.xml", false);
        checkEntity(relative("s/2/tx.xml"), document, null, "http://example.com/1.1/r/x/tx.xml", false);
        checkEntity(relative("s/1/tx.xml"), document, null, "http://example.com/1.1/r/tx.xml", false);
        checkEntity(relative("s/4/tx.xml"), document, null, "http://example.com/1.1/q/tx.xml", false);
    }

    /**
     * Test TR9401 catalog elements
     */
    @Test
    public void testTR9401() { // NOPMD
        checkTR9401(relative("future-1.dtd"), true, true, DTDDeclEntry.class, "-//Example//Future 1.x//EN");
        checkTR9401(relative("future-1.dtd"), false, true, DTDDeclEntry.class, "-//Example//Future 1.x//EN");
        checkTR9401(relative("future-1.dtd"), false, true, DTDDeclEntry.class, "-//Example//Future 1.x//EN");
        checkTR9401(relative("future-3.dtd"), true, false, DTDDeclEntry.class, "-//Example//Future 1.x//EN");
        checkTR9401(relative("future-t.dtd"), true, true, DTDDeclEntry.class, "-//Example//Future 1.x.t//EN");
        checkTR9401(null, true, false, DTDDeclEntry.class, "-//Example//Future 1.x.t//EN");

        checkTR9401(relative("n1.txt"), true, true, NotationEntry.class, "notation-1");
        checkTR9401(relative("e-0.txt"), true, true, EntityEntry.class, "entity-0");
        checkTR9401(relative("dt-0.txt"), true, true, DoctypeEntry.class, "doctype-0");
        checkTR9401(relative("lt-0.txt"), true, true, LinkTypeEntry.class, "linktype-0");

        checkTR9401(relative("future-t.dtd"), true, true, DTDDeclEntry.class, "-//Example//Future 1.x.t//EN");

        checkTR9401(relative("document-1.txt"), true, true, DocumentEntry.class, null);
        checkTR9401(relative("sgmldecl.dtd"), true, true, SGMLDeclEntry.class, null);
    }

    /**
     * Test URI resolution
     */
    @Test
    public void testURI() { // NOPMD
        checkUri(relative("base-u/future-u.xsd"), "http://sample.com/1.0/future.xsd", null, null);
        checkUri(null, "http://x-sample.com/1.0/future.xsd", null, null);
        // rewrite tests
        checkUri(relative("1/t.xml"), "http://sample.com/1.0/r/t.xml", null, null);
        checkUri(relative("2/t.xml"), "http://sample.com/1.0/r/x/t.xml", null, null);
        checkUri(relative("4/t.xml"), "http://sample.com/1.0/r/x/y/t.xml", null, null);
        checkUri(relative("2/tx.xml"), "http://sample.com/1.0/r/x/tx.xml", null, null);
        checkUri(relative("s/2/tx.xml"), "http://sample.com/1.1/r/x/tx.xml", null, null);
        checkUri(relative("s/1/tx.xml"), "http://sample.com/1.1/r/tx.xml", null, null);
        checkUri(relative("s/4/tx.xml"), "http://sample.com/1.1/q/tx.xml", null, null);
        // rewrite tests with natures and purposes
        checkUri(relative("2/tx.xml"), "http://sample.com/1.0/r/x/tx.xml", "n:1", "p:1");
        checkUri(relative("3/tx.xml"), "http://sample.com/1.0/r/x/tx.xml", "n:1", "p:2");
        checkUri(relative("s/4/tx.xml"), "http://sample.com/1.0/r/x/tx.xml", null, "p:3");
    }

    /**
     * Check URI
     *
     * @param expected the expected URI
     * @param uri      the URI to resolve
     * @param nature   the nature to use
     * @param purpose  the purpose to use
     */
    private void checkUri(final URI expected, final String uri, final String nature, final String purpose) {
        checkUri(document, expected, uri, nature, purpose);
    }

    /**
     * Check delegation of URIs
     *
     * @param uri      the URI to check
     * @param nature   the nature
     * @param purpose  the purpose
     * @param expected the expected result
     */
    private void checkUriDelegate(final String uri, final String nature, final String purpose,
                                  final CatalogRequest[] expected) {
        final URIResolutionStep resolution = doResolve(document, uri, nature, purpose);
        assertNull(resolution.getResolvedUri());
        assertEquals(Arrays.asList(expected), resolution.getResolvedDelegates());
    }

    /**
     * Check resolution for TR9401 definitions
     *
     * @param expected         the expected URI
     * @param mustPreferPublic resolve only if prefer public is specified
     * @param preferPublic     the initial value of prefer public
     * @param type             the type to resolve
     * @param name             the name or publicId to resolve
     */
    private void checkTR9401(final URI expected, final boolean mustPreferPublic, final boolean preferPublic,
                             final Class<?> type, final String name) {
        final TR9401ResolutionStep resolution = new TR9401ResolutionStep(type, name, mustPreferPublic, preferPublic);
        resolution.resolve(DomParser.parse(document));
        assertEquals(expected, resolution.getResolvedUri());
    }

    /**
     * Calculate relative path with respect to {@link #catalog}
     *
     * @param path the path to evaluate
     * @return the resulting URI
     */
    private URI relative(final String path) {
        return catalog.resolve(path);
    }

    /**
     * Check if expected public delegates are found. Used only for {@link }
     *
     * @param resolution the resolution process
     */
    private void checkPdResolve(final EntityResolutionStep resolution) {
        assertNull(resolution.getResolvedUri());
        assertNull(resolution.getSystemId());
        assertEquals(Arrays.asList(
                catalogRequest("pd-catalog1.xml"),
                catalogRequest("pd-catalog5.xml"),
                catalogRequest("pd-catalog2.xml")
        ), resolution.getResolvedDelegates());
    }

    /**
     * Preform resolution over the document
     *
     * @param document     the document to use
     * @param publicId     the public identifier
     * @param systemId     the system identifier
     * @param preferPublic true, if public identifiers are preferred
     * @return the resolution object
     */
    private EntityResolutionStep doResolve(final Document document, final String publicId, final String systemId,
                                           final boolean preferPublic) {
        final EntityResolutionStep resolution = new EntityResolutionStep(publicId, systemId, preferPublic);
        resolution.resolve(DomParser.parse(document));
        return resolution;
    }

    /**
     * Check if catalog reacts on system and public identifiers
     *
     * @param document document to check
     * @param publicId public id
     * @param systemId system id
     * @param expected the expected resulting system id
     */
    private void checkSystemPublic(final Document document, final String publicId, final String systemId,
                                   final URI expected) {
        checkEntity(expected, document, publicId, systemId);
        if (publicId != null) {
            checkEntity(expected, document, publicId, null);
        }
        if (systemId != null) {
            checkEntity(expected, document, null, systemId);
        }
    }

    /**
     * Check if catalog resolves public and system id in expected ways
     *
     * @param expected the expected resulting system id
     * @param document document to check
     * @param publicId public id
     * @param systemId system id
     */
    private void checkEntity(final URI expected, final Document document,
                             final String publicId, final String systemId) {
        checkEntity(expected, document, publicId, systemId, true);
    }

    /**
     * Check if catalog resolves public and system id in expected ways
     *
     * @param expected     the expected resulting system id
     * @param document     document to check
     * @param publicId     public id
     * @param systemId     system id
     * @param preferPublic the prefer public flag
     */
    private void checkEntity(final URI expected, final Document document, final String publicId,
                             final String systemId, final boolean preferPublic) {
        assertEquals(expected, resolveEntity(document, publicId, systemId, preferPublic));
    }

    /**
     * Resolve URI in catalog
     *
     * @param document     document to check
     * @param publicId     public id
     * @param systemId     system id
     * @param preferPublic the prefer public flag
     * @return resolved id
     */
    private URI resolveEntity(final Document document, final String publicId, final String systemId,
                              final boolean preferPublic) {
        final EntityResolutionStep resolution = new EntityResolutionStep(publicId, systemId, preferPublic);
        resolution.resolve(DomParser.parse(document));
        return resolution.getResolvedUri();
    }
}
