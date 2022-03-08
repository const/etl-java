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

package net.sf.etl.parsers.characters;

/**
 * The class is used to check for numbers.
 */
public final class Numbers {
    /**
     * The hex base.
     */
    public static final int HEX = 16;
    /**
     * The decimal base.
     */
    public static final int DECIMAL = 10;
    /**
     * The binary base.
     */
    public static final int BINARY = 2;

    /**
     * Private constructor for utility class.
     */
    private Numbers() {
        // do nothing
    }

    /**
     * Check if the character is an decimal exponent character.
     *
     * @param codepoint the codepoint to check
     * @return true if exponent character
     */
    public static boolean isDecimalExponentChar(final int codepoint) {
        return codepoint == 'e' || codepoint == 'E' || codepoint == '\u23E8';
    }

    /**
     * Check if the character is an decimal exponent character.
     *
     * @param codepoint the codepoint to check
     * @return true if exponent character
     */
    public static boolean isBinaryExponentChar(final int codepoint) {
        return codepoint == 'p' || codepoint == 'P' || codepoint == '\u2082';
    }

    /**
     * Check if the character is a valid exponent character for the specified base.
     *
     * @param codepoint the codepoint to check
     * @param base      the base
     * @return true if exponent character
     */
    public static boolean isExponentChar(final int codepoint, final int base) {
        if (base == DECIMAL) {
            return isDecimalExponentChar(codepoint);
        } else if (base == HEX || base == BINARY) {
            return isBinaryExponentChar(codepoint);
        } else {
            throw new IllegalStateException("The base is not supported: " + base);
        }
    }

    /**
     * Check if hexadecimal digit.
     *
     * @param codepoint codepoint to check
     * @return true if hex digit
     */
    public static boolean isHex(final int codepoint) {
        return isValidDigit(codepoint, Numbers.HEX);
    }

    /**
     * Check if decimal digit.
     *
     * @param codepoint codepoint to check
     * @return true if a digit
     */
    public static boolean isDecimal(final int codepoint) {
        return Character.isDigit(codepoint);
    }

    /**
     * Check if valid digit in the specified base.
     *
     * @param codepoint codepoint to check
     * @param base      the base to check
     * @return true if a digit
     */
    public static boolean isValidDigit(final int codepoint, final int base) {
        return digit(codepoint, base) != -1;
    }

    /**
     * Check for plus sign.
     *
     * @param codepoint the codepoint to check
     * @return true if this is a plus sign
     */
    public static boolean isPlus(final int codepoint) {
        //CHECKSTYLE:OFF
        switch (codepoint) {
            case 0x002B: // PLUS SIGN	Sm	0	ES					N
            case 0x2795: // HEAVY PLUS SIGN	So	0	ON					N
            case 0xFE62: // SMALL PLUS SIGN	Sm	0	ES	<small> 002B				N
            case 0xFF0B: // FULLWIDTH PLUS SIGN	Sm	0	ES	<wide> 002B				N
                return true;
            default:
                return false;
        }
        //CHECKSTYLE:ON
    }

    /**
     * Check for minus sign.
     *
     * @param codepoint the codepoint to check
     * @return true if this is a plus sign
     */
    public static boolean isMinus(final int codepoint) {
        //CHECKSTYLE:OFF
        switch (codepoint) {
            case 0x002D: // HYPHEN-MINUS	Pd	0	ES					N
            case 0x2212: // MINUS SIGN	Sm	0	ES					N
            case 0x2796: // HEAVY MINUS SIGN	So	0	ON					N
            case 0xFE63: // SMALL HYPHEN-MINUS	Pd	0	ES	<small> 002D				N
            case 0xFF0D: // FULLWIDTH HYPHEN-MINUS	Pd	0	ES	<wide> 002D				N
                return true;
            default:
                return false;
        }
        //CHECKSTYLE:ON
    }

    /**
     * Check if acodepoint is a decimal point.
     *
     * @param codepoint the codepoint to check
     * @return true if codepoint represent decimal number
     */
    public static boolean isDecimalDot(final int codepoint) {
        //CHECKSTYLE:OFF
        switch (codepoint) {
            case 0x002E: //	FULL STOP	Po	0	CS					N	PERIOD
            case 0xFE52: //	SMALL FULL STOP	Po	0	CS	<small> 002E				N	SMALL PERIOD
            case 0xFF0E: //	FULLWIDTH FULL STOP	Po	0	CS	<wide> 002E				N	FULLWIDTH PERIOD
                return true;
            default:
                return false;
        }
        //CHECKSTYLE:ON
    }

    /**
     * Check if a codepoint is sign character.
     *
     * @param codepoint the codepoint
     * @return true if sign character (plus or minus)
     */
    public static boolean isSign(final int codepoint) {
        return isPlus(codepoint) || isMinus(codepoint);
    }

    /**
     * Get digit value in the specified radix.
     *
     * @param codepoint the codepoint
     * @param radix     the radix
     * @return the value (or -1 if it is not digit in this radix)
     */
    public static int digit(final int codepoint, final int radix) {
        return Character.digit(codepoint, radix);
    }

    /**
     * Get decimal digit value.
     *
     * @param codepoint the codepoint
     * @return the value (or -1 if non-decimal)
     */
    public static int digit(final int codepoint) {
        return digit(codepoint, DECIMAL);
    }

    /**
     * Check if indicator for binary number.
     *
     * @param codepoint the codepoint
     * @return the indicator
     */
    public static boolean isBinaryIndicator(final int codepoint) {
        return codepoint == 'b' || codepoint == 'B';
    }

    /**
     * Check if the hex indicator.
     *
     * @param codepoint the codepoint
     * @return the indicator
     */
    public static boolean isHexIndicator(final int codepoint) {
        return codepoint == 'x' || codepoint == 'X';
    }

    /**
     * Check if codepoint is a valid start fo number suffix.
     *
     * @param codepoint the codepoint to check
     * @return true if suffix start
     */
    public static boolean isValidNumberSuffixStart(final int codepoint) {
        return Identifiers.isIdentifierStart(codepoint)
                && !isDecimalExponentChar(codepoint)
                && !Identifiers.isConnectorChar(codepoint);
    }
}
