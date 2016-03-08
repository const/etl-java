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

import java.util.Collections;
import java.util.List;

/**
 * The resource used for catalog loading, it might include other catalogs, dtd or xsd files.
 */
public final class CatalogResourceUsage {
    /**
     * No resources are used.
     */
    public static final List<CatalogResourceUsage> NONE = Collections.emptyList();
    /**
     * The resource role.
     */
    private final String role;
    /**
     * The system id.
     */
    private final String systemId;
    /**
     * The used version.
     */
    private final Object version;

    /**
     * The constructor.
     *
     * @param role     the role
     * @param systemId the system id
     * @param version  the version
     */
    public CatalogResourceUsage(final String role, final String systemId, final Object version) {
        this.role = role;
        this.systemId = systemId;
        this.version = version;
    }

    /**
     * @return the resource role
     */
    public String getRole() {
        return role;
    }

    /**
     * @return the resource system id
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * @return the resource version
     */
    public Object getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "CatalogResourceUsage" + "{role='" + role + '\'' + ", systemId='" + systemId + '\''
                + ", version=" + version + '}';
    }
}
