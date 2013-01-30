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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * The compiled grammar based on supplied data. The supplied data is used as is,
 * and it should not be modified from other places after grammar is returned
 * from the creation process. The supplied lists are wrapped into
 * {@link Collections#unmodifiableList(java.util.List)}. So grammar
 * is generally safe to use.
 */
public class BasicCompiledGrammar implements CompiledGrammar {
    /**
     * The descriptor for the grammar
     */
    private final ResourceDescriptor resourceDescriptor;
    /**
     * The compilation errors for the grammar
     */
    private final ErrorInfo errors;
    /**
     * The default context for the grammar
     */
    private final DefinitionContext defaultContext;
    /**
     * The keywords for the grammar
     */
    private final HashMap<DefinitionContext, KeywordContext> keywords;
    /**
     * The statement parsers
     */
    private final HashMap<DefinitionContext, TermParserStateFactory> statementParsers;
    /**
     * The statement sequence parsers for the grammar
     */
    private final HashMap<DefinitionContext, TermParserStateFactory> statementSequenceParsers;
    /**
     * The expression parsers for the grammar
     */
    private final HashMap<ExpressionContext, TermParserStateFactory> expressionParsers;
    /**
     * Other grammars that are referenced from this grammar
     */
    private final List<CompiledGrammar> otherGrammars;
    /**
     * The definition contexts
     */
    private final List<DefinitionContext> definitionContexts;
    /**
     * The expression contexts
     */
    private final List<ExpressionContext> expressionContexts;
    /**
     * If true, default mode for the grammar is script mode
     */
    private final boolean script;

    /**
     * The constructor from the state
     *
     * @param resourceDescriptor       the resource descriptor
     * @param errors                   the errors
     * @param defaultContext           the default context
     * @param keywords                 the keywords for the context
     * @param statementSequenceParsers the parsers for statement sequences
     * @param expressionParsers        the parsers for the expressions
     * @param otherGrammars            the parsers for other grammars
     * @param definitionContexts       the definition contexts
     * @param expressionContexts       the expression contexts
     * @param script                   the default script mode
     */
    public BasicCompiledGrammar(ResourceDescriptor resourceDescriptor,
                                ErrorInfo errors,
                                DefinitionContext defaultContext,
                                HashMap<DefinitionContext, KeywordContext> keywords,
                                HashMap<DefinitionContext, TermParserStateFactory> statementParsers,
                                HashMap<DefinitionContext, TermParserStateFactory> statementSequenceParsers,
                                HashMap<ExpressionContext, TermParserStateFactory> expressionParsers,
                                ArrayList<CompiledGrammar> otherGrammars,
                                ArrayList<DefinitionContext> definitionContexts,
                                ArrayList<ExpressionContext> expressionContexts, boolean script) {
        this.resourceDescriptor = resourceDescriptor;
        this.errors = errors;
        this.defaultContext = defaultContext;
        this.keywords = keywords;
        this.statementParsers = statementParsers;
        this.statementSequenceParsers = statementSequenceParsers;
        this.expressionParsers = expressionParsers;
        this.script = script;
        this.otherGrammars = Collections.unmodifiableList(otherGrammars);
        this.definitionContexts = Collections.unmodifiableList(definitionContexts);
        this.expressionContexts = Collections.unmodifiableList(expressionContexts);
    }


    @Override
    public List<CompiledGrammar> getOtherGrammars() {
        return otherGrammars;
    }

    @Override
    public ResourceDescriptor getDescriptor() {
        return resourceDescriptor;
    }

    @Override
    public ErrorInfo getErrors() {
        return errors;
    }

    @Override
    public DefinitionContext getDefaultContext() {
        return defaultContext;
    }

    @Override
    public List<DefinitionContext> getStatementContexts() {
        return definitionContexts;
    }

    @Override
    public List<ExpressionContext> getExpressionContexts() {
        return expressionContexts;
    }

    @Override
    public KeywordContext getKeywordContext(DefinitionContext context) {
        final KeywordContext keywordContext = keywords.get(context);
        if (keywordContext == null) {
            throw new IllegalArgumentException("Keywords are not defined for the context: " + context);
        }
        return keywordContext;
    }

    @Override
    public TermParserStateFactory statementSequenceParser() {
        return statementSequenceParser(getDefaultContext());
    }

    @Override
    public TermParserStateFactory statementSequenceParser(DefinitionContext context) {
        final TermParserStateFactory parser = statementSequenceParsers.get(context);
        if (parser == null) {
            throw new IllegalArgumentException("The parser is not available for context: " + context);
        }
        return parser;
    }

    @Override
    public TermParserStateFactory statementParser(DefinitionContext context) {
        final TermParserStateFactory parser = statementParsers.get(context);
        if (parser == null) {
            throw new IllegalArgumentException("The parser is not available for context: " + context);
        }
        return parser;
    }

    @Override
    public TermParserStateFactory expressionParser(ExpressionContext context) {
        final TermParserStateFactory parser = expressionParsers.get(context);
        if (parser == null) {
            throw new IllegalArgumentException("The parser is not available for context: " + context);
        }
        return parser;
    }

    @Override
    public boolean isScript() {
        return script;
    }
}
