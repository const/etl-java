package net.sf.etl.parsers.event.lexer;

import net.sf.etl.parsers.Tokens;
import org.junit.Test;

/**
 * Test for numbers
 */
public class NumberTest extends LexerTestCase {
    @Test
    public void integers() {
        single("1", Tokens.INTEGER);
        single("090", Tokens.INTEGER);
        single("0_9_0QA", Tokens.INTEGER_WITH_SUFFIX);
        single("2#101#", Tokens.INTEGER);
        single("8#777#", Tokens.INTEGER);
        single("16#7FFF_FFFF#", Tokens.INTEGER);
        single("36#_a_z_#", Tokens.INTEGER);
        single("16#7FFF_FFFF#U", Tokens.INTEGER_WITH_SUFFIX);
    }

    @Test
    public void shebang() {
        start("1#!test");
        read("1", Tokens.INTEGER);
        read("#!test", Tokens.LINE_COMMENT);
        start("1.0#!test");
        read("1.0", Tokens.FLOAT);
        read("#!test", Tokens.LINE_COMMENT);
    }

    @Test
    public void floats() {
        single("1e1", Tokens.FLOAT);
        single("0.1e+2", Tokens.FLOAT);
        single("3.1_4", Tokens.FLOAT);
        single("2#1.01#E+2", Tokens.FLOAT);
        single("8#0.777#", Tokens.FLOAT);
        single("16#7FFF_FFFF#e-4", Tokens.FLOAT);
        single("36#_a._z_#", Tokens.FLOAT);
        single("16#7F.FF_FFFF#DDDD", Tokens.FLOAT_WITH_SUFFIX);
    }

}
