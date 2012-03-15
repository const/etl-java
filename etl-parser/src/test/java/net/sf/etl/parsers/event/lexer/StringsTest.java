package net.sf.etl.parsers.event.lexer;

import net.sf.etl.parsers.TokenKey;
import net.sf.etl.parsers.Tokens;
import org.junit.Test;

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
    }

    private void checkSingleStringToken(String text, int quote) {
        single(text, TokenKey.string(Tokens.STRING, quote));
    }

    /**
     * test multiline strings
     */
    @Test
    public void testMutlitLineStrings() {
        checkSingleMultilineStringToken("\"\"\"simple string\"\"\"", '\"');
        checkSingleMultilineStringToken("\"\"\"simple \n string\"\"\"", '\"');
        checkSingleMultilineStringToken("\"\"\"simple \\\n string\"\"\"", '\"');
        checkSingleMultilineStringToken("\"\"\"simple \ntest\n string\"\"\"", '\"');
    }

    private void checkSingleMultilineStringToken(String text, int quote) {
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
