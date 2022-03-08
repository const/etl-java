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

package net.sf.etl.parsers;

/**
 * A phrase token. These tokens are returned by phrase parser.
 *
 * @author const
 */
public final class PhraseToken extends AbstractToken {
    /**
     * kind of phase token.
     */
    private final PhraseTokens kind;
    /**
     * Wrapped token from lexer.
     */
    private final Token wrappedToken;

    /**
     * A constructor from token.
     *
     * @param kind   a phrase token kind
     * @param token  a a token from lexer
     * @param errors errors
     */
    public PhraseToken(final PhraseTokens kind, final Token token, final ErrorInfo errors) {
        super(token.start(), token.end(), errors);
        if (kind == null) {
            throw new IllegalArgumentException("Kind must not be null");
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
     * A constructor for mark-up phrase token.
     *
     * @param kind     a kind of token. It must be one of the following values
     *                 START_BLOCK, END_BLOCK, STATEMENT_END, or END_BLOCK.
     * @param position a position in stream
     * @param errors   errors
     */
    public PhraseToken(final PhraseTokens kind, final TextPos position, final ErrorInfo errors) {
        super(position, position, errors);
        if (kind == null) {
            throw new IllegalArgumentException("Kind must not be null");
        }
        switch (kind) {
            case END_BLOCK:
            case SOFT_STATEMENT_END:
            case START_BLOCK:
            case STATEMENT_END:
                break;
            default:
                throw new IllegalArgumentException("Invalid kind " + kind
                        + "for this constructor.");
        }
        this.kind = kind;
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
                return true;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return "PhraseToken{" + kind
                + ", " + wrappedToken
                + ", " + start() + "-" + end() + "}";
    }
}
