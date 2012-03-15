package net.sf.etl.parsers.event.lexer;

import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.TokenKey;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.impl.LexerImpl;

import java.nio.CharBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * The basic lexer test case
 */
public abstract class LexerTestCase {
    /**
     * The current lexer to use
     */
    protected LexerImpl lexer;
    /**
     * The used character buffer
     */
    protected CharBuffer buffer;
    /**
     * The current token
     */
    protected Token current;

    /**
     * Start parsing
     *
     * @param text the text to parse
     */
    protected void start(String text) {
        lexer = new LexerImpl();
        buffer = CharBuffer.wrap(text);
    }

    /**
     * @return parse next token
     */
    protected Token next() {
        ParserState status = lexer.parse(buffer, true);
        if (status == ParserState.OUTPUT_AVAILABLE) {
            current = lexer.read();
        } else if (status == ParserState.EOF) {
            current = null;
        } else {
            throw new IllegalStateException("Not yet supported status: " + status);
        }
        return current;
    }

    /**
     * Test string that contains a single token of the specified kind
     *
     * @param text the text parse
     * @param kind the expected token kind
     * @return the parsed token
     */
    protected Token single(String text, Tokens kind) {
        start(text);
        Token rc = read(text, kind);
        readEof();
        return rc;
    }

    /**
     * Check single token using token key
     *
     * @param text     the text to check
     * @param tokenKey the token key to use
     */
    protected void single(String text, TokenKey tokenKey) {
        Token t = single(text, tokenKey.kind());
        assertEquals(tokenKey, t.key());
    }


    /**
     * Test uniform sequence of the tokens
     *
     * @param text   the text to check
     * @param kind   the kind of the token
     * @param tokens the token text
     */
    protected void sequence(String text, Tokens kind, String... tokens) {
        start(text);
        for (String t : tokens) {
            read(t, kind);
        }
        readEof();
    }

    /**
     * Read end of stream
     */
    protected void readEof() {
        read("", Tokens.EOF);
        next();
        assertNull(current);
    }


    /**
     * Read single token and check it
     *
     * @param text the text to parse
     * @param kind the expected token kind
     * @return the parsed token
     */
    private Token read(String text, Tokens kind) {
        next();
        checkCurrent(text, kind);
        return current;
    }

    private void checkCurrent(String text, Tokens kind) {
        assertEquals(text, current.text());
        assertEquals(kind, current.kind());
    }

}
