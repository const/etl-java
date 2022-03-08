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

package net.sf.etl.parsers.event.grammar;

import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.GrammarId;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.unstable.model.grammar.Grammar;
import net.sf.etl.parsers.resource.ResolvedObject;
import net.sf.etl.parsers.resource.ResourceRequest;
import net.sf.etl.parsers.resource.ResourceUsage;

import java.util.Collection;

/**
 * The public grammar compiler engine interface. The grammar compiler allows making complied grammars
 * from loaded grammar objects. The grammar compiler accepts incomplete objects.
 */
public interface GrammarCompilerEngine {
    /**
     * Start compiling the grammar.
     *
     * @param reference the initial grammar request (the value will be used for value returned from
     *                  {@link #read()} after grammar is completed)
     */
    void start(ResourceRequest reference);

    /**
     * @return compile the current state for grammars
     */
    ParserState process();

    /**
     * @return get more grammars needed to compile
     */
    Collection<ResourceRequest> requests();

    /**
     * Parse and provide grammar object.
     *
     * @param grammar the parsed grammar model object
     * @param errors  the errors that happened during the parsing grammar object
     * @throws IllegalStateException if grammar was already provided of there is no active requests for this grammar
     */
    void provide(ResolvedObject<Grammar> grammar, ErrorInfo errors);

    /**
     * This method is invoked if the resource request fails to be satisfied.
     *
     * @param request   the resource request
     * @param resources the used resources
     * @param errors    the errors related to the request
     */
    void fail(ResourceRequest request, Collection<ResourceUsage> resources, ErrorInfo errors);

    /**
     * Get already provided object by resolved system id the resolved objects with the same resolved system id
     * are considered the same from point of view of the compiler.
     *
     * @param systemId the system id that was previously provided as a resolved object
     * @return the first provided resolved object with the same system id
     */
    ResolvedObject<Grammar> getProvided(GrammarId systemId);

    /**
     * @return read compiled grammar
     */
    ResolvedObject<CompiledGrammar> read();
}
