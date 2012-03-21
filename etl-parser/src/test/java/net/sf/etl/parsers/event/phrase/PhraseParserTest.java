package net.sf.etl.parsers.event.phrase;

import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.PhraseTokens;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.event.Cell;
import net.sf.etl.parsers.event.Lexer;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.PhraseParser;
import net.sf.etl.parsers.event.impl.LexerImpl;
import net.sf.etl.parsers.event.impl.PhraseParserImpl;
import org.junit.Test;

import java.nio.CharBuffer;

import static org.junit.Assert.assertEquals;

/**
 * The test for phrase parser
 */
public class PhraseParserTest {
    private Cell<Token> cell = new Cell<Token>();
    private Lexer lexer = new LexerImpl();
    private CharBuffer buffer;
    private PhraseParser phraseParser = new PhraseParserImpl();
    private PhraseToken current;

    @Test
    public void simple() {
        start("");
        read(PhraseTokens.EOF);
        start("{}");
        read(PhraseTokens.START_BLOCK);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.END_BLOCK);
        read(PhraseTokens.STATEMENT_END);
        read(PhraseTokens.EOF);
        start("{{;};}");
        read(PhraseTokens.START_BLOCK);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.START_BLOCK);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.END_BLOCK);
        read(PhraseTokens.STATEMENT_END);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.END_BLOCK);
        read(PhraseTokens.STATEMENT_END);
        read(PhraseTokens.EOF);
    }

    @Test
    public void simpleError() {
        start("b\n }{a");
        read(PhraseTokens.SIGNIFICANT);
        read(PhraseTokens.SOFT_STATEMENT_END);
        read(PhraseTokens.IGNORABLE);
        read(PhraseTokens.IGNORABLE);
        read(PhraseTokens.STATEMENT_END);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.START_BLOCK);
        read(PhraseTokens.CONTROL);
        read(PhraseTokens.SIGNIFICANT);
        read(PhraseTokens.STATEMENT_END);
        read(PhraseTokens.END_BLOCK);
        read(PhraseTokens.STATEMENT_END);
        read(PhraseTokens.EOF);
    }

    private void next() {
        ParserState r = phraseParser.parse(cell);
        switch (r) {
            case OUTPUT_AVAILABLE:
                current = phraseParser.read();
                return;
            case INPUT_NEEDED:
                ParserState state = lexer.parse(buffer, true);
                assertEquals(ParserState.OUTPUT_AVAILABLE, state);
                cell.put(lexer.read());
                next();
                return;
            default:
                throw new IllegalStateException("Unknown state: " + r);
        }
    }

    private void read(PhraseTokens kind) {
        next();
        assertEquals(kind, current.kind());
    }

    private void start(String text) {
        buffer = CharBuffer.wrap(text);
        cell = new Cell<Token>();
        lexer = new LexerImpl();
        phraseParser = new PhraseParserImpl();
        current = null;
    }


}
