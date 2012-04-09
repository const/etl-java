/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2012 Constantine A Plotnikov
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

package net.sf.etl.parsers.event;

import net.sf.etl.parsers.PhraseToken;
import net.sf.etl.parsers.StatementContext;
import net.sf.etl.parsers.TermToken;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.resource.ResolvedObject;
import net.sf.etl.parsers.resource.ResourceRequest;

/**
 * The term parser interface that parses the stream. The initial
 */
public interface TermParser {
    /**
     * @return true after grammar was supplied to the term parser of the parser was constructed from compiled grammar
     */
    boolean isGrammarDetermined();

    /**
     * @return the compiled grammar after it is resolved
     * @throws IllegalStateException if the grammar was determined yet
     */
    CompiledGrammar grammar();

    /**
     * @return the initial context after it is resolved
     * @throws IllegalStateException if the grammar was determined yet
     */
    StatementContext initialContext();

    /**
     * Start parsing
     *
     * @param systemId the system id
     */
    void start(String systemId);

    /**
     * Get resource request for the grammar. The grammar should be loaded and compiled externally.
     *
     * @return the grammar request, note if there is no doctype statement is the source that is being parsed,
     *         the resource request will contain nulls for both public id and system id
     * @throws IllegalStateException if the grammar already determined
     */
    ResourceRequest grammarRequest();

    /**
     * Supply grammar to the term parser according to the grammar request
     *
     * @param grammar the provided grammar
     * @throws IllegalStateException if the grammar already determined
     */
    void provideGrammar(ResolvedObject<CompiledGrammar> grammar);

    /**
     * @return read current token for the parser
     */
    TermToken read();

    /**
     * Parse token,
     *
     * @param token the cell with the token. The element is removed if it is consumed and more date is needed.
     * @return the parsed state
     */
    ParserState parse(Cell<PhraseToken> token);
}
