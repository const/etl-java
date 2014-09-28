/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2013 Constantine A Plotnikov
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.sf.etl.parsers.event.impl;

import net.sf.etl.parsers.DefaultTermParserConfiguration;
import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.TermParserConfiguration;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.TokenKey;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.characters.Brackets;
import net.sf.etl.parsers.characters.Graphics;
import net.sf.etl.parsers.characters.Identifiers;
import net.sf.etl.parsers.characters.Numbers;
import net.sf.etl.parsers.characters.QuoteClass;
import net.sf.etl.parsers.characters.Whitespaces;
import net.sf.etl.parsers.event.Lexer;
import net.sf.etl.parsers.event.ParserState;

import java.nio.CharBuffer;

/**
 * The lexer implementation.
 */
public final class LexerImpl implements Lexer { // NOPMD
    /**
     * The unknown file URL.
     */
    public static final String UNKNOWN_FILE = "unknown:file";
    /**
     * The system identifier for the source.
     */
    private String systemId = UNKNOWN_FILE;
    /**
     * The max supported base.
     */
    public static final int MAX_BASE = 36;
    /**
     * Number: Based.
     */
    private static final int NUMBER_START = 0;
    /**
     * Number: Decimal digits.
     */
    private static final int NUMBER_AFTER_INITIAL_ZERO = 1;
    /**
     * Number: Decimal digits.
     */
    private static final int NUMBER_DIGITS = 2;
    /**
     * Number: Decimal fraction.
     */
    private static final int NUMBER_DIGITS_FRACTION = 3;
    /**
     * Number: Before exponent.
     */
    private static final int NUMBER_BEFORE_EXPONENT = 4;
    /**
     * Number: before suffix.
     */
    private static final int NUMBER_BEFORE_SUFFIX = 5;
    /**
     * Number: after exponent.
     */
    private static final int NUMBER_AFTER_EXPONENT = 6;
    /**
     * Number: after exponent sign.
     */
    private static final int NUMBER_AFTER_EXPONENT_SIGN = 7;
    /**
     * Number: in exponent value.
     */
    private static final int NUMBER_IN_EXPONENT_VALUE = 8;
    /**
     * Number: suffix.
     */
    private static final int NUMBER_SUFFIX = 9;
    /**
     * Graphics: normal.
     */
    private static final int GRAPHICS_NORMAL = 10;
    /**
     * Line comment: normal.
     */
    private static final int LINE_COMMENT_NORMAL = 60;
    /**
     * Line comment: start.
     */
    private static final int LINE_COMMENT_START = 61;
    /**
     * Block comment: after start.
     */
    private static final int BLOCK_COMMENT_AFTER_STAR = 20;
    /**
     * Block comment: after CR.
     */
    private static final int BLOCK_COMMENT_AFTER_CR = 21;
    /**
     * Block comment: normal.
     */
    private static final int BLOCK_COMMENT_NORMAL = 22;
    /**
     * String: start, first quote.
     */
    private static final int STRING_START_FIRST_QUOTE = 31;
    /**
     * String: start, second quote.
     */
    private static final int STRING_START_SECOND_QUOTE = 32;
    /**
     * String: normal.
     */
    private static final int STRING_NORMAL = 33;
    /**
     * String: escaped.
     */
    private static final int STRING_ESCAPED = 35;
    /**
     * String: multiline normal.
     */
    private static final int STRING_MULTILINE_NORMAL = 36;
    /**
     * String: multiline after CR.
     */
    private static final int STRING_MULTILINE_AFTER_CR = 37;
    /**
     * String: multiline escaped.
     */
    private static final int STRING_MULTILINE_ESCAPED = 38;
    /**
     * String: multiline end fist quote.
     */
    private static final int STRING_MULTILINE_END_FIRST_QUOTE = 39;
    /**
     * String multiline end second quote.
     */
    private static final int STRING_MULTILINE_END_SECOND_QUOTE = 40;
    /**
     * Newline: normal.
     */
    private static final int NEWLINE_NORMAL = 50;
    /**
     * Newline: after CR.
     */
    private static final int NEWLINE_AFTER_CR = 51;
    /**
     * The parser configuration.
     */
    private final TermParserConfiguration configuration;
    /**
     * If true, parsing started.
     */
    private boolean started;
    /**
     * If true, parsing finished.
     */
    private boolean finished;
    /**
     * null or buffer with current data for the token.
     */
    private StringBuilder text; // NOPMD
    /**
     * The key for the token.
     */
    private Tokens kind;
    /**
     * The token.
     */
    private Token next;
    /**
     * The start position.
     */
    private TextPos start = TextPos.START;
    /**
     * The line.
     */
    private int line = TextPos.START_LINE;
    /**
     * The column.
     */
    private int column = TextPos.START_COLUMN;
    /**
     * The offset.
     */
    private long offset = TextPos.START_OFFSET;
    /**
     * Start modifier.
     */
    private String modifier;
    /**
     * Start quote.
     */
    private int startQuote;
    /**
     * End quote.
     */
    private int endQuote;
    /**
     * The error.
     */
    private ErrorInfo errorInfo;
    /**
     * The quote class when parsing string.
     */
    private QuoteClass quoteClass;
    /**
     * The phase of parsing complex token.
     */
    private int phase;
    /**
     * Phase start position in the buffer.
     */
    private int phaseStart;
    /**
     * The base of the number.
     */
    private int numberBase;
    /**
     * The tab size.
     */
    private int tabSize;

