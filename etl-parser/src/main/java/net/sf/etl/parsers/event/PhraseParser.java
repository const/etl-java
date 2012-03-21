package net.sf.etl.parsers.event;

import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.Token;

/**
 * The phrase parser
 */
public interface PhraseParser {
    /**
     * Start parsing
     *
     * @param systemId the system id
     */
    void start(String systemId);

    /**
     * @return read current token for the parser
     */
    PhraseToken read();

    /**
     * Parse token,
     *
     * @param token the cell with the token, the cell must contain element. The element is removed if
     *              it is consumed and more date is needed.
     * @return the parsed state
     */
    ParserState parse(Cell<Token> token);
}
