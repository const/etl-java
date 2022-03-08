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

package net.sf.etl.parsers.streams; // NOPMD

import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.LoadedGrammarInfo;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.TermParser;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The common grammar resolution process that uses blocking catalog.
 */
final class BlockingCatalogSession { // NOPMD
    /**
     * The catalog to use.
     */
    private final GrammarLocator catalog;
    /**
     * The compiler engine.
     */
    private final GrammarCompilerEngine grammarCompilerEngine = new GrammarAssemblyBuilder();
    /**
     * The target parser.
     */
    private final TermParser parser;
    /**
     * The configuration for term parser.
     */
    private final TermReaderCatalogConfiguration configuration;
    /**
     * The set of grammars which loading is already started. This is done in order
     * to prevent the case, when document type for the grammar is not the same
     * standard grammar language, and it recursively refers to itself.
     */
    private final Set<String> loadedGrammars;

    /**
     * The constructor. It immediately start loading parsers for the grammar, and possibly exits, or not.
     * If it exits, it means that the grammar loading is asynchronous.
     *
     * @param configuration  the configuration
     * @param catalog        the catalog
     * @param parser         the parser
     * @param loadedGrammars the grammars that has been already loaded (to prevent cyclic loading of grammars
     *                       for grammars)
     */
    BlockingCatalogSession(final TermReaderCatalogConfiguration configuration, final GrammarLocator catalog,
                           final TermParser parser,
                           final Set<String> loadedGrammars) {
        this.loadedGrammars = loadedGrammars;
        this.catalog = catalog != null ? catalog : configuration.getCatalog(parser.getSystemId());
        this.parser = parser;
        this.configuration = configuration;
        // neither system id no public id matches
    }

    /**
     * Preform resolution.
     */
    public void resolve() {
        final ResourceRequest resourceRequest = parser.grammarRequest();
        if (resourceRequest == null) {
            throw new IllegalStateException("The parser should have an active request for the grammar");
        } else if (parser.isGrammarDetermined()) {
            throw new IllegalStateException("The parser must not have a loaded grammar.");
        }
        if (checkBootstrapGrammars(resourceRequest, Collections.emptyList())) {
            return;
        }
        var result = resolveInitialGrammar(resourceRequest);
        if (!startGrammar(resourceRequest, result)) {
            return;
        }
        while (true) {
            final ParserState state = grammarCompilerEngine.process();
            switch (state) {
                case OUTPUT_AVAILABLE:
                    final ResolvedObject<CompiledGrammar> read = grammarCompilerEngine.read();
                    configuration.getParserConfiguration().cacheGrammar(read.getObject());
                    finish(read);
                    return;
                case RESOURCE_NEEDED:
                    assert !grammarCompilerEngine.requests().isEmpty();
                    final ResourceRequest request = grammarCompilerEngine.requests().iterator().next();
                    loadGrammar(request, catalog.resolve(request));
                    break;
                case EOF:
                case INPUT_NEEDED:
                    throw new IllegalStateException("Invalid compiler state: " + state);
                default:
                    throw new IllegalStateException("Unknown compiler state: " + state);
            }
        }
    }

    /**
     * Start resolving grammar.
     *
     * @param resourceRequest the resource request
     * @param result          the result
     * @return true if grammar started successfully and needs to be processed,
     * false if resolution finished.
     */
    private boolean startGrammar(final ResourceRequest resourceRequest, final ResolvedObject<String> result) {
        final List<ResourceUsage> resolutionHistory = result.getResolutionHistory();
        if (checkBootstrapGrammars(resourceRequest, resolutionHistory)) {
            return false;
        }
        if (result.getObject() != null) {
            final CompiledGrammar cachedGrammar =
                    configuration.getParserConfiguration().getCachedGrammar(result.getObject());
            if (cachedGrammar != null) {
                finish(new ResolvedObject<>(resourceRequest,
                        resolutionHistory,
                        cachedGrammar.getDescriptor(),
                        cachedGrammar
                ));
                return false;
            }
        }
        if (loadedGrammars.contains(result.getObject())) {
            grammarCompilerEngine.start(resourceRequest);
            grammarCompilerEngine.fail(resourceRequest, resolutionHistory,
                    new ErrorInfo("syntax.RecursiveGrammarDefinition",
                            Collections.<Object>singletonList(result.getObject()),
                            new SourceLocation(TextPos.START, TextPos.START, result.getObject()),
                            null));
        } else {
            grammarCompilerEngine.start(resourceRequest);
            loadGrammar(resourceRequest, result);
        }
        return true;
    }

    /**
     * Start parsing.
     *
     * @param resourceRequest the resource request
     * @return the initial grammar resolution result
     */
    private ResolvedObject<String> resolveInitialGrammar(final ResourceRequest resourceRequest) {
        return catalog.resolve(resourceRequest);
    }