    /**
     * The constructor from configuration.
     *
     * @param configuration the configuration
     */
    public LexerImpl(final TermParserConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * The default constructor.
     */
    public LexerImpl() {
        this(DefaultTermParserConfiguration.INSTANCE);
    }

    /**
     * Find column after tab.
     *
     * @param current the current position
     * @param tabSize the tabulation size
     * @return the new position after tab
     */
    public static int tab(final int current, final int tabSize) {
        return ((current - 1) / tabSize + 1) * tabSize + 1;
    }

    @Override
    public void start(final String startSystemId, final TextPos startPosition) {
        tabSize = configuration.getTabSize(startSystemId);
        if (started) {
            throw new ParserException("The parsing is already started with: " + systemId + " : " + start);
        }
        started = true;
        this.start = startPosition;
        this.systemId = startSystemId;
    }

    @Override
    public ParserState parse(final CharBuffer buffer, final boolean eof) { // NOPMD
        if (!started) {
            throw new IllegalStateException("The parser is not yet started!");
        }
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
            final int c = peek(buffer, eof);
            // CHECKSTYLE:OFF
            switch (c) {
                case 0x007B: // Ps: LEFT CURLY BRACKET
                case 0xFF5B: // Ps: FULLWIDTH LEFT CURLY BRACKET
                    return single(buffer, eof, Tokens.OPEN_CURLY);
                case 0x007D: // Pe: RIGHT CURLY BRACKET
                case 0xFF5D: // Pe: FULLWIDTH RIGHT CURLY BRACKET
                    return single(buffer, eof, Tokens.CLOSE_CURLY);
                default:
                    // continue
            }
            // CHECKSTYLE:ON
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
            // TODO make invalid characters a range token rather than single character one
            // invalid character
            codepoint(buffer, eof);
            error("lexical.InvalidCharacter", start, current());
            kind = Tokens.WHITESPACE;
            return makeToken();
        } else {
            switch (kind) {
                case GRAPHICS:
                    return continueGraphics(buffer, eof);
                case IDENTIFIER:
                    return continueIdentifier(buffer, eof);
                case INTEGER:
                case INTEGER_WITH_SUFFIX:
                case FLOAT:
                case FLOAT_WITH_SUFFIX:
                    return continueNumber(buffer, eof);
                case WHITESPACE:
                    return continueSpaces(buffer, eof);
                case NEWLINE:
                    return continueNewLine(buffer, eof);
                case LINE_COMMENT:
                case DOC_COMMENT:
                    return continueLineComment(buffer, eof);
                case BLOCK_COMMENT:
                    return continueBlockComment(buffer, eof);
                case STRING:
                case PREFIXED_STRING:
                case MULTILINE_STRING:
                case PREFIXED_MULTILINE_STRING:
                    return continueString(buffer, eof);
                default:
                    throw new IllegalStateException("[BUG] Unsupported branch: " + kind);
            }
        }
    }

