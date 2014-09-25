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
 * The resource reference.
 */
public final class ResourceReference implements Serializable {
    /**
     * The system id.
     */
    private final String systemId;
    /**
     * The public id.
     */
    private final String publicId;

    /**
     * The constructor.
     *
     * @param systemId the public id
     * @param publicId the system id
     */
    public ResourceReference(final String systemId, final String publicId) {
        this.systemId = systemId;
        this.publicId = publicId;
    }

    /**
     * @return the system id
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * @return the public id
     */
    public String getPublicId() {
        return publicId;
    }

    @Override
    public boolean equals(final Object o) {
        //CHECKSTYLE:OFF
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceReference that = (ResourceReference) o;

        if (publicId != null ? !publicId.equals(that.publicId) : that.publicId != null) return false;
        //noinspection RedundantIfStatement
        if (systemId != null ? !systemId.equals(that.systemId) : that.systemId != null) return false;

        return true;
        //CHECKSTYLE:ON
    }

    @Override
    public int hashCode() {
        //CHECKSTYLE:OFF
        int result = systemId != null ? systemId.hashCode() : 0;
        result = 31 * result + (publicId != null ? publicId.hashCode() : 0);
        return result;
        //CHECKSTYLE:ON
    }

    @Override
    public String toString() {
        return "ResourceReference{systemId='" + systemId + "', publicId='" + publicId + "'}";
    }
}
