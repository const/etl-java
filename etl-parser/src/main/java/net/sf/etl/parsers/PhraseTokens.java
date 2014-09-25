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
 * This class describes kinds for phrase tokens.
 *
 * @author const
 */
public enum PhraseTokens {
    /**
     * End of file token.
     */
    EOF,
    /**
     * start of the block event. this event normally happens before opening
     * brace.
     */
    START_BLOCK,
    /**
     * end of the block event. this event normally happens after closing brace.
     */
    END_BLOCK,
    /**
     * the hard segment separator.
     */
    STATEMENT_END,
    /**
     * the soft end of the segment.
     */
    SOFT_STATEMENT_END,
    /**
     * ignorable token for example whitespace, new line or comment.
     */
    IGNORABLE,
    /**
     * token that is marks start or end of block or segment. Such tokens are
     * normally ignored by further parsers because they are already processed at
     * this level.
     */
    CONTROL,
    /**
     * significant token like identifier.
     */
    SIGNIFICANT
}
