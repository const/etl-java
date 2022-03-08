/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2022 Konstantin Plotnikov
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

import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.PhraseTokens;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.event.Cell;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.PhraseParser;

import java.util.ArrayList;
import java.util.List;

/**
 * The phrase parser.
 */
public final class PhraseParserImpl implements PhraseParser {
    /**
     * In statement sequence.
     */
    private static final int STATEMENT_SEQUENCE = 0;
    /**
     * In the statement.
     */
    private static final int STATEMENT = 1;
    /**
     * In the statement just after soft statement end.
     */
    private static final int STATEMENT_AFTER_SOFT = 2;
    /**
     * In the statement just after block start.
     */
    private static final int AFTER_BLOCK_START = 3;
    /**
     * In the statement before block end.
     */
    private static final int BEFORE_BLOCK_END = 4;
    /**
     * Before EOF.
     */
    private static final int BEFORE_EOF = 5;
    /**
     * After EOF was reported.
     */
    private static final int AFTER_EOF = 6;
    /**
     * The stack of block positions.
     */
    private final List<TextPos> blockStarts = new ArrayList<>();
    /**
     * The parser state.
     */
    private int state = STATEMENT_SEQUENCE;
    /**
     * The system id for phrase parser.
     */
    private String systemId = LexerImpl.UNKNOWN_FILE;
    /**
     * The output.
     */
    private PhraseToken output;
    /**
     * The accumulated errors.
     */
    private ErrorInfo errors;

    @Override
    public void start(final String reportedSystemId) {
        this.systemId = reportedSystemId;
    }

    @Override
    public ParserState parse(final Cell<Token> token) { // NOPMD
        if (output != null) {
            return ParserState.OUTPUT_AVAILABLE;
        }
        if (state == AFTER_EOF) {
            return ParserState.EOF;
        }
        if (!token.hasElement()) {
            return ParserState.INPUT_NEEDED;
        }
        final Token t = token.peek();
        while (true) {
            switch (state) {
                case STATEMENT_SEQUENCE:
                    switch (t.kind().getPhraseRole()) {
                        case IGNORABLE:
                        case SOFT_STATEMENT_END:
                            return consume(token, PhraseTokens.IGNORABLE);
                        case SIGNIFICANT:
                            return consume(token, PhraseTokens.SIGNIFICANT, STATEMENT);
                        case STATEMENT_END:
                            return consume(token, PhraseTokens.CONTROL);
                        case EOF:
                            state = BEFORE_EOF;
                            break;
                        case END_BLOCK:
                            if (blockStarts.isEmpty()) {
                                error("phrase.UnmatchedClosingCurly", t.start(), t.end());
                                return consume(token, PhraseTokens.CONTROL);
                            } else {
                                blockStarts.remove(blockStarts.size() - 1);
                                return peek(t, PhraseTokens.CONTROL, BEFORE_BLOCK_END);
                            }
                        case START_BLOCK:
                            blockStarts.add(t.start());
                            return before(t, PhraseTokens.START_BLOCK, AFTER_BLOCK_START);
                        default:
                            throw new IllegalStateException("Unknown phrase role: " + t.kind().getPhraseRole());
                    }
                    break;
                case STATEMENT:
                    switch (t.kind().getPhraseRole()) {
                        case IGNORABLE:
                            return consume(token, PhraseTokens.IGNORABLE);
                        case SOFT_STATEMENT_END:
                            return before(t, PhraseTokens.SOFT_STATEMENT_END, STATEMENT_AFTER_SOFT);
                        case SIGNIFICANT:
                            return consume(token, PhraseTokens.SIGNIFICANT);
                        case STATEMENT_END:
                        case EOF:
                        case END_BLOCK:
                            return before(t, PhraseTokens.STATEMENT_END, STATEMENT_SEQUENCE);
                        case START_BLOCK:
                            blockStarts.add(t.start());
                            return before(t, PhraseTokens.START_BLOCK, AFTER_BLOCK_START);
                        default:
                            throw new IllegalStateException("Unknown phrase role: " + t.kind().getPhraseRole());
                    }
                case STATEMENT_AFTER_SOFT:
                    switch (t.kind().getPhraseRole()) {
                        case IGNORABLE:
                        case SOFT_STATEMENT_END:
                            return consume(token, PhraseTokens.IGNORABLE);
                        default:
                            state = STATEMENT;
                            break;
                    }
                    break;
                case AFTER_BLOCK_START:
                    return consume(token, PhraseTokens.CONTROL, STATEMENT_SEQUENCE);
                case BEFORE_BLOCK_END:
                    return after(token, PhraseTokens.END_BLOCK, STATEMENT);
                case BEFORE_EOF:
                    if (blockStarts.isEmpty()) {
                        return consume(token, PhraseTokens.EOF, AFTER_EOF);
                    } else {
                        final TextPos startPos = blockStarts.remove(blockStarts.size() - 1);
                        error("phrase.UnterminatedBlock", startPos);
                        return before(t, PhraseTokens.END_BLOCK, STATEMENT);
                    }
                default:
                    throw new IllegalStateException("Unknown state: " + state);
            }
        }
    }

