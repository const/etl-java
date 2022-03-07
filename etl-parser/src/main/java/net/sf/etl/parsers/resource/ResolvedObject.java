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
import java.util.List;
import java.util.Objects;

/**
 * The resolved object wrapper that is used to describe a resolution process to the compiler. The object is immutable.
 *
 * @param <T> the value type
 */
public final class ResolvedObject<T extends Serializable> implements Serializable {
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
                ? List.of()
                : List.copyOf(resolutionHistory);
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
    public <Other extends Serializable> ResolvedObject<Other> withOtherObject(final Other newObject) {
        return new ResolvedObject<>(request, resolutionHistory, descriptor, newObject);
    }

    /**
     * @return the resolved object
     */
    public T getObject() {
        return object;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResolvedObject<?> that = (ResolvedObject<?>) o;
        return Objects.equals(request, that.request)
                && Objects.equals(resolutionHistory, that.resolutionHistory)
                && Objects.equals(descriptor, that.descriptor)
                && Objects.equals(object, that.object);
    }

    @Override
    public int hashCode() {
        return Objects.hash(request, resolutionHistory, descriptor, object);
    }

    /**
     * @return the resolution history
     */
    public List<ResourceUsage> getResolutionHistory() {
        return resolutionHistory;
    }
}
