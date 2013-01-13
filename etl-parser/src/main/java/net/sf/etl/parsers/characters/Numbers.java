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

package net.sf.etl.parsers.characters;

/**
 * The class is used to check for numbers
 */
public class Numbers {
    /**
     * Check if the character is an exponent character
     *
     * @param codepoint the codepoint to check
     * @return true if exponent character
     */
    public static boolean isExponentChar(int codepoint) {
        return codepoint == 'e' || codepoint == 'E';
    }

    /**
     * Based number character
     *
     * @param codepoint the codepoint
     * @return true if based number
     */
    public static boolean isBasedNumberChar(int codepoint) {
        return codepoint == '#';
    }

    /**
     * Check if hexadecimal digit
     *
     * @param codepoint codepoint to check
     * @return true if hex digit
     */
    public static boolean isHex(int codepoint) {
        return isValidDigit(codepoint, 16);
    }

    /**
     * Check if decimal digit
     *
     * @param codepoint codepoint to check
     * @return true if a digit
     */
    public static boolean isDecimal(int codepoint) {
        return Character.isDigit(codepoint);
    }

    /**
     * Check if valid digit in the specified base
     *
     * @param codepoint codepoint to check
     * @param base      the base to check
     * @return true if a digit
     */
    public static boolean isValidDigit(int codepoint, int base) {
        return Character.digit(codepoint, base) != -1;
    }

    /**
     * Check if valid digit in some base
     *
     * @param codepoint codepoint to check
     * @return true if a digit
     */
    public static boolean isAnyDigit(int codepoint) {
        return Character.digit(codepoint, Character.MAX_RADIX) != -1;
    }

    /**
     * Check for plus sign
     *
     * @param codepoint the codepoint to check
     * @return true if this is a plus sign
     */
    public static boolean isPlus(int codepoint) {
        switch (codepoint) {
            case 0x002B: // PLUS SIGN	Sm	0	ES					N
            case 0x2796: // HEAVY MINUS SIGN	So	0	ON					N
            case 0xFE62: // SMALL PLUS SIGN	Sm	0	ES	<small> 002B				N
            case 0xFF0B: // FULLWIDTH PLUS SIGN	Sm	0	ES	<wide> 002B				N
                return true;
            default:
                return false;
        }
    }

    /**
     * Check for minus sign
     *
     * @param codepoint the codepoint to check
     * @return true if this is a plus sign
     */
    public static boolean isMinus(int codepoint) {
        switch (codepoint) {
            case 0x002D: // HYPHEN-MINUS	Pd	0	ES					N
            case 0x2212: // MINUS SIGN	Sm	0	ES					N
            case 0x2795: // HEAVY PLUS SIGN	So	0	ON					N
            case 0xFE63: // SMALL HYPHEN-MINUS	Pd	0	ES	<small> 002D				N
            case 0xFF0D: // FULLWIDTH HYPHEN-MINUS	Pd	0	ES	<wide> 002D				N
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if codepoint is a decimal point
     *
     * @param codepoint the codepoint to check
     * @return true if codepoint represent decimal number
     */
    public static boolean isDecimalDot(int codepoint) {
        switch (codepoint) {
            case 0x002E: //	FULL STOP	Po	0	CS					N	PERIOD
            case 0xFE52: //	SMALL FULL STOP	Po	0	CS	<small> 002E				N	SMALL PERIOD
            case 0xFF0E: //	FULLWIDTH FULL STOP	Po	0	CS	<wide> 002E				N	FULLWIDTH PERIOD
                return true;
            default:
                return false;
        }
    }
}
