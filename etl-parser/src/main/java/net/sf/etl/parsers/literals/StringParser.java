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

package net.sf.etl.parsers.literals;

import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.Tokens;
import net.sf.etl.parsers.characters.Identifiers;
import net.sf.etl.parsers.characters.Numbers;
import net.sf.etl.parsers.characters.QuoteClass;
import net.sf.etl.parsers.characters.Whitespaces;

/**
 * Standard string parser
 */
public class StringParser extends BaseLiteralParser {
    /**
     * The string literal prefix
     */
    private String prefix;
    /**
     * The parsed string text
     */
    private String text;
    /**
     * The quote class for the string
     */
    private QuoteClass quoteClass;
    /**
     * The start quote for string
     */
    private int startQuote = -1;
    /**
     * The end quote
     */
    private int endQuote = -1;
    /**
     * The token kind
     */
    private Tokens kind;

    /**
     * The constructor
     *
     * @param inputText the input text
     */
    public StringParser(String inputText, TextPos start, String systemId) {
        super(inputText, start, systemId);
    }


    /**
     * @return the parsed string
     */
    public StringInfo parse() {
        if (kind == null) {
            doParse();
            text = buffer.toString();
        }
        return new StringInfo(inputText, kind, text, prefix, quoteClass, startQuote, endQuote, errors);
    }

    private void doParse() {
        kind = Tokens.STRING;
        int ch = la();
        if (Identifiers.isIdentifierStart(ch)) {
            do {
                ch = next(true);
            } while (Identifiers.isIdentifierPart(ch));
            prefix = buffer.toString();
            buffer.setLength(0);
            kind = Tokens.PREFIXED_STRING;
        }
        quoteClass = QuoteClass.classify(ch);
        if (quoteClass == null) {
            error("lexical.NonQuoteCharacter");
            return;
        }
        startQuote = ch;
        final boolean multiline = ch == la(1) && ch == la(2);
        final boolean cut;
        if (multiline) {
            kind = kind == Tokens.STRING ? Tokens.MULTILINE_STRING : Tokens.PREFIXED_MULTILINE_STRING;
            next(false);
            next(false);
            ch = next(false);
            cut = ch == '|';
            if (cut) {
                ch = next(false);
            }
        } else {
            ch = next(false);
            cut = false;
        }
        while (ch != -1) {
            final QuoteClass quote = QuoteClass.classify(ch);
            if (quote != null) {
                if (multiline) {
                    if (ch == la(1) && ch == la(2)) {
                        endQuote = ch;
                        next(false);
                        next(false);
                        ch = next(false);
                        break;
                    }
                } else {
                    if (quote == quoteClass) {
                        endQuote = ch;
                        ch = next(false);
                        break;
                    }
                }
                ch = next(true);
                continue;
            }
            if (ch == '\\') {
                ch = next(false);
                switch (ch) {
                    case 'x':
                        ch = next(false);
                        if (ch == '{') {
                            ch = next(false);
                            int i = 0, value = 0;
                            while (Numbers.isHex(ch)) {
                                value = (value << 4) + Character.digit(ch, 16);
                                ch = next(false);
                                i++;
                            }
                            if (Character.isValidCodePoint(value)) {
                                if (i != 0) {
                                    buffer.appendCodePoint(value);
                                }
                            } else {
                                error("lexical.InvalidCodePoint");
                            }
                            if (ch == '}') {
                                ch = next(false);
                            } else {
                                if (ch == -1) {
                                    error("lexical.EOFInString");
                                    break;
                                } else {
                                    error("lexical.UnexpectedNonHexDigit");
                                }
                            }
                        } else {
                            ch = parseHexEscape(2, ch);
                        }
                        break;
                    case 'u':
                        ch = next(false);
                        ch = parseHexEscape(4, ch);
                        break;
                    case 'U':
                        ch = next(false);
                        ch = parseHexEscape(8, ch);
                        break;
                    case 'n':
                        buffer.append('\n');
                        ch = next(false);
                        break;
                    case 'r':
                        buffer.append('\r');
                        ch = next(false);
                        break;
                    case 't':
                        buffer.append('\t');
                        ch = next(false);
                        break;
                    case 'f':
                        buffer.append('\f');
                        ch = next(false);
                        break;
                    case 'b':
                        buffer.append('\b');
                        ch = next(false);
                        break;
                    default:
                        ch = next(true);
                }
                continue;
            }
            if (Whitespaces.isNewline(ch)) {
                if (kind == Tokens.PREFIXED_STRING || kind == Tokens.STRING) {
                    error("lexical.NewLineInString");
                    break;
                } else {
                    boolean cr = ch == Whitespaces.CR;
                    ch = next(true);
                    if (cr && ch == Whitespaces.LF) {
                        ch = next(true);
                    }
                    line++;
                    column = TextPos.START_COLUMN;
                    if (cut) {
                        while (Whitespaces.isSpace(ch)) {
                            ch = next(false);
                        }
                        if (ch == -1) {
                            error("lexical.EOFInString");
                            break;
                        }
                        if (ch == '|') {
                            ch = next(false);
                        } else {
                            error("lexical.PrefixCharExpected");
                        }
                    }
                }
                continue;
            }
            ch = next(true);
        }
        if (ch != -1) {
            error("lexical.TooManyCharactersInToken");
        }
    }

    /**
     * Parse fixed size hex escape and add it to buffer as a codepoint
     *
     * @param n  max number of characters to parse
     * @param ch the current character
     * @return the next current character
     */
    private int parseHexEscape(int n, int ch) {
        int i = 0;
        int value = 0;
        while (i < n && Numbers.isHex(ch)) {
            value = (value << 4) + Character.digit(ch, 16);
            ch = next(false);
            i++;
        }
        if (Character.isValidCodePoint(value)) {
            if (i != 0) {
                buffer.appendCodePoint(value);
            }
        } else {
            error("lexical.InvalidCodePoint");
        }
        if (ch == -1) {
            error("lexical.EOFInString");
        }
        if (i != n) {
            error("lexical.UnexpectedNonHexDigit");
        }
        return ch;
    }

}
