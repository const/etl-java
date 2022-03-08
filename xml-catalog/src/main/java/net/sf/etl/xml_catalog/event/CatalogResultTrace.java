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

package net.sf.etl.xml_catalog.event;

import java.util.List;

/**
 * The resolution trace for the catalog.
 */
public final class CatalogResultTrace {
    /**
     * The request for this catalog.
     */
    private final CatalogRequest catalogRequest;
    /**
     * The resources used to locate this catalog.
     */
    private final List<CatalogResourceUsage> resolutionResources;
    /**
     * The resolved catalog file or null if catalog is missing.
     */
    private final CatalogFile catalogFile;
    /**
     * If available, the problem that caused catalog loading failure.
     */
    private final Throwable problem;
    /**
     * The location that caused resolution, or switch to the next catalog (might be null).
     */
    private final String location;
    /**
     * The previous element in the trace.
     */
    private final CatalogResultTrace previous;
    /**
     * The warning.
     */
    private final Warning warning;

    /**
     * The constructor.
     *
     * @param catalogRequest      the catalog request that caused this element to load
     * @param resolutionResources the resources used to resolve this catalog
     * @param catalogFile         the catalog file
     * @param problem             the problem in case of failure to load catalog
     * @param location            the catalog location
     * @param nextElement         the next element
     * @param warning             the warning element
     */
    public CatalogResultTrace(final CatalogRequest catalogRequest, final List<CatalogResourceUsage> resolutionResources,
                              final CatalogFile catalogFile, final Throwable problem, final String location,
                              final CatalogResultTrace nextElement, final Warning warning) {
        this.catalogRequest = catalogRequest;
        this.resolutionResources = resolutionResources;
        this.catalogFile = catalogFile;
        this.problem = problem;
        this.location = location;
        this.previous = nextElement;
        this.warning = warning;
    }

    /**
     * @return the catalog request
     */
    public CatalogRequest getCatalogRequest() {
        return catalogRequest;
    }

    /**
     * @return the resources used for the resolution
     */
    public List<CatalogResourceUsage> getResolutionResources() {
        return resolutionResources;
    }

    /**
     * @return the catalog file
     */
    public CatalogFile getCatalogFile() {
        return catalogFile;
    }

    /**
     * @return the problem that caused catalog not to load
     */
    public Throwable getProblem() {
        return problem;
    }

    /**
     * @return the catalog location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @return the previous element in resolution chain
     */
    public CatalogResultTrace getPrevious() {
        return previous;
    }

    /**
     * @return the warning from this stage
     */
    public Warning getWarning() {
        return warning;
    }

    /**
     * The associated warning.
     */
    public static final class Warning {
        /**
         * The test of the warning.
         */
        private final String text;
        /**
         * The warning.
         */
        private final Warning next;

        /**
         * The warning associated with the process.
         *
         * @param text the text
         * @param next the next warning
         */
        public Warning(final String text, final Warning next) {
            this.text = text;
            this.next = next;
        }

        /**
         * @return the warning text
         */
        public String getText() {
            return text;
        }

        /**
         * @return the next warning
         */
        public Warning getNext() {
            return next;
        }
    }
}
