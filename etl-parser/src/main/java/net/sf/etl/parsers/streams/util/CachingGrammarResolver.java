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

package net.sf.etl.parsers.streams.util;

import net.sf.etl.parsers.*;
import net.sf.etl.parsers.event.Cell;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.grammar.BootstrapGrammars;
import net.sf.etl.parsers.event.grammar.CompiledGrammar;
import net.sf.etl.parsers.event.grammar.GrammarCompilerEngine;
import net.sf.etl.parsers.event.grammar.impl.GrammarAssemblyBuilder;
import net.sf.etl.parsers.event.unstable.model.grammar.Grammar;
import net.sf.etl.parsers.event.unstable.model.grammar.GrammarLiteTermParser;
import net.sf.etl.parsers.resource.ResolvedObject;
import net.sf.etl.parsers.resource.ResourceDescriptor;
import net.sf.etl.parsers.resource.ResourceRequest;
import net.sf.etl.parsers.resource.ResourceUsage;
import net.sf.etl.parsers.streams.GrammarResolver;
import net.sf.etl.parsers.streams.LexerReader;
import net.sf.etl.parsers.streams.PhraseParserReader;
import net.sf.etl.parsers.streams.TermParserReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 * The caching grammar resolver, it is based on system ids
 */
public class CachingGrammarResolver implements GrammarResolver {
    /**
     * The resolver instance
     */
    public static final CachingGrammarResolver INSTANCE = new CachingGrammarResolver();
    /**
     * The cache for the grammars
     */
    private final HashMap<String, CompiledGrammarEntry> grammarCache = new HashMap<String, CompiledGrammarEntry>();

    /**
     * The grammar resolver
     *
     * @param request the request
     * @param errors  the holder for resolution errors
     * @return the resolver
     */
    @Override
    public ResolvedObject<CompiledGrammar> resolve(ResourceRequest request, Cell<ErrorInfo> errors) {
        if (StandardGrammars.ETL_GRAMMAR_PUBLIC_ID.equals(request.getReference().getPublicId())) {
            final CompiledGrammar grammar = BootstrapGrammars.grammarGrammar();
            return new ResolvedObject<CompiledGrammar>(request, null, grammar.getDescriptor(), grammar);
        }
        final String systemId = request.getReference().getSystemId();
        CompiledGrammarEntry entry;
        synchronized (grammarCache) {
            entry = grammarCache.get(systemId);
            if (entry == null) {
                entry = new CompiledGrammarEntry();
                grammarCache.put(systemId, entry);
            }
        }
        CompiledGrammar grammar;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (entry) {
            if (entry.grammar == null) {
                entry.grammar = getCompiledGrammar(request);
            }
            grammar = entry.grammar;
        }
        // TODO introduce ParseContext where to take info like tab size, logging, resolution, and other
        // TODO resource usage for entity resolver
        // TODO separate compilation, resolution, and caching
        return new ResolvedObject<CompiledGrammar>(request, null, grammar.getDescriptor(), grammar);

    }


    /**
     * Compile grammar using bootstrap parser
     *
     * @param resourceRequest the resource request
     * @return the compiled grammar
     */
    private CompiledGrammar getCompiledGrammar(ResourceRequest resourceRequest) {
        GrammarCompilerEngine compiler = new GrammarAssemblyBuilder();
        compiler.start(resourceRequest);
        while (true) {
            final ParserState state = compiler.process();
            switch (state) {
                case OUTPUT_AVAILABLE:
                    return compiler.read().getObject();
                case RESOURCE_NEEDED:
                    for (ResourceRequest request : compiler.requests()) {
                        try {
                            final String systemId = request.getReference().getSystemId();
                            // TODO add resolver
                            if (systemId == null) {
                                compiler.fail(request, Collections.<ResourceUsage>emptyList(),
                                        new ErrorInfo("grammar.ParseError",
                                                Collections.<Object>unmodifiableList(Arrays.<Object>asList(
                                                        request.getReference().getSystemId(),
                                                        request.getReference().getPublicId(),
                                                        "This grammar resolver does not implement " +
                                                                "entity resolution: " + request
                                                )), SourceLocation.UNKNOWN, null));
                                continue;
                            }
                            URL url = new URL(systemId);
                            InputStream input = url.openStream();
                            ArrayList<ErrorInfo> errors = new ArrayList<ErrorInfo>();
                            try {
                                final TermParserReader reader = new TermParserReader(
                                        new PhraseParserReader(
                                                new LexerReader(
                                                        new InputStreamReader(input, BootstrapGrammars.UTF8),
                                                        systemId, TextPos.START)));
                                reader.setResolver(this);
                                reader.advance();
                                GrammarLiteTermParser parser = new GrammarLiteTermParser(reader);
                                if (parser.hasNext()) {
                                    Grammar grammar = (Grammar) parser.next();
                                    if (parser.hasNext()) {
                                        errors.add(new ErrorInfo("grammar.TooManyGrammars",
                                                Collections.<Object>unmodifiableList(Arrays.<Object>asList(
                                                        request.getReference().getSystemId(),
                                                        request.getReference().getPublicId()
                                                )), SourceLocation.UNKNOWN, null));

                                    }
                                    errors.addAll(parser.errors());
                                    final LoadedGrammarInfo doctype = parser.getLoadedGrammar();
                                    ResourceUsage usedGrammar = doctype == null ? null :
                                            new ResourceUsage(doctype.resolvedGrammar().getRequest().getReference(),
                                                    doctype.resolvedGrammar().getDescriptor(),
                                                    CompiledGrammar.USED_GRAMMAR_REQUEST_TYPE);
                                    // TODO better version
                                    // TODO fix grammar type (globally)
                                    compiler.provide(new ResolvedObject<Grammar>(request,
                                            Collections.<ResourceUsage>emptyList(),
                                            new ResourceDescriptor(systemId,
                                                    CompiledGrammar.GRAMMAR_NATURE,
                                                    "" + System.currentTimeMillis(),
                                                    usedGrammar != null ? Collections.singletonList(usedGrammar) : null),
                                            grammar),
                                            ErrorInfo.merge(errors));

                                } else {
                                    compiler.fail(request, Collections.<ResourceUsage>emptyList(),
                                            new ErrorInfo("grammar.EmptyGrammar",
                                                    Collections.<Object>unmodifiableList(Arrays.<Object>asList(
                                                            request.getReference().getSystemId(),
                                                            request.getReference().getPublicId()
                                                    )), SourceLocation.UNKNOWN, null));
                                }
                            } finally {
                                input.close();
                            }
                        } catch (Throwable ex) {
                            // TODO logging?
                            ex.printStackTrace();
                            compiler.fail(request, Collections.<ResourceUsage>emptyList(),
                                    new ErrorInfo("grammar.ParseError",
                                            Collections.<Object>unmodifiableList(Arrays.<Object>asList(
                                                    request.getReference().getSystemId(),
                                                    request.getReference().getPublicId(),
                                                    ex.toString()
                                            )), SourceLocation.UNKNOWN, null));
                        }
                    }
                    break;
                case EOF:
                case INPUT_NEEDED:
                    throw new RuntimeException("Invalid compiler state: " + state);
            }
        }
    }

    /**
     * The entry for compiled grammar
     */
    private class CompiledGrammarEntry {
        /**
         * The grammar, might be null
         */
        CompiledGrammar grammar;
    }
}
