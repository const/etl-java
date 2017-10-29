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

package net.sf.etl.parsers;

import java.util.Objects;

/**
 * Information about definition, this information could be used to locate definition location.
 */
public final class DefinitionInfo {
    /**
     * The context for the definition.
     */
    private final DefinitionContext context;
    /**
     * The definition name withing context.
     */
    private final String name;
    /**
     * The source location.
     */
    private final SourceLocation location;

    /**
     * The constructor.
     *
     * @param context  definition context
     * @param name     the definition name
     * @param location the definition location
     */
    public DefinitionInfo(final DefinitionContext context, final String name, final SourceLocation location) {
        this.context = context;
        this.name = name;
        this.location = location;
    }

    /**
     * @return the definition name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the definition location
     */
    public SourceLocation getLocation() {
        return location;
    }

    /**
     * @return the context for this definition
     */
    public DefinitionContext getContext() {
        return context;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DefinitionInfo that = (DefinitionInfo) o;
        return Objects.equals(context, that.context) && Objects.equals(name, that.name)
                && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, name, location);
    }

    @Override
    public String toString() {
        return "DefinitionInfo{"
                + "context=" + context
                + ", name='" + name + '\''
                + ", location=" + location
                + '}';
    }
}
