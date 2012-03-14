package net.sf.etl.parsers.event.impl;

import net.sf.etl.parsers.*;
import net.sf.etl.parsers.characters.*;
import net.sf.etl.parsers.event.Lexer;
import net.sf.etl.parsers.event.ParserState;

import java.nio.CharBuffer;

/**
 * The lexer implementation
 */
public class LexerImpl implements Lexer {
    enum StringPhase {
        START_FIRST_QUOTE,
        START_SECOND_QUOTE,
        NORMAL,
        ESCAPED,
        END_FIRST_QUOTE,
        END_SECOND_QUOTE,
    }
    // number phases
    // graphics phases // /// /*
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
    private Tokens kind;
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
    private int line = TextPos.START_LINE;
    /**
     * The column
     */
    private int column = TextPos.START_COLUMN;
    /**
     * The offset
     */
    private long offset = TextPos.START_OFFSET;
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
    private int startQuote;
    /**
     * End quote
     */
    private int endQuote;
    /**
     * The error
     */
    private ErrorInfo errorInfo;
    /**
     * The quote class when parsing string
     */
    private QuoteClass quoteClass;

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
        if (finished && next == null) {
            return ParserState.EOF;
        }
        if (moreDataNeeded(buffer, eof)) {
            return ParserState.INPUT_NEEDED;
        }
        if (kind == null) {
            if (buffer.remaining() == 0 && eof) {
                kind = Tokens.EOF;
                finished = true;
                return makeToken();
            }
            // handle one lines
            int c = peek(buffer, eof);
            switch (c) {
                case 0x007B: // Ps: LEFT CURLY BRACKET
                case 0xFF5B: // Ps: FULLWIDTH LEFT CURLY BRACKET
                    return single(buffer, eof, Tokens.LEFT_CURLY);
                case 0x007D: // Pe: RIGHT CURLY BRACKET
                case 0xFF5D: // Pe: FULLWIDTH RIGHT CURLY BRACKET
                    return single(buffer, eof, Tokens.RIGHT_CURLY);
            }
            if (Whitespaces.isSpace(c)) {
                return parseSpace(buffer, eof);
            }
            if (Whitespaces.isNewline(c)) {
                return parseNewline(buffer, eof);
            }
            if (Graphics.isSemicolon(c)) {
                return single(buffer, eof, Tokens.SEMICOLON);
            }
            if (Graphics.isComma(c)) {
                return single(buffer, eof, Tokens.COMMA);
            }
            if (Graphics.isGraphics(c)) {
                return parseGraphics(buffer, eof);
            }
            if (Identifiers.isIdentifierStart(c)) {
                return parseIdentifier(buffer, eof);
            }
            if (Brackets.isBracket(c)) {
                return single(buffer, eof, Tokens.BRACKET);
            }
            if (Numbers.isDecimal(c)) {
                return parseNumber(buffer, eof);
            }
            quoteClass = QuoteClass.classify(c);
            if (quoteClass != null) {
                return parseString(buffer, eof);
            }
            // invalid character
            codepoint(buffer, eof);
            error("net.sf.etl.parsers.errors.lexical.InvalidCharacter", start, current());
            kind = Tokens.WHITESPACE;
            return makeToken();
        } else {
            // TODO implement continuation
            throw new RuntimeException("Unsupported branch");
        }
    }

    private ParserState parseNumber(CharBuffer buffer, boolean eof) {
        // TODO implement it
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private ParserState parseString(CharBuffer buffer, boolean eof) {
        // TODO implement it
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private ParserState parseSpace(CharBuffer buffer, boolean eof) {
        kind = Tokens.WHITESPACE;
        do {
            if (moreDataNeeded(buffer, eof)) {
                return ParserState.INPUT_NEEDED;
            }
            codepoint(buffer, eof);
        } while (Whitespaces.isSpace(peek(buffer, eof)));
        return makeToken();
    }

    private ParserState parseNewline(CharBuffer buffer, boolean eof) {
        kind = Tokens.NEWLINE;
        int c = codepoint(buffer, eof);
        if (c == Whitespaces.CR) {
            if (moreDataNeeded(buffer, eof)) {
                return ParserState.INPUT_NEEDED;
            }
            c = peek(buffer, eof);
            if (c == Whitespaces.LF) {
                codepoint(buffer, eof);
            }
        }
        line++;
        column = TextPos.START_COLUMN;
        return makeToken();
    }

    /**
     * Parse identifier
     *
     * @param buffer the buffer to parse
     * @param eof    the eof
     * @return the parsed identifier
     */
    private ParserState parseIdentifier(CharBuffer buffer, boolean eof) {
        kind = Tokens.IDENTIFIER;
        int c;
        do {
            codepoint(buffer, eof);
            if (moreDataNeeded(buffer, eof)) {
                return ParserState.INPUT_NEEDED;
            }
        } while (Identifiers.isIdentifierPart(c = peek(buffer, eof)));
        quoteClass = QuoteClass.classify(c);
        if (quoteClass != null) {
            return parseString(buffer, eof);
        }
        return makeToken();
    }

    /**
     * Parse identifier
     *
     * @param buffer the buffer to parse
     * @param eof    the eof
     * @return the parsed identifier
     */
    private ParserState parseGraphics(CharBuffer buffer, boolean eof) {
        // TODO implement comments
        kind = Tokens.GRAPHICS;
        do {
            codepoint(buffer, eof);
            if (moreDataNeeded(buffer, eof)) {
                return ParserState.INPUT_NEEDED;
            }
        } while (Graphics.isGraphics(peek(buffer, eof)));
        return makeToken();
    }

    /**
     * Parse single char token.
     *
     * @param buffer the buffer to parse
     * @param eof    true if the last token
     * @param token  the token type
     * @return parsing result
     */
    private ParserState single(CharBuffer buffer, boolean eof, Tokens token) {
        codepoint(buffer, eof);
        kind = token;
        return makeToken();
    }


    private char consumeChar(CharBuffer buffer) {
        char ch = buffer.get();
        if (text == null) {
            text = new StringBuilder();
        }
        text.append(ch);
        return ch;
    }


    /**
     * Consume single code point
     *
     * @param buffer the buffer to codepoint from
     * @param eof    the eof flag
     * @return the codepoint value
     */
    private int codepoint(CharBuffer buffer, boolean eof) {
        assert !moreDataNeeded(buffer, eof) : "Can consume only if there is data available";
        char ch = consumeChar(buffer);
        if (Character.isHighSurrogate(ch)) {
            offset += 2;
            return Character.toCodePoint(ch, consumeChar(buffer));
        } else {
            offset++;
            return ch;
        }
    }

    private int peek(CharBuffer buffer, boolean eof) {
        assert !moreDataNeeded(buffer, eof) : "Can peek only if there is data available";
        if (eof && buffer.remaining() == 0) {
            return -1;
        }
        return Character.codePointAt(buffer, 0);
    }

    private void error(String id, TextPos s, TextPos e) {
        errorInfo = new ErrorInfo(id, ErrorInfo.NO_ARGS, s, e, systemId, errorInfo);
    }

    private boolean moreDataNeeded(CharBuffer buffer, boolean eof) {
        return !eof && (buffer.remaining() == 0 ||
                (buffer.remaining() == 1 && Character.isHighSurrogate(buffer.charAt(0))));
    }

    /**
     * Create token
     *
     * @return output available status
     */
    private ParserState makeToken() {
        if (next != null) {
            throw new IllegalStateException("Next token is already available: " + next);
        }
        TextPos end = current();
        TokenKey key;
        if (kind.hasQuotes()) {
            key = TokenKey.quoted(kind, modifier, startQuote, endQuote);
        } else if (kind.hasModifier()) {
            key = TokenKey.modified(kind, modifier);
        } else {
            key = TokenKey.simple(kind);
        }
        next = new Token(key, text == null ? "" : text.toString(), start, end, errorInfo);
        kind = null;
        text = null;
        start = end;
        errorInfo = null;
        return ParserState.OUTPUT_AVAILABLE;
    }

    /**
     * @return the current position in the token
     */
    private TextPos current() {
        return new TextPos(line, column, offset);
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
}
