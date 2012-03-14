package net.sf.etl.parsers.event.lexer;

import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.event.Lexer;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.impl.LexerImpl;
import org.junit.Test;
import static org.junit.Assert.*; 

import java.nio.CharBuffer;

/**
 * The lexer test
 */
public class BasicLexerTest {
    @Test
    public void testEof() {
        Lexer l = new LexerImpl();
        ParserState r = l.parse(CharBuffer.wrap(""), false );
        assertEquals(ParserState.INPUT_NEEDED, r);
        r = l.parse(CharBuffer.wrap(""), true);
        assertEquals(ParserState.OUTPUT_AVAILABLE, r);
        Token t = l.read();
        assertEquals(Tokens.EOF, t.kind());
        assertEquals(TextPos.START, t.start());
        assertEquals(TextPos.START, t.end());
    }
    
}
