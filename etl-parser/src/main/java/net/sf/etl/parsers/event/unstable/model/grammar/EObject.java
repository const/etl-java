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
package net.sf.etl.parsers.event.unstable.model.grammar;

import net.sf.etl.parsers.SourceLocation;

import java.io.Serializable;

/**
 * Base class for lightweight grammar objects.
 *
 * @author const
 */
public abstract class EObject implements Serializable { // NOPMD
    /**
     * The owner.
     */
    private EObject ownerObject;
    /**
     * The owner feature.
     */
    private String ownerFeature;
    /**
     * The location.
     */
    private SourceLocation location;

    /**
     * @return source location for object
     */
    public final SourceLocation getSourceLocation() {
        return location;
    }

    /**
     * @return owner of the element
     */
    public final EObject getOwnerObject() {
        return ownerObject;
    }

    /**
     * Set owner.
     *
     * @param ownerObject the owner
     */
    public final void setOwnerObject(final EObject ownerObject) {
        this.ownerObject = ownerObject;
    }

    /**
     * @return feature of the owner that has this element
     */
    public final String getOwnerFeature() {
        return ownerFeature;
    }

    /**
     * Set owner feature.
     *
     * @param ownerFeature the feature
     */
    public final void setOwnerFeature(final String ownerFeature) {
        this.ownerFeature = ownerFeature;
    }

    /**
     * @return The source location
     */
    public final SourceLocation getLocation() {
        return location;
    }

    /**
     * Set location.
     *
     * @param location the location
     */
    public final void setLocation(final SourceLocation location) {
        this.location = location;
    }
}
