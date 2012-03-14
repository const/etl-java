package net.sf.etl.parsers.characters;

/**
 * This class contains whitespace checks
 */
public class Whitespaces {
    /**
     * Unicode code point for carriage return
     */
    public static final int CR = 0x000D;
    /**
     * Unicode code point for carriage return
     */
    public static final int LF = 0x000A;
    /**
     * Check if codepoint is a new line. The Unicode standard specifies the following new lines.
     * <p/>
     * <table>
     * <tr><td>CR</td><td>carriage return</td><td>000D</td></tr>
     * <tr><td>LF</td><td>line feed</td><td>000A</td></tr>
     * <tr><td>CRLF</td><td> carriage return and    line feed</td><td>000D 000A</td></tr>
     * <tr><td>NEL</td><td> next line </td><td>0085</td></tr>
     * <tr><td>VT</td><td>vertical tab</td><td>000B</td></tr>
     * <tr><td>FF</td><td> form feed</td><td> 000C</td></tr>
     * <tr><td>LS</td><td> line separator</td><td>2028</td></tr>
     * <tr><td>PS</td><td> paragraph separator</td><td>2029</td></tr>
     * </table>
     *
     * @param codepoint the codepoint to check
     * @return the new lines
     */
    public static boolean isNewline(int codepoint) {
        switch (codepoint) {
            case 0x000A: // Cc: LINE FEED (LF)
            case 0x000B: // Cc: LINE TABULATION
            case 0x000C: // Cc: FORM FEED (FF)
            case 0x000D: // Cc: CARRIAGE RETURN (CR)
            case 0x0085: // Cc: NEXT LINE (NEL)
            case 0x2029: // Zp: PARAGRAPH SEPARATOR
            case 0x2028: // Zl: LINE SEPARATOR
                return true;
        }
        return false;
    }

    public static boolean isSpace(int codepoint) {
        switch (codepoint) {
            case 0x0009: // Cc: CHARACTER TABULATION
            case 0x0020: // Zs: SPACE
            case 0x00A0: // Zs: NO-BREAK SPACE
            case 0x1680: // Zs: OGHAM SPACE MARK
            case 0x180E: // Zs: MONGOLIAN VOWEL SEPARATOR
            case 0x2000: // Zs: EN QUAD
            case 0x2001: // Zs: EM QUAD
            case 0x2002: // Zs: EN SPACE
            case 0x2003: // Zs: EM SPACE
            case 0x2004: // Zs: THREE-PER-EM SPACE
            case 0x2005: // Zs: FOUR-PER-EM SPACE
            case 0x2006: // Zs: SIX-PER-EM SPACE
            case 0x2007: // Zs: FIGURE SPACE
            case 0x2008: // Zs: PUNCTUATION SPACE
            case 0x2009: // Zs: THIN SPACE
            case 0x200A: // Zs: HAIR SPACE
            case 0x202F: // Zs: NARROW NO-BREAK SPACE
            case 0x205F: // Zs: MEDIUM MATHEMATICAL SPACE
            case 0x3000: // Zs: IDEOGRAPHIC SPACE
                return true;
        }
        return false;
    }
}
