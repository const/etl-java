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
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.grammar.BootstrapGrammars;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.event.grammar.GrammarCompilerEngine;
import net.sf.etl.parsers.event.grammar.impl.flattened.GrammarAssembly;
import net.sf.etl.parsers.event.grammar.impl.flattened.GrammarView;
import net.sf.etl.parsers.event.impl.term.action.buildtime.ActionLinker;
import net.sf.etl.parsers.event.unstable.model.grammar.Grammar;
import net.sf.etl.parsers.resource.ResolvedObject;
import net.sf.etl.parsers.resource.ResourceRequest;
import net.sf.etl.parsers.resource.ResourceUsage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * The grammar compiler
 */
public class GrammarAssemblyBuilder implements GrammarCompilerEngine {
    /**
     * The collection of grammar views
     */
    private final GrammarAssembly assembly = new GrammarAssembly();
    /**
     * The map from grammar view to builders
     */
    private final HashMap<GrammarView, GrammarBuilder> viewToBuilder = new HashMap<GrammarView, GrammarBuilder>();
    /**
     * The root grammar request
     */
    private ResourceRequest rootGrammarRequest;
    /**
     * The root compiled grammar
     */
    private ResolvedObject<CompiledGrammar> rootGrammar;
    /**
     * The linker
     */
    private ActionLinker linker = new ActionLinker();

    @Override
    public void start(ResourceRequest reference) {
        rootGrammarRequest = reference;
        assembly.start(reference);
    }

    @Override
    public ParserState process() {
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
        for (GrammarView grammarView : assembly.grammars()) {
            viewToBuilder.put(grammarView, new GrammarBuilder(this, grammarView));
        }
        if (assembly.hadErrors()) {
            return buildFailedGrammar();
        }
        for (GrammarBuilder builder : viewToBuilder.values()) {
            builder.prepare();
        }
        if (assembly.hadErrors()) {
            return buildFailedGrammar();
        }
        for (GrammarBuilder builder : viewToBuilder.values()) {
            builder.buildNodes();
        }
        if (assembly.hadErrors()) {
            return buildFailedGrammar();
        }
        for (GrammarBuilder builder : viewToBuilder.values()) {
            builder.buildLookAhead();
        }
        if (assembly.hadErrors()) {
            return buildFailedGrammar();
        }
        for (GrammarBuilder builder : viewToBuilder.values()) {
            builder.buildStateMachines();
        }
        if (assembly.hadErrors()) {
            return buildFailedGrammar();
        }
        for (GrammarBuilder builder : viewToBuilder.values()) {
            builder.buildCompiledGrammars();
        }
        if (assembly.hadErrors()) {
            return buildFailedGrammar();
        }
        for (GrammarBuilder builder : viewToBuilder.values()) {
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
     * Build the grammar that failed the compilation
     */
    private ParserState buildFailedGrammar() {
        final ResolvedObject<GrammarView> grammarView = assembly.resolveGrammar(rootGrammarRequest.getReference());
        rootGrammar = new ResolvedObject<CompiledGrammar>(rootGrammarRequest, grammarView.getResolutionHistory(),
                grammarView.getDescriptor(), new DelegateCompiledGrammar(
                BootstrapGrammars.defaultGrammar(),
                assembly.getErrors(),
                grammarView.getDescriptor(),
                Collections.<CompiledGrammar>emptyList()));
        return ParserState.OUTPUT_AVAILABLE;

    }

    @Override
    public Collection<ResourceRequest> requests() {
        return new HashSet<ResourceRequest>(assembly.unresolved());
    }

    @Override
    public void provide(ResolvedObject<Grammar> grammar, ErrorInfo errors) {
        assembly.provide(grammar, errors);
    }

    @Override
    public void fail(ResourceRequest request, Collection<ResourceUsage> resourceUsages, ErrorInfo errors) {
        assembly.fail(request, resourceUsages, errors);
    }

    @Override
    public ResolvedObject<Grammar> getProvided(String systemId) {
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
     * The builder for grammar
     *
     * @param grammar the grammar view
     * @return the grammar builder
     */
    public GrammarBuilder grammarBuilder(GrammarView grammar) {
        return viewToBuilder.get(grammar);
    }

    public ActionLinker geLinker() {
        return linker;
    }

    public ErrorInfo getErrors() {
        return assembly.getErrors();
    }
}
