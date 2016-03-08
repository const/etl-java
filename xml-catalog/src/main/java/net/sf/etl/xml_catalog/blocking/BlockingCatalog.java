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

package net.sf.etl.xml_catalog.blocking;

import net.sf.etl.xml_catalog.blocking.provider.CatalogProvider;
import net.sf.etl.xml_catalog.blocking.provider.CatalogProviders;
import net.sf.etl.xml_catalog.event.CatalogContext;
import net.sf.etl.xml_catalog.event.CatalogRequest;
import net.sf.etl.xml_catalog.event.CatalogResolutionEvent;
import net.sf.etl.xml_catalog.event.CatalogResult;
import net.sf.etl.xml_catalog.event.DefaultCatalogContext;
import net.sf.etl.xml_catalog.event.engine.CatalogEngine;
import net.sf.etl.xml_catalog.event.engine.CatalogEngineStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The catalog instance, it is usually created only once for some catalog scope, and then it is
 * used for asynchronous resolution.
 * </p>
 * <p>
 * The object itself is immutable, so it is safe to share, but it access {@link CatalogContext} for settings
 * that could change during the resolution. These settings should be implemented in a safe way.
 * </p>
 */
public final class BlockingCatalog {
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(BlockingCatalog.class);
    /**
     * The start catalog resource.
     */
    private final CatalogRequest startCatalog;
    /**
     * The catalog provider.
     */
    private final CatalogProvider catalogProvider;
    /**
     * The method that returns the current value of prefer public value, in case when it taken from settings.
     */
    private final CatalogContext catalogContext;

    /**
     * The the constructor.
     *
     * @param startCatalog    the start catalog
     * @param catalogProvider the provider for catalogs
     * @param catalogContext  the catalog settings
     */
    public BlockingCatalog(final CatalogRequest startCatalog, final CatalogProvider catalogProvider,
                           final CatalogContext catalogContext) {
        this.startCatalog = startCatalog;
        this.catalogProvider = catalogProvider;
        this.catalogContext = catalogContext != null ? catalogContext : DefaultCatalogContext.INSTANCE;
    }

    /**
     * The the constructor.
     *
     * @param startCatalog    the start catalog
     * @param catalogProvider the provider for catalogs
     */
    public BlockingCatalog(final CatalogRequest startCatalog, final CatalogProvider catalogProvider) {
        this(startCatalog, catalogProvider, null);
    }

    /**
     * Get default catalog with context class.
     *
     * @param contextClass the context class
     * @return the default catalog
     */
    public static BlockingCatalog getDefaultCatalog(final Class<?> contextClass) {
        return new BlockingCatalog(CatalogProviders.DEFAULT_ROOT_REQUEST,
                CatalogProviders.createDefaultCatalogProvider(contextClass));
    }

    /**
     * Get default catalog with context class.
     *
     * @param contextClassLoader the context class loader
     * @return the default catalog
     */
    public static BlockingCatalog getDefaultCatalog(final ClassLoader contextClassLoader) {

        return new BlockingCatalog(CatalogProviders.DEFAULT_ROOT_REQUEST,
                CatalogProviders.createDefaultCatalogProvider(contextClassLoader));
    }


    /**
     * @return get start catalog
     */
    public CatalogRequest getStartCatalog() {
        return startCatalog;
    }

    /**
     * @return the catalog provider
     */
    public CatalogProvider getCatalogProvider() {
        return catalogProvider;
    }

    /**
     * @return the current prefer public settings provider
     */
    public CatalogContext getCurrentSettings() {
        return catalogContext;
    }

    /**
     * Get copy of the catalog with new provider.
     *
     * @param newProvider the new provider
     * @return the catalog settings
     */
    public BlockingCatalog withOtherProvider(final CatalogProvider newProvider) {
        return new BlockingCatalog(getStartCatalog(), newProvider, getCurrentSettings());
    }

