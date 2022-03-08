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
 * Syntax roles for tokens returned by parser.
 *
 * @author const
 */
public enum SyntaxRole {
    /**
     * unknown token syntax role. this usually happens because of previous
     * syntax error so current token could not be classified
     */
    UNKNOWN,

    /**
     * primary token. this is used for literal values and identifiers that match specific literal expression like
     * "float", "integer", or "identifier".
     */
    PRIMARY,

    /**
     * primary any token, it is used when the token matches unqualified "token" expression in grammar. This case
     * is different from the normal matching, as such expression could match the values that would otherwise
     * considered keywords.
     */
    PRIMARY_ANY,

    /**
     * a keyword.
     */
    KEYWORD,

    /**
     * opening or closing bracket.
     */
    BRACKET,

    /**
     * control token.
     */
    CONTROL,

    /**
     * an operator.
     */
    OPERATOR,

    /**
     * modifier token.
     */
    MODIFIER,

    /**
     * ignorable token.
     */
    IGNORABLE,

    /**
     * a separator.
     */
    SEPARATOR,

    /**
     * a token that usually have some effect on parsing but which is not
     * specified as one other syntax tokens. For example EOF and processing
     * instructions (if future).
     */
    SPECIAL,

    /**
     * a documentation token.
     */
    DOCUMENTATION,
}
