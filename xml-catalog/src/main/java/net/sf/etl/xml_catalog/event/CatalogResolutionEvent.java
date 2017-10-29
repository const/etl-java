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

package net.sf.etl.xml_catalog.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The resolution event for the catalog.
 */
public final class CatalogResolutionEvent {
    /**
     * The request.
     */
    private final CatalogRequest request;
    /**
     * The catalog file, null in case of missing catalog.
     */
    private final CatalogFile file;
    /**
     * The problem (might be null in case of missing catalog as well).
     */
    private final Throwable problem;
    /**
     * The resource usage during resolution process.
     */
    private final List<CatalogResourceUsage> resolutionHistory;

    /**
     * The constructor.
     *
     * @param request           the catalog request
     * @param file              the resolved file or null in case of missing catalog
     * @param problem           the resolution problem
     * @param resolutionHistory the resolution history
     */
    public CatalogResolutionEvent(final CatalogRequest request, final CatalogFile file, final Throwable problem,
                                  final List<CatalogResourceUsage> resolutionHistory) {
        this.request = request;
        this.file = file;
        this.problem = problem;
        this.resolutionHistory = resolutionHistory == null || resolutionHistory.isEmpty()
                ? CatalogResourceUsage.NONE
                : Collections.unmodifiableList(new ArrayList<CatalogResourceUsage>(resolutionHistory));
    }

    /**
     * The constructor.
     *
     * @param request the catalog request
     * @param file    the resolved file or null in case of missing catalog
     * @param problem the resolution problem
     */
    public CatalogResolutionEvent(final CatalogRequest request, final CatalogFile file, final Throwable problem) {
        this(request, file, problem, CatalogResourceUsage.NONE);
    }

    /**
     * @return the catalog request
     */
    public CatalogRequest getRequest() {
        return request;
    }

    /**
     * @return the catalog file
     */
    public CatalogFile getFile() {
        return file;
    }

    /**
     * @return the catalog problem
     */
    public Throwable getProblem() {
        return problem;
    }

    /**
     * @return the used resources
     */
    public List<CatalogResourceUsage> getResolutionHistory() {
        return resolutionHistory;
    }

    @Override
    public boolean equals(final Object o) {
        // CHECKSTYLE:OFF
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CatalogResolutionEvent that = (CatalogResolutionEvent) o;

        return Objects.equals(file, that.file) && Objects.equals(problem, that.problem)
                && Objects.equals(request, that.request) && Objects.equals(resolutionHistory, that.resolutionHistory);
    }

    @Override
    public int hashCode() { // NOPMD
        // CHECKSTYLE:OFF
        int result = request != null ? request.hashCode() : 0;
        result = 31 * result + (file != null ? file.hashCode() : 0);
        result = 31 * result + (problem != null ? problem.hashCode() : 0);
        result = 31 * result + (resolutionHistory != null ? resolutionHistory.hashCode() : 0);
        return result;
        // CHECKSTYLE:ON
    }
}
