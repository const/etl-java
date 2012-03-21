package net.sf.etl.parsers.event;

import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.TermToken;

/**
 * The phrase parser
 */
public interface TermParser {
    /**
     * Start parsing
     *
     * @param systemId the system id
     */
    void start(String systemId);

    /**
     * @return read current token for the parser
     */
    TermToken read();

    /**
     * Parse token,
     *
     * @param token the cell with the token. The element is removed if it is consumed and more date is needed.
     * @return the parsed state
     */
    ParserState parse(Cell<PhraseToken> token);
}
