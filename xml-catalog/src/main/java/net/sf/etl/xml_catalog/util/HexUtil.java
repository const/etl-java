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

package net.sf.etl.xml_catalog.util;

/**
 * Hex utilities.
 */
public final class HexUtil {
    /**
     * Hex digits (lowercase).
     */
    private static final char[] HEX_LC =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Hex digits (uppercase).
     */
    private static final char[] HEX_UC =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * Private constructor for utility class.
     */
    private HexUtil() {
        // DO NOTHING
    }

    /**
     * Append hex byte to StringBuilder.
     *
     * @param sb the string builder to use
     * @param b  the byte to append
     */
    public static void appendHexByte(final StringBuilder sb, final byte b) {
        // CHECKSTYLE:OFF
        sb.append(HEX_LC[(b >> 4) & 0xf]);
        sb.append(HEX_LC[b & 0xf]);
        // CHECKSTYLE:ON
    }

    /**
     * Append hex byte to StringBuilder.
     *
     * @param sb the string builder to use
     * @param b  the byte to append
     */
    public static void appendHexByteUppercase(final StringBuilder sb, final byte b) {
        // CHECKSTYLE:OFF
        sb.append(HEX_UC[(b >> 4) & 0xf]);
        sb.append(HEX_UC[b & 0xf]);
        // CHECKSTYLE:ON
    }

    /**
     * Get hex byte from string.
     *
     * @param hex the string that contains hex
     * @param i   used position
     * @return the value to return
     */
    public static byte getHexByte(final String hex, final int i) {
        // CHECKSTYLE:OFF
        final int b = (Character.digit(hex.charAt(i), 16) << 4) | Character.digit(hex.charAt(i + 1), 16);
        if (b < 0) {
            throw new IllegalArgumentException("Non hex digit in string: " + hex);
        }
        return (byte) b;
        // CHECKSTYLE:ON
    }
}