    /**
     * Check if the grammar is one of bootstrap grammars, and load it instead.
     *
     * @param resourceRequest   the resource request
     * @param resolutionHistory the resolution history
     * @return the true if the grammar matched
     */
    private boolean checkBootstrapGrammars(final ResourceRequest resourceRequest,
                                           final List<ResourceUsage> resolutionHistory) {
        if (StandardGrammars.ETL_GRAMMAR_ID.equals(resourceRequest.grammarId())) {
            final CompiledGrammar compiledGrammar = BootstrapGrammars.grammarGrammar();
            finish(new ResolvedObject<>(resourceRequest,
                    resolutionHistory,
                    compiledGrammar.getDescriptor(),
                    compiledGrammar
            ));
            return true;
        } else if (StandardGrammars.DEFAULT_GRAMMAR_ID.equals(resourceRequest.grammarId())) {
            final CompiledGrammar compiledGrammar = BootstrapGrammars.defaultGrammar();
            finish(new ResolvedObject<>(resourceRequest,
                    resolutionHistory,
                    compiledGrammar.getDescriptor(),
                    compiledGrammar
            ));
            return true;
        } else if (StandardGrammars.DOCTYPE_GRAMMAR_ID.equals(resourceRequest.grammarId())) {
            final CompiledGrammar compiledGrammar = BootstrapGrammars.doctypeGrammar();
            finish(new ResolvedObject<>(resourceRequest,
                    resolutionHistory,
                    compiledGrammar.getDescriptor(),
                    compiledGrammar
            ));
            return true;
        }
        return false;
    }

    /**
     * Finish the session.
     *
     * @param grammar the grammar
     */
    private void finish(final ResolvedObject<CompiledGrammar> grammar) {
        parser.provideGrammar(grammar, null);
    }


    /**
     * Load grammar basing on resolution result.
     *
     * @param request the catalog request
     * @param result  the resolution
     */
    protected void loadGrammar(final ResourceRequest request, final ResolvedObject<String> result) { // NOPMD
        final String systemId = result.getObject();
        final List<ResourceUsage> resolution = result.getResolutionHistory();
        try {
            if (systemId == null) {
                grammarCompilerEngine.fail(request, resolution,
                        new ErrorInfo("syntax.FailedToResolve",
                                List.of(
                                        request.grammarId(),
                                        request
                                ), SourceLocation.UNKNOWN, null));
                return;
            }
            final ResolvedObject<Grammar> alreadyProvided = grammarCompilerEngine.getProvided(request.grammarId());
            if (alreadyProvided != null) {
                grammarCompilerEngine.provide(new ResolvedObject<>(request,
                                resolution,
                                alreadyProvided.getDescriptor(),
                                alreadyProvided.getObject()),
                        null);
            }
            final URL url = new URL(systemId);
            var errors = new ArrayList<ErrorInfo>();
            final TermParserReader reader = new TermParserReader(configuration, url);
            try {
                reader.setResolver(termParser -> {
                    final ResourceRequest resourceRequest = termParser.grammarRequest();
                    if (StandardGrammars.ETL_GRAMMAR_ID.equals(request.grammarId())) {
                        termParser.provideGrammar(new ResolvedObject<>(
                                resourceRequest,
                                List.of(),
                                BootstrapGrammars.grammarGrammar().getDescriptor(),
                                BootstrapGrammars.grammarGrammar()), null);
                    } else {
                        final HashSet<String> grammars = new HashSet<>(loadedGrammars);
                        grammars.add(result.getObject());
                        new DefaultGrammarResolver(configuration,
                                Collections.unmodifiableSet(grammars)).resolve(termParser);
                    }
                });
                reader.advance();
                final GrammarLiteTermParser grammarParser = new GrammarLiteTermParser(reader);
                if (grammarParser.advance()) {
                    final Grammar grammar = (Grammar) grammarParser.current();
                    if (grammarParser.advance()) {
                        errors.add(new ErrorInfo("grammar.TooManyGrammars",
                                List.of(
                                        reader.getSystemId()
                                ), grammar.getLocation(), null));

                    }
                    errors.addAll(grammarParser.errors());
                    final LoadedGrammarInfo doctype = grammarParser.getLoadedGrammar();
                    final ResourceUsage usedGrammar = doctype == null ? null
                            : new ResourceUsage(
                            doctype.resolvedGrammar().getDescriptor(),
                            StandardGrammars.USED_GRAMMAR_REQUEST_TYPE);
                    // TODO better version
                    grammarCompilerEngine.provide(new ResolvedObject<>(request,
                                    resolution,
                                    new ResourceDescriptor(systemId,
                                            StandardGrammars.GRAMMAR_NATURE,
                                            Long.toString(System.currentTimeMillis()),
                                            usedGrammar != null ? Collections.singletonList(usedGrammar) : null),
                                    grammar),
                            ErrorInfo.merge(errors));

                } else {
                    var lastToken = reader.currentOrNull();
                    var location = new SourceLocation(
                            lastToken == null ? TextPos.START : lastToken.start(),
                            lastToken == null ? TextPos.START : lastToken.start(),
                            reader.getSystemId());
                    grammarCompilerEngine.fail(request, resolution,
                            new ErrorInfo("grammar.EmptyGrammar",
                                    List.of(
                                            reader.getSystemId()
                                    ), location, null));
                }
            } finally {
                reader.close();
            }
        } catch (final Throwable ex) {
            grammarCompilerEngine.fail(request, resolution,
                    new ErrorInfo("grammar.ParseError",
                            List.of(
                                    request.grammarId(),
                                    ex.toString()
                            ), SourceLocation.UNKNOWN, null));
        }
    }
}
