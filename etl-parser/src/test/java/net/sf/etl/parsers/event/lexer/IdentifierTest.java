package net.sf.etl.parsers.event.lexer;

import net.sf.etl.parsers.Tokens;
import org.junit.Test;

/**
 * test identifiers
 */
public class IdentifierTest extends LexerTestCase {

    /**
     * test simple identifiers
     */
    @Test
    public void testSimpleIdentifiers() {
        single("name", Tokens.IDENTIFIER);
        single("a2a", Tokens.IDENTIFIER);
        single("A0Z", Tokens.IDENTIFIER);
        single("A_0_Z", Tokens.IDENTIFIER);
        single("_0_Z", Tokens.IDENTIFIER);
        single("_a_0_Z", Tokens.IDENTIFIER);
        single("_", Tokens.IDENTIFIER);
        single("__", Tokens.IDENTIFIER);
    }
}
