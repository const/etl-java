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

package net.sf.etl.parsers.event.grammar;

import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.event.TermParser;

/**
 * The implementation side API for {@link net.sf.etl.parsers.event.impl.term.TermParserImpl}
 * that could be used by parser implementations.
 */
public interface TermParserContext {
    /**
     * @return true the parser is in the script mode.
     */
    boolean isScriptMode();

    /**
     * @return the current token
     */
    PhraseToken current();

    /**
     * Indicate that phrase token is consumed and the parser should request next token from phrase parser
     * After calling this method, the control should be passed back to the original parser.
     */
    void consumePhraseToken();

    /**
     * Produce token at the end of the stream.
     *
     * @param token the token to produce
     * @return true if next token should be generated
     */
    boolean produce(TermToken token);

    /**
     * Produce token after mark.
     *
     * @param token the token to produce
     * @return true if next token should be generated
     */
    boolean produceAfterMark(TermToken token);

    /**
     * Produce before mark.
     *
     * @param termToken the token mark
     */
    void produceBeforeMark(TermToken termToken);

    /**
     * Push mark at mark stack.
     */
    void pushMark();

    /**
     * Commit the current mark, the mark could be committed only if all previous marks were committed.
     */
    void commitMark();

    /**
     * Pop mark at mark stack.
     */
    void popMark();

    /**
     * Push keyword context.
     *
     * @param context context
     */
    void pushKeywordContext(KeywordContext context);

    /**
     * @return classify as keyword
     */
    Keyword classify();

    /**
     * Pop keyword context.
     *
     * @param context context
     */
    void popKeywordContext(KeywordContext context);

    /**
     * State management.
     *
     * @param stateFactory state factory
     */
    void call(TermParserStateFactory stateFactory);

    /**
     * Exit using state.
     *
     * @param state   the state
     * @param success if ended successfully
     */
    void exit(TermParserState state, boolean success);

    /**
     * Change state of flag is skip ignorable is needed.
     */
    void advanced();

    /**
     * @return true if skip ignorable is needed
     */
    boolean isAdvanceNeeded();

    /**
     * @return true if statement could be ended now by soft statement end
     */
    boolean canSoftEndStatement();

    /**
     * Start soft end context.
     */
    void startSoftEndContext();

    /**
     * Disable soft end for the segment.
     */
    void disableSoftEnd();

    /**
     * Enable soft end for the segment, note if the segment soft end was disabled twice,
     * then it should be enabled the same amount of times.
     *
     * @return true if the soft ends are actually enabled (false if it was disabled more times than enabled)
     */
    boolean enableSoftEnd();

    /**
     * End soft end context.
     */
    void endSoftEndContext();

    /**
     * @return term parser for this context
     */
    TermParser parser();

    /**
     * @return the current token at mark or null
     */
    TermToken peekObjectAtMark();
}
