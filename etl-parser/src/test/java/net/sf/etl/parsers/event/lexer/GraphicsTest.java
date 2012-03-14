package net.sf.etl.parsers.event.lexer;

import net.sf.etl.parsers.Tokens;
import org.junit.Test;

/**
 * The test for graphics tokens
 */
public class GraphicsTest extends LexerTestCase {
    @Test
    public void simple() {
        single("*+$-\\/%", Tokens.GRAPHICS);
        single("<:=?>.", Tokens.GRAPHICS);
        single("!^~&|`@", Tokens.GRAPHICS);
    }
}