    /**
     * Start parsing number.
     *
     * @param buffer the buffer
     * @param eof    the eof indicator
     * @return the parser state
     */
    private ParserState parseNumber(final CharBuffer buffer, final boolean eof) {
        kind = Tokens.INTEGER;
        phase = NUMBER_START;
        return continueNumber(buffer, eof);
    }

    /**
     * Continue parsing number.
     *
     * @param buffer the buffer
     * @param eof    the eof indicator
     * @return the parser state
     */
    private ParserState continueNumber(final CharBuffer buffer, final boolean eof) { // NOPMD
        while (true) {
            if (moreDataNeeded(buffer, eof)) {
                return ParserState.INPUT_NEEDED;
            }
            int codepoint = peek(buffer, eof);
            switch (phase) { // NOPMD
                case NUMBER_START:
                    codepoint(buffer, eof);
                    numberBase = Numbers.DECIMAL;
                    if (Numbers.digit(codepoint) == 0) {
                        phase = NUMBER_AFTER_INITIAL_ZERO;
                    } else {
                        phase = NUMBER_DIGITS;
                    }
                    break;
                case NUMBER_AFTER_INITIAL_ZERO:
                    if (Numbers.isHexIndicator(codepoint)) {
                        numberBase = Numbers.HEX;
                        codepoint(buffer, eof);
                    } else if (Numbers.isBinaryIndicator(codepoint)) {
                        numberBase = Numbers.BINARY;
                        codepoint(buffer, eof);
                    }
                    phase = NUMBER_DIGITS;
                    break;
                case NUMBER_DIGITS:
                    if (Numbers.isValidDigit(codepoint, numberBase) || Identifiers.isConnectorChar(codepoint)) {
                        codepoint(buffer, eof);
                    } else if (Numbers.isDecimalDot(codepoint)) {
                        if (Identifiers.isConnectorChar(text.codePointBefore(text.length()))) {
                            return makeToken();
                        }
                        if (moreDataNeededNext(buffer, eof)) {
                            return ParserState.INPUT_NEEDED;
                        }
                        codepoint = peekNext(buffer, eof);
                        if (Numbers.isValidDigit(codepoint, numberBase)) {
                            codepoint(buffer, eof);
                            codepoint(buffer, eof);
                            kind = Tokens.FLOAT;
                            phase = NUMBER_DIGITS_FRACTION;
                        } else {
                            return makeToken();
                        }
                    } else {
                        phase = NUMBER_BEFORE_EXPONENT;
                    }
                    break;
                case NUMBER_DIGITS_FRACTION:
                    if (Numbers.isValidDigit(codepoint, numberBase) || Identifiers.isConnectorChar(codepoint)) {
                        codepoint(buffer, eof);
                    } else {
                        phase = NUMBER_BEFORE_EXPONENT;
                    }
                    break;
                case NUMBER_BEFORE_EXPONENT:
                    if (Identifiers.isConnectorChar(previousCodepoint())) {
                        return makeToken();
                    }
                    if (Numbers.isExponentChar(codepoint, numberBase)) {
                        kind = Tokens.FLOAT;
                        codepoint(buffer, eof);
                        phase = NUMBER_AFTER_EXPONENT;
                    } else {
                        if (kind == Tokens.FLOAT && numberBase != Numbers.DECIMAL) {
                            error("lexical.BinaryExponentRequired", start, current());
                            return makeToken();
                        }
                        phase = NUMBER_BEFORE_SUFFIX;
                    }
                    break;
                case NUMBER_AFTER_EXPONENT:
                    if (Numbers.isMinus(codepoint) || Numbers.isPlus(codepoint)) {
                        codepoint(buffer, eof);
                    }
                    phase = NUMBER_AFTER_EXPONENT_SIGN;
                    break;
                case NUMBER_AFTER_EXPONENT_SIGN:
                    if (Numbers.isDecimal(codepoint) || Identifiers.isConnectorChar(codepoint)) {
                        codepoint(buffer, eof);
                        phase = NUMBER_IN_EXPONENT_VALUE;
                    } else {
                        error("lexical.UnterminatedNumberExponent", start, current());
                        phase = NUMBER_BEFORE_SUFFIX;
                    }
                    break;
                case NUMBER_IN_EXPONENT_VALUE:
                    if (Numbers.isDecimal(codepoint) || Identifiers.isConnectorChar(codepoint)) {
                        codepoint(buffer, eof);
                    } else {
                        phase = NUMBER_BEFORE_SUFFIX;
                    }
                    break;
                case NUMBER_BEFORE_SUFFIX:
                    if (Numbers.isValidNumberSuffixStart(codepoint)
                            && !Identifiers.isConnectorChar(previousCodepoint())) {
                        phase = NUMBER_SUFFIX;
                        phaseStart = text.length();
                        codepoint(buffer, eof);
                        kind = kind == Tokens.FLOAT ? Tokens.FLOAT_WITH_SUFFIX : Tokens.INTEGER_WITH_SUFFIX;
                        break;
                    } else {
                        return makeToken();
                    }
                case NUMBER_SUFFIX:
                    if (Identifiers.isIdentifierPart(codepoint)) {
                        codepoint(buffer, eof);
                        break;
                    } else {
                        modifier = text.substring(phaseStart, text.length());
                        return makeToken();
                    }
                default:
                    throw new IllegalStateException("Unknown phase: " + this);
            }
        }
    }

