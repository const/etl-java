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

package net.sf.etl.xml_catalog.blocking.provider.schema;

import net.sf.etl.xml_catalog.blocking.provider.CatalogProvider;
import net.sf.etl.xml_catalog.blocking.provider.CatalogProviders;
import net.sf.etl.xml_catalog.event.CatalogContext;
import net.sf.etl.xml_catalog.event.CatalogFile;
import net.sf.etl.xml_catalog.event.CatalogRequest;
import net.sf.etl.xml_catalog.event.CatalogResolutionEvent;
import net.sf.etl.xml_catalog.event.CatalogResourceUsage;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Classpath schema module. The usage of this module is not so safe, because it takes a classloader as an argument.
 */
public final class ClasspathSchemaModule extends MapSchemaProviderModule {
    /**
     * The schema for all catalogs in classpath with the specified name.
     */
    public static final String CATALOG_CLASSPATH = "x-catalog-classpath";
    /**
     * Default catalog request for classpath.
     */
    public static final CatalogRequest DEFAULT_CATALOG_CLASSPATH_REQUEST =
            new CatalogRequest(CATALOG_CLASSPATH + ":/META-INF/xml/catalog.xml");
    /**
     * The schema for any catalogs in classpath with the specified name.
     */
    public static final String CATALOG_CLASSPATH_ANY = "x-catalog-classpath-any";

    /**
     * The class loader.
     *
     * @param classLoader the class loader
     */
    public ClasspathSchemaModule(final ClassLoader classLoader) {
        getCatalogProviders().put(CATALOG_CLASSPATH, new ClasspathCatalogProvider(classLoader, false));
        getCatalogProviders().put(CATALOG_CLASSPATH_ANY, new ClasspathCatalogProvider(classLoader, true));
    }

    /**
     * The classpath provider.
     */
    public static final class ClasspathCatalogProvider implements CatalogProvider {
        /**
         * The class loader.
         */
        private final ClassLoader classLoader;
        /**
         * if true load only first catalog, otherwise load all catalogs with the name.
         */
        private final boolean any;

        /**
         * The catalog provider.
         *
         * @param classLoader the classloader to search
         * @param any         to load single catalog with the name
         */
        public ClasspathCatalogProvider(final ClassLoader classLoader, final boolean any) {
            this.classLoader = classLoader;
            this.any = any;
        }

        @Override
        public CatalogResolutionEvent getCatalog(final CatalogContext catalogContext, final CatalogRequest request) {
            final ArrayList<URI> uriList = new ArrayList<>();
            try {
                String path = new URI(request.getSystemId()).getPath();
                if (path == null || path.isEmpty()) {
                    return new CatalogResolutionEvent(request,
                            null, new IllegalStateException("Path is empty: " + request.getSystemId()),
                            CatalogResourceUsage.NONE);
                }
                if (path.charAt(0) == '/') {
                    // leading slash is not removed, the classloader fails to find resources
                    path = path.substring(1);
                }
                if (any) {
                    if (!path.isEmpty()) {
                        final URL url = classLoader.getResource(path);
                        if (url != null) {
                            uriList.add(url.toURI());
                        }
                    }
                } else {
                    final Enumeration<URL> resources = classLoader.getResources(path);
                    while (resources.hasMoreElements()) {
                        final URL url = resources.nextElement();
                        uriList.add(url.toURI()); // NOPMD
                    }
                }
            } catch (Throwable t) {
                return CatalogProviders.catalogLoaded(catalogContext, request, null, t, CatalogResourceUsage.NONE);
            }
            return CatalogProviders.catalogLoaded(catalogContext, request,
                    CatalogFile.fromNextCatalogs(request.getSystemId(), System.currentTimeMillis(),
                            CatalogResourceUsage.NONE, uriList),
                    null, CatalogResourceUsage.NONE);
        }
    }
}
