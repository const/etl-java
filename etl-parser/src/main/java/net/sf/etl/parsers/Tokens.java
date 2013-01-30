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

/**
 * This class represents token kinds that might be generated by lexer. The class
 * follows enumeration pattern.
 *
 * @author const
 */
public enum Tokens {
    /**
     * String token. String token is sequence of characters enclosed by matching
     * quote characters (note that back quote '`' is treated as graphics
     * character). "\" is start of escape sequence. The string is pretty much
     * like Java string, except full unicode is supported. Also there is no
     * octal escape sequence and unicode escape sequence is changed to format
     * \UH...H;, (java escape and C-like escape are still supported).
     */
    STRING(false, true, PhraseTokens.SIGNIFICANT),
    /**
     * A string with alphanumeric prefix
     */
    PREFIXED_STRING(true, true, PhraseTokens.SIGNIFICANT),
    /**
     * A multiline string token. String token is sequence of characters enclosed
     * by three matching quote characters (note that back quote '`' is treated
     * as graphics character). "\" is start of escape sequence. A new line
     * character is allowed inside string and it is interpreted as \n. Also
     * there is no octal escape sequence and unicode escape sequence is changed
     * to format \UH...H;, (java escape and C-like escape are still supported).
     */
    MULTILINE_STRING(false, true, PhraseTokens.SIGNIFICANT),
    /**
     * A multiline string with alphanumeric prefix
     */
    PREFIXED_MULTILINE_STRING(true, true, PhraseTokens.SIGNIFICANT),
    /**
     * identifier token
     */
    IDENTIFIER(false, false, PhraseTokens.SIGNIFICANT),
    /**
     * Lexical rule for it is non zero length line consisting from "~+-/%^&*|<>
     * =:?!.^@#`" graphics could not contain "//" and "/*" because they are
     * always treated as beginning of comment, any standard Java operator could
     * be defined using it.
     */
    GRAPHICS(false, false, PhraseTokens.SIGNIFICANT),
    /**
     * open round bracket
     */
    BRACKET(false, false, PhraseTokens.SIGNIFICANT),
    /**
     * open curly bracket
     */
    OPEN_CURLY(false, false, PhraseTokens.START_BLOCK),
    /**
     * close curly bracket
     */
    CLOSE_CURLY(false, false, PhraseTokens.END_BLOCK),
    /**
     * a semicolon
     */
    SEMICOLON(false, false, PhraseTokens.STATEMENT_END),
    /**
     * a comma
     */
    COMMA(false, false, PhraseTokens.SIGNIFICANT),
    /**
     * an integer literal
     */
    INTEGER(false, false, PhraseTokens.SIGNIFICANT),
    /**
     * a floating point literal
     */
    FLOAT(false, false, PhraseTokens.SIGNIFICANT),
    /**
     * integer literal with suffix like 1L or 1ul
     */
    INTEGER_WITH_SUFFIX(true, false, PhraseTokens.SIGNIFICANT),
    /**
     * floating point literal with suffix like 1.0D or 0.1f
     */
    FLOAT_WITH_SUFFIX(true, false, PhraseTokens.SIGNIFICANT),
    /**
     * a line comment
     */
    LINE_COMMENT(false, false, PhraseTokens.IGNORABLE),
    /**
     * a doc comment
     */
    DOC_COMMENT(false, false, PhraseTokens.SIGNIFICANT),
    /**
     * a C-like block comment. This token is used to report block comments in
     * case when block comment is one line, or when partial tokens are disabled.
     */
    BLOCK_COMMENT(false, false, PhraseTokens.IGNORABLE),
    /**
     * whitespace token. It is a sequence of space or tab characters. It also represents invalid characters.
     */
    WHITESPACE(false, false, PhraseTokens.IGNORABLE),
    /**
     * New line token. It is one of the following: "\n" "\f" "\n\r" "\r\n"
     */
    NEWLINE(false, false, PhraseTokens.SOFT_STATEMENT_END),
    /**
     * End of file token. This is the only token that has zero content
     */
    EOF(false, false, PhraseTokens.EOF);

    /**
     * if true the token should have start and end quotes
     */
    private final boolean quoted;
    /**
     * The role in the phrase syntax
     */
    private final PhraseTokens phraseRole;
    /**
     * if true, the token should have some modifier (prefix or suffix)
     */
    private final boolean modified;

    private Tokens(boolean modified, boolean quoted, PhraseTokens role) {
        this.modified = modified;
        this.quoted = quoted;
        phraseRole = role;
    }

    /**
     * @return if true, the token of this kind should have start and end quotes
     */
    public boolean hasQuotes() {
        return quoted;
    }

    /**
     * @return if true, the token of this kind should have a modifier
     */
    public boolean hasModifier() {
        return modified;
    }

    /**
     * @return the role in the phrase syntax
     */
    public PhraseTokens getPhraseRole() {
        return phraseRole;
    }
}
