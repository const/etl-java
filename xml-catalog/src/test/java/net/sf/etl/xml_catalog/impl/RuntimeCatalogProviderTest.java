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
import net.sf.etl.xml_catalog.blocking.provider.CatalogProvider;
import net.sf.etl.xml_catalog.blocking.provider.CatalogRuntimeProvider;
import net.sf.etl.xml_catalog.event.CatalogFile;
import net.sf.etl.xml_catalog.event.CatalogRequest;
import net.sf.etl.xml_catalog.event.CatalogResolutionEvent;
import net.sf.etl.xml_catalog.event.CatalogResult;
import net.sf.etl.xml_catalog.event.CatalogResultTrace;
import net.sf.etl.xml_catalog.event.DefaultCatalogContext;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URL;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The test for runtime catalog provider
 */
public class RuntimeCatalogProviderTest {
    /**
     * The core catalog to process
     */
    public final static URI CORE_CATALOG;

    static {
        try {
            CORE_CATALOG = BlockingCatalog.class.getResource("/net/sf/etl/xml_catalog/grammars/catalog.xml").toURI();
        } catch (Exception e) { // NOPMD
            throw new IllegalStateException("Missing resource", e);
        }
    }

    /**
     * Get catalog file for test
     *
     * @param provider the catalog provider
     * @param request  the catalog requests
     * @return the catalog file
     * @throws Throwable in case of loading problem
     */
    public static CatalogFile getCatalogFile(final CatalogProvider provider, final CatalogRequest request)
            throws Throwable {
        final CatalogResolutionEvent event = provider.getCatalog(DefaultCatalogContext.INSTANCE, request);
        //noinspection ThrowableResultOfMethodCallIgnored
        if (event.getProblem() != null) {
            throw event.getProblem();
        }
        return event.getFile();
    }

    /**
     * The test for classpath catalogs
     */
    @Test
    public void testClassPath() throws Throwable {
        final CatalogRuntimeProvider provider = new CatalogRuntimeProvider();
        // create classpath catalog
        CatalogRequest originalClassPathRequest = provider.registerClasspathCatalog(RuntimeCatalogProviderTest.class);
        final CatalogRequest classpathRequest = new CatalogRequest(originalClassPathRequest.getSystemId());
        final CatalogFile catalogFile = getCatalogFile(provider, originalClassPathRequest);
        //noinspection UnusedAssignment
        assertEquals(2, catalogFile.getNextCatalogs().size(), "Catalogs: " + catalogFile.getNextCatalogs());

        // check public id resolution
        final String expected = CORE_CATALOG.resolve("oasis_xml_catalogs_1_1/catalog.dtd").toString();
        final String systemId = "http://www.oasis-open.org/committees/entity/release/1.1/catalog.dtd";
        final String publicId = "-//OASIS//DTD XML Catalogs V1.1//EN";
        final BlockingCatalog classPathCatalog = new BlockingCatalog(classpathRequest, provider);
        final CatalogResult catalogResult = classPathCatalog.resolveEntity(publicId, systemId, null);
        assertEquals(expected, catalogResult.getResolution(), catalogResult.toDebugString());
        final String publicIdAsSystemId = "urn:publicid:-:OASIS:DTD+XML+Catalogs+V1.1:EN";
        final CatalogResult catalogResult2 = classPathCatalog.resolveEntity(null, publicIdAsSystemId, null);
        assertEquals(expected, catalogResult2.getResolution(), catalogResult.toDebugString());
        final CatalogResult catalogResult3 = classPathCatalog.resolveEntity(publicId, publicIdAsSystemId + "X", null);
        assertEquals(expected, catalogResult3.getResolution(), catalogResult3.toDebugString());
        // create vector catalog
        final Vector<URI> newScope = new Vector<URI>(); // NOPMD
        final CatalogRequest vectorRequest = provider.registerVectorCatalog("x-vector:", true, newScope);
        final BlockingCatalog vectorCatalog = new BlockingCatalog(vectorRequest, provider);
        final URL testResource = getClass().getClassLoader().getResource("net/sf/etl/xml_catalog/impl/test_data/entities-catalog.xml");
        assertNotNull(testResource);
        newScope.add(new URI(classpathRequest.getSystemId()));
        newScope.add(testResource.toURI());
        // check URI delegate resolution
        final CatalogResult uriResult = vectorCatalog.resolveUri("https://www.oasis-open.org/committees/index.html", "n:1", "p:1", null);
        assertNull(uriResult.getResolution());
        final CatalogResultTrace trace = uriResult.getTrace();
        assertNotNull(trace);
        assertTrue(trace.getCatalogRequest().getSystemId().endsWith("/pd-catalog2.xml"), uriResult.toDebugString());
        // forget catalog to enable garbage collection
        originalClassPathRequest.getSystemId();
        //noinspection UnusedAssignment
        originalClassPathRequest = null;
        System.gc(); // NOPMD
        try {
            getCatalogFile(provider, classpathRequest);
            fail("Catalog should not resolve!"); // NOPMD
        } catch (java.net.MalformedURLException url) { // NOPMD
            // ok
        }
    }
}
