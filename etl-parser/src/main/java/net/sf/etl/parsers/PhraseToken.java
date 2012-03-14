/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2009 Constantine A Plotnikov
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

package net.sf.etl.parsers;

/**
 * A phrase token. These tokens are returned by phrase parser.
 *
 * @author const
 */
public final class PhraseToken extends AbstractToken {
    /**
     * kind of phase token
     */
    private final PhraseTokens kind;
    /**
     * Wrapped token from lexer
     */
    private final Token wrappedToken;

    /**
     * A constructor from token
     *
     * @param kind  a phrase token kind
     * @param token a a token from lexer
     */
    public PhraseToken(PhraseTokens kind, Token token) {
        super(token.start(), token.end(), token.errorInfo());
        if (kind == null) {
            throw new NullPointerException("Kind must not be null");
        }
        switch (kind) {
            case SIGNIFICANT:
            case IGNORABLE:
            case CONTROL:
                break;
            case EOF:
                if (token.kind() != Tokens.EOF) {
                    throw new IllegalArgumentException("Invalid kind " + kind
                            + " for token " + token);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid kind " + kind
                        + "for this constructor.");
        }
        this.kind = kind;
        this.wrappedToken = token;
    }

    /**
     * A constructor for mark-up phrase token
     *
     * @param kind     a kind of token. It must be one of the following values
     *                 START_BLOCK, END_BLOCK, START_SEGMENT, or END_BLOCK.
     * @param position a position in stream
     */
    public PhraseToken(PhraseTokens kind, TextPos position) {
        super(position, position);
        if (kind == null) {
            throw new NullPointerException("Kind must not be null");
        }
        if (position == null) {
            throw new NullPointerException("Position must not be null");
        }
        switch (kind) {
            case END_BLOCK:
            case END_SEGMENT:
            case START_BLOCK:
            case START_SEGMENT:
                break;
            default:
                throw new IllegalArgumentException("Invalid kind " + kind
                        + "for this constructor.");
        }
        this.kind = kind;
        this.wrappedToken = null;
    }

    /**
     * A constructor for phrase error token
     *
     * @param start start of error region
     * @param end   end of error region
     * @param error identifier of error
     */
    public PhraseToken(TextPos start, TextPos end, ErrorInfo error) {
        super(start, end, error);
        this.kind = PhraseTokens.ERROR;
        this.wrappedToken = null;
    }

    /**
     * @return phrase token kind
     */
    public PhraseTokens kind() {
        return kind;
    }

    /**
     * @return a wrapped token if there is one or null
     */
    public Token token() {
        return wrappedToken;
    }

    /**
     * This method checks if segment parser currently reporting a token.
     *
     * @return kind of current token.
     */
    public boolean hasToken() {
        switch (kind) {
            case SIGNIFICANT:
            case IGNORABLE:
            case CONTROL:
            case EOF:
            case LEXICAL_ERROR:
                return true;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return "PhraseToken{" +
                "kind=" + kind +
                ", wrappedToken=" + wrappedToken +
                '}';
    }
}
