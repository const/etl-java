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
 * This class represents location in the source code. It is primary used for
 * error reporting.
 *
 * @author const
 */
public final class SourceLocation {
    /**
     * The unknown location.
     */
    public static final SourceLocation UNKNOWN = new SourceLocation(TextPos.START, TextPos.START, "unknown:");
    /**
     * the start of source code fragment.
     */
    private final TextPos start;
    /**
     * the end of source code fragment.
     */
    private final TextPos end;
    /**
     * the location of source code fragment.
     */
    private final String systemId;

    /**
     * A constructor from fields.
     *
     * @param start    the start of source code fragment
     * @param end      the end of source code fragment
     * @param systemId the location of source code fragment
     */
    public SourceLocation(final TextPos start, final TextPos end, final String systemId) {
        super();
        this.end = end;
        this.start = start;
        this.systemId = systemId;
    }

    /**
     * @return the end of source code fragment
     */
    public TextPos end() {
        return end;
    }

    /**
     * @return the start of source code fragment
     */
    public TextPos start() {
        return start;
    }

    /**
     * @return the location of source code fragment
     */
    public String systemId() {
        return systemId;
    }

    /**
     * @return the short name for system identifier
     */
    public String shortSystemId() {
        final int p = systemId.lastIndexOf('/');
        return p == -1 ? systemId : systemId.substring(p + 1);
    }

    /**
     * @return The short form of location string useful for reporting. It only
     * reports the last name component of the URL.
     */
    public String toShortString() {
        return shortSystemId() + start + "-" + end;
    }

    @Override
    public String toString() {
        return "[" + systemId + ':' + start + '-' + end + "]";
    }
}
