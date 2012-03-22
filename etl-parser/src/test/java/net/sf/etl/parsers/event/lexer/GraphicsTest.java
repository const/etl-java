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

    @Test
    public void boundaryTest() {
        start("= ");
        read("=", Tokens.GRAPHICS);
        read(" ", Tokens.WHITESPACE);
        readEof();
    }

    /**
     * test graphics + comments
     */
    @Test
    public void testGraphicsToComments() {
        start("+//");
        read("+", Tokens.GRAPHICS);
        read("//", Tokens.LINE_COMMENT);
        readEof();
        start("-/**/");
        read("-", Tokens.GRAPHICS);
        read("/**/", Tokens.BLOCK_COMMENT);
        readEof();
        start("##!///**/");
        read("#", Tokens.GRAPHICS);
        read("#!///**/", Tokens.LINE_COMMENT);
        readEof();
    }

}
