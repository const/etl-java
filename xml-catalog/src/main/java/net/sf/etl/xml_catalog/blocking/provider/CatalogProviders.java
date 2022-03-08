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

package net.sf.etl.xml_catalog.blocking.provider;

import net.sf.etl.xml_catalog.blocking.provider.schema.ClasspathSchemaModule;
import net.sf.etl.xml_catalog.blocking.provider.schema.FileSystemCatalogProviderModule;
import net.sf.etl.xml_catalog.blocking.provider.schema.SystemCatalogs;
import net.sf.etl.xml_catalog.event.CatalogContext;
import net.sf.etl.xml_catalog.event.CatalogFile;
import net.sf.etl.xml_catalog.event.CatalogRequest;
import net.sf.etl.xml_catalog.event.CatalogResolutionEvent;
import net.sf.etl.xml_catalog.event.CatalogResourceUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Vector;

/**
 * Different catalog providers.
 */
public final class CatalogProviders {
    /**
     * The classpath catalog enabled property (default = true).
     */
    public static final String CLASSPATH_ENABLED_PROPERTY = "classpath.catalog.enabled";
    /**
     * The property indicating that system catalog is enabled (default = true).
     */
    public static final String USER_ENABLED_PROPERTY = "user.catalog.enabled";
    /**
     * The property indicating that system catalog is enabled (default = true).
     */
    public static final String SYSTEM_ENABLED_PROPERTY = "system.catalog.enabled";
    /**
     * The default root request for command line tools.
     */
    public static final CatalogRequest DEFAULT_ROOT_REQUEST = new CatalogRequest("x-catalog-root:command-line-tool");
    /**
     * The default value of prefer public property.
     */
    public static final String PREFER_PUBLIC_DEFAULT = "true";
    /**
     * The name of prefer public property.
     */
    public static final String PREFER_PUBLIC_PROPERTY = "catalog.prefer.public";
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CatalogProviders.class);

    /**
     * Private constructor for utility class.
     */
    private CatalogProviders() {
        // do nothing
    }

    /**
     * Create a default catalog provider that is tailored toward command line tools.
     *
     * @param contextClass the context class
     * @return the catalog provider
     */
    public static CatalogRuntimeProvider createDefaultCatalogProvider(final Class<?> contextClass) {
        return createDefaultCatalogProvider(contextClass == null ? null : contextClass.getClassLoader());
    }

    /**
     * Create a default catalog provider that is tailored toward command line tools.
     *
     * @param contextClassLoader the context class
     * @return the catalog provider
     */
    public static CatalogRuntimeProvider createDefaultCatalogProvider(final ClassLoader contextClassLoader) {
        return createDefaultCatalogProvider(DEFAULT_ROOT_REQUEST, contextClassLoader);
    }

    /**
     * Create a default catalog provider that is tailored toward command line tools, it could be further
     * customized if needed.
     *
     * @param rootRequest        the request for constructed root catalog
     * @param contextClassLoader the context class
     * @return the catalog provider
     */
    public static CatalogRuntimeProvider createDefaultCatalogProvider(
            final CatalogRequest rootRequest, final ClassLoader contextClassLoader) {
        final boolean classPathEnabled = Boolean.parseBoolean(System.getProperty(CLASSPATH_ENABLED_PROPERTY, "true"));
        final boolean userEnabled = Boolean.parseBoolean(System.getProperty(USER_ENABLED_PROPERTY, "true"));
        final boolean systemEnabled = Boolean.parseBoolean(System.getProperty(SYSTEM_ENABLED_PROPERTY, "true"));
        return createDefaultCatalogProvider(rootRequest, contextClassLoader,
                classPathEnabled, userEnabled, systemEnabled, null);
    }

    /**
     * Create default catalog provider.
     *
     * @param rootRequest        the root request
     * @param contextClassLoader the context classloader
     * @param classPathEnabled   the classpath enabled
     * @param userEnabled        the user catalog enabled
     * @param systemEnabled      the system catalog enabled
     * @param prependedUris      the uris added at the beggining of the list
     * @return the catalog provider
     */
    public static CatalogRuntimeProvider createDefaultCatalogProvider(final CatalogRequest rootRequest,
                                                                      final ClassLoader contextClassLoader,
                                                                      final boolean classPathEnabled,
                                                                      final boolean userEnabled,
                                                                      final boolean systemEnabled,
                                                                      final List<URI> prependedUris) {
        final Vector<URI> uriList = new Vector<URI>(); // NOPMD
        if (prependedUris != null) {
            uriList.addAll(prependedUris);
        }
        final CatalogRuntimeProvider runtimeProvider = new CatalogRuntimeProvider();
        if (contextClassLoader != null) {
            runtimeProvider.registerSchemaModule(new ClasspathSchemaModule(contextClassLoader));
            if (classPathEnabled) {
                try {
                    uriList.add(new URI(ClasspathSchemaModule.DEFAULT_CATALOG_CLASSPATH_REQUEST.getSystemId()));
                } catch (URISyntaxException e) {
                    LOG.error("Unable to parse default request: "
                            + ClasspathSchemaModule.DEFAULT_CATALOG_CLASSPATH_REQUEST, e);
                }
            }
        }
        runtimeProvider.registerSchemaModule(new FileSystemCatalogProviderModule());
        SystemCatalogs.addSystemCatalogs(uriList, userEnabled, systemEnabled);
        runtimeProvider.registerVectorCatalog(rootRequest, uriList);
        return runtimeProvider;
    }

    /**
     * The naive caching wrapper for the provider. It prevents garbage collection of catalogs.
     * But it could be useful for command line tools,  that have limited lifetime and generally
     * do not need any cache expiration policy.
     *
     * @param source the caching source
     * @return the result
     */
    public static CatalogProvider createCachedCatalog(final CatalogProvider source) {
        return createCachedCatalog(source, new CatalogResolutionCache());
    }

    /**
     * The cached catalog.
     *
     * @param source the source catalog
     * @param cache  the cache to use
     * @return the cached catalog provider
     */
    public static CatalogProvider createCachedCatalog(final CatalogProvider source,
                                                      final CatalogResolutionCache cache) {
        return new CatalogProvider() {
            @Override
            public CatalogResolutionEvent getCatalog(final CatalogContext catalogContext,
                                                     final CatalogRequest request) {
                CatalogResolutionEvent event = cache.getFile(request);
                if (event == null) {
                    event = source.getCatalog(catalogContext, request);
                    if (event != null) {
                        cache.cacheFile(event);
                    }
                }
                return event;
            }
        };
    }


    /**
     * The create and report resolution event.
     *
     * @param catalogContext    the catalog context
     * @param request           the catalog request
     * @param file              the resolved file or null in case of missing catalog
     * @param problem           the resolution problem
     * @param resolutionHistory the resolution history
     * @return the resolution event
     */
    public static CatalogResolutionEvent catalogLoaded(final CatalogContext catalogContext,
                                                       final CatalogRequest request, final CatalogFile file,
                                                       final Throwable problem,
                                                       final List<CatalogResourceUsage> resolutionHistory) {
        final CatalogResolutionEvent event = new CatalogResolutionEvent(request, file, problem, resolutionHistory);
        try {
            catalogContext.catalogLoaded(event);
        } catch (Throwable t) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to notify about event " + event, t);
            }
            // ignore problem from the context
        }
        return event;
    }

    /**
     * The create and report resolution event.
     *
     * @param catalogContext the catalog context
     * @param request        the catalog request
     * @param file           the resolved file or null in case of missing catalog
     * @param problem        the resolution problem
     * @return the resolution event
     */
    public static CatalogResolutionEvent catalogLoaded(final CatalogContext catalogContext,
                                                       final CatalogRequest request,
                                                       final CatalogFile file, final Throwable problem) {
        return catalogLoaded(catalogContext, request, file, problem, CatalogResourceUsage.NONE);
    }
}
