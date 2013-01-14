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
package net.sf.etl.parsers.event.grammar.impl.flattened;

import net.sf.etl.parsers.SourceLocation;

/**
 * A link in wrapper chain used to include other context in this context. By
 * construction of the object there can be no cycles.
 *
 * @author const
 */
public final class WrapperLink {
    /**
     * The source location for the wrapper link
     */
    private final SourceLocation objectLocation;
    /**
     * The property location for the wrapper link
     */
    private final SourceLocation propertyLocation;
    /**
     * The object name
     */
    private final String name;
    /**
     * The namespace for object name
     */
    private final String namespace;
    /**
     * The property used for wrapping
     */
    private final String property;
    /**
     * More inner wrapper in the chain
     */
    private final WrapperLink innerWrapper;

    /**
     * The constructor from fields
     *
     * @param innerWrapper     the inner wrapper in the chain
     * @param namespace        the object namespace
     * @param name             the object name
     * @param property         the property to contain definition from included context
     * @param objectLocation   the object location
     * @param propertyLocation the property location
     */
    public WrapperLink(WrapperLink innerWrapper, String namespace, String name, String property, SourceLocation objectLocation, SourceLocation propertyLocation) {
        this.innerWrapper = innerWrapper;
        this.objectLocation = objectLocation;
        this.propertyLocation = propertyLocation;
        this.name = checkNull(name, "name");
        this.namespace = checkNull(namespace, "namespace");
        this.property = checkNull(property, "property");
    }

    /**
     * Check if argument is null
     *
     * @param argValue the value to check
     * @param argName  the message for exception
     * @return the value if it is not null
     */
    private String checkNull(String argValue, String argName) {
        if (argValue == null) {
            throw new NullPointerException("The argument " + argName + " cannot be null.");
        }
        return argValue;
    }

    /**
     * @return the object location
     */
    public SourceLocation objectLocation() {
        return objectLocation;
    }

    /**
     * @return the property location
     */
    public SourceLocation propertyLocation() {
        return propertyLocation;
    }

    /**
     * @return the inner wrapper.
     */
    public WrapperLink innerWrapper() {
        return innerWrapper;
    }

    /**
     * @return the name of an object.
     */
    public String name() {
        return name;
    }

    /**
     * @return the namespace of an object.
     */
    public String namespace() {
        return namespace;
    }

    /**
     * @return the property of an object.
     */
    public String property() {
        return property;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WrapperLink that = (WrapperLink) o;

        if (innerWrapper != null ? !innerWrapper.equals(that.innerWrapper) : that.innerWrapper != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (namespace != null ? !namespace.equals(that.namespace) : that.namespace != null) return false;
        //noinspection RedundantIfStatement
        if (property != null ? !property.equals(that.property) : that.property != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        result = 31 * result + (property != null ? property.hashCode() : 0);
        result = 31 * result + (innerWrapper != null ? innerWrapper.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder rc = new StringBuilder();
        toString(rc);
        return rc.toString();
    }

    /**
     * Append string representation to string buffer
     *
     * @param rc the string buffer to use
     */
    private void toString(StringBuilder rc) {
        rc.append("{");
        rc.append(namespace);
        rc.append("}");
        rc.append(name);
        rc.append(".");
        rc.append(property);
        if (innerWrapper != null) {
            rc.append("/");
            innerWrapper.toString(rc);
        }
    }

    /**
     * Concatenate two wrapper lists.
     *
     * @param first  the first list
     * @param second the second list
     * @return concatenated list
     */
    public static WrapperLink concatenate(WrapperLink first, WrapperLink second) {
        if (second == null) {
            return first;
        } else if (first == null) {
            return second;
        } else {
            return new WrapperLink(concatenate(first.innerWrapper, second),
                    first.namespace, first.name, first.property, first.objectLocation, first.propertyLocation);
        }
    }
}
