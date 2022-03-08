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

package net.sf.etl.xml_catalog.impl;

import net.sf.etl.xml_catalog.blocking.BlockingCatalog;
import net.sf.etl.xml_catalog.blocking.provider.CatalogProvider;
import net.sf.etl.xml_catalog.blocking.provider.CatalogProviders;
import net.sf.etl.xml_catalog.blocking.provider.schema.ClasspathSchemaModule;
import net.sf.etl.xml_catalog.blocking.provider.schema.FileSystemCatalogProviderModule;
import net.sf.etl.xml_catalog.event.CatalogFile;
import net.sf.etl.xml_catalog.event.CatalogRequest;
import net.sf.etl.xml_catalog.event.CatalogResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;


/**
 * The default catalog test
 */
public class DefaultCatalogTest {

    @Test
    public void test() throws Throwable {
        final CatalogProvider catalogProvider = CatalogProviders.createCachedCatalog(
                CatalogProviders.createDefaultCatalogProvider(DefaultCatalogTest.class));
        final CatalogFile catalogFile1 = RuntimeCatalogProviderTest.getCatalogFile(catalogProvider,
                new CatalogRequest(FileSystemCatalogProviderModule.CATALOG_FILE_LIST
                        + ":" + getClass().getResource("/net/sf/etl/xml_catalog/impl/test_data")));
        final CatalogFile catalogFile2 = RuntimeCatalogProviderTest.getCatalogFile(catalogProvider,
                new CatalogRequest(FileSystemCatalogProviderModule.CATALOG_FILE_LIST
                        + ":" + getClass().getResource("/net/sf/etl/xml_catalog/impl/test_data")));
        assertSame(catalogFile1, catalogFile2);
        assertEquals(1, catalogFile1.getNextCatalogs().size());
        final CatalogFile catalogFile3 = RuntimeCatalogProviderTest.getCatalogFile(catalogProvider,
                new CatalogRequest(FileSystemCatalogProviderModule.CATALOG_FILE_LIST
                        + ":" + getClass().getResource("/net/sf/etl/xml_catalog/impl")));
        assertEquals(0, catalogFile3.getNextCatalogs().size());
        final CatalogFile catalogFile4 = RuntimeCatalogProviderTest.getCatalogFile(catalogProvider,
                new CatalogRequest(FileSystemCatalogProviderModule.CATALOG_FILE_LIST_RECURSIVE
                        + ":" + getClass().getResource("/net/sf/etl/xml_catalog/impl")));
        assertEquals(1, catalogFile4.getNextCatalogs().size());
        final CatalogFile catalogFile5 = RuntimeCatalogProviderTest.getCatalogFile(catalogProvider,
                ClasspathSchemaModule.DEFAULT_CATALOG_CLASSPATH_REQUEST);
        assertEquals(2, catalogFile5.getNextCatalogs().size());

        final String expected = RuntimeCatalogProviderTest.CORE_CATALOG.resolve(
                "oasis_xml_catalogs_1_1/catalog.dtd").toString();
        final String systemId = "http://www.oasis-open.org/committees/entity/release/1.1/catalog.dtd";
        final String publicId = "-//OASIS//DTD XML Catalogs V1.1//EN";
        final BlockingCatalog catalog = new BlockingCatalog(CatalogProviders.DEFAULT_ROOT_REQUEST, catalogProvider);
        final CatalogResult catalogResult1 = catalog.resolveEntity(publicId, systemId, null);
        assertEquals(expected, catalogResult1.getResolution(), catalogResult1.toDebugString());
        final CatalogResult catalogResult2 = catalog.resolveEntity(publicId, expected, null);
        assertEquals(expected, catalogResult2.getResolution(), catalogResult2.toDebugString());
    }
}
