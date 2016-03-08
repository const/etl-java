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

package net.sf.etl.xml_catalog.blocking.provider.schema;

import net.sf.etl.xml_catalog.blocking.provider.CatalogProvider;
import net.sf.etl.xml_catalog.blocking.provider.CatalogProviders;
import net.sf.etl.xml_catalog.event.CatalogContext;
import net.sf.etl.xml_catalog.event.CatalogFile;
import net.sf.etl.xml_catalog.event.CatalogRequest;
import net.sf.etl.xml_catalog.event.CatalogResolutionEvent;
import net.sf.etl.xml_catalog.event.CatalogResourceUsage;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The file system provider module.
 */
public final class FileSystemCatalogProviderModule extends MapSchemaProviderModule implements SchemaProviderModule {
    /**
     * The file list catalog.
     */
    public static final String CATALOG_FILE_LIST = "x-catalog-file-list";
    /**
     * The recursive list catalog.
     */
    public static final String CATALOG_FILE_LIST_RECURSIVE = "x-catalog-file-list-recursive";
    /**
     * The catalog dir type of dependency.
     */
    public static final String CATALOG_DIR_TYPE = "http://apache-extras.org/p/xml-catalog/types/catalog-dir";

    /**
     * The constructor.
     */
    public FileSystemCatalogProviderModule() {
        final FileListCatalogProvider fileListCatalogProvider = new FileListCatalogProvider();
        getCatalogProviders().put(CATALOG_FILE_LIST_RECURSIVE, fileListCatalogProvider);
        getCatalogProviders().put(CATALOG_FILE_LIST, fileListCatalogProvider);
    }

    /**
     * Catalog provider for file lists.
     */
    public static final class FileListCatalogProvider implements CatalogProvider {
        /**
         * Max scan level.
         */
        public static final int MAX_LEVEL = 64;

        @Override
        public CatalogResolutionEvent getCatalog(final CatalogContext catalogContext, final CatalogRequest request) {
            final ArrayList<CatalogResourceUsage> resourceUsages = new ArrayList<CatalogResourceUsage>();
            final ArrayList<URI> requests = new ArrayList<URI>();
            final long version;
            try {
                final URI uri = URI.create(request.getSystemId());
                final boolean recursive;
                if (CATALOG_FILE_LIST.equals(uri.getScheme())) {
                    recursive = false;
                } else if (CATALOG_FILE_LIST_RECURSIVE.equals(uri.getScheme())) {
                    recursive = true;
                } else {
                    throw new IllegalArgumentException("Unable to handle request: " + request);
                }
                final File root = new File(URI.create(uri.getRawSchemeSpecificPart()).getPath());
                version = processDirectory(1, recursive, root, requests, resourceUsages);
            } catch (Throwable t) {
                return CatalogProviders.catalogLoaded(catalogContext, request, null, t, resourceUsages);
            }
            return CatalogProviders.catalogLoaded(catalogContext, request,
                    CatalogFile.fromNextCatalogs(request.getSystemId(), version, resourceUsages, requests),
                    null, resourceUsages);
        }

        /**
         * Process directory possibly recursively.
         *
         * @param level          the current processing level
         * @param recursive      the recursive flag
         * @param directory      the directory to scan
         * @param requests       the collected requests
         * @param resourceUsages the resource usages
         * @return the version
         */
        private long processDirectory(final int level, final boolean recursive, final File directory,
                                      final List<URI> requests,
                                      final List<CatalogResourceUsage> resourceUsages) {
            long rc = directory.lastModified();
            if (directory.isDirectory()) {
                resourceUsages.add(new CatalogResourceUsage(CATALOG_DIR_TYPE,
                        directory.toURI().toString(), directory.lastModified()));
                final File[] list = directory.listFiles();
                if (list != null) {
                    for (final File file : list) {
                        if (file.getName().toLowerCase(Locale.ENGLISH).endsWith(".xml") && file.isFile()) {
                            rc = Math.max(rc, file.lastModified());
                            requests.add(file.toURI()); // NOPMD
                        }
                    }
                    if (recursive && level < MAX_LEVEL) {
                        for (final File file : list) {
                            if (file.isDirectory()) {
                                rc = Math.max(rc, processDirectory(level + 1, true, file, requests, resourceUsages));
                            }
                        }
                    }
                }
            }
            return rc;
        }
    }
}
