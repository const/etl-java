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
package net.sf.etl.parsers;

import net.sf.etl.parsers.characters.QuoteClass;

/**
 * Objects of this class represent tokens in token stream. The object is
 * immutable provided that error arguments are immutable if it is a error token.
 *
 * @author const
 */
public final class Token extends AbstractToken {
    /**
     * kind of token or would-be-kind in case of error
     */
    private final TokenKey key;
    /**
     * full text of token
     */
    private final String text;

    /**
     * A constructor for token with or without special value
     *
     * @param kind   the kind of token
     * @param text   the token text
     * @param start  the start of token in text
     * @param end    the end of token in text
     * @param errors the errors
     * @throws IllegalArgumentException when special value cannot be associated with token.
     */
    public Token(TokenKey kind, String text, TextPos start, TextPos end, ErrorInfo errors) {
        super(start, end, errors);
        this.key = kind;
        this.text = text;
    }


    /**
     * @return the key for the token
     */
    public TokenKey key() {
        return key;
    }

    /**
     * @return kind of token
     */
    public Tokens kind() {
        return key.kind();
    }

    /**
     * @return token text
     */
    public String text() {
        return text;
    }

    /**
     * @return quote character for string
     */
    public QuoteClass quoteClass() {
        return key.quoteClass();
    }

    /**
     * @return suffix for numeric literal with suffix
     */
    public String suffix() {
        return key.suffix();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Token["
                + key
                + ":"
                + text
                + ", "
                + start()
                + " - "
                + end()
                + (errorInfo() != null ? ", errorId=" + errorInfo().errorId()
                : "") + "]";
    }
}
