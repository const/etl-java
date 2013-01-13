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
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.event.grammar.GrammarCompiler;
import net.sf.etl.parsers.event.grammar.impl.flattened.GrammarAssembly;
import net.sf.etl.parsers.event.grammar.impl.flattened.GrammarView;
import net.sf.etl.parsers.event.impl.term.action.buildtime.ActionLinker;
import net.sf.etl.parsers.event.unstable.model.grammar.Grammar;
import net.sf.etl.parsers.resource.ResolvedObject;
import net.sf.etl.parsers.resource.ResourceRequest;
import net.sf.etl.parsers.resource.ResourceUsage;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * The grammar compiler
 */
public class GrammarAssemblyBuilder implements GrammarCompiler {
    /**
     * The collection of grammar views
     */
    private final GrammarAssembly assembly = new GrammarAssembly();
    /**
     * The map from grammar view to builders
     */
    private final HashMap<GrammarView, GrammarBuilder> viewToBuilder = new HashMap<GrammarView, GrammarBuilder>();
    /**
     * The root grammar
     */
    private ResourceRequest rootGrammar;
    /**
     * The linker
     */
    private ActionLinker linker = new ActionLinker();

    @Override
    public void start(ResourceRequest reference) {
        rootGrammar = reference;
        assembly.start(reference);
    }

    @Override
    public ParserState process() {
        if (!assembly.unresolved().isEmpty()) {
            return ParserState.RESOURCE_NEEDED;
        }
        if (assembly.hadErrors()) {
            // TODO assembly.buildStubGrammars();
            // return ParserState.OUTPUT_AVAILABLE;
            throw new RuntimeException("Not implemented yet");
        }
        // all grammars are resolved by this point, proceed with processing them
        assembly.flatten();
        if (assembly.hadErrors()) {
            // TODO assembly.buildStubGrammars();
            // return ParserState.OUTPUT_AVAILABLE;
            throw new RuntimeException("Not implemented yet");
        }
        for (GrammarView grammarView : assembly.grammars()) {
            viewToBuilder.put(grammarView, new GrammarBuilder(this, grammarView));
        }
        if (assembly.hadErrors()) {
            // TODO assembly.buildStubGrammars();
            // return ParserState.OUTPUT_AVAILABLE;
            throw new RuntimeException("Not implemented yet");
        }
        for (GrammarBuilder builder : viewToBuilder.values()) {
            builder.prepare();
        }
        if (assembly.hadErrors()) {
            // TODO assembly.buildStubGrammars();
            // return ParserState.OUTPUT_AVAILABLE;
            throw new RuntimeException("Not implemented yet");
        }
        for (GrammarBuilder builder : viewToBuilder.values()) {
            builder.buildNodes();
        }
        if (assembly.hadErrors()) {
            // TODO assembly.buildStubGrammars();
            // return ParserState.OUTPUT_AVAILABLE;
            throw new RuntimeException("Not implemented yet");
        }
        for (GrammarBuilder builder : viewToBuilder.values()) {
            builder.buildLookAhead();
        }
        if (assembly.hadErrors()) {
            // TODO assembly.buildStubGrammars();
            // return ParserState.OUTPUT_AVAILABLE;
            throw new RuntimeException("Not implemented yet");
        }
        for (GrammarBuilder builder : viewToBuilder.values()) {
            builder.buildStateMachines();
        }
        if (assembly.hadErrors()) {
            // TODO assembly.buildStubGrammars();
            // return ParserState.OUTPUT_AVAILABLE;
            throw new RuntimeException("Not implemented yet");
        }
        for (GrammarBuilder builder : viewToBuilder.values()) {
            builder.buildCompiledGrammars();
        }
        if (assembly.hadErrors()) {
            // TODO assembly.buildStubGrammars();
            // return ParserState.OUTPUT_AVAILABLE;
            throw new RuntimeException("Not implemented yet");
        }
        for (GrammarBuilder builder : viewToBuilder.values()) {
            builder.linkGrammars();
        }
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
        // TODO error recovery
        final ResolvedObject<GrammarView> grammarView = assembly.resolveGrammar(rootGrammar);
        final GrammarBuilder grammarBuilder = viewToBuilder.get(grammarView.getObject());
        return new ResolvedObject<CompiledGrammar>(rootGrammar, grammarView.getResolutionHistory(),
                grammarView.getDescriptor(), grammarBuilder.compiledGrammar());
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
