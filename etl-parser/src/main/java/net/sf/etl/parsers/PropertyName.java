/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2022 Konstantin Plotnikov
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
 * Name of property.
 *
 * @author const
 */
// NOTE POST 0.2: this class possibly should be eliminated later and replaced
// with String in order to simplify API. However strong type checking is major
// benefit so far from point of view of internal implementation.
public final class PropertyName {
    /**
     * Name of property.
     */
    private final String name;

    /**
     * A constructor.
     *
     * @param name name of property
     */
    public PropertyName(final String name) {
        super();
        if (name == null) {
            throw new IllegalArgumentException("Null is not allowed as property name");
        }
        this.name = name;
    }

    /**
     * The field name from property name.
     *
     * @param name the field name
     * @return the adjusted field name
     */
    public static String lowerCaseFeatureName(final String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        final int first = name.codePointAt(0);
        if (!Character.isLowerCase(first)) {
            return new StringBuilder().
                    appendCodePoint(Character.toLowerCase(first)).
                    append(name.substring(Character.charCount(first))).
                    toString();
        }
        return name;
    }

    /**
     * @return name of property
     */
    public String name() {
        return name;
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

        final PropertyName that = (PropertyName) o;

        return name.equals(that.name);
        //CHECKSTYLE:ON
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

}