    /**
     * @return the current value of prefersPublic attribute
     */
    public boolean prefersPublic() {
        try {
            return catalogContext.prefersPublic();
        } catch (Throwable t) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to evaluate preferPublic", t);
            }
            return true;
        }
    }

    /**
     * Perform catalog resolution for entity.
     *
     * @param publicId the public id
     * @param systemId the system id
     * @param baseUri  the base URI for system id
     * @return resolution results
     */
    public CatalogResult resolveEntity(final String publicId, final String systemId, final String baseUri) {
        final CatalogEngine catalogEngine = new CatalogEngine();
        catalogEngine.startResolveEntity(startCatalog, publicId, systemId, baseUri, prefersPublic());
        return resolve(catalogEngine);
    }

    /**
     * Perform catalog resolution for URI.
     *
     * @param uri     the URI to resolve
     * @param nature  the uri nature (RDDL) (nullable)
     * @param purpose the uri purpose (RDDL) (nullable)
     * @param baseUri the base uri
     * @return resolution results
     */
    public CatalogResult resolveUri(final String uri, final String nature, final String purpose, // NOPMD
                                    final String baseUri) {
        final CatalogEngine catalogEngine = new CatalogEngine();
        catalogEngine.startResolveURI(startCatalog, uri, nature, purpose, baseUri);
        return resolve(catalogEngine);
    }

    /**
     * Perform catalog resolution for doctype.
     *
     * @param name     the name to resolve
     * @param publicId the public id
     * @param systemId the system id
     * @param baseUri  the base uri
     * @return resolution results
     */
    public CatalogResult resolveDoctype(final String name, final String publicId, final String systemId, // NOPMD
                                        final String baseUri) {
        final CatalogEngine catalogEngine = new CatalogEngine();
        catalogEngine.startDoctype(startCatalog, name, publicId, systemId, baseUri, prefersPublic());
        return resolve(catalogEngine);
    }

    /**
     * Perform catalog resolution for notation.
     *
     * @param name     the name to resolve
     * @param publicId the public id
     * @param systemId the system id
     * @param baseUri  the base uri
     * @return resolution results
     */
    public CatalogResult resolveNotation(final String name, final String publicId, final String systemId, // NOPMD
                                         final String baseUri) {
        final CatalogEngine catalogEngine = new CatalogEngine();
        catalogEngine.startNotation(startCatalog, name, publicId, systemId, baseUri, prefersPublic());
        return resolve(catalogEngine);
    }

    /**
     * Resolve resource.
     *
     * @param uri      the resource namespace
     * @param nature   the nature
     * @param purpose  the purpose
     * @param publicId the public id
     * @param systemId the system id
     * @param baseUri  the base URI to use
     * @return resolution result
     */
    public CatalogResult resolveResource(final String uri, final String nature, final String purpose, // NOPMD
                                         final String publicId, final String systemId, final String baseUri) {
        final CatalogEngine catalogEngine = new CatalogEngine();
        catalogEngine.startResource(startCatalog, uri, nature, purpose, publicId, systemId, baseUri, prefersPublic());
        return resolve(catalogEngine);
    }

    /**
     * Perform resolution process.
     *
     * @param catalogEngine the catalog engine
     * @return the resolution result
     */
    private CatalogResult resolve(final CatalogEngine catalogEngine) {
        CatalogRequest catalogRequest = startCatalog;
        while (true) {
            final CatalogResolutionEvent resolutionEvent = catalogProvider.getCatalog(catalogContext, catalogRequest);
            catalogEngine.resolve(resolutionEvent);
            final CatalogEngineStatus status = catalogEngine.process();
            switch (status) {
                case NOT_STARTED:
                    throw new IllegalStateException("The engine must have been started");
                case CATALOG_NEEDED:
                    catalogRequest = catalogEngine.getCatalogRequest();
                    continue;
                case RESULT_AVAILABLE:
                    return catalogEngine.result();
                default:
                    throw new IllegalStateException("Unknown status: " + status);
            }
        }
    }
}
