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
import net.sf.etl.parsers.event.Cell;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.PhraseParser;

import java.util.ArrayList;

/**
 * The phrase parser
 */
public class PhraseParserImpl implements PhraseParser {
    String systemId = LexerImpl.UNKNOWN_FILE;
    static final int STATEMENT_SEQUENCE = 0;
    static final int STATEMENT = 1;
    static final int STATEMENT_AFTER_SOFT = 2;
    static final int AFTER_BLOCK_START = 3;
    static final int BEFORE_BLOCK_END = 4;
    static final int BEFORE_EOF = 5;
    static final int AFTER_EOF = 6;

    final ArrayList<TextPos> blockStarts = new ArrayList<TextPos>();
    int state = STATEMENT_SEQUENCE;
    PhraseToken output;
    ErrorInfo errors;

    @Override
    public void start(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public ParserState parse(Cell<Token> token) {
        if (output != null) {
            return ParserState.OUTPUT_AVAILABLE;
        }
        if (state == AFTER_EOF) {
            return ParserState.EOF;
        }
        if (!token.hasElement()) {
            return ParserState.INPUT_NEEDED;
        }
        Token t = token.peek();
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
                        TextPos startPos = blockStarts.remove(blockStarts.size() - 1);
                        error("phrase.UnterminatedBlock", startPos);
                        return before(t, PhraseTokens.END_BLOCK, STATEMENT);
                    }
            }
        }
    }

    private ParserState before(Token token, PhraseTokens kind, int nextState) {
        state = nextState;
        output = new PhraseToken(kind, token.start(), errors);
        errors = null;
        return ParserState.OUTPUT_AVAILABLE;
    }

    private ParserState after(Cell<Token> token, PhraseTokens kind, int nextState) {
        state = nextState;
        output = new PhraseToken(kind, token.take().end(), errors);
        errors = null;
        return ParserState.OUTPUT_AVAILABLE;
    }

    private void error(String id, TextPos s, TextPos e) {
        errors = new ErrorInfo(id, ErrorInfo.NO_ARGS, s, e, systemId, errors);
    }

    private void error(String id, TextPos s) {
        error(id, s, s);
    }


    private ParserState consume(Cell<Token> token, PhraseTokens kind, int nextState) {
        state = nextState;
        return consume(token, kind);
    }

    private ParserState consume(Cell<Token> token, PhraseTokens kind) {
        output = new PhraseToken(kind, token.take(), errors);
        errors = null;
        return ParserState.OUTPUT_AVAILABLE;
    }

    private ParserState peek(Token token, PhraseTokens kind, int nextState) {
        state = nextState;
        output = new PhraseToken(kind, token, errors);
        errors = null;
        return ParserState.OUTPUT_AVAILABLE;
    }


    @Override
    public PhraseToken read() {
        if (output != null) {
            PhraseToken rc = output;
            output = null;
            return rc;
        } else if (state == AFTER_EOF) {
            return null;
        } else {
            throw new IllegalStateException("No output available, parse more tokens: " + output);
        }
    }
}
