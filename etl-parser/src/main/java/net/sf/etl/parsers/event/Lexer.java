package net.sf.etl.parsers.event;

import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;

import java.nio.CharBuffer;

/**
 * The published event interface for the lexer
 */
public interface Lexer {
    /**
     * Start parsing
     *
     * @param systemId the system id
     * @param start the start position
     */
    void start(String systemId, TextPos start);
    /**
     * The character buffer
     *
     * @param buffer the buffer
     * @param last   if true, no more data is expected
     * @return the parsed state
     */
    ParserState parse(CharBuffer buffer, boolean last);

    /**
     * Read token
     *
     * @return the token to retrieve
     */
    Token read();
}