    /**
     * @return the previous codepoint
     */
    private int previousCodepoint() {
        return Character.codePointBefore(text, text.length());
    }

    /**
     * Start parsing string.
     *
     * @param buffer the buffer
     * @param eof    the eof flag
     * @return the parser state
     */
    private ParserState parseString(final CharBuffer buffer, final boolean eof) {
        if (text != null && text.length() != 0) {
            modifier = text.toString();
            kind = Tokens.PREFIXED_STRING;
        } else {
            kind = Tokens.STRING;
        }
        startQuote = codepoint(buffer, eof);
        phase = STRING_START_FIRST_QUOTE;
        return continueString(buffer, eof);
    }

    /**
     * Continue parsing string.
     *
     * @param buffer the buffer
     * @param eof    the eof indicator
     * @return the parser state
     */
    private ParserState continueString(final CharBuffer buffer, final boolean eof) { // NOPMD
        while (true) {
            if (moreDataNeeded(buffer, eof)) {
                return ParserState.INPUT_NEEDED;
            }
            final int codepoint = peek(buffer, eof);
            switch (phase) { // NOPMD
                case STRING_START_FIRST_QUOTE:
                    if (codepoint == startQuote) {
                        // possibly multiline sting
                        codepoint(buffer, eof);
                        phase = STRING_START_SECOND_QUOTE;
                    } else {
                        phase = STRING_NORMAL;
                    }
                    break;
                case STRING_START_SECOND_QUOTE:
                    if (codepoint != startQuote) {
                        // empty string with the same start and end quotes
                        endQuote = startQuote;
                        return makeToken();
                    }
                    phase = STRING_MULTILINE_NORMAL;
                    kind = kind == Tokens.PREFIXED_STRING ? Tokens.PREFIXED_MULTILINE_STRING : Tokens.MULTILINE_STRING;
                    break;
                case STRING_NORMAL:
                    if (codepoint == '\\') {
                        codepoint(buffer, eof);
                        phase = STRING_ESCAPED;
                    } else if (Whitespaces.isNewline(codepoint)) {
                        error("lexical.NewLineInString", start, current());
                        return makeToken();
                    } else if (codepoint == -1) {
                        error("lexical.EOFInString", start, current());
                        return makeToken();
                    } else {
                        codepoint(buffer, eof);
                        final QuoteClass endQuoteClass = QuoteClass.classify(codepoint);
                        if (endQuoteClass == quoteClass) { // NOPMD
                            endQuote = codepoint;
                            return makeToken();
                        }
                    }
                    break;
                case STRING_ESCAPED:
                    phase = STRING_NORMAL;
                    if (Whitespaces.isNewline(codepoint)) {
                        error("lexical.NewLineInString", start, current());
                        return makeToken();
                    } else if (codepoint == -1) {
                        error("lexical.EOFInString", start, current());
                        return makeToken();
                    } else {
                        codepoint(buffer, eof);
                    }
                    break;
                case STRING_MULTILINE_NORMAL:
                    if (codepoint == '\\') {
                        codepoint(buffer, eof);
                        phase = STRING_MULTILINE_ESCAPED;
                    } else if (Whitespaces.isNewline(codepoint)) {
                        if (!consumeNewLine(buffer, eof, STRING_MULTILINE_AFTER_CR, STRING_MULTILINE_NORMAL)) {
                            return ParserState.INPUT_NEEDED;
                        }
                    } else if (codepoint == -1) {
                        error("lexical.EOFInString", start, current());
                        return makeToken();
                    } else {
                        final QuoteClass endQuoteClass = QuoteClass.classify(codepoint);
                        if (endQuoteClass == quoteClass) { // NOPMD
                            endQuote = codepoint(buffer, eof);
                            phase = STRING_MULTILINE_END_FIRST_QUOTE;
                        } else {
                            codepoint(buffer, eof);
                        }
                    }
                    break;
                case STRING_MULTILINE_ESCAPED:
                    if (codepoint == -1) {
                        error("lexical.EOFInString", start, current());
                        return makeToken();
                    }
                    phase = STRING_MULTILINE_NORMAL;
                    if (Whitespaces.isNewline(codepoint)) {
                        if (!consumeNewLine(buffer, eof, STRING_MULTILINE_AFTER_CR, STRING_MULTILINE_NORMAL)) {
                            return ParserState.INPUT_NEEDED;
                        }
                    } else {
                        codepoint(buffer, eof);
                    }
                    break;
                case STRING_MULTILINE_AFTER_CR:
                    if (codepoint == -1) {
                        error("lexical.EOFInString", start, current());
                        return makeToken();
                    }
                    if (!consumeNewLine(buffer, eof, STRING_MULTILINE_AFTER_CR, STRING_MULTILINE_NORMAL)) {
                        throw new IllegalStateException("Invalid lexer state, in case of after CR, "
                                + "never should need more data: " + this);
                    }
                    break;
                case STRING_MULTILINE_END_FIRST_QUOTE:
                    if (codepoint == -1) {
                        error("lexical.EOFInString", start, current());
                        return makeToken();
                    }
                    if (codepoint == endQuote) {
                        phase = STRING_MULTILINE_END_SECOND_QUOTE;
                        codepoint(buffer, eof);
                    } else {
                        endQuote = -1;
                        phase = STRING_MULTILINE_NORMAL;
                    }
                    break;
                case STRING_MULTILINE_END_SECOND_QUOTE:
                    if (codepoint == -1) {
                        error("lexical.EOFInString", start, current());
                        return makeToken();
                    }
                    if (codepoint == endQuote) {
                        codepoint(buffer, eof);
                        return makeToken();
                    } else {
                        endQuote = -1;
                        phase = STRING_MULTILINE_NORMAL;
                    }
                    break;
                default:
                    throw new IllegalStateException("Invalid phase: " + this);
            }
        }
    }

