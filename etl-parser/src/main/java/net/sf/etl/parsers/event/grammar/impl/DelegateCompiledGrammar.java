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

package net.sf.etl.parsers.event.grammar.impl;

import net.sf.etl.parsers.DefinitionContext;
import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.ExpressionContext;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.event.grammar.KeywordContext;
import net.sf.etl.parsers.event.grammar.TermParserStateFactory;
import net.sf.etl.parsers.resource.ResourceDescriptor;

import java.util.List;

/**
 * The delegate compiled grammar that wraps other grammar, but allows to redefine returned values for
 * the following methods:
 * <ul>
 * <li>{@link #getErrors()}</li>
 * <li>{@link #getDescriptor()} </li>
 * <li>{@link #getOtherGrammars()}</li>
 * </ul>
 * This implementation is used to create grammars that have compilation errors
 */
public class DelegateCompiledGrammar implements CompiledGrammar {
    /**
     * The grammar to delegate to
     */
    private final CompiledGrammar grammar;
    /**
     * The list of errors
     */
    private final ErrorInfo errors;
    /**
     * The descriptor
     */
    private final ResourceDescriptor descriptor;
    /**
     * Other grammars referenced from this grammar
     */
    private final List<CompiledGrammar> otherGrammars;

    /**
     * The constructor
     *
     * @param grammar       the grammar this grammar delegates to
     * @param errors        the grammar errors for this grammar
     * @param descriptor    the descriptor of this grammars
     * @param otherGrammars the other grammars (not that the list of other grammars is build only in case of success)
     */
    public DelegateCompiledGrammar(CompiledGrammar grammar, ErrorInfo errors, ResourceDescriptor descriptor, List<CompiledGrammar> otherGrammars) {
        this.grammar = grammar;
        this.errors = errors;
        this.descriptor = descriptor;
        this.otherGrammars = otherGrammars;
    }

    @Override
    public List<CompiledGrammar> getOtherGrammars() {
        return otherGrammars;
    }

    @Override
    public ResourceDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public ErrorInfo getErrors() {
        return errors;
    }

    @Override
    public DefinitionContext getDefaultContext() {
        return grammar.getDefaultContext();
    }

    @Override
    public List<DefinitionContext> getStatementContexts() {
        return grammar.getStatementContexts();
    }

    @Override
    public List<ExpressionContext> getExpressionContexts() {
        return grammar.getExpressionContexts();
    }

    @Override
    public KeywordContext getKeywordContext(DefinitionContext context) {
        return grammar.getKeywordContext(context);
    }

    @Override
    public TermParserStateFactory statementSequenceParser() {
        return grammar.statementSequenceParser();
    }

    @Override
    public TermParserStateFactory statementSequenceParser(DefinitionContext context) {
        return grammar.statementSequenceParser(context);
    }

    @Override
    public TermParserStateFactory statementParser(DefinitionContext context) {
        return grammar.statementParser(context);
    }

    @Override
    public TermParserStateFactory expressionParser(ExpressionContext context) {
        return grammar.expressionParser(context);
    }

    @Override
    public boolean isScript() {
        return grammar.isScript();
    }
}