    /**
     * Report token before the current lexer token.
     *
     * @param token     the token
     * @param kind      the kind
     * @param nextState the next state
     * @return the parser state
     */
    private ParserState before(final Token token, final PhraseTokens kind, final int nextState) {
        state = nextState;
        output = new PhraseToken(kind, token.start(), errors);
        errors = null;
        return ParserState.OUTPUT_AVAILABLE;
    }

    /**
     * Report token after the current lexer token.
     *
     * @param token     the token
     * @param kind      the kind
     * @param nextState the next state
     * @return the parser state
     */
    private ParserState after(final Cell<Token> token, final PhraseTokens kind, final int nextState) {
        state = nextState;
        output = new PhraseToken(kind, token.take().end(), errors);
        errors = null;
        return ParserState.OUTPUT_AVAILABLE;
    }

    /**
     * Report new error.
     *
     * @param id    error id
     * @param start the start position
     * @param end   the end position
     */
    private void error(final String id, final TextPos start, final TextPos end) {
        errors = new ErrorInfo(id, ErrorInfo.NO_ARGS, start, end, systemId, errors);
    }

    /**
     * Report new error.
     *
     * @param id       the error id
     * @param position the start and end position
     */
    private void error(final String id, final TextPos position) {
        error(id, position, position);
    }


    /**
     * Consume token from the cell, report it, and switch to the next state.
     *
     * @param token     the token cell
     * @param kind      the kind
     * @param nextState the next state
     * @return the parser state
     */
    private ParserState consume(final Cell<Token> token, final PhraseTokens kind, final int nextState) {
        state = nextState;
        return consume(token, kind);
    }

    /**
     * Consume token from the cell, and report it.
     *
     * @param token the token cell
     * @param kind  the kind
     * @return the parser state
     */
    private ParserState consume(final Cell<Token> token, final PhraseTokens kind) {
        output = new PhraseToken(kind, token.take(), errors);
        errors = null;
        return ParserState.OUTPUT_AVAILABLE;
    }

    /**
     * Report current token, and switch to the new state.
     *
     * @param token     the token
     * @param kind      the kind
     * @param nextState the next state
     * @return the parser state
     */
    private ParserState peek(final Token token, final PhraseTokens kind, final int nextState) {
        state = nextState;
        output = new PhraseToken(kind, token, errors);
        errors = null;
        return ParserState.OUTPUT_AVAILABLE;
    }


    @Override
    public PhraseToken read() {
        if (output != null) {
            final PhraseToken rc = output;
            output = null;
            return rc;
        } else if (state == AFTER_EOF) {
            return null;
        } else {
            throw new IllegalStateException("No output available, parse more tokens: " + state);
        }
    }
}