    /**
     * Parse space.
     *
     * @param buffer the buffer.
     * @param eof    the eof indicator
     * @return the parser state
     */
    private ParserState parseSpace(final CharBuffer buffer, final boolean eof) {
        kind = Tokens.WHITESPACE;
        return continueSpaces(buffer, eof);
    }

    /**
     * Continue parsing space.
     *
     * @param buffer the buffer.
     * @param eof    the eof indicator
     * @return the parser state
     */
    private ParserState continueSpaces(final CharBuffer buffer, final boolean eof) {
        while (Whitespaces.isSpace(peek(buffer, eof))) {
            final int codepoint = codepoint(buffer, eof);
            if (codepoint == '\t') {
                column = tab(column - 1, tabSize);
            }
            if (moreDataNeeded(buffer, eof)) {
                return ParserState.INPUT_NEEDED;
            }
        }
        return makeToken();
    }

    /**
     * Parse new line.
     *
     * @param buffer the buffer.
     * @param eof    the eof indicator
     * @return the parser state
     */
    private ParserState parseNewline(final CharBuffer buffer, final boolean eof) {
        kind = Tokens.NEWLINE;
        phase = NEWLINE_NORMAL;
        return continueNewLine(buffer, eof);
    }

    /**
     * Continue parsing new line.
     *
     * @param buffer the buffer
     * @param eof    the eof indicator
     * @return the parser state
     */
    private ParserState continueNewLine(final CharBuffer buffer, final boolean eof) {
        if (consumeNewLine(buffer, eof, NEWLINE_AFTER_CR, NEWLINE_NORMAL)) {
            return makeToken();
        } else {
            return ParserState.INPUT_NEEDED;
        }
    }

