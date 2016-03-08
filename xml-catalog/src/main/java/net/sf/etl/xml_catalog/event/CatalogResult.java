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

/**
 * The catalog result.
 */
public final class CatalogResult {
    /**
     * The resulting uri, it might be null if resolution is not found.
     */
    private final String uri;
    /**
     * If true, the uri was resolved, if false, it was taken just from request or is null.
     */
    private final boolean resolved;
    /**
     * The catalog that caused resolution of URI.
     */
    private final CatalogResultTrace trace;

    /**
     * The constructor.
     *
     * @param uri      the resolved URI
     * @param resolved if uri was resolved
     * @param trace    the resolution trace
     */
    public CatalogResult(final String uri, final boolean resolved, final CatalogResultTrace trace) {
        this.uri = uri;
        this.resolved = resolved;
        this.trace = trace;
    }

    /**
     * @return the resolved URI, it might be null if resolution is not found
     */
    public String getResolution() {
        return uri;
    }

    /**
     * @return check if catalog has resolved something or value was unmodified
     */
    public boolean isResolved() {
        return resolved;
    }

    /**
     * @return the resolution trace
     */
    public CatalogResultTrace getTrace() {
        return trace;
    }

    @Override
    public String toString() {
        return getResolution();
    }

    /**
     * @return debug string with all details
     */
    public String toDebugString() { // NOPMD
        final StringBuilder rc = new StringBuilder(); // NOPMD
        rc.append(!resolved ? "UNRESOLVED " + (uri == null ? "" : uri) : uri).append("{\n");
        for (CatalogResultTrace t = getTrace(); t != null; t = t.getPrevious()) {
            rc.append("  ").append(t.getCatalogRequest().getSystemId());
            final String requestLocation = t.getCatalogRequest().getRequestLocation();
            if (requestLocation != null) {
                rc.append(" (").append(requestLocation).append(')');
            }
            final String location = t.getLocation();
            if (location != null) {
                rc.append(" [").append(location).append(']');
            }
            rc.append("=>");
            if (t.getCatalogFile() != null) {
                rc.append("VERSION: ").append(t.getCatalogFile().getVersion());
            } else {
                rc.append("FAILURE: ").append(t.getProblem());
            }
            if (!t.getResolutionResources().isEmpty()) {
                rc.append(' ').append(t.getResolutionResources());
            }
            rc.append('\n');
            for (CatalogResultTrace.Warning w = t.getWarning(); w != null; w = w.getNext()) {
                rc.append("   * ").append(w.getText()).append('\n');
            }
        }
        rc.append("}\n");
        return rc.toString();
    }
}
