package net.sf.etl.parsers.characters;

/**
 * The classes of the quotes
 */
public enum QuoteClass {
    DOUBLE_QUOTE,
    SINGLE_QUOTE,
    DOUBLE_ANGLE,
    SINGLE_ANGLE,
    SINGLE_CORNER,
    DOUBLE_CORNER,
    SINGLE_DIRECTED,
    DOUBLE_DIRECTED,
    DOUBLE_PRIME;

    /**
     * Classify codepoint with respect to quote class.
     *
     * @param codepoint the codepoint to classify
     * @return the quote class, null if the codepoint does not represents the quote
     */
    public static QuoteClass classify(int codepoint) {
        switch (codepoint) {
            case 0x0022: // Quotation_Mark # Po       QUOTATION MARK
            case 0xFF02: // Quotation_Mark # Po       FULLWIDTH QUOTATION MARK
                return DOUBLE_QUOTE;
            case 0x0027: // Quotation_Mark # Po       APOSTROPHE
            case 0xFF07: // Quotation_Mark # Po       FULLWIDTH APOSTROPHE
                return SINGLE_QUOTE;
            case 0x00AB: // Quotation_Mark # Pi       LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
            case 0x00BB: // Quotation_Mark # Pf       RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
                return DOUBLE_ANGLE;
            case 0x2018: // Quotation_Mark # Pi       LEFT SINGLE QUOTATION MARK
            case 0x2019: // Quotation_Mark # Pf       RIGHT SINGLE QUOTATION MARK
            case 0x201A: // Quotation_Mark # Ps       SINGLE LOW-9 QUOTATION MARK
            case 0x201B: // Quotation_Mark # Pi       SINGLE HIGH-REVERSED-9 QUOTATION MARK
                return SINGLE_DIRECTED;
            case 0x201C: // Quotation_Mark # Pi       LEFT DOUBLE QUOTATION MARK
            case 0x201D: // Quotation_Mark # Pf       RIGHT DOUBLE QUOTATION MARK
            case 0x201E: // Quotation_Mark # Ps       DOUBLE LOW-9 QUOTATION MARK
            case 0x201F: // Quotation_Mark # Pi       DOUBLE HIGH-REVERSED-9 QUOTATION MARK
                return DOUBLE_DIRECTED;
            case 0x2039: // Quotation_Mark # Pi       SINGLE LEFT-POINTING ANGLE QUOTATION MARK
            case 0x203A: // Quotation_Mark # Pf       SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
                return SINGLE_ANGLE;
            case 0x300C: // Quotation_Mark # Ps       LEFT CORNER BRACKET
            case 0x300D: // Quotation_Mark # Pe       RIGHT CORNER BRACKET
            case 0xFE41: // Quotation_Mark # Ps       PRESENTATION FORM FOR VERTICAL LEFT CORNER BRACKET
            case 0xFE42: // Quotation_Mark # Pe       PRESENTATION FORM FOR VERTICAL RIGHT CORNER BRACKET
            case 0xFF62: // Quotation_Mark # Ps       HALFWIDTH LEFT CORNER BRACKET
            case 0xFF63: // Quotation_Mark # Pe       HALFWIDTH RIGHT CORNER BRACKET
                return SINGLE_CORNER;
            case 0x300E: // Quotation_Mark # Ps       LEFT WHITE CORNER BRACKET
            case 0x300F: // Quotation_Mark # Pe       RIGHT WHITE CORNER BRACKET
            case 0xFE43: // Quotation_Mark # Ps       PRESENTATION FORM FOR VERTICAL LEFT WHITE CORNER BRACKET
            case 0xFE44: // Quotation_Mark # Pe       PRESENTATION FORM FOR VERTICAL RIGHT WHITE CORNER BRACKET
                return DOUBLE_CORNER;
            case 0x301D: // Quotation_Mark # Ps       REVERSED DOUBLE PRIME QUOTATION MARK
            case 0x301E: // Quotation_Mark # Pe       DOUBLE PRIME QUOTATION MARK
            case 0x301F: // Quotation_Mark # Pe       LOW DOUBLE PRIME QUOTATION MARK
                return DOUBLE_PRIME;
            default:
                return null;
        }
    }
}
