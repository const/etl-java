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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

/**
 * The key that is an immutable sequence of bytes that could act as key in hash tables. Note that comparison works as
 * if bytes were unsigned (so order over toString values and key values match).
 */
public final class BinaryKey implements Comparable<BinaryKey> {
    /**
     * The empty key.
     */
    public static final BinaryKey EMPTY = new BinaryKey(new byte[0]);
    /**
     * The key's data.
     */
    private final byte[] data;

    /**
     * The private constructor from data.
     *
     * @param data the data to use
     */
    private BinaryKey(final byte[] data) { // NOPMD
        this.data = data;
    }

    /**
     * Decode key from hex string.
     *
     * @param hex the hex string
     * @return the decoded key
     */
    public static BinaryKey fromString(final String hex) {
        if (hex.length() == 0) {
            return EMPTY;
        }
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid number of characters: " + hex);
        }
        final int n = hex.length() / 2;
        final byte[] data = new byte[n];
        for (int i = 0; i < n; i++) {
            data[i] = HexUtil.getHexByte(hex, i * 2);
        }
        return new BinaryKey(data);
    }

    /**
     * Get binary key from message digest.
     *
     * @param digest the digest to analyse
     * @return the resulting binary key
     */
    public static BinaryKey fromDigest(final MessageDigest digest) {
        return new BinaryKey(digest.digest());
    }

    /**
     * Binary key from bytes.
     *
     * @param data   the data to use
     * @param offset the offset in the data
     * @param length the length in the data
     * @return the resulting key
     */
    public static BinaryKey fromBytes(final byte[] data, final int offset, final int length) {
        if (length == 0) {
            return EMPTY;
        }
        final byte[] c = new byte[length];
        System.arraycopy(data, offset, c, 0, length);
        return new BinaryKey(c);
    }

    /**
     * Binary key from bytes.
     *
     * @param data the data to use
     * @return the resulting key
     */
    public static BinaryKey fromBytes(final byte[] data) {
        return fromBytes(data, 0, data.length);
    }

    /**
     * Binary key from hash.
     *
     * @param data   the data to use
     * @param offset the offset in the data
     * @param length the length in the data
     * @return the resulting key
     */
    public static BinaryKey sha1(final byte[] data, final int offset, final int length) {
        try {
            final MessageDigest d = MessageDigest.getInstance("SHA-1");
            d.update(data, offset, length);
            return fromDigest(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 not found", e);
        }
    }

    /**
     * User random number generator to generate key of the specified length.
     *
     * @param random the random number generator ({@link java.security.SecureRandom} is supposed to be used)
     * @param length the length of the key
     * @return the newly generated key
     */
    public static BinaryKey fromRandom(final Random random, final int length) {
        if (length == 0) {
            return EMPTY;
        }
        final byte[] data = new byte[length];
        random.nextBytes(data);
        return new BinaryKey(data);
    }

    /**
     * Create binary key from long value.
     *
     * @param l the long value to process
     * @return the binary key
     */
    public static BinaryKey fromLong(final long l) {
        // CHECKSTYLE:OFF
        return new BinaryKey(new byte[]{
                (byte) (l >> 56),
                (byte) (l >> 48),
                (byte) (l >> 40),
                (byte) (l >> 32),
                (byte) (l >> 24),
                (byte) (l >> 16),
                (byte) (l >> 8),
                (byte) l});
        // CHECKSTYLE:ON
    }

    /**
     * Binary key from bytes.
     *
     * @param data the data to use
     * @return the resulting key
     */
    public static BinaryKey sha1(final byte[] data) {
        return sha1(data, 0, data.length);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof BinaryKey)) {
            return false;
        }
        final BinaryKey o = (BinaryKey) obj;
        return Arrays.equals(o.data, data);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(data.length * 2);
        for (final byte b : data) {
            HexUtil.appendHexByte(sb, b);
        }
        return sb.toString();
    }

    /**
     * @return the key that represent SHA-1 code of this key
     */
    public BinaryKey toSha1() {
        return sha1(data);
    }

    @Override
    public int compareTo(final BinaryKey o) {
        final int n = Math.min(data.length, o.data.length);
        for (int i = 0; i < n; i++) {
            // CHECKSTYLE:OFF
            final int d = (int) data[i] & 0xFF - (int) o.data[i] & 0xFF;
            // CHECKSTYLE:ON
            if (d != 0) {
                return d;
            }
        }
        return data.length - o.data.length;
    }
}
