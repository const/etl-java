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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The resolved object wrapper that is used to describe a resolution process to the compiler. The object is immutable.
 *
 * @param <T> the value type
 */
public final class ResolvedObject<T> implements Serializable {
    /**
     * UID.
     */
    private static final long serialVersionUID = -194207955111642983L;
    /**
     * The original resource request.
     */
    private final ResourceRequest request;
    /**
     * The resources consulted while resolving the object.
     */
    private final List<ResourceUsage> resolutionHistory;
    /**
     * The descriptor of resource (assuming that system id is known, so it does not contains resolution history).
     */
    private final ResourceDescriptor descriptor;
    /**
     * The resolved object itself.
     */
    private final T object;

    /**
     * The constructor.
     *
     * @param request           the original request
     * @param resolutionHistory the resolution history
     * @param descriptor        the resource descriptor
     * @param object            the resolved object
     */
    public ResolvedObject(final ResourceRequest request, final List<ResourceUsage> resolutionHistory,
                          final ResourceDescriptor descriptor, final T object) {
        if (request == null) {
            throw new IllegalArgumentException("the request must not be null");
        }
        if (descriptor == null) {
            throw new IllegalArgumentException("the descriptor must not be null");
        }
        this.request = request;
        this.resolutionHistory = resolutionHistory == null || resolutionHistory.isEmpty()
                ? Collections.<ResourceUsage>emptyList()
                : Collections.unmodifiableList(new ArrayList<ResourceUsage>(resolutionHistory));
        this.descriptor = descriptor;
        this.object = object;
    }

    /**
     * @return original request
     */
    public ResourceRequest getRequest() {
        return request;
    }

    /**
     * @return the resource descriptor
     */
    public ResourceDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * The same request but with other object.
     *
     * @param newObject the object
     * @param <Other>   the object type
     * @return new resolved object instance
     */
    public <Other> ResolvedObject<Other> withOtherObject(final Other newObject) {
        return new ResolvedObject<Other>(request, resolutionHistory, descriptor, newObject);
    }

    /**
     * @return the resolved object
     */
    public T getObject() {
        return object;
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

        final ResolvedObject that = (ResolvedObject) o;

        if (object != null ? !object.equals(that.object) : that.object != null) {
            return false;
        }
        if (!request.equals(that.request)) {
            return false;
        }
        if (!resolutionHistory.equals(that.resolutionHistory)) {
            return false;
        }
        //noinspection RedundantIfStatement
        if (!descriptor.equals(that.descriptor)) {
            return false;
        }

        return true;
        //CHECKSTYLE:ON
    }

    @Override
    public int hashCode() {
        //CHECKSTYLE:OFF
        int result = request.hashCode();
        result = 31 * result + resolutionHistory.hashCode();
        result = 31 * result + descriptor.hashCode();
        result = 31 * result + (object != null ? object.hashCode() : 0);
        return result;
        //CHECKSTYLE:ON
    }

    /**
     * @return the resolution history
     */
    public List<ResourceUsage> getResolutionHistory() {
        return resolutionHistory;
    }
}
