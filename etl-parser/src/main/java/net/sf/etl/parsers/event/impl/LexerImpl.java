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
    // phases for parsing number
    private static final int NUMBER_DECIMAL = 0;
    private static final int NUMBER_DECIMAL_FRACTION = 1;
    private static final int NUMBER_BASED = 2;
    private static final int NUMBER_BASED_FRACTION = 3;
    private static final int NUMBER_AFTER_BASED = 4;
    private static final int NUMBER_AFTER_EXPONENT = 5;
    private static final int NUMBER_AFTER_EXPONENT_SIGN = 6;
    private static final int NUMBER_AFTER_EXPONENT_DIGIT = 7;
    private static final int NUMBER_SUFFIX = 8;
    private static final int GRAPHICS_NORMAL = 10;
    private static final int GRAPHICS_AFTER_SLASH = 11;
    private static final int GRAPHICS_AFTER_HASH = 12;
    private static final int BLOCK_COMMENT_AFTER_STAR = 20;
    private static final int BLOCK_COMMENT_AFTER_CR = 21;
    private static final int BLOCK_COMMENT_NORMAL = 22;
    private static final int STRING_START_FIRST_QUOTE = 31;
    private static final int STRING_START_SECOND_QUOTE = 32;
    private static final int STRING_NORMAL = 33;
    private static final int STRING_AFTER_CR = 34;
    private static final int STRING_ESCAPED = 35;
    private static final int STRING_END_FIRST_QUOTE = 36;
    private static final int STRING_END_SECOND_QUOTE = 37;
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
    /**
     * The phase of parsing complex token
     */
    private int phase;
    /**
     * Phase start position in the buffer
     */
    private int phaseStart;
    /**
     * The base of the number
     */
    private int numberBase;

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
        kind = Tokens.INTEGER;
        phase = NUMBER_DECIMAL;
        int codepoint;
        do {
            codepoint(buffer, eof);
            if (moreDataNeeded(buffer, eof)) return ParserState.INPUT_NEEDED;
            codepoint = peek(buffer, eof);
        } while (Numbers.isDecimal(codepoint) || Identifiers.isConnectorChar(codepoint));
        if (codepoint == '#' && !Identifiers.isConnectorChar(Character.codePointBefore(text, text.length()))) {
            // consume hash and evaluate base
            try {
                numberBase = Integer.parseInt(text.toString());
            } catch (Throwable t) {
                numberBase = -1;
            }
            codepoint(buffer, eof);
            phase = NUMBER_BASED;
            if (numberBase < 2 || numberBase > 36) {
                error("net.sf.etl.parsers.errors.lexical.NumberBaseIsOutOfRange", start, current());
                numberBase = 36; // using it for sake of parsing only
            }
            // parse based part
            while (true) {
                if (moreDataNeeded(buffer, eof)) return ParserState.INPUT_NEEDED;
                codepoint = peek(buffer, eof);
                if (Numbers.isValidDigit(codepoint, numberBase)) {
                    codepoint(buffer, eof);
                } else if (Identifiers.isConnectorChar(codepoint)) {
                    codepoint(buffer, eof);
                } else if (codepoint == '#') {
                    phase = NUMBER_AFTER_BASED;
                    codepoint(buffer, eof);
                    break;
                } else if (codepoint == '.') {
                    kind = Tokens.FLOAT;
                    if (phase == NUMBER_BASED_FRACTION) {
                        TextPos p = current();
                        codepoint(buffer, eof);
                        error("net.sf.etl.parsers.errors.lexical.FloatTooManyDots", p, current());
                    } else {
                        phase = NUMBER_BASED_FRACTION;
                        codepoint(buffer, eof);
                    }
                } else if (Numbers.isAnyDigit(codepoint)) {
                    TextPos p = current();
                    codepoint(buffer, eof);
                    error("net.sf.etl.parsers.errors.lexical.SomeDigitAreOutOfBase", p, current());
                } else {
                    error("net.sf.etl.parsers.errors.lexical.UnterminatedBasedNumber", start, current());
                    return makeToken();
                }
            }
            codepoint = peek(buffer, eof);
        } else if (codepoint == '.') {
            if (Identifiers.isConnectorChar(text.codePointBefore(text.length()))) {
                return makeToken();
            }
            if (moreDataNeededNext(buffer, eof)) return ParserState.INPUT_NEEDED;
            if (Numbers.isDecimal(peekNext(buffer, eof))) {
                kind = Tokens.FLOAT;
                phase = NUMBER_DECIMAL_FRACTION;
                codepoint(buffer, eof);
                codepoint(buffer, eof);
            } else {
                return makeToken();
            }
            codepoint = peek(buffer, eof);
        }
        if ((codepoint == 'e' || codepoint == 'E') && !Identifiers.isConnectorChar(Character.codePointBefore(text, text.length()))) {
            kind = Tokens.FLOAT;
            codepoint(buffer, eof);
            phase = NUMBER_AFTER_EXPONENT;
            if (moreDataNeeded(buffer, eof)) return ParserState.INPUT_NEEDED;
            codepoint = peek(buffer, eof);
            if (codepoint == '-' || codepoint == '+') {
                codepoint(buffer, eof);
                phase = NUMBER_AFTER_EXPONENT_SIGN;
                if (moreDataNeeded(buffer, eof)) return ParserState.INPUT_NEEDED;
                codepoint = peek(buffer, eof);
            }
            if (!Numbers.isDecimal(codepoint)) {
                error("net.sf.etl.parsers.errors.lexical.UnterminatedNumberExponent", start, current());
                return makeToken();
            }
            phase = NUMBER_AFTER_EXPONENT_DIGIT;
            do {
                codepoint(buffer, eof);
                if (moreDataNeeded(buffer, eof)) return ParserState.INPUT_NEEDED;
                codepoint = peek(buffer, eof);
            } while (Numbers.isDecimal(codepoint) || Identifiers.isConnectorChar(codepoint));
        }
        if (Identifiers.isIdentifierStart(codepoint) && codepoint != 'e' && codepoint != 'E' &&
                !Identifiers.isConnectorChar(Character.codePointBefore(text, text.length()))) {
            phase = NUMBER_SUFFIX;
            phaseStart = text.length();
            kind = kind == Tokens.FLOAT ? Tokens.FLOAT_WITH_SUFFIX : Tokens.INTEGER_WITH_SUFFIX;
            do {
                codepoint(buffer, eof);
                if (moreDataNeeded(buffer, eof)) return ParserState.INPUT_NEEDED;
                codepoint = peek(buffer, eof);
            } while (Identifiers.isIdentifierPart(codepoint));
            modifier = text.substring(phaseStart, text.length());
        }
        return makeToken();
    }

    private ParserState parseString(CharBuffer buffer, boolean eof) {
        // TODO implement it
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private ParserState parseSpace(CharBuffer buffer, boolean eof) {
        kind = Tokens.WHITESPACE;
        do {
            if (moreDataNeeded(buffer, eof)) return ParserState.INPUT_NEEDED;
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

    private int peekNext(CharBuffer buffer, boolean eof) {
        assert !moreDataNeededNext(buffer, eof) : "Can peek only if there is data available";
        if (eof && buffer.remaining() == 0) {
            return -1;
        }
        int codepoint = Character.codePointAt(buffer, 0);
        int p = Character.charCount(codepoint);
        if (eof && buffer.remaining() == p) {
            return -1;
        }
        return Character.codePointAt(buffer, p);
    }

    private void error(String id, TextPos s, TextPos e) {
        errorInfo = new ErrorInfo(id, ErrorInfo.NO_ARGS, s, e, systemId, errorInfo);
    }

    private boolean moreDataNeeded(CharBuffer buffer, boolean eof) {
        return !eof && (buffer.remaining() == 0 ||
                (buffer.remaining() == 1 && Character.isHighSurrogate(buffer.charAt(0))));
    }

    private boolean moreDataNeededNext(CharBuffer buffer, boolean eof) {
        if (eof) {
            return false;
        }
        if (buffer.remaining() == 0 || (buffer.remaining() == 1 && Character.isHighSurrogate(buffer.charAt(0)))) {
            return true;
        }
        int codepoint = Character.codePointAt(buffer, 0);
        int p = Character.charCount(codepoint);
        if (buffer.remaining() == p || (buffer.remaining() == p + 1 && Character.isHighSurrogate(buffer.charAt(p)))) {
            return true;
        }
        return false;
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