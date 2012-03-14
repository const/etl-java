package net.sf.etl.parsers.characters;

/**
 * This is class provides checks for identifiers. Identifiers are unicode identifiers except that they could start
 * from connector character as well.
 */
public class Identifiers {
    /**
     * Check if the character is unicode connector character ("Pc" category)
     *
     * @param c the character to check
     * @return true if the character is a connector character
     */
    public static boolean isConnectorChar(int c) {
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
    }

    /**
     * Check if the character is a valid start of the identifier. Currently the method relies on Java implementation
     * of this check. This check might fail to conform to Unicode 6.0.
     *
     * @param codepoint the code point to test
     * @return true if the character is a valid identifier start character
     */
    public static boolean isIdentifierStart(int codepoint) {
        return isConnectorChar(codepoint) || Character.isUnicodeIdentifierStart(codepoint);
    }

    /**
     * Check if the character is a valid part of the identifier. Currently the method relies on Java implementation
     * of this check. This check might fail to conform to Unicode 6.0.
     *
     * @param codepoint the code point to test
     * @return true if the character is a valid identifier start character
     */
    public static boolean isIdentifierPart(int codepoint) {
        return Character.isUnicodeIdentifierPart(codepoint);
    }
}
