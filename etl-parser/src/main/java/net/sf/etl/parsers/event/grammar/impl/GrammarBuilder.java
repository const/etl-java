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
import net.sf.etl.parsers.GrammarInfo;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.event.grammar.KeywordContext;
import net.sf.etl.parsers.event.grammar.TermParserStateFactory;
import net.sf.etl.parsers.event.grammar.impl.flattened.ContextView;
import net.sf.etl.parsers.event.grammar.impl.flattened.GrammarView;
import net.sf.etl.parsers.event.impl.term.BlockStateFactory;
import net.sf.etl.parsers.event.impl.term.StatementSequenceStateFactory;
import net.sf.etl.parsers.event.impl.term.action.buildtime.ActionLinker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A builder for specific grammar. This class takes flattened
 * {@link GrammarView} of the grammar and builds an instance of
 * {@link net.sf.etl.parsers.event.grammar.CompiledGrammar}. Translation is done in context of instance
 * of {@link GrammarAssemblyBuilder} class.
 *
 * @author const
 */
public final class GrammarBuilder {
    /**
     * The assembly builder that is used to build an assembly of grammars.
     */
    private final GrammarAssemblyBuilder assemblyBuilder;
    /**
     * The grammar view.
     */
    private final GrammarView grammarView;
    /**
     * The context builders.
     */
    private final Map<ContextView, ContextBuilder> contextBuilders = new HashMap<ContextView, ContextBuilder>(); // NOPMD
    /**
     * The grammars linked from this grammar.
     */
    private final List<CompiledGrammar> linkedGrammars = new ArrayList<CompiledGrammar>();
    /**
     * The default context.
     */
    private DefinitionContext defaultContext;
    /**
     * The compiled grammar.
     */
    private CompiledGrammar compiledGrammar;


    /**
     * The constructor.
     *
     * @param g       the grammar view to wrap
     * @param builder the assembly builder
     */
    public GrammarBuilder(final GrammarAssemblyBuilder builder, final GrammarView g) {
        super();
        this.assemblyBuilder = builder;
        this.grammarView = g;
    }

    /**
     * This method identifies which factories should be created and
     * creates them. But it does not yet fill them with actual content. Because
     * it might require to reference other peer factories or activations in
     * them.
     */
    public void prepare() {
        // prepare contexts
        for (final ContextView view : grammarView.contexts()) {
            final ContextBuilder builder = new ContextBuilder(this, view); // NOPMD
            contextBuilders.put(view, builder);
            builder.prepare();
        }
    }

    /**
     * @return Returns the assemblyBuilder.
     */
    public GrammarAssemblyBuilder assemblyBuilder() {
        return assemblyBuilder;
    }

    /**
     * @return Returns the view.
     */
    public GrammarView grammarView() {
        return grammarView;
    }

    /**
     * Build nodes for all contexts.
     */
    public void buildNodes() {
        for (final ContextBuilder builder : contextBuilders.values()) {
            builder.buildNodes();
        }
    }

    /**
     * Build nodes for all contexts.
     */
    public void buildLookAhead() {
        for (final ContextBuilder builder : contextBuilders.values()) {
            builder.buildLookAhead();
        }
    }

    /**
     * Build nodes for all contexts.
     */
    public void buildStateMachines() {
        for (final ContextBuilder builder : contextBuilders.values()) {
            builder.buildStateMachines();
        }
    }

    /**
     * Find context builder for corresponding context view. Note that context
     * might come from other grammar.
     *
     * @param contextView a context view for which context builder is searched.
     * @return a context builder for corresponding context view
     */
    public ContextBuilder contextBuilder(final ContextView contextView) {
        final GrammarBuilder b;
        if (contextView.grammar() == grammarView) {
            b = this;
        } else {
            b = assemblyBuilder.grammarBuilder(contextView.grammar());
        }
        return b.contextBuilders.get(contextView);
    }

    /**
     * @return logical name of grammar
     */
    public String grammarName() {
        return grammarView.grammarName();
    }

    /**
     * @return the linker
     */
    public ActionLinker getLinker() {
        return assemblyBuilder().geLinker();
    }

    /**
     * @return the default context
     */
    public DefinitionContext getDefaultContext() {
        return defaultContext;
    }

    /**
     * Set default context.
     *
     * @param defaultContext the context.
     */
    public void setDefaultContext(final DefinitionContext defaultContext) {
        this.defaultContext = defaultContext;
    }

    /**
     * @return the grammar information
     */
    public GrammarInfo getGrammarInfo() {
        return grammarView.grammarInfo();
    }

    /**
     * @return all errors.
     */
    public ErrorInfo allErrors() {
        // TODO make errors more local
        return assemblyBuilder.getErrors();
    }


    /**
     * Create compiled grammars.
     */
    public void buildCompiledGrammars() {
        final HashMap<DefinitionContext, TermParserStateFactory> statements =
                new HashMap<DefinitionContext, TermParserStateFactory>();
        final HashMap<DefinitionContext, TermParserStateFactory> statementSequences =
                new HashMap<DefinitionContext, TermParserStateFactory>();
        final HashMap<DefinitionContext, KeywordContext> keywords = new HashMap<DefinitionContext, KeywordContext>();
        final HashMap<ExpressionContext, TermParserStateFactory> expressionParsers =
                new HashMap<ExpressionContext, TermParserStateFactory>();
        // TODO expression
        final ActionLinker linker = getLinker();
        for (final ContextBuilder contextBuilder : contextBuilders.values()) {
            if (contextBuilder.contextView().isAbstract()) {
                continue;
            }
            final TermParserStateFactory parser = contextBuilder.parser();
            if (parser != null) {
                final DefinitionContext definitionContext = contextBuilder.termContext();
                final StatementSequenceStateFactory sequenceParser = new StatementSequenceStateFactory(parser); // NOPMD
                linker.resolveBlock(definitionContext, new BlockStateFactory(definitionContext, sequenceParser));//NOPMD
                statementSequences.put(definitionContext, sequenceParser);
                statements.put(definitionContext, parser);
                keywords.put(definitionContext, contextBuilder.getKeywordContext());
            }
        }
        final boolean script = grammarView.getGrammar().getScriptModifier() != null;
        compiledGrammar = new BasicCompiledGrammar(grammarView.createDescriptor(),
                allErrors(),
                defaultContext,
                keywords,
                statements,
                statementSequences,
                expressionParsers,
                linkedGrammars,
                new ArrayList<DefinitionContext>(statementSequences.keySet()),
                new ArrayList<ExpressionContext>(expressionParsers.keySet()), script);
    }

    /**
     * @return the compiled grammar
     */
    public CompiledGrammar compiledGrammar() {
        return compiledGrammar;
    }

    /**
     * Link compiled grammars.
     */
    public void linkGrammars() {
        for (final GrammarView view : grammarView.getGrammarDependencies()) {
            linkedGrammars.add(assemblyBuilder.grammarBuilder(view).compiledGrammar());
        }
    }
}
