package net.sf.etl.parsers.characters;

/**
 * The code to detect brackets. The brackets are all of the Pe and Ps categories except for quotes
 * (corner quotes could be reconsidered later). Curly braces are excluded because they are considered as block delimiters.
 */
public class Brackets {
    /**
     * Check if the codepoint is a bracket
     *
     * @param codepoint the codepoint
     * @return true if the codepoint is a bracket
     */
    public static boolean isBracket(int codepoint) {
        switch (codepoint) {
            // **** Ps characters *****
            case 0x0028: // Ps: LEFT PARENTHESIS
            case 0x005B: // Ps: LEFT SQUARE BRACKET
                // BLOCK DELIMITER: case 0x007B: // Ps: LEFT CURLY BRACKET
            case 0x0F3A: // Ps: TIBETAN MARK GUG RTAGS GYON
            case 0x0F3C: // Ps: TIBETAN MARK ANG KHANG GYON
            case 0x169B: // Ps: OGHAM FEATHER MARK
                // QUOTE: case 0x201A: // Ps: SINGLE LOW-9 QUOTATION MARK
                // QUOTE: case 0x201E: // Ps: DOUBLE LOW-9 QUOTATION MARK
            case 0x2045: // Ps: LEFT SQUARE BRACKET WITH QUILL
            case 0x207D: // Ps: SUPERSCRIPT LEFT PARENTHESIS
            case 0x208D: // Ps: SUBSCRIPT LEFT PARENTHESIS
            case 0x2329: // Ps: LEFT-POINTING ANGLE BRACKET
            case 0x2768: // Ps: MEDIUM LEFT PARENTHESIS ORNAMENT
            case 0x276A: // Ps: MEDIUM FLATTENED LEFT PARENTHESIS ORNAMENT
            case 0x276C: // Ps: MEDIUM LEFT-POINTING ANGLE BRACKET ORNAMENT
            case 0x276E: // Ps: HEAVY LEFT-POINTING ANGLE QUOTATION MARK ORNAMENT
            case 0x2770: // Ps: HEAVY LEFT-POINTING ANGLE BRACKET ORNAMENT
            case 0x2772: // Ps: LIGHT LEFT TORTOISE SHELL BRACKET ORNAMENT
            case 0x2774: // Ps: MEDIUM LEFT CURLY BRACKET ORNAMENT
            case 0x27C5: // Ps: LEFT S-SHAPED BAG DELIMITER
            case 0x27E6: // Ps: MATHEMATICAL LEFT WHITE SQUARE BRACKET
            case 0x27E8: // Ps: MATHEMATICAL LEFT ANGLE BRACKET
            case 0x27EA: // Ps: MATHEMATICAL LEFT DOUBLE ANGLE BRACKET
            case 0x27EC: // Ps: MATHEMATICAL LEFT WHITE TORTOISE SHELL BRACKET
            case 0x27EE: // Ps: MATHEMATICAL LEFT FLATTENED PARENTHESIS
            case 0x2983: // Ps: LEFT WHITE CURLY BRACKET
            case 0x2985: // Ps: LEFT WHITE PARENTHESIS
            case 0x2987: // Ps: Z NOTATION LEFT IMAGE BRACKET
            case 0x2989: // Ps: Z NOTATION LEFT BINDING BRACKET
            case 0x298B: // Ps: LEFT SQUARE BRACKET WITH UNDERBAR
            case 0x298D: // Ps: LEFT SQUARE BRACKET WITH TICK IN TOP CORNER
            case 0x298F: // Ps: LEFT SQUARE BRACKET WITH TICK IN BOTTOM CORNER
            case 0x2991: // Ps: LEFT ANGLE BRACKET WITH DOT
            case 0x2993: // Ps: LEFT ARC LESS-THAN BRACKET
            case 0x2995: // Ps: DOUBLE LEFT ARC GREATER-THAN BRACKET
            case 0x2997: // Ps: LEFT BLACK TORTOISE SHELL BRACKET
            case 0x29D8: // Ps: LEFT WIGGLY FENCE
            case 0x29DA: // Ps: LEFT DOUBLE WIGGLY FENCE
            case 0x29FC: // Ps: LEFT-POINTING CURVED ANGLE BRACKET
            case 0x2E22: // Ps: TOP LEFT HALF BRACKET
            case 0x2E24: // Ps: BOTTOM LEFT HALF BRACKET
            case 0x2E26: // Ps: LEFT SIDEWAYS U BRACKET
            case 0x2E28: // Ps: LEFT DOUBLE PARENTHESIS
            case 0x3008: // Ps: LEFT ANGLE BRACKET
            case 0x300A: // Ps: LEFT DOUBLE ANGLE BRACKET
                // QUOTE: case 0x300C: // Ps: LEFT CORNER BRACKET
                // QUOTE: case 0x300E: // Ps: LEFT WHITE CORNER BRACKET
            case 0x3010: // Ps: LEFT BLACK LENTICULAR BRACKET
            case 0x3014: // Ps: LEFT TORTOISE SHELL BRACKET
            case 0x3016: // Ps: LEFT WHITE LENTICULAR BRACKET
            case 0x3018: // Ps: LEFT WHITE TORTOISE SHELL BRACKET
            case 0x301A: // Ps: LEFT WHITE SQUARE BRACKET
                // QUOTE: case 0x301D: // Ps: REVERSED DOUBLE PRIME QUOTATION MARK
            case 0xFD3E: // Ps: ORNATE LEFT PARENTHESIS
            case 0xFE17: // Ps: PRESENTATION FORM FOR VERTICAL LEFT WHITE LENTICULAR BRACKET
            case 0xFE35: // Ps: PRESENTATION FORM FOR VERTICAL LEFT PARENTHESIS
            case 0xFE37: // Ps: PRESENTATION FORM FOR VERTICAL LEFT CURLY BRACKET
            case 0xFE39: // Ps: PRESENTATION FORM FOR VERTICAL LEFT TORTOISE SHELL BRACKET
            case 0xFE3B: // Ps: PRESENTATION FORM FOR VERTICAL LEFT BLACK LENTICULAR BRACKET
            case 0xFE3D: // Ps: PRESENTATION FORM FOR VERTICAL LEFT DOUBLE ANGLE BRACKET
            case 0xFE3F: // Ps: PRESENTATION FORM FOR VERTICAL LEFT ANGLE BRACKET
                // QUOTE: case 0xFE41: // Ps: PRESENTATION FORM FOR VERTICAL LEFT CORNER BRACKET
                // QUOTE: case 0xFE43: // Ps: PRESENTATION FORM FOR VERTICAL LEFT WHITE CORNER BRACKET
            case 0xFE47: // Ps: PRESENTATION FORM FOR VERTICAL LEFT SQUARE BRACKET
            case 0xFE59: // Ps: SMALL LEFT PARENTHESIS
            case 0xFE5B: // Ps: SMALL LEFT CURLY BRACKET
            case 0xFE5D: // Ps: SMALL LEFT TORTOISE SHELL BRACKET
            case 0xFF08: // Ps: FULLWIDTH LEFT PARENTHESIS
            case 0xFF3B: // Ps: FULLWIDTH LEFT SQUARE BRACKET
                // BLOCK DELIMITER: case 0xFF5B: // Ps: FULLWIDTH LEFT CURLY BRACKET
            case 0xFF5F: // Ps: FULLWIDTH LEFT WHITE PARENTHESIS
                // QUOTE: case 0xFF62: // Ps: HALFWIDTH LEFT CORNER BRACKET
                // **** Pe characters *****
            case 0x0029: // Pe: RIGHT PARENTHESIS
            case 0x005D: // Pe: RIGHT SQUARE BRACKET
                // BLOCK DELIMITER: case 0x007D: // Pe: RIGHT CURLY BRACKET
            case 0x0F3B: // Pe: TIBETAN MARK GUG RTAGS GYAS
            case 0x0F3D: // Pe: TIBETAN MARK ANG KHANG GYAS
            case 0x169C: // Pe: OGHAM REVERSED FEATHER MARK
            case 0x2046: // Pe: RIGHT SQUARE BRACKET WITH QUILL
            case 0x207E: // Pe: SUPERSCRIPT RIGHT PARENTHESIS
            case 0x208E: // Pe: SUBSCRIPT RIGHT PARENTHESIS
            case 0x232A: // Pe: RIGHT-POINTING ANGLE BRACKET
            case 0x2769: // Pe: MEDIUM RIGHT PARENTHESIS ORNAMENT
            case 0x276B: // Pe: MEDIUM FLATTENED RIGHT PARENTHESIS ORNAMENT
            case 0x276D: // Pe: MEDIUM RIGHT-POINTING ANGLE BRACKET ORNAMENT
            case 0x276F: // Pe: HEAVY RIGHT-POINTING ANGLE QUOTATION MARK ORNAMENT
            case 0x2771: // Pe: HEAVY RIGHT-POINTING ANGLE BRACKET ORNAMENT
            case 0x2773: // Pe: LIGHT RIGHT TORTOISE SHELL BRACKET ORNAMENT
            case 0x2775: // Pe: MEDIUM RIGHT CURLY BRACKET ORNAMENT
            case 0x27C6: // Pe: RIGHT S-SHAPED BAG DELIMITER
            case 0x27E7: // Pe: MATHEMATICAL RIGHT WHITE SQUARE BRACKET
            case 0x27E9: // Pe: MATHEMATICAL RIGHT ANGLE BRACKET
            case 0x27EB: // Pe: MATHEMATICAL RIGHT DOUBLE ANGLE BRACKET
            case 0x27ED: // Pe: MATHEMATICAL RIGHT WHITE TORTOISE SHELL BRACKET
            case 0x27EF: // Pe: MATHEMATICAL RIGHT FLATTENED PARENTHESIS
            case 0x2984: // Pe: RIGHT WHITE CURLY BRACKET
            case 0x2986: // Pe: RIGHT WHITE PARENTHESIS
            case 0x2988: // Pe: Z NOTATION RIGHT IMAGE BRACKET
            case 0x298A: // Pe: Z NOTATION RIGHT BINDING BRACKET
            case 0x298C: // Pe: RIGHT SQUARE BRACKET WITH UNDERBAR
            case 0x298E: // Pe: RIGHT SQUARE BRACKET WITH TICK IN BOTTOM CORNER
            case 0x2990: // Pe: RIGHT SQUARE BRACKET WITH TICK IN TOP CORNER
            case 0x2992: // Pe: RIGHT ANGLE BRACKET WITH DOT
            case 0x2994: // Pe: RIGHT ARC GREATER-THAN BRACKET
            case 0x2996: // Pe: DOUBLE RIGHT ARC LESS-THAN BRACKET
            case 0x2998: // Pe: RIGHT BLACK TORTOISE SHELL BRACKET
            case 0x29D9: // Pe: RIGHT WIGGLY FENCE
            case 0x29DB: // Pe: RIGHT DOUBLE WIGGLY FENCE
            case 0x29FD: // Pe: RIGHT-POINTING CURVED ANGLE BRACKET
            case 0x2E23: // Pe: TOP RIGHT HALF BRACKET
            case 0x2E25: // Pe: BOTTOM RIGHT HALF BRACKET
            case 0x2E27: // Pe: RIGHT SIDEWAYS U BRACKET
            case 0x2E29: // Pe: RIGHT DOUBLE PARENTHESIS
            case 0x3009: // Pe: RIGHT ANGLE BRACKET
            case 0x300B: // Pe: RIGHT DOUBLE ANGLE BRACKET
                // QUOTE: case 0x300D: // Pe: RIGHT CORNER BRACKET
                // QUOTE: case 0x300F: // Pe: RIGHT WHITE CORNER BRACKET
            case 0x3011: // Pe: RIGHT BLACK LENTICULAR BRACKET
            case 0x3015: // Pe: RIGHT TORTOISE SHELL BRACKET
            case 0x3017: // Pe: RIGHT WHITE LENTICULAR BRACKET
            case 0x3019: // Pe: RIGHT WHITE TORTOISE SHELL BRACKET
            case 0x301B: // Pe: RIGHT WHITE SQUARE BRACKET
                // QUOTE: case 0x301E: // Pe: DOUBLE PRIME QUOTATION MARK
                // QUOTE: case 0x301F: // Pe: LOW DOUBLE PRIME QUOTATION MARK
            case 0xFD3F: // Pe: ORNATE RIGHT PARENTHESIS
            case 0xFE18: // Pe: PRESENTATION FORM FOR VERTICAL RIGHT WHITE LENTICULAR BRAKCET
            case 0xFE36: // Pe: PRESENTATION FORM FOR VERTICAL RIGHT PARENTHESIS
            case 0xFE38: // Pe: PRESENTATION FORM FOR VERTICAL RIGHT CURLY BRACKET
            case 0xFE3A: // Pe: PRESENTATION FORM FOR VERTICAL RIGHT TORTOISE SHELL BRACKET
            case 0xFE3C: // Pe: PRESENTATION FORM FOR VERTICAL RIGHT BLACK LENTICULAR BRACKET
            case 0xFE3E: // Pe: PRESENTATION FORM FOR VERTICAL RIGHT DOUBLE ANGLE BRACKET
            case 0xFE40: // Pe: PRESENTATION FORM FOR VERTICAL RIGHT ANGLE BRACKET
                // QUOTE: case 0xFE42: // Pe: PRESENTATION FORM FOR VERTICAL RIGHT CORNER BRACKET
                // QUOTE: case 0xFE44: // Pe: PRESENTATION FORM FOR VERTICAL RIGHT WHITE CORNER BRACKET
            case 0xFE48: // Pe: PRESENTATION FORM FOR VERTICAL RIGHT SQUARE BRACKET
            case 0xFE5A: // Pe: SMALL RIGHT PARENTHESIS
            case 0xFE5C: // Pe: SMALL RIGHT CURLY BRACKET
            case 0xFE5E: // Pe: SMALL RIGHT TORTOISE SHELL BRACKET
            case 0xFF09: // Pe: FULLWIDTH RIGHT PARENTHESIS
            case 0xFF3D: // Pe: FULLWIDTH RIGHT SQUARE BRACKET
                // BLOCK DELIMITER: case 0xFF5D: // Pe: FULLWIDTH RIGHT CURLY BRACKET
            case 0xFF60: // Pe: FULLWIDTH RIGHT WHITE PARENTHESIS
                // QUOTE: case 0xFF63: // Pe: HALFWIDTH RIGHT CORNER BRACKET
                // *** Pi characters ***
                // QUOTE: case 0x00AB: // Pi: LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
                // QUOTE: case 0x2018: // Pi: LEFT SINGLE QUOTATION MARK
                // QUOTE: case 0x201B: // Pi: SINGLE HIGH-REVERSED-9 QUOTATION MARK
                // QUOTE: case 0x201C: // Pi: LEFT DOUBLE QUOTATION MARK
                // QUOTE: case 0x201F: // Pi: DOUBLE HIGH-REVERSED-9 QUOTATION MARK
                // QUOTE: case 0x2039: // Pi: SINGLE LEFT-POINTING ANGLE QUOTATION MARK
            case 0x2E02: // Pi: LEFT SUBSTITUTION BRACKET
            case 0x2E04: // Pi: LEFT DOTTED SUBSTITUTION BRACKET
            case 0x2E09: // Pi: LEFT TRANSPOSITION BRACKET
            case 0x2E0C: // Pi: LEFT RAISED OMISSION BRACKET
            case 0x2E1C: // Pi: LEFT LOW PARAPHRASE BRACKET
            case 0x2E20: // Pi: LEFT VERTICAL BAR WITH QUILL
                // **** Pf characters ***
                // QUOTE: case 0x00BB: // Pf: RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
                // QUOTE: case 0x2019: // Pf: RIGHT SINGLE QUOTATION MARK
                // QUOTE: case 0x201D: // Pf: RIGHT DOUBLE QUOTATION MARK
                // QUOTE: case 0x203A: // Pf: SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
            case 0x2E03: // Pf: RIGHT SUBSTITUTION BRACKET
            case 0x2E05: // Pf: RIGHT DOTTED SUBSTITUTION BRACKET
            case 0x2E0A: // Pf: RIGHT TRANSPOSITION BRACKET
            case 0x2E0D: // Pf: RIGHT RAISED OMISSION BRACKET
            case 0x2E1D: // Pf: RIGHT LOW PARAPHRASE BRACKET
            case 0x2E21: // Pf: RIGHT VERTICAL BAR WITH QUILL
                return true;
            default:
                return false;
        }
    }
}
