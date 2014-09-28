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

import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.grammar.BootstrapGrammars;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.event.grammar.GrammarCompilerEngine;
import net.sf.etl.parsers.event.grammar.impl.flattened.GrammarAssembly;
import net.sf.etl.parsers.event.grammar.impl.flattened.GrammarView;
import net.sf.etl.parsers.event.impl.term.action.buildtime.ActionLinker;
import net.sf.etl.parsers.event.unstable.model.grammar.Grammar;
import net.sf.etl.parsers.resource.ResolvedObject;
import net.sf.etl.parsers.resource.ResourceDescriptor;
import net.sf.etl.parsers.resource.ResourceRequest;
import net.sf.etl.parsers.resource.ResourceUsage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * The grammar compiler.
 */
public final class GrammarAssemblyBuilder implements GrammarCompilerEngine {
    /**
     * The collection of grammar views.
     */
    private final GrammarAssembly assembly = new GrammarAssembly();
    /**
     * The map from grammar view to builders.
     */
    private final Map<GrammarView, GrammarBuilder> viewToBuilder = new HashMap<GrammarView, GrammarBuilder>(); // NOPMD
    /**
     * The linker.
     */
    private final ActionLinker linker = new ActionLinker();
    /**
     * The root grammar request.
     */
    private ResourceRequest rootGrammarRequest;
    /**
     * The root compiled grammar.
     */
    private ResolvedObject<CompiledGrammar> rootGrammar;

    @Override
    public void start(final ResourceRequest reference) {
        rootGrammarRequest = reference;
        assembly.start(reference);
    }

    @Override
    public ParserState process() { // NOPMD
        if (!assembly.unresolved().isEmpty()) {
            return ParserState.RESOURCE_NEEDED;
        }
        if (rootGrammar != null) {
            return ParserState.OUTPUT_AVAILABLE;
        }
        if (assembly.hadErrors()) {
            return buildFailedGrammar();
        }
        // all grammars are resolved by this point, proceed with processing them
        assembly.flatten();
        if (assembly.hadErrors()) {
            return buildFailedGrammar();
        }
        for (final GrammarView grammarView : assembly.grammars()) {
            viewToBuilder.put(grammarView, new GrammarBuilder(this, grammarView)); // NOPMD
        }
        if (assembly.hadErrors()) {
            return buildFailedGrammar();
        }
        for (final GrammarBuilder builder : viewToBuilder.values()) {
            builder.prepare();
        }
        if (assembly.hadErrors()) {
            return buildFailedGrammar();
        }
        for (final GrammarBuilder builder : viewToBuilder.values()) {
            builder.buildNodes();
        }
        if (assembly.hadErrors()) {
            return buildFailedGrammar();
        }
        for (final GrammarBuilder builder : viewToBuilder.values()) {
            builder.buildLookAhead();
        }
        if (assembly.hadErrors()) {
            return buildFailedGrammar();
        }
        for (final GrammarBuilder builder : viewToBuilder.values()) {
            builder.buildStateMachines();
        }
        if (assembly.hadErrors()) {
            return buildFailedGrammar();
        }
        for (final GrammarBuilder builder : viewToBuilder.values()) {
            builder.buildCompiledGrammars();
        }
        if (assembly.hadErrors()) {
            return buildFailedGrammar();
        }
        for (final GrammarBuilder builder : viewToBuilder.values()) {
            builder.linkGrammars();
        }
        // actually get root grammar
        final ResolvedObject<GrammarView> grammarView = assembly.resolveGrammar(rootGrammarRequest.getReference());
        final GrammarBuilder grammarBuilder = viewToBuilder.get(grammarView.getObject());
        rootGrammar = new ResolvedObject<CompiledGrammar>(rootGrammarRequest, grammarView.getResolutionHistory(),
                grammarView.getDescriptor(), grammarBuilder.compiledGrammar());
        return ParserState.OUTPUT_AVAILABLE;
    }

    /**
     * Build the grammar that failed the compilation.
     *
     * @return the new parser state
     */
    private ParserState buildFailedGrammar() {
        final ResolvedObject<GrammarView> grammarView = assembly.resolveGrammar(rootGrammarRequest.getReference());
        if (grammarView != null) {
            rootGrammar = new ResolvedObject<CompiledGrammar>(rootGrammarRequest, grammarView.getResolutionHistory(),
                    grammarView.getDescriptor(), new DelegateCompiledGrammar(
                    BootstrapGrammars.defaultGrammar(),
                    assembly.getErrors(),
                    grammarView.getDescriptor(),
                    Collections.<CompiledGrammar>emptyList()));
            return ParserState.OUTPUT_AVAILABLE;
        } else {
            final GrammarAssembly.FailedGrammar failedGrammar = assembly.getFailedGrammar(rootGrammarRequest);
            final ResourceDescriptor unresolved = new ResourceDescriptor("unresolved:grammar",
                    StandardGrammars.GRAMMAR_NATURE, null);
            rootGrammar = new ResolvedObject<CompiledGrammar>(
                    rootGrammarRequest,
                    failedGrammar.getUsedResources(),
                    unresolved,
                    new DelegateCompiledGrammar(
                            BootstrapGrammars.defaultGrammar(),
                            assembly.getErrors(),
                            unresolved,
                            Collections.<CompiledGrammar>emptyList()));
            return ParserState.OUTPUT_AVAILABLE;
        }
    }

    @Override
    public Collection<ResourceRequest> requests() {
        return new HashSet<ResourceRequest>(assembly.unresolved());
    }

    @Override
    public void provide(final ResolvedObject<Grammar> grammar, final ErrorInfo errors) {
        assembly.provide(grammar, errors);
    }

    @Override
    public void fail(final ResourceRequest request, final Collection<ResourceUsage> resourceUsages,
                     final ErrorInfo errors) {
        assembly.fail(request, resourceUsages, errors);
    }

    @Override
    public ResolvedObject<Grammar> getProvided(final String systemId) {
        return assembly.resolvedGrammar(systemId);
    }


    @Override
    public ResolvedObject<CompiledGrammar> read() {
        if (rootGrammar == null) {
            throw new IllegalStateException("The grammar is not available yet!");
        }
        return rootGrammar;
    }

    /**
     * The builder for grammar.
     *
     * @param grammar the grammar view
     * @return the grammar builder
     */
    public GrammarBuilder grammarBuilder(final GrammarView grammar) {
        return viewToBuilder.get(grammar);
    }

    /**
     * @return the linker
     */
    public ActionLinker geLinker() {
        return linker;
    }

    /**
     * @return the collected errors.
     */
    public ErrorInfo getErrors() {
        return assembly.getErrors();
    }
}
