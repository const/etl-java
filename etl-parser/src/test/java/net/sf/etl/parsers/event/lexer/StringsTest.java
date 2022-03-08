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

package net.sf.etl.parsers.event.lexer;

import net.sf.etl.parsers.TokenKey;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.characters.QuoteClass;
import org.junit.jupiter.api.Test;

/**
 * The test for the strings
 */
public class StringsTest extends LexerTestCase {

    /**
     * test double quoted strings
     */
    @Test
    public void testDoubleQuotedStrings() {
        checkSingleStringToken("\"simple string\"", '\"');
        checkSingleStringToken("\"simple \\\"string\\\"\"", '\"');
        checkSingleStringToken("\"simple 'string'\"", '\"');
        checkSingleStringToken("\"simple `string`\"", '\"');
        checkSingleStringToken("\"\\\\\"", '\"');
        checkSingleStringToken("\"\\x1F\\U00403;\\U404;\"", '\"');
        single("\"test\"", TokenKey.quoted(Tokens.STRING, null, QuoteClass.DOUBLE_QUOTE));
    }

    private void checkSingleStringToken(final String text, final int quote) {
        single(text, TokenKey.string(Tokens.STRING, quote));
    }

    /**
     * test multiline strings
     */
    @Test
    public void testMultiLineStrings() {
        single("\"\"\"night,\n" +
                        "morning,\n" +
                        "day,\n" +
                        "evening\"\"\"",
                TokenKey.quoted(Tokens.MULTILINE_STRING, null, QuoteClass.DOUBLE_QUOTE));
        checkSingleMultilineStringToken("\"\"\"simple string\"\"\"", '\"');
        checkSingleMultilineStringToken("\"\"\"simple \n string\"\"\"", '\"');
        checkSingleMultilineStringToken("\"\"\"simple \\\n string\"\"\"", '\"');
        checkSingleMultilineStringToken("\"\"\"simple \ntest\n string\"\"\"", '\"');
    }

    private void checkSingleMultilineStringToken(final String text, final int quote) {
        single(text, TokenKey.string(Tokens.MULTILINE_STRING, quote));
    }

    /**
     * test prefixed
     */
    @Test
    public void testPrefixedStrings() {
        single("UTF8\"\"\"simple string\"\"\"", TokenKey.quoted(Tokens.PREFIXED_MULTILINE_STRING, "UTF8", '\"', '\"'));
        single("UTF8'''first\nsecond'''", TokenKey.quoted(Tokens.PREFIXED_MULTILINE_STRING, "UTF8", '\'', '\''));
        single("A\"\"\"simple \n string\"\"\"", TokenKey.quoted(Tokens.PREFIXED_MULTILINE_STRING, "A", '\"', '\"'));
        single("A'n'", TokenKey.quoted(Tokens.PREFIXED_STRING, "A", '\'', '\''));
    }


    /**
     * test single quoted strings
     */
    @Test
    public void testSingleQuotedStrings() {
        checkSingleStringToken("\'simple string\'", '\'');
        checkSingleStringToken("\'simple \"string\"\'", '\'');
        checkSingleStringToken("\'simple \\'string\\'\'", '\'');
        checkSingleStringToken("\'\\\\\'", '\'');
        checkSingleStringToken("'\\x1F;\\u0403\\U404;'", '\'');
    }
}
