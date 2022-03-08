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
 * This is class provides checks for identifiers. Identifiers are unicode identifiers except that they could start
 * from connector character as well.
 */
public final class Identifiers {
    /**
     * Private constructor for utility class.
     */
    private Identifiers() {
        // do nothing
    }

    /**
     * Check if the character is unicode connector character ("Pc" category).
     *
     * @param c the character to check
     * @return true if the character is a connector character
     */
    public static boolean isConnectorChar(final int c) {
        //CHECKSTYLE:OFF
        switch (c) {
            case 0x005F: // Pc: LOW LINE
            case 0x203F: // Pc: UNDERTIE
            case 0x2040: // Pc: CHARACTER TIE
            case 0x2054: // Pc: INVERTED UNDERTIE
            case 0xFE33: // Pc: PRESENTATION FORM FOR VERTICAL LOW LINE
            case 0xFE34: // Pc: PRESENTATION FORM FOR VERTICAL WAVY LOW LINE
            case 0xFE4D: // Pc: DASHED LOW LINE
            case 0xFE4E: // Pc: CENTRELINE LOW LINE
            case 0xFE4F: // Pc: WAVY LOW LINE
            case 0xFF3F: // Pc: FULLWIDTH LOW LINE
                return true;
            default:
                return false;
        }
        //CHECKSTYLE:ON
    }

    /**
     * Check if the character is a valid start of the identifier. Currently the method relies on Java implementation
     * of this check. This check might fail to conform to Unicode 6.0.
     *
     * @param codepoint the code point to test
     * @return true if the character is a valid identifier start character
     */
    public static boolean isIdentifierStart(final int codepoint) {
        return isConnectorChar(codepoint) || Character.isUnicodeIdentifierStart(codepoint);
    }

    /**
     * Check if the character is a valid part of the identifier. Currently the method relies on Java implementation
     * of this check. This check might fail to conform to Unicode 6.0.
     *
     * @param codepoint the code point to test
     * @return true if the character is a valid identifier start character
     */
    public static boolean isIdentifierPart(final int codepoint) {
        return Character.isUnicodeIdentifierPart(codepoint);
    }
}