    /**
     * Consume new line.
     *
     * @param buffer      the buffer to consume from
     * @param eof         true if the eof
     * @param crPhase     the id of cr phase
     * @param normalPhase the id of normal phase
     * @return true if new line was successfully parsed, false if more data is needed
     */
    private boolean consumeNewLine(final CharBuffer buffer, final boolean eof, final int crPhase,
                                   final int normalPhase) {
        int codepoint;
        if (phase == normalPhase) {
            codepoint = codepoint(buffer, eof);
            if (codepoint == Whitespaces.CR) {
                phase = crPhase;
                if (moreDataNeeded(buffer, eof)) {
                    return false;
                }
            } else {
                line++;
                column = TextPos.START_COLUMN;
                return true;
            }
        } else if (phase != crPhase) {
            throw new IllegalStateException("The lexer is invalid state: " + this);
        }
        codepoint = peek(buffer, eof);
        if (codepoint == Whitespaces.LF) {
            codepoint(buffer, eof);
        }
        phase = normalPhase;
        line++;
        column = TextPos.START_COLUMN;
        return true;
    }

    /**
     * Parse identifier.
     *
     * @param buffer the buffer to parse
     * @param eof    the eof
     * @return the parser state
     */
    private ParserState parseIdentifier(final CharBuffer buffer, final boolean eof) {
        kind = Tokens.IDENTIFIER;
        codepoint(buffer, eof);
        if (moreDataNeeded(buffer, eof)) {
            return ParserState.INPUT_NEEDED;
        }
        return continueIdentifier(buffer, eof);
    }

    /**
     * Continue parsing identifier.
     *
     * @param buffer the buffer
     * @param eof    the eof indicator
     * @return the parser state
     */
    private ParserState continueIdentifier(final CharBuffer buffer, final boolean eof) {
        int codepoint = peek(buffer, eof);
        while (Identifiers.isIdentifierPart(codepoint)) {
            codepoint(buffer, eof);
            if (moreDataNeeded(buffer, eof)) {
                return ParserState.INPUT_NEEDED;
            }
            codepoint = peek(buffer, eof);
        }
        quoteClass = QuoteClass.classify(codepoint);
        if (quoteClass != null) {
            return parseString(buffer, eof);
        }
        return makeToken();
    }

    /**
     * Parse graphics.
     *
     * @param buffer the buffer to parse
     * @param eof    the eof
     * @return the parser state
     */
    private ParserState parseGraphics(final CharBuffer buffer, final boolean eof) {
        kind = Tokens.GRAPHICS;
        phase = GRAPHICS_NORMAL;
        return continueGraphics(buffer, eof);
    }

