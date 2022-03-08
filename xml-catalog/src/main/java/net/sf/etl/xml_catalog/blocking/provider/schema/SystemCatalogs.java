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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;

/**
 * The utilities that adds system catalogs if they exists to the collection.
 */
public final class SystemCatalogs {
    /**
     * The system catalog path for unix, possibly add a list of paths to check in the future.
     * This path was taken from Debian documentation.
     */
    public static final String UNIX_SYSTEM_CATALOG_PATH = "/etc/xml/catalog";
    /**
     * User directory that stores automatic configuration.
     */
    public static final String USER_XML_DIR = ".xml";
    /**
     * The directory in user directory that is recursively scanned for automatic catalogs.
     */
    public static final String AUTOMATIC_CATALOGS = "automatic-catalogs";
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SystemCatalogs.class);

    /**
     * Private constructor for utility class.
     */
    private SystemCatalogs() {
        // do nothing
    }

    /**
     * Add system catalogs.
     *
     * @param uriList              the system catalogs
     * @param userCatalogEnabled   true if user catalog is enabled
     * @param systemCatalogEnabled true if system catalog is enabled
     */
    public static void addSystemCatalogs(final List<URI> uriList,
                                         final boolean userCatalogEnabled, final boolean systemCatalogEnabled) {
        addSystemCatalogs(
                !systemCatalogEnabled || System.getProperty("os.name").toLowerCase(Locale.US).contains("windows")
                        ? null : UNIX_SYSTEM_CATALOG_PATH,
                !userCatalogEnabled ? null : System.getProperty("user.home"), USER_XML_DIR,
                uriList);
    }

    /**
     * Add catalog requests for the system catalogs. This method is used primarily for testing.
     *
     * @param systemCatalogPath the expected system catalog path
     * @param userDirPath       the user directory path
     * @param subDirPath        the sub directory path
     * @param catalogRequests   the collection to update
     */
    public static void addSystemCatalogs(final String systemCatalogPath, final String userDirPath,
                                         final String subDirPath,
                                         final List<URI> catalogRequests) {
        if (userDirPath != null && subDirPath != null) {
            try {
                final File userDir = new File(userDirPath, subDirPath).getCanonicalFile();
                final File catalogFile = new File(userDir, "catalog.xml");
                if (catalogFile.isFile()) {
                    catalogRequests.add(catalogFile.toURI());
                }
                final File catalogsDir = new File(userDir, AUTOMATIC_CATALOGS);
                if (catalogsDir.isDirectory()) {
                    catalogRequests.add(
                            new URI(FileSystemCatalogProviderModule.CATALOG_FILE_LIST_RECURSIVE + ":"
                                    + catalogsDir.toURI()));
                }
            } catch (Throwable ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to process user catalog: " + userDirPath + " / " + subDirPath, ex);
                }
            }
        }
        if (systemCatalogPath != null) {
            final File systemCatalogFile;
            try {
                systemCatalogFile = new File(systemCatalogPath).getCanonicalFile();
                if (systemCatalogFile.isFile()) {
                    catalogRequests.add(systemCatalogFile.toURI());
                }
            } catch (IOException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to process system catalog: " + systemCatalogPath, e);
                }
            }
        }
    }
}
