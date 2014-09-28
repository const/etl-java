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

package net.sf.etl.parsers.resource;

import java.io.Serializable;

/**
 * The request for the resource.
 */
public final class ResourceRequest implements Serializable {
    /**
     * UID.
     */
    private static final long serialVersionUID = 8235987917918078795L;
    /**
     * The resource reference.
     */
    private final ResourceReference resource;
    /**
     * The role.
     */
    private final String role;

    /**
     * The constructor.
     *
     * @param resource the resource
     * @param role     the role of the resource
     */
    public ResourceRequest(final ResourceReference resource, final String role) {
        this.resource = resource;
        this.role = role;
    }

    /**
     * @return the resource reference
     */
    public ResourceReference getReference() {
        return resource;
    }

    /**
     * @return the role of the resource
     */
    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "ResourceRequest{resource=" + resource + ", role='" + role + "'}";
    }

    @Override
    public boolean equals(final Object o) {
        //CHECKSTYLE:OFF
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ResourceRequest that = (ResourceRequest) o;

        if (resource != null ? !resource.equals(that.resource) : that.resource != null) {
            return false;
        }
        //noinspection RedundantIfStatement
        if (role != null ? !role.equals(that.role) : that.role != null) {
            return false;
        }

        return true;
        //CHECKSTYLE:ON
    }

    @Override
    public int hashCode() {
        //CHECKSTYLE:OFF
        int result = resource != null ? resource.hashCode() : 0;
        result = 31 * result + (role != null ? role.hashCode() : 0);
        return result;
        //CHECKSTYLE:ON
    }
}
