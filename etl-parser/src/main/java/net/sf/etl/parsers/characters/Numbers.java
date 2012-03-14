package net.sf.etl.parsers.characters;

/**
 * The class is used to check for numbers
 */
public class Numbers {
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
}
