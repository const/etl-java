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
        // CHECKSTYLE:OFF
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DefinitionInfo that = (DefinitionInfo) o;

        if (context != null ? !context.equals(that.context) : that.context != null) {
            return false;
        }
        if (location != null ? !location.equals(that.location) : that.location != null) {
            return false;
        }
        //noinspection RedundantIfStatement
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
        // CHECKSTYLE:ON
    }

    @Override
    public int hashCode() {
        // CHECKSTYLE:OFF
        int result = context != null ? context.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
        // CHECKSTYLE:ON
    }
}
