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
package net.sf.etl.parsers.event.grammar.impl.flattened;

import net.sf.etl.parsers.event.unstable.model.grammar.GrammarImport;

/**
 * A view for grammar import, beyond imported grammar reference it contains
 * original import definition used to resolve conflicts later.
 *
 * @author const
 */
public class GrammarImportView {
    /**
     * The source grammar that contains initial import directive
     */
    private final GrammarView sourceGrammar;
    /**
     * The grammar import construct
     */
    private final GrammarImport grammarImport;
    /**
     * The imported grammar
     */
    private final GrammarView importedGrammar;

    /**
     * The constructor
     *
     * @param sourceGrammar   the source grammar that contains initial import directive
     * @param importedGrammar the imported grammar
     * @param grammarImport   the grammar import construct from grammar definition
     */
    public GrammarImportView(GrammarView sourceGrammar, GrammarImport grammarImport, GrammarView importedGrammar) {
        super();
        this.sourceGrammar = sourceGrammar;
        this.grammarImport = grammarImport;
        this.importedGrammar = importedGrammar;
    }

    /**
     * @return the grammar import.
     */
    public GrammarImport getGrammarImport() {
        return grammarImport;
    }

    /**
     * @return the imported grammar.
     */
    public GrammarView getImportedGrammar() {
        return importedGrammar;
    }

    /**
     * @return the source grammar.
     */
    public GrammarView getSourceGrammar() {
        return sourceGrammar;
    }
}
