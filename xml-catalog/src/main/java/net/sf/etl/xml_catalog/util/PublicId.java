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

package net.sf.etl.xml_catalog.util;

import java.util.Locale;

/**
 * The public identifier utilities. The class implements public id transformations specified in
 * RFC 3151 "A URN Namespace for Public Identifiers".
 */
public final class PublicId {
    /**
     * public id URN prefix.
     */
    private static final String URN_PUBLIC_ID = "urn:publicid:";

    /**
     * Private constructor for utility class.
     */
    private PublicId() {
        // do nothing
    }

    /**
     * Normalize public identifier.
     *
     * @param publicId the public identifier
     * @return the normalized identifier
     */
    public static String normalize(final String publicId) {
        if (publicId == null) {
            return null;
        }
        final StringBuilder rc = new StringBuilder(publicId.length());
        final int n = publicId.length();
        boolean spaces = true;
        for (int i = 0; i < n; i++) {
            final char c = publicId.charAt(i);
            switch (c) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    if (!spaces) {
                        spaces = true;
                        rc.append(' ');
                    }
                    break;
                default:
                    spaces = false;
                    rc.append(c);
                    break;
            }
        }
        return rc.toString().trim();
    }

    /**
     * Create URN from public id.
     *
     * @param publicId the public id to encode
     * @return the encoded id
     */
    public static String encodeURN(final String publicId) {
        final String normalizedPublicId = normalize(publicId);
        final int n = normalizedPublicId.length();
        // CHECKSTYLE:OFF
        final StringBuilder rc = new StringBuilder(n + 16);
        // CHECKSTYLE:ON
        rc.append(URN_PUBLIC_ID);
        for (int i = 0; i < n; i++) {
            final char c = normalizedPublicId.charAt(i);
            switch (c) {
                case ' ':
                    rc.append('+');
                    break;
                case '/':
                    if (i < n - 1 && normalizedPublicId.charAt(i + 1) == '/') {
                        rc.append(':');
                        i++;
                    } else {
                        rc.append("%2F");
                    }
                    break;
                case ':':
                    if (i < n - 1 && normalizedPublicId.charAt(i + 1) == ':') {
                        rc.append(';');
                        i++;
                    } else {
                        rc.append("%3A");
                    }
                    break;
                case '+':
                case ';':
                case '\'':
                case '?':
                case '#':
                case '%':
                    rc.append('%');
                    HexUtil.appendHexByteUppercase(rc, (byte) c);
                    break;
                default:
                    rc.append(c);
                    break;
            }
        }
        return rc.toString();
    }

    /**
     * Create public id from URN.
     *
     * @param urn the public id to encode
     * @return the encoded id
     */
    public static String decodeURN(final String urn) {
        if (!isPublicIdURN(urn)) {
            throw new IllegalArgumentException("Non-publicId URN");
        }
        final int n = urn.length();
        final StringBuilder rc = new StringBuilder(n);
        for (int i = URN_PUBLIC_ID.length(); i < n; i++) {
            final char c = urn.charAt(i);
            switch (c) {
                case '+':
                    rc.append(' ');
                    break;
                case ':':
                    rc.append("//");
                    break;
                case ';':
                    rc.append("::");
                    break;
                case '%':
                    if (i + 2 >= n) {
                        throw new IllegalArgumentException("Invalid hex sequence size: " + urn);
                    }
                    // CHECKSTYLE:OFF
                    rc.append((char) ((int) HexUtil.getHexByte(urn, i + 1) & 0xFF));
                    // CHECKSTYLE:ON
                    i += 2;
                    break;
                default:
                    rc.append(c);
                    break;
            }
        }
        return rc.toString();
    }

    /**
     * Check if URN is public id one.
     *
     * @param urn the urn to check
     * @return true if public id URN
     */
    public static boolean isPublicIdURN(final String urn) {
        return urn != null && urn.substring(0, URN_PUBLIC_ID.length()).toLowerCase(Locale.US).startsWith(URN_PUBLIC_ID);
    }
}
