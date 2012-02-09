package net.sf.etl.parsers.event;

/**
 * The event driven parse result
 */
public enum ParserState {
    /**
     * The parser needs more data to produce tokens
     */
    INPUT_NEEDED,
    /**
     * The output is available
     */
    OUTPUT_AVAILABLE,
    /**
     * Additional resource is needed
     */
    RESOURCE_NEEDED,
    /**
     * The end of file is reached
     */
    EOF
}
