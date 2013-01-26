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

import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.resource.ResolvedObject;

/**
 * The information about loaded grammar
 */
public class LoadedGrammarInfo {
    /**
     * The grammar that has been provided from user
     */
    private final ResolvedObject<CompiledGrammar> loadedGrammar;
    /**
     * The actually used grammar (in case of problems with loaded grammar)
     */
    private final CompiledGrammar usedGrammar;
    /**
     * The context from used grammar that actually used
     */
    private final DefinitionContext usedContext;

    /**
     * The constructor
     *
     * @param loadedGrammar the grammar from the compiler
     * @param usedGrammar   the actually used grammar
     * @param usedContext   the used context from the grammar
     */
    public LoadedGrammarInfo(ResolvedObject<CompiledGrammar> loadedGrammar, CompiledGrammar usedGrammar, DefinitionContext usedContext) {
        this.loadedGrammar = loadedGrammar;
        this.usedGrammar = usedGrammar;
        this.usedContext = usedContext;
    }

    /**
     * @return the resolved grammar
     */
    public ResolvedObject<CompiledGrammar> resolvedGrammar() {
        return loadedGrammar;
    }

    /**
     * @return get used grammar
     */
    public CompiledGrammar getUsedGrammar() {
        return usedGrammar;
    }

    /**
     * @return get used context
     */
    public DefinitionContext getUsedContext() {
        return usedContext;
    }
}
