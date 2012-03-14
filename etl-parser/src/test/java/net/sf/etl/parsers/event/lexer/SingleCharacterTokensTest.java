package net.sf.etl.parsers.event.lexer;

import net.sf.etl.parsers.Tokens;
import org.junit.Test;

/**
 * The test for single character tokens
 */
public class SingleCharacterTokensTest extends LexerTestCase {
    @Test
    public void asciiTokens() {
        single(",", Tokens.COMMA);
        single(";", Tokens.SEMICOLON);
        single("{", Tokens.LEFT_CURLY);
        single("}", Tokens.RIGHT_CURLY);
        single("(", Tokens.BRACKET);
        single(")", Tokens.BRACKET);
        single("[", Tokens.BRACKET);
        single("]", Tokens.BRACKET);
    }
}
