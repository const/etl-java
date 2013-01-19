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

import net.sf.etl.parsers.*;
import net.sf.etl.parsers.characters.*;
import net.sf.etl.parsers.event.Lexer;
import net.sf.etl.parsers.event.ParserState;

import java.nio.CharBuffer;

/**
 * The lexer implementation
 */
public class LexerImpl implements Lexer {
    // TODO implement tabulation size
    // phases for parsing number
    private static final int NUMBER_DECIMAL = 0;
    private static final int NUMBER_DECIMAL_FRACTION = 1;
    private static final int NUMBER_BASED = 2;
    private static final int NUMBER_BASED_FRACTION = 3;
    private static final int NUMBER_BEFORE_EXPONENT = 4;
    private static final int NUMBER_BEFORE_SUFFIX = 5;
    private static final int NUMBER_AFTER_EXPONENT = 6;
    private static final int NUMBER_AFTER_EXPONENT_SIGN = 7;
    private static final int NUMBER_IN_EXPONENT_VALUE = 8;
    private static final int NUMBER_SUFFIX = 9;
    private static final int GRAPHICS_NORMAL = 10;
    private static final int LINE_COMMENT_NORMAL = 60;
    private static final int LINE_COMMENT_START = 61;
    private static final int BLOCK_COMMENT_AFTER_STAR = 20;
    private static final int BLOCK_COMMENT_AFTER_CR = 21;
    private static final int BLOCK_COMMENT_NORMAL = 22;
    private static final int STRING_START_FIRST_QUOTE = 31;
    private static final int STRING_START_SECOND_QUOTE = 32;
    private static final int STRING_NORMAL = 33;
    private static final int STRING_ESCAPED = 35;
    private static final int STRING_MULTILINE_NORMAL = 36;
    private static final int STRING_MULTILINE_AFTER_CR = 37;
    private static final int STRING_MULTILINE_ESCAPED = 38;
    private static final int STRING_MULTILINE_END_FIRST_QUOTE = 39;
    private static final int STRING_MULTILINE_END_SECOND_QUOTE = 40;
    private static final int NEWLINE_NORMAL = 50;
    private static final int NEWLINE_AFTER_CR = 51;
    public static final String UNKNOWN_FILE = "unknown:file";
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
    private String systemId = UNKNOWN_FILE;
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
                    throw new RuntimeException("Unsupported branch: " + kind);
            }
        }
    }

    private ParserState parseNumber(CharBuffer buffer, boolean eof) {
        kind = Tokens.INTEGER;
        phase = NUMBER_DECIMAL;
        return continueNumber(buffer, eof);
    }

    private ParserState continueNumber(CharBuffer buffer, boolean eof) {
        while (true) {
            if (moreDataNeeded(buffer, eof)) return ParserState.INPUT_NEEDED;
            int codepoint = peek(buffer, eof);
            switch (phase) {
                case NUMBER_DECIMAL:
                    if (Numbers.isDecimal(codepoint) || Identifiers.isConnectorChar(codepoint)) {
                        codepoint(buffer, eof);
                    } else if (Numbers.isBasedNumberChar(codepoint) && !Identifiers.isConnectorChar(Character.codePointBefore(text, text.length()))) {
                        if (moreDataNeededNext(buffer, eof)) {
                            return ParserState.INPUT_NEEDED;
                        }
                        if (peekNext(buffer, eof) == '!') {
                            // shebang comment next
                            return makeToken();
                        }
                        try {
                            numberBase = Integer.parseInt(text.toString());
                        } catch (Throwable t) {
                            numberBase = -1;
                        }
                        codepoint(buffer, eof);
                        phase = NUMBER_BASED;
                        if (numberBase < 2 || numberBase > 36) {
                            error("lexical.NumberBaseIsOutOfRange", start, current());
                            numberBase = 36; // using it for sake of parsing only
                        }
                    } else if (codepoint == '.') {
                        if (Identifiers.isConnectorChar(text.codePointBefore(text.length()))) {
                            return makeToken();
                        }
                        if (moreDataNeededNext(buffer, eof)) return ParserState.INPUT_NEEDED;
                        codepoint = peekNext(buffer, eof);
                        if (Numbers.isDecimal(codepoint)) {
                            kind = Tokens.FLOAT;
                            phase = NUMBER_DECIMAL_FRACTION;
                            codepoint(buffer, eof);
                        } else {
                            return makeToken();
                        }
                    } else {
                        phase = NUMBER_BEFORE_EXPONENT;
                    }
                    break;
                case NUMBER_DECIMAL_FRACTION:
                    if (Numbers.isDecimal(codepoint) || Identifiers.isConnectorChar(codepoint)) {
                        codepoint(buffer, eof);
                    } else {
                        phase = NUMBER_BEFORE_EXPONENT;
                    }
                    break;
                case NUMBER_BASED:
                case NUMBER_BASED_FRACTION:
                    if (Numbers.isValidDigit(codepoint, numberBase)) {
                        codepoint(buffer, eof);
                    } else if (Identifiers.isConnectorChar(codepoint)) {
                        codepoint(buffer, eof);
                    } else if (Numbers.isBasedNumberChar(codepoint)) {
                        phase = NUMBER_BEFORE_EXPONENT;
                        codepoint(buffer, eof);
                        break;
                    } else if (codepoint == '.') {
                        kind = Tokens.FLOAT;
                        if (phase == NUMBER_BASED_FRACTION) {
                            TextPos p = current();
                            codepoint(buffer, eof);
                            error("lexical.FloatTooManyDots", p, current());
                        } else {
                            phase = NUMBER_BASED_FRACTION;
                            codepoint(buffer, eof);
                        }
                    } else if (Numbers.isAnyDigit(codepoint)) {
                        TextPos p = current();
                        codepoint(buffer, eof);
                        error("lexical.SomeDigitAreOutOfBase", p, current());
                    } else {
                        error("lexical.UnterminatedBasedNumber", start, current());
                        return makeToken();
                    }
                    break;
                case NUMBER_BEFORE_EXPONENT:
                    if (Numbers.isExponentChar(codepoint) && !Identifiers.isConnectorChar(Character.codePointBefore(text, text.length()))) {
                        kind = Tokens.FLOAT;
                        codepoint(buffer, eof);
                        phase = NUMBER_AFTER_EXPONENT;
                    } else {
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
                    if (Identifiers.isIdentifierStart(codepoint) && !Numbers.isExponentChar(codepoint) &&
                            !Identifiers.isConnectorChar(codepoint) &&
                            !Identifiers.isConnectorChar(Character.codePointBefore(text, text.length()))) {
                        phase = NUMBER_SUFFIX;
                        phaseStart = text.length();
                        codepoint(buffer, eof);
                        kind = kind == Tokens.FLOAT ? Tokens.FLOAT_WITH_SUFFIX : Tokens.INTEGER_WITH_SUFFIX;
                        break;
                    } else {
                        return makeToken();
                    }
                case NUMBER_SUFFIX:
                    if ((Identifiers.isIdentifierPart(codepoint))) {
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

    private ParserState parseString(CharBuffer buffer, boolean eof) {
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

    private ParserState continueString(CharBuffer buffer, boolean eof) {
        while (true) {
            if (moreDataNeeded(buffer, eof)) return ParserState.INPUT_NEEDED;
            int codepoint = peek(buffer, eof);
            switch (phase) {
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
                        QuoteClass endQuoteClass = QuoteClass.classify(codepoint);
                        if (endQuoteClass == quoteClass) {
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
                        QuoteClass endQuoteClass = QuoteClass.classify(codepoint);
                        if (endQuoteClass == quoteClass) {
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
                        throw new IllegalStateException("Invalid lexer state, in case of after CR, " +
                                "never should need more data: " + this);
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

    private ParserState parseSpace(CharBuffer buffer, boolean eof) {
        kind = Tokens.WHITESPACE;
        codepoint(buffer, eof);
        if (moreDataNeeded(buffer, eof)) return ParserState.INPUT_NEEDED;
        return continueSpaces(buffer, eof);
    }

    private ParserState continueSpaces(CharBuffer buffer, boolean eof) {
        while (Whitespaces.isSpace(peek(buffer, eof))) {
            codepoint(buffer, eof);
            if (moreDataNeeded(buffer, eof)) return ParserState.INPUT_NEEDED;
        }
        return makeToken();
    }

    private ParserState parseNewline(CharBuffer buffer, boolean eof) {
        kind = Tokens.NEWLINE;
        phase = NEWLINE_NORMAL;
        return continueNewLine(buffer, eof);
    }

    private ParserState continueNewLine(CharBuffer buffer, boolean eof) {
        if (consumeNewLine(buffer, eof, NEWLINE_AFTER_CR, NEWLINE_NORMAL)) {
            return makeToken();
        } else {
            return ParserState.INPUT_NEEDED;
        }
    }

    /**
     * Consume new line
     *
     * @param buffer      the buffer to consume from
     * @param eof         true if the eof
     * @param crPhase     the id of cr phase
     * @param normalPhase the id of normal phase
     * @return true if new line was successfully parsed, false if more data is needed
     */
    private boolean consumeNewLine(CharBuffer buffer, boolean eof, int crPhase, int normalPhase) {
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
     * Parse identifier
     *
     * @param buffer the buffer to parse
     * @param eof    the eof
     * @return the parsed identifier
     */
    private ParserState parseIdentifier(CharBuffer buffer, boolean eof) {
        kind = Tokens.IDENTIFIER;
        codepoint(buffer, eof);
        if (moreDataNeeded(buffer, eof)) {
            return ParserState.INPUT_NEEDED;
        }
        return continueIdentifier(buffer, eof);
    }

    private ParserState continueIdentifier(CharBuffer buffer, boolean eof) {
        int codepoint;
        while (Identifiers.isIdentifierPart(codepoint = peek(buffer, eof))) {
            codepoint(buffer, eof);
            if (moreDataNeeded(buffer, eof)) {
                return ParserState.INPUT_NEEDED;
            }
        }
        quoteClass = QuoteClass.classify(codepoint);
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
        kind = Tokens.GRAPHICS;
        phase = GRAPHICS_NORMAL;
        return continueGraphics(buffer, eof);
    }

    private ParserState continueGraphics(CharBuffer buffer, boolean eof) {
        while (true) {
            if (moreDataNeeded(buffer, eof)) {
                return ParserState.INPUT_NEEDED;
            }
            int codepoint = peek(buffer, eof);
            switch (phase) {
                case GRAPHICS_NORMAL:
                    switch (codepoint) {
                        case '/':
                            if (moreDataNeededNext(buffer, eof)) {
                                return ParserState.INPUT_NEEDED;
                            }
                            int next = peekNext(buffer, eof);
                            if (next == '*' || next == '/') {
                                if (text == null || text.length() == 0) {
                                    if (next == '*') {
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
                            int nextShebang = peekNext(buffer, eof);
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
            }
        }
    }

    private ParserState parseLineComment(CharBuffer buffer, boolean eof) {
        // it is already known that it is line comment
        kind = Tokens.LINE_COMMENT;
        int first = codepoint(buffer, eof);
        int second = codepoint(buffer, eof);
        phase = first == '/' && second == '/' ? LINE_COMMENT_START : LINE_COMMENT_NORMAL;
        if (moreDataNeeded(buffer, eof)) {
            return ParserState.INPUT_NEEDED;
        }
        return continueLineComment(buffer, eof);
    }

    private ParserState continueLineComment(CharBuffer buffer, boolean eof) {
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

    private ParserState parseBlockComment(CharBuffer buffer, boolean eof) {
        // it is already known that it is line comment
        kind = Tokens.BLOCK_COMMENT;
        codepoint(buffer, eof);
        codepoint(buffer, eof);
        phase = BLOCK_COMMENT_NORMAL;
        return continueBlockComment(buffer, eof);
    }

    private ParserState continueBlockComment(CharBuffer buffer, boolean eof) {
        while (true) {
            if (moreDataNeededNext(buffer, eof)) {
                return ParserState.INPUT_NEEDED;
            }
            int codepoint = peek(buffer, eof);
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
    private ParserState single(CharBuffer buffer, boolean eof, Tokens token) {
        codepoint(buffer, eof);
        kind = token;
        return makeToken();
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
        int c = Character.codePointAt(buffer, 0);
        if (text == null) {
            text = new StringBuilder();
        }
        int s = Character.charCount(c);
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
        return buffer.remaining() == p || (buffer.remaining() == p + 1 && Character.isHighSurrogate(buffer.charAt(p)));
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
                ", startQuote=" + startQuote +
                ", endQuote=" + endQuote +
                ", errorInfo=" + errorInfo +
                ", quoteClass=" + quoteClass +
                ", phase=" + phase +
                ", phaseStart=" + phaseStart +
                ", numberBase=" + numberBase +
                '}';
    }
}