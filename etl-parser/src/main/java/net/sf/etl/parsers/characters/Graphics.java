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
 * This class classify graphics characters. The class uses Unicode 6.1.
 */
public final class Graphics {
    /**
     * Private constructor for utility class.
     */
    private Graphics() {
        // do nothing
    }

    /**
     * Check if the character is some from of comma.
     *
     * @param codepoint the codepoint to check
     * @return true if comma, false otherwise
     */
    public static boolean isComma(final int codepoint) {
        //CHECKSTYLE:OFF
        switch (codepoint) {
            case 0x002C: // Po: COMMA
            case 0x055D: // Po: ARMENIAN COMMA
            case 0x060C: // Po: ARABIC COMMA
            case 0x07F8: // Po: NKO COMMA
            case 0x0F14: // Po: TIBETAN MARK GTER TSHEG (TIBETAN COMMA)
            case 0x1363: // Po: ETHIOPIC COMMA
            case 0x1802: // Po: MONGOLIAN COMMA
            case 0x1808: // Po: MONGOLIAN MANCHU COMMA
            case 0x2E32: // Po: TURNED COMMA
            case 0x2E34: // Po: RAISED COMMA
            case 0x3001: // Po: IDEOGRAPHIC COMMA
            case 0xA4FE: // Po: LISU PUNCTUATION COMMA
            case 0xA60D: // Po: VAI COMMA
            case 0xA6F5: // Po: BAMUM COMMA
            case 0xFE10: // Po: PRESENTATION FORM FOR VERTICAL COMMA
            case 0xFE11: // Po: PRESENTATION FORM FOR VERTICAL IDEOGRAPHIC COMMA
            case 0xFE50: // Po: SMALL COMMA
            case 0xFE51: // Po: SMALL IDEOGRAPHIC COMMA
            case 0xFF0C: // Po: FULLWIDTH COMMA
            case 0xFF64: // Po: HALFWIDTH IDEOGRAPHIC COMMA
                return true;
            default:
                return false;
        }
        //CHECKSTYLE:ON
    }

    /**
     * Check if the character is some from of semicolon.
     *
     * @param codepoint the codepoint to check
     * @return true if semicolon, false otherwise
     */
    public static boolean isSemicolon(final int codepoint) {
        //CHECKSTYLE:OFF
        switch (codepoint) {
            case 0x003B: // Po: SEMICOLON
            case 0x061B: // Po: ARABIC SEMICOLON
            case 0x1364: // Po: ETHIOPIC SEMICOLON
            case 0x204F: // Po: REVERSED SEMICOLON
            case 0x2E35: // Po: TURNED SEMICOLON
            case 0xA6F6: // Po: BAMUM SEMICOLON
            case 0xFE14: // Po: PRESENTATION FORM FOR VERTICAL SEMICOLON
            case 0xFE54: // Po: SMALL SEMICOLON
            case 0xFF1B: // Po: FULLWIDTH SEMICOLON
                return true;
            default:
                return false;
        }
        //CHECKSTYLE:ON
    }

    //CHECKSTYLE:OFF

