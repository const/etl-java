package net.sf.etl.parsers.event.impl;

import net.sf.etl.parsers.*;
import net.sf.etl.parsers.event.Lexer;
import net.sf.etl.parsers.event.ParserState;

import java.nio.CharBuffer;

/**
 * The lexer implementation
 */
public class LexerImpl implements Lexer {
    /**
     * If true, parsing started
     */
    private boolean started;
    /**
     * If true, parsing finished
     */
    private boolean finished;
    /**
     * null or buffer with current data for the token
     */
    private StringBuilder text;
    /**
     * The key for the token
     */
    private TokenKey kind;
    /**
     * The token
     */
    private Token next;
    /**
     * The start position
     */
    private TextPos start = TextPos.START;
    /**
     * The line
     */
    private int line = TextPos.START.line();
    /**
     * The column
     */
    private int column = TextPos.START.column();
    /**
     * The offset
     */
    private long offset = TextPos.START.offset();
    /**
     * The system identifier for the source
     */
    private String systemId = "unknown:file";
    /**
     * Start modifier
     */
    private String modifier;
    /**
     * Start quote
     */
    private String startQuote;
    /**
     * End quote
     */
    private String endQuote;
    /**
     * The error
     */
    private ErrorInfo errorInfo;

    @Override
    public void start(String systemId, TextPos start) {
        if (started) {
            throw new IllegalStateException("The parsing is already started with: " + systemId + " : " + start);
        }
        this.start = start;
        this.systemId = systemId;
    }

    @Override
    public ParserState parse(CharBuffer buffer, boolean eof) {
        started = true;
        if (next != null) {
            return ParserState.OUTPUT_AVAILABLE;
        }
        if (finished) {
            return ParserState.EOF;
        }
        if (moreDataNeeded(buffer, eof)) {
            return ParserState.INPUT_NEEDED;
        }
        if (kind == null) {
            if (buffer.remaining() == 0 && eof) {
                kind = TokenKey.simple(Tokens.EOF);
                finished = true;
                makeToken();
                return ParserState.OUTPUT_AVAILABLE;
            }
        }
        throw new IllegalStateException("Lexer is in invalid state: " + this);
    }

    private boolean moreDataNeeded(CharBuffer buffer, boolean eof) {
        return !eof && (buffer.remaining() == 0 ||
                (buffer.remaining() == 1 && Character.isHighSurrogate(buffer.charAt(buffer.position()))));
    }

    private void makeToken() {
        if (next != null) {
            throw new IllegalStateException("Next token is already available: " + next);
        }
        TextPos end = new TextPos(line, column, offset);
        next = new Token(kind, text == null ? "" : text.toString(), start, end, errorInfo);
        kind = null;
        text = null;
        start = end;
        errorInfo = null;
    }

    @Override
    public Token read() {
        Token rc = next;
        if (rc == null) {
            throw new IllegalStateException("No token available. Call parse: " + this);
        }
        next = null;
        return rc;
    }

    @Override
    public String toString() {
        return "LexerImpl{" +
                "started=" + started +
                ", finished=" + finished +
                ", text=" + text +
                ", kind=" + kind +
                ", next=" + next +
                ", start=" + start +
                ", line=" + line +
                ", column=" + column +
                ", offset=" + offset +
                ", systemId='" + systemId + '\'' +
                ", modifier='" + modifier + '\'' +
                ", startQuote='" + startQuote + '\'' +
                ", endQuote='" + endQuote + '\'' +
                ", errorInfo=" + errorInfo +
                '}';
    }
}