    /**
     * Continue graphics.
     *
     * @param buffer the buffer
     * @param eof    the eof
     * @return the parser state
     */
    private ParserState continueGraphics(final CharBuffer buffer, final boolean eof) {
        while (true) {
            if (moreDataNeeded(buffer, eof)) {
                return ParserState.INPUT_NEEDED;
            }
            final int codepoint = peek(buffer, eof);
            switch (phase) { // NOPMD
                case GRAPHICS_NORMAL:
                    switch (codepoint) {
                        case '/':
                            if (moreDataNeededNext(buffer, eof)) {
                                return ParserState.INPUT_NEEDED;
                            }
                            final int nextCodepoint = peekNext(buffer, eof);
                            if (nextCodepoint == '*' || nextCodepoint == '/') {
                                if (text == null || text.length() == 0) {
                                    if (nextCodepoint == '*') {
                                        return parseBlockComment(buffer, eof);
                                    } else {
                                        return parseLineComment(buffer, eof);
                                    }
                                } else {
                                    return makeToken();
                                }
                            } else {
                                codepoint(buffer, eof);
                            }
                            break;
                        case '#':
                            if (moreDataNeededNext(buffer, eof)) {
                                return ParserState.INPUT_NEEDED;
                            }
                            final int nextShebang = peekNext(buffer, eof);
                            if (nextShebang == '!') {
                                if (text == null || text.length() == 0) {
                                    return parseLineComment(buffer, eof);
                                } else {
                                    return makeToken();
                                }
                            } else {
                                codepoint(buffer, eof);
                            }
                            break;
                        case -1:
                            return makeToken();
                        default:
                            if (Graphics.isGraphics(codepoint)) {
                                codepoint(buffer, eof);
                            } else {
                                return makeToken();
                            }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Parse line comment.
     *
     * @param buffer the buffer to parse
     * @param eof    the eof indicator
     * @return the parser state
     */
    private ParserState parseLineComment(final CharBuffer buffer, final boolean eof) {
        // it is already known that it is line comment
        kind = Tokens.LINE_COMMENT;
        final int first = codepoint(buffer, eof);
        final int second = codepoint(buffer, eof);
        phase = first == '/' && second == '/' ? LINE_COMMENT_START : LINE_COMMENT_NORMAL;
        if (moreDataNeeded(buffer, eof)) {
            return ParserState.INPUT_NEEDED;
        }
        return continueLineComment(buffer, eof);
    }

    /**
     * Continue line comment.
     *
     * @param buffer the buffer to parse
     * @param eof    the eof indicator
     * @return the parser state
     */
    private ParserState continueLineComment(final CharBuffer buffer, final boolean eof) {
        int codepoint = peek(buffer, eof);
        if (phase == LINE_COMMENT_START) {
            if (codepoint == '/') {
                kind = Tokens.DOC_COMMENT;
            }
            phase = LINE_COMMENT_NORMAL;
        }
        if (codepoint == -1 || Whitespaces.isNewline(codepoint)) {
            return makeToken();
        }
        do {
            codepoint(buffer, eof);
            if (moreDataNeededNext(buffer, eof)) {
                return ParserState.INPUT_NEEDED;
            }
            codepoint = peek(buffer, eof);
        } while (codepoint != -1 && !Whitespaces.isNewline(codepoint));
        return makeToken();
    }

    /**
     * Parse block comment.
     *
     * @param buffer the buffer
     * @param eof    the eof indicator
     * @return the parser state
     */
    private ParserState parseBlockComment(final CharBuffer buffer, final boolean eof) {
        // it is already known that it is line comment
        kind = Tokens.BLOCK_COMMENT;
        codepoint(buffer, eof);
        codepoint(buffer, eof);
        phase = BLOCK_COMMENT_NORMAL;
        return continueBlockComment(buffer, eof);
    }

    /**
     * Continue block comment.
     *
     * @param buffer the buffer
     * @param eof    the eof indicator
     * @return the parser state
     */
    private ParserState continueBlockComment(final CharBuffer buffer, final boolean eof) {
        while (true) {
            if (moreDataNeededNext(buffer, eof)) {
                return ParserState.INPUT_NEEDED;
            }
            final int codepoint = peek(buffer, eof);
            if (codepoint == -1) {
                error("lexical.EOFInBlockComment", start, current());
                return makeToken();
            }
            switch (phase) {
                case BLOCK_COMMENT_NORMAL:
                    if (codepoint == '*') {
                        codepoint(buffer, eof);
                        phase = BLOCK_COMMENT_AFTER_STAR;
                    } else if (Whitespaces.isNewline(codepoint)) {
                        if (!consumeNewLine(buffer, eof, BLOCK_COMMENT_AFTER_CR, BLOCK_COMMENT_NORMAL)) {
                            return ParserState.INPUT_NEEDED;
                        }
                    } else {
                        codepoint(buffer, eof);
                    }
                    break;
                case BLOCK_COMMENT_AFTER_CR:
                    if (!consumeNewLine(buffer, eof, BLOCK_COMMENT_AFTER_CR, BLOCK_COMMENT_NORMAL)) {
                        return ParserState.INPUT_NEEDED;
                    }
                    break;
                case BLOCK_COMMENT_AFTER_STAR:
                    if (codepoint == '/') {
                        codepoint(buffer, eof);
                        return makeToken();
                    }
                    phase = BLOCK_COMMENT_NORMAL;
                    break;
                default:
                    throw new IllegalStateException("Invalid phase: " + this);
            }
        }
    }

    /**
     * Parse single char token.
     *
     * @param buffer the buffer to parse
     * @param eof    true if the last token
     * @param token  the token type
     * @return parsing result
     */
    private ParserState single(final CharBuffer buffer, final boolean eof, final Tokens token) {
        codepoint(buffer, eof);
        kind = token;
        return makeToken();
    }

    /**
     * Consume single code point.
     *
     * @param buffer the buffer to codepoint from
     * @param eof    the eof flag
     * @return the codepoint value
     */
    private int codepoint(final CharBuffer buffer, final boolean eof) {
        assert !moreDataNeeded(buffer, eof) : "Can consume only if there is data available";
        final int c = Character.codePointAt(buffer, 0);
        if (text == null) {
            text = new StringBuilder();
        }
        final int s = Character.charCount(c);
        if (s == 2) {
            buffer.get();
            buffer.get();
            offset += 2;
        } else {
            buffer.get();
            offset++;
        }
        column++;
        text.appendCodePoint(c);
        return c;
    }

    /**
     * Peek character in the buffer.
     *
     * @param buffer the buffer
     * @param eof    the eof indicator
     * @return the parser state
     */
    private int peek(final CharBuffer buffer, final boolean eof) {
        assert !moreDataNeeded(buffer, eof) : "Can peek only if there is data available";
        if (eof && buffer.remaining() == 0) {
            return -1;
        }
        return Character.codePointAt(buffer, 0);
    }

    /**
     * Peek the next character in the buffer.
     *
     * @param buffer the buffer.
     * @param eof    the eof indicator
     * @return the codepoint
     */
    private int peekNext(final CharBuffer buffer, final boolean eof) {
        assert !moreDataNeededNext(buffer, eof) : "Can peek only if there is data available";
        if (eof && buffer.remaining() == 0) {
            return -1;
        }
        final int codepoint = Character.codePointAt(buffer, 0);
        final int p = Character.charCount(codepoint);
        if (eof && buffer.remaining() == p) {
            return -1;
        }
        return Character.codePointAt(buffer, p);
    }

    /**
     * Add error to the token.
     *
     * @param id the error id
     * @param s  the star pos
     * @param e  the end pos
     */
    private void error(final String id, final TextPos s, final TextPos e) {
        errorInfo = new ErrorInfo(id, ErrorInfo.NO_ARGS, s, e, systemId, errorInfo);
    }

    /**
     * Check if more data is needed.
     *
     * @param buffer the buffer
     * @param eof    the eof indicator
     * @return true if more data is needed
     */
    private boolean moreDataNeeded(final CharBuffer buffer, final boolean eof) {
        return !eof && (buffer.remaining() == 0
                || buffer.remaining() == 1 && Character.isHighSurrogate(buffer.charAt(0)));
    }

    /**
     * Check if more data is needed for peeking the next character (after the current).
     *
     * @param buffer the buffer
     * @param eof    the eof indicator
     * @return true if more data is needed
     */
    private boolean moreDataNeededNext(final CharBuffer buffer, final boolean eof) {
        if (eof) {
            return false;
        }
        if (buffer.remaining() == 0 || buffer.remaining() == 1 && Character.isHighSurrogate(buffer.charAt(0))) {
            return true;
        }
        final int codepoint = Character.codePointAt(buffer, 0);
        final int p = Character.charCount(codepoint);
        return buffer.remaining() == p || buffer.remaining() == p + 1 && Character.isHighSurrogate(buffer.charAt(p));
    }

    /**
     * Create token.
     *
     * @return output available status
     */
    private ParserState makeToken() {
        if (next != null) {
            throw new IllegalStateException("Next token is already available: " + next);
        }
        final TextPos end = current();
        final TokenKey key;
        if (kind.hasQuotes()) {
            key = TokenKey.quoted(kind, modifier, quoteClass);
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
        modifier = null;
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
        final Token rc = next;
        if (rc == null) {
            throw new IllegalStateException("No token available. Call parse: " + this);
        }
        next = null;
        return rc;
    }

    @Override
    public String toString() {
        return "LexerImpl{"
                + "started=" + started
                + ", finished=" + finished
                + ", text=" + text
                + ", kind=" + kind
                + ", next=" + next
                + ", start=" + start
                + ", line=" + line
                + ", column=" + column
                + ", offset=" + offset
                + ", systemId='" + systemId + '\''
                + ", modifier='" + modifier + '\''
                + ", startQuote=" + startQuote
                + ", endQuote=" + endQuote
                + ", errorInfo=" + errorInfo
                + ", quoteClass=" + quoteClass
                + ", phase=" + phase
                + ", phaseStart=" + phaseStart
                + ", numberBase=" + numberBase
                + '}';
    }
}
