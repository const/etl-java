/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2012 Constantine A Plotnikov
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
 * The resolved object
 */
public class ResolvedObject<T> implements Serializable {
    private final ResourceRequest request;
    private final ResourceDescriptor resource;
    private final T object;

    public ResolvedObject(ResourceRequest request, ResourceDescriptor resource, T object) {
        this.request = request;
        this.resource = resource;
        this.object = object;
    }

    public ResourceRequest getRequest() {
        return request;
    }

    public ResourceDescriptor getResource() {
        return resource;
    }

    public T getObject() {
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResolvedObject that = (ResolvedObject) o;

        if (object != null ? !object.equals(that.object) : that.object != null) return false;
        if (request != null ? !request.equals(that.request) : that.request != null) return false;
        //noinspection RedundantIfStatement
        if (resource != null ? !resource.equals(that.resource) : that.resource != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = request != null ? request.hashCode() : 0;
        result = 31 * result + (resource != null ? resource.hashCode() : 0);
        result = 31 * result + (object != null ? object.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ResolvedObject");
        sb.append("{request=").append(request);
        sb.append(", resource=").append(resource);
        sb.append(", object=").append(object);
        sb.append('}');
        return sb.toString();
    }
}