    /**
     * Check if codepoint is a graphics character. The graphics characters belong to the following categories:
     * <table>
     * <tr><td>Pd</td><td>Dash_Punctuation</td><td>a dash or hyphen punctuation mark</td></tr>
     * <tr><td>Po</td><td>Other_Punctuation</td><td>a punctuation mark of other type</td></tr>
     * <tr><td>Sm</td><td>Math_Symbol</td><td>a symbol of mathematical use</td></tr>
     * <tr><td>Sc</td><td>Currency_Symbol</td><td>a currency sign</td></tr>
     * <tr><td>Sk</td><td>Modifier_Symbol</td><td>a non-letterlike modifier symbol</td></tr>
     * <tr><td>So</td><td>Other_Symbol</td><td>a symbol of other type</td></tr>
     * </table>
     * Except for for the following categories:
     * <ul>
     * <li>Quotes</li>
     * <li>Designated separator characters (currently only all forms of comma(",") and semicolon (";")</li>
     * </ul>
     *
     * @param codepoint the codepoint to check
     * @return true if the character is a graphics character
     */
    @SuppressWarnings({"RedundantIfStatement", "PMD"})
    public static boolean isGraphics(final int codepoint) { // NOPMD
        // the method is generated by GraphicsCheckGenerator utility
        final int high = codepoint >> 8;
        final int low = codepoint & 0xFF;
        switch (high) {
            case 0x0:
                switch (low) {
                    case 0x21:
                    case 0x2a:
                    case 0x2b:
                    case 0x2d:
                    case 0x2e:
                    case 0x2f:
                    case 0x3a:
                    case 0x5c:
                    case 0x5e:
                    case 0x60:
                    case 0x7c:
                    case 0x7e:
                    case 0xac:
                    case 0xb4:
                    case 0xb6:
                    case 0xb7:
                    case 0xb8:
                    case 0xbf:
                    case 0xd7:
                    case 0xf7:
                        return true;
                    default:
                        if (0x23 <= low && low <= 0x26) return true;
                        if (0x3c <= low && low <= 0x40) return true;
                        if (0xa1 <= low && low <= 0xa9) return true;
                        if (0xae <= low && low <= 0xb1) return true;
                        return false;
                }
            case 0x2:
                switch (low) {
                    case 0xed:
                        return true;
                    default:
                        if (0xc2 <= low && low <= 0xc5) return true;
                        if (0xd2 <= low && low <= 0xdf) return true;
                        if (0xe5 <= low && low <= 0xeb) return true;
                        if (0xef <= low) return true;
                        return false;
                }
            case 0x3:
                switch (low) {
                    case 0x75:
                    case 0x7e:
                    case 0x84:
                    case 0x85:
                    case 0x87:
                    case 0xf6:
                        return true;
                    default:
                        return false;
                }
            case 0x4:
                switch (low) {
                    case 0x82:
                        return true;
                    default:
                        return false;
                }
            case 0x5:
                switch (low) {
                    case 0x5a:
                    case 0x5b:
                    case 0x5c:
                    case 0x5e:
                    case 0x5f:
                    case 0x89:
                    case 0x8a:
                    case 0x8f:
                    case 0xbe:
                    case 0xc0:
                    case 0xc3:
                    case 0xc6:
                    case 0xf3:
                    case 0xf4:
                        return true;
                    default:
                        return false;
                }
            case 0x6:
                switch (low) {
                    case 0xd:
                    case 0xe:
                    case 0xf:
                    case 0x1e:
                    case 0x1f:
                    case 0xd4:
                    case 0xde:
                    case 0xe9:
                    case 0xfd:
                    case 0xfe:
                        return true;
                    default:
                        if (0x6 <= low && low <= 0xb) return true;
                        if (0x6a <= low && low <= 0x6d) return true;
                        return false;
                }
            case 0x7:
                switch (low) {
                    case 0xf6:
                    case 0xf7:
                    case 0xf9:
                        return true;
                    default:
                        if (low <= 0xd) return true;
                        return false;
                }
            case 0x8:
                switch (low) {
                    case 0x5e:
                        return true;
                    default:
                        if (0x30 <= low && low <= 0x3e) return true;
                        return false;
                }
            case 0x9:
                switch (low) {
                    case 0x64:
                    case 0x65:
                    case 0x70:
                    case 0xf2:
                    case 0xf3:
                    case 0xfa:
                    case 0xfb:
                        return true;
                    default:
                        return false;
                }
            case 0xa:
                switch (low) {
                    case 0xf0:
                    case 0xf1:
                        return true;
                    default:
                        return false;
                }
            case 0xb:
                switch (low) {
                    case 0x70:
                        return true;
                    default:
                        if (0xf3 <= low && low <= 0xfa) return true;
                        return false;
                }
            case 0xc:
                switch (low) {
                    case 0x7f:
                        return true;
                    default:
                        return false;
                }
            case 0xd:
                switch (low) {
                    case 0x79:
                    case 0xf4:
                        return true;
                    default:
                        return false;
                }
            case 0xe:
                switch (low) {
                    case 0x3f:
                    case 0x4f:
                    case 0x5a:
                    case 0x5b:
                        return true;
                    default:
                        return false;
                }
            case 0xf:
                switch (low) {
                    case 0x15:
                    case 0x16:
                    case 0x17:
                    case 0x34:
                    case 0x36:
                    case 0x38:
                    case 0x85:
                        return true;
                    default:
                        if (0x1 <= low && low <= 0x13) return true;
                        if (0x1a <= low && low <= 0x1f) return true;
                        if (0xbe <= low && low <= 0xc5) return true;
                        if (0xc7 <= low && low <= 0xcc) return true;
                        if (0xce <= low && low <= 0xda) return true;
                        return false;
                }
            case 0x10:
                switch (low) {
                    case 0x9e:
                    case 0x9f:
                    case 0xfb:
                        return true;
                    default:
                        if (0x4a <= low && low <= 0x4f) return true;
                        return false;
                }
            case 0x13:
                switch (low) {
                    case 0x60:
                    case 0x61:
                    case 0x62:
                        return true;
                    default:
                        if (0x65 <= low && low <= 0x68) return true;
                        if (0x90 <= low && low <= 0x99) return true;
                        return false;
                }
            case 0x14:
                switch (low) {
                    case 0x0:
                        return true;
                    default:
                        return false;
                }
            case 0x16:
                switch (low) {
                    case 0x6d:
                    case 0x6e:
                    case 0xeb:
                    case 0xec:
                    case 0xed:
                        return true;
                    default:
                        return false;
                }
            case 0x17:
                switch (low) {
                    case 0x35:
                    case 0x36:
                    case 0xd4:
                    case 0xd5:
                    case 0xd6:
                        return true;
                    default:
                        if (0xd8 <= low && low <= 0xdb) return true;
                        return false;
                }
            case 0x18:
                switch (low) {
                    case 0x0:
                    case 0x1:
                    case 0x9:
                    case 0xa:
                        return true;
                    default:
                        if (0x3 <= low && low <= 0x7) return true;
                        return false;
                }
            case 0x19:
                switch (low) {
                    case 0x40:
                    case 0x44:
                    case 0x45:
                        return true;
                    default:
                        if (0xde <= low) return true;
                        return false;
                }
            case 0x1a:
                switch (low) {
                    case 0x1e:
                    case 0x1f:
                        return true;
                    default:
                        if (0xa0 <= low && low <= 0xa6) return true;
                        if (0xa8 <= low && low <= 0xad) return true;
                        return false;
                }
            case 0x1b:
                if (0x5a <= low && low <= 0x6a) return true;
                if (0x74 <= low && low <= 0x7c) return true;
                if (0xfc <= low) return true;
                return false;
            case 0x1c:
                switch (low) {
                    case 0x7e:
                    case 0x7f:
                    case 0xd3:
                        return true;
                    default:
                        if (0x3b <= low && low <= 0x3f) return true;
                        if (0xc0 <= low && low <= 0xc7) return true;
                        return false;
                }
            case 0x1f:
                switch (low) {
                    case 0xbd:
                    case 0xbf:
                    case 0xc0:
                    case 0xc1:
                    case 0xcd:
                    case 0xce:
                    case 0xcf:
                    case 0xdd:
                    case 0xde:
                    case 0xdf:
                    case 0xed:
                    case 0xee:
                    case 0xef:
                    case 0xfd:
                    case 0xfe:
                        return true;
                    default:
                        return false;
                }
            case 0x20:
                switch (low) {
                    case 0x7a:
                    case 0x7b:
                    case 0x7c:
                    case 0x8a:
                    case 0x8b:
                    case 0x8c:
                        return true;
                    default:
                        if (0x10 <= low && low <= 0x17) return true;
                        if (0x20 <= low && low <= 0x27) return true;
                        if (0x30 <= low && low <= 0x38) return true;
                        if (0x3b <= low && low <= 0x3e) return true;
                        if (0x41 <= low && low <= 0x44) return true;
                        if (0x47 <= low && low <= 0x4e) return true;
                        if (0x50 <= low && low <= 0x53) return true;
                        if (0x55 <= low && low <= 0x5e) return true;
                        if (0xa0 <= low && low <= 0xb9) return true;
                        return false;
                }
            case 0x21:
                switch (low) {
                    case 0x0:
                    case 0x1:
                    case 0x8:
                    case 0x9:
                    case 0x14:
                    case 0x16:
                    case 0x17:
                    case 0x18:
                    case 0x25:
                    case 0x27:
                    case 0x29:
                    case 0x2e:
                    case 0x3a:
                    case 0x3b:
                    case 0x4f:
                        return true;
                    default:
                        if (0x3 <= low && low <= 0x6) return true;
                        if (0x1e <= low && low <= 0x23) return true;
                        if (0x40 <= low && low <= 0x44) return true;
                        if (0x4a <= low && low <= 0x4d) return true;
                        if (0x90 <= low) return true;
                        return false;
                }
            case 0x22:
                return true;
            case 0x23:
                if (low <= 0x28) return true;
                if (0x2b <= low && low <= 0xf3) return true;
                return false;
            case 0x24:
                if (low <= 0x26) return true;
                if (0x40 <= low && low <= 0x4a) return true;
                if (0x9c <= low && low <= 0xe9) return true;
                return false;
            case 0x25:
                return true;
            case 0x26:
                return true;
            case 0x27:
                if (0x1 <= low && low <= 0x67) return true;
                if (0x94 <= low && low <= 0xc4) return true;
                if (0xc7 <= low && low <= 0xe5) return true;
                if (0xf0 <= low) return true;
                return false;
            case 0x28:
                return true;
            case 0x29:
                switch (low) {
                    case 0xfe:
                    case 0xff:
                        return true;
                    default:
                        if (low <= 0x82) return true;
                        if (0x99 <= low && low <= 0xd7) return true;
                        if (0xdc <= low && low <= 0xfb) return true;
                        return false;
                }
            case 0x2a:
                return true;
            case 0x2b:
                if (low <= 0x4c) return true;
                if (0x50 <= low && low <= 0x59) return true;
                return false;
            case 0x2c:
                switch (low) {
                    case 0xfe:
                    case 0xff:
                        return true;
                    default:
                        if (0xe5 <= low && low <= 0xea) return true;
                        if (0xf9 <= low && low <= 0xfc) return true;
                        return false;
                }
            case 0x2d:
                switch (low) {
                    case 0x70:
                        return true;
                    default:
                        return false;
                }
            case 0x2e:
                switch (low) {
                    case 0x0:
                    case 0x1:
                    case 0x6:
                    case 0x7:
                    case 0x8:
                    case 0xb:
                    case 0x1e:
                    case 0x1f:
                    case 0x30:
                    case 0x31:
                    case 0x33:
                        return true;
                    default:
                        if (0xe <= low && low <= 0x1b) return true;
                        if (0x2a <= low && low <= 0x2e) return true;
                        if (0x36 <= low && low <= 0x3b) return true;
                        if (0x80 <= low && low <= 0x99) return true;
                        if (0x9b <= low && low <= 0xf3) return true;
                        return false;
                }
            case 0x2f:
                if (low <= 0xd5) return true;
                if (0xf0 <= low && low <= 0xfb) return true;
                return false;
            case 0x30:
                switch (low) {
                    case 0x2:
                    case 0x3:
                    case 0x4:
                    case 0x12:
                    case 0x13:
                    case 0x1c:
                    case 0x20:
                    case 0x30:
                    case 0x36:
                    case 0x37:
                    case 0x3d:
                    case 0x3e:
                    case 0x3f:
                    case 0x9b:
                    case 0x9c:
                    case 0xa0:
                    case 0xfb:
                        return true;
                    default:
                        return false;
                }
            case 0x31:
                switch (low) {
                    case 0x90:
                    case 0x91:
                        return true;
                    default:
                        if (0x96 <= low && low <= 0x9f) return true;
                        if (0xc0 <= low && low <= 0xe3) return true;
                        return false;
                }
            case 0x32:
                switch (low) {
                    case 0x50:
                        return true;
                    default:
                        if (low <= 0x1e) return true;
                        if (0x2a <= low && low <= 0x47) return true;
                        if (0x60 <= low && low <= 0x7f) return true;
                        if (0x8a <= low && low <= 0xb0) return true;
                        if (0xc0 <= low && low <= 0xfe) return true;
                        return false;
                }
            case 0x33:
                return true;
            case 0x4d:
                if (0xc0 <= low) return true;
                return false;
            case 0xa4:
                switch (low) {
                    case 0xff:
                        return true;
                    default:
                        if (0x90 <= low && low <= 0xc6) return true;
                        return false;
                }
            case 0xa6:
                switch (low) {
                    case 0xe:
                    case 0xf:
                    case 0x73:
                    case 0x7e:
                    case 0xf2:
                    case 0xf3:
                    case 0xf4:
                    case 0xf7:
                        return true;
                    default:
                        return false;
                }
            case 0xa7:
                switch (low) {
                    case 0x20:
                    case 0x21:
                    case 0x89:
                    case 0x8a:
                        return true;
                    default:
                        if (low <= 0x16) return true;
                        return false;
                }
            case 0xa8:
                switch (low) {
                    case 0xce:
                    case 0xcf:
                    case 0xf8:
                    case 0xf9:
                    case 0xfa:
                        return true;
                    default:
                        if (0x28 <= low && low <= 0x2b) return true;
                        if (0x36 <= low && low <= 0x39) return true;
                        if (0x74 <= low && low <= 0x77) return true;
                        return false;
                }
            case 0xa9:
                switch (low) {
                    case 0x2e:
                    case 0x2f:
                    case 0x5f:
                    case 0xde:
                    case 0xdf:
                        return true;
                    default:
                        if (0xc1 <= low && low <= 0xcd) return true;
                        return false;
                }
            case 0xaa:
                switch (low) {
                    case 0x77:
                    case 0x78:
                    case 0x79:
                    case 0xde:
                    case 0xdf:
                    case 0xf0:
                    case 0xf1:
                        return true;
                    default:
                        if (0x5c <= low && low <= 0x5f) return true;
                        return false;
                }
            case 0xab:
                switch (low) {
                    case 0xeb:
                        return true;
                    default:
                        return false;
                }
            case 0xfb:
                switch (low) {
                    case 0x29:
                        return true;
                    default:
                        if (0xb2 <= low && low <= 0xc1) return true;
                        return false;
                }
            case 0xfd:
                switch (low) {
                    case 0xfc:
                    case 0xfd:
                        return true;
                    default:
                        return false;
                }
            case 0xfe:
                switch (low) {
                    case 0x12:
                    case 0x13:
                    case 0x15:
                    case 0x16:
                    case 0x19:
                    case 0x30:
                    case 0x31:
                    case 0x32:
                    case 0x45:
                    case 0x46:
                    case 0x52:
                        return true;
                    default:
                        if (0x49 <= low && low <= 0x4c) return true;
                        if (0x55 <= low && low <= 0x58) return true;
                        if (0x5f <= low && low <= 0x66) return true;
                        if (0x68 <= low && low <= 0x6b) return true;
                        return false;
                }
            case 0xff:
                switch (low) {
                    case 0x1:
                    case 0xa:
                    case 0xb:
                    case 0xd:
                    case 0xe:
                    case 0xf:
                    case 0x1a:
                    case 0x3c:
                    case 0x3e:
                    case 0x40:
                    case 0x5c:
                    case 0x5e:
                    case 0x61:
                    case 0x65:
                    case 0xfc:
                    case 0xfd:
                        return true;
                    default:
                        if (0x3 <= low && low <= 0x7) return true;
                        if (0x1c <= low && low <= 0x20) return true;
                        if (0xe0 <= low && low <= 0xe6) return true;
                        if (0xe8 <= low && low <= 0xee) return true;
                        return false;
                }
            case 0x101:
                switch (low) {
                    case 0x0:
                    case 0x1:
                    case 0x2:
                        return true;
                    default:
                        if (0x37 <= low && low <= 0x3f) return true;
                        if (0x79 <= low && low <= 0x89) return true;
                        if (0x90 <= low && low <= 0x9b) return true;
                        if (0xd0 <= low && low <= 0xfc) return true;
                        return false;
                }
            case 0x103:
                switch (low) {
                    case 0x9f:
                    case 0xd0:
                        return true;
                    default:
                        return false;
                }
            case 0x108:
                switch (low) {
                    case 0x57:
                        return true;
                    default:
                        return false;
                }
            case 0x109:
                switch (low) {
                    case 0x1f:
                    case 0x3f:
                        return true;
                    default:
                        return false;
                }
            case 0x10a:
                switch (low) {
                    case 0x7f:
                        return true;
                    default:
                        if (0x50 <= low && low <= 0x58) return true;
                        return false;
                }
            case 0x10b:
                if (0x39 <= low && low <= 0x3f) return true;
                return false;
            case 0x110:
                switch (low) {
                    case 0xbb:
                    case 0xbc:
                        return true;
                    default:
                        if (0x47 <= low && low <= 0x4d) return true;
                        if (0xbe <= low && low <= 0xc1) return true;
                        return false;
                }
            case 0x111:
                if (0x40 <= low && low <= 0x43) return true;
                if (0xc5 <= low && low <= 0xc8) return true;
                return false;
            case 0x124:
                if (0x70 <= low && low <= 0x73) return true;
                return false;
            case 0x1d0:
                if (low <= 0xf5) return true;
                return false;
            case 0x1d1:
                switch (low) {
                    case 0x6a:
                    case 0x6b:
                    case 0x6c:
                    case 0x83:
                    case 0x84:
                        return true;
                    default:
                        if (low <= 0x26) return true;
                        if (0x29 <= low && low <= 0x64) return true;
                        if (0x8c <= low && low <= 0xa9) return true;
                        if (0xae <= low && low <= 0xdd) return true;
                        return false;
                }
            case 0x1d2:
                switch (low) {
                    case 0x45:
                        return true;
                    default:
                        if (low <= 0x41) return true;
                        return false;
                }
            case 0x1d3:
                if (low <= 0x56) return true;
                return false;
            case 0x1d6:
                switch (low) {
                    case 0xc1:
                    case 0xdb:
                    case 0xfb:
                        return true;
                    default:
                        return false;
                }
            case 0x1d7:
                switch (low) {
                    case 0x15:
                    case 0x35:
                    case 0x4f:
                    case 0x6f:
                    case 0x89:
                    case 0xa9:
                    case 0xc3:
                        return true;
                    default:
                        return false;
                }
            case 0x1ee:
                switch (low) {
                    case 0xf0:
                    case 0xf1:
                        return true;
                    default:
                        return false;
                }
            case 0x1f0:
                if (low <= 0x2b) return true;
                if (0x30 <= low && low <= 0x93) return true;
                if (0xa0 <= low && low <= 0xae) return true;
                if (0xb1 <= low && low <= 0xbe) return true;
                if (0xc1 <= low && low <= 0xcf) return true;
                if (0xd1 <= low && low <= 0xdf) return true;
                return false;
            case 0x1f1:
                if (0x10 <= low && low <= 0x2e) return true;
                if (0x30 <= low && low <= 0x6b) return true;
                if (0x70 <= low && low <= 0x9a) return true;
                if (0xe6 <= low) return true;
                return false;
            case 0x1f2:
                switch (low) {
                    case 0x0:
                    case 0x1:
                    case 0x2:
                    case 0x50:
                    case 0x51:
                        return true;
                    default:
                        if (0x10 <= low && low <= 0x3a) return true;
                        if (0x40 <= low && low <= 0x48) return true;
                        return false;
                }
            case 0x1f3:
                if (low <= 0x20) return true;
                if (0x30 <= low && low <= 0x35) return true;
                if (0x37 <= low && low <= 0x7c) return true;
                if (0x80 <= low && low <= 0x93) return true;
                if (0xa0 <= low && low <= 0xc4) return true;
                if (0xc6 <= low && low <= 0xca) return true;
                if (0xe0 <= low && low <= 0xf0) return true;
                return false;
            case 0x1f4:
                switch (low) {
                    case 0x40:
                        return true;
                    default:
                        if (low <= 0x3e) return true;
                        if (0x42 <= low && low <= 0xf7) return true;
                        if (0xf9 <= low && low <= 0xfc) return true;
                        return false;
                }
            case 0x1f5:
                if (low <= 0x3d) return true;
                if (0x40 <= low && low <= 0x43) return true;
                if (0x50 <= low && low <= 0x67) return true;
                if (0xfb <= low) return true;
                return false;
            case 0x1f6:
                if (low <= 0x40) return true;
                if (0x45 <= low && low <= 0x4f) return true;
                if (0x80 <= low && low <= 0xc5) return true;
                return false;
            case 0x1f7:
                if (low <= 0x73) return true;
                return false;
            default:
                return false;
        }
    }
    //CHECKSTYLE:ON
}
