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

package net.sf.etl.parsers.event.grammar;

import net.sf.etl.parsers.ErrorInfo;
import net.sf.etl.parsers.LoadedGrammarInfo;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.TermParserConfiguration;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.TermParser;
import net.sf.etl.parsers.event.grammar.impl.GrammarAssemblyBuilder;
import net.sf.etl.parsers.event.unstable.model.grammar.Grammar;
import net.sf.etl.parsers.event.unstable.model.grammar.GrammarLiteTermParser;
import net.sf.etl.parsers.resource.ResolvedObject;
import net.sf.etl.parsers.resource.ResourceDescriptor;
import net.sf.etl.parsers.resource.ResourceReference;
import net.sf.etl.parsers.resource.ResourceRequest;
import net.sf.etl.parsers.resource.ResourceUsage;
import net.sf.etl.parsers.streams.GrammarResolver;
import net.sf.etl.parsers.streams.TermParserReader;
import net.sf.etl.parsers.streams.util.DefaultGrammarResolver;
import org.apache_extras.xml_catalog.event.Catalog;
import org.apache_extras.xml_catalog.event.CatalogFile;
import org.apache_extras.xml_catalog.event.CatalogResourceUsage;
import org.apache_extras.xml_catalog.event.CatalogResult;
import org.apache_extras.xml_catalog.event.CatalogResultTrace;
import org.apache_extras.xml_catalog.event.ResultReceiver;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * The common customizable asynchronous grammar compiler process that uses catalog
 */
public class GrammarCompilerSession {
    /**
     * An executor to schedule resolution actions
     */
    private final Executor executor;
    /**
     * The catalog to use
     */
    private final Catalog catalog;
    /**
     * The compiler engine
     */
    private final GrammarCompilerEngine grammarCompilerEngine = new GrammarAssemblyBuilder();
    /**
     * The target parser
     */
    private final TermParser parser;
    /**
     * The action that is executed when loading grammar has been finished
     */
    private final Runnable finishedAction;
    /**
     * The configuration for term parser
     */
    private final TermParserConfiguration configuration;
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
     * @param executor       the executor
     * @param finishedAction the action to be executed to indicate that loading grammar has been finished
     */
    public GrammarCompilerSession(TermParserConfiguration configuration, Catalog catalog, TermParser parser,
                                  Executor executor, Runnable finishedAction) {
        this(configuration, catalog, parser, executor, finishedAction, Collections.<String>emptySet());
    }

    /**
     * The constructor. It immediately start loading parsers for the grammar, and possibly exits, or not.
     * If it exits, it means that the grammar loading is asynchronous.
     *
     * @param configuration  the configuration
     * @param catalog        the catalog
     * @param parser         the parser
     * @param executor       the executor
     * @param finishedAction the action to be executed to indicate that loading grammar has been finished
     * @param loadedGrammars the grammars that has been already loaded (to prevent cyclic loading of grammars
     *                       for grammars)
     */
    public GrammarCompilerSession(TermParserConfiguration configuration, Catalog catalog, TermParser parser,
                                  Executor executor, Runnable finishedAction, Set<String> loadedGrammars) {
        this.executor = executor;
        this.loadedGrammars = loadedGrammars;
        this.catalog = catalog != null ? catalog : configuration.getCatalog(parser.getSystemId());
        this.parser = parser;
        this.finishedAction = finishedAction;
        this.configuration = configuration;
        final ResourceRequest resourceRequest = parser.grammarRequest();
        if (resourceRequest == null) {
            throw new IllegalStateException("The parser should have an active request for the grammar");
        } else if (parser.isGrammarDetermined()) {
            throw new IllegalStateException("The parser must not have a loaded grammar.");
        }
        // neither system id no public id matches
        if (!checkBootstrapGrammars(resourceRequest, null, Collections.<ResourceUsage>emptyList())) {
            startParsing(resourceRequest);
        }
    }

    /**
     * Start parsing
     *
     * @param resourceRequest the resource request
     */
    private void startParsing(final ResourceRequest resourceRequest) {
        catalog.resolveEntity(executor,
                resourceRequest.getReference().getPublicId(),
                resourceRequest.getReference().getSystemId(),
                null,
                new ResultReceiver<CatalogResult>() {
                    @Override
                    public void result(CatalogResult result) {
                        if (result.getResolution() == null) {
                            catalog.resolveResource(executor,
                                    parser.getSystemId(),
                                    StandardGrammars.GRAMMAR_NATURE,
                                    StandardGrammars.GRAMMAR_EXTENSION_MAPPING,
                                    resourceRequest.getReference().getPublicId(),
                                    resourceRequest.getReference().getSystemId(),
                                    parser.getSystemId(),
                                    new ResultReceiver<CatalogResult>() {
                                        @Override
                                        public void result(CatalogResult result) {
                                            handleResolution(result, resourceRequest);
                                        }
                                    });
                        } else {
                            handleResolution(result, resourceRequest);
                        }
                    }
                });
    }

    /**
     * Handle the resolution of initial request
     *
     * @param result          the resolution result
     * @param resourceRequest the resource request
     */
    private void handleResolution(CatalogResult result, ResourceRequest resourceRequest) {
        final List<ResourceUsage> resolutionHistory = getResolutionHistory(result);
        if (!checkBootstrapGrammars(resourceRequest, result.getResolution(), resolutionHistory)) {
            if (result.getResolution() != null) {
                final CompiledGrammar cachedGrammar = configuration.getCachedGrammar(result.getResolution());
                if (cachedGrammar != null) {
                    finish(new ResolvedObject<CompiledGrammar>(resourceRequest,
                            resolutionHistory,
                            cachedGrammar.getDescriptor(),
                            cachedGrammar
                    ));
                    return;
                }
            }
            if (loadedGrammars.contains(result.getResolution())) {
                grammarCompilerEngine.start(resourceRequest);
                grammarCompilerEngine.fail(resourceRequest, resolutionHistory,
                        new ErrorInfo("syntax.RecursiveGrammarDefinition",
                                Collections.<Object>singletonList(result.getResolution()),
                                new SourceLocation(TextPos.START, TextPos.START, result.getResolution()),
                                null));
                process();
            }
            grammarCompilerEngine.start(resourceRequest);
            loadGrammar(resourceRequest, result);
            process();
        }
    }

    /**
     * Check if the grammar is one of bootstrap grammars, and load it instead
     *
     * @param resourceRequest   the resource request
     * @param systemId          the resolved system id
     * @param resolutionHistory the resolution history
     * @return the true if the grammar matched
     */
    private boolean checkBootstrapGrammars(ResourceRequest resourceRequest, String systemId, List<ResourceUsage> resolutionHistory) {
        if (StandardGrammars.ETL_GRAMMAR_PUBLIC_ID.equals(resourceRequest.getReference().getPublicId()) ||
                StandardGrammars.ETL_GRAMMAR_SYSTEM_ID.equals(resourceRequest.getReference().getSystemId()) ||
                StandardGrammars.ETL_GRAMMAR_SYSTEM_ID.equals(systemId)) {
            CompiledGrammar compiledGrammar = BootstrapGrammars.grammarGrammar();
            finish(new ResolvedObject<CompiledGrammar>(resourceRequest,
                    resolutionHistory,
                    compiledGrammar.getDescriptor(),
                    compiledGrammar
            ));
            return true;
        } else if (StandardGrammars.DEFAULT_GRAMMAR_SYSTEM_ID.equals(resourceRequest.getReference().getSystemId()) ||
                StandardGrammars.DEFAULT_GRAMMAR_SYSTEM_ID.equals(systemId)) {
            CompiledGrammar compiledGrammar = BootstrapGrammars.defaultGrammar();
            finish(new ResolvedObject<CompiledGrammar>(resourceRequest,
                    resolutionHistory,
                    compiledGrammar.getDescriptor(),
                    compiledGrammar
            ));
            return true;
        } else if (StandardGrammars.DOCTYPE_GRAMMAR_SYSTEM_ID.equals(resourceRequest.getReference().getSystemId()) ||
                StandardGrammars.DOCTYPE_GRAMMAR_SYSTEM_ID.equals(systemId)) {
            CompiledGrammar compiledGrammar = BootstrapGrammars.doctypeGrammar();
            finish(new ResolvedObject<CompiledGrammar>(resourceRequest,
                    resolutionHistory,
                    compiledGrammar.getDescriptor(),
                    compiledGrammar
            ));
            return true;
        }
        return false;
    }

    /**
     * Finish the project
     *
     * @param grammar the grammar
     */
    private void finish(ResolvedObject<CompiledGrammar> grammar) {
        parser.provideGrammar(grammar, null);
        executor.execute(finishedAction);
    }


    /**
     * A step in the process of loading the grammar
     */
    private void process() {
        final ParserState state = grammarCompilerEngine.process();
        switch (state) {
            case OUTPUT_AVAILABLE:
                final ResolvedObject<CompiledGrammar> read = grammarCompilerEngine.read();
                configuration.cacheGrammar(read.getObject());
                finish(read);
                break;
            case RESOURCE_NEEDED:
                assert !grammarCompilerEngine.requests().isEmpty();
                loadGrammar(grammarCompilerEngine.requests().iterator().next());
                break;
            case EOF:
            case INPUT_NEEDED:
                throw new RuntimeException("Invalid compiler state: " + state);
        }
    }

    /**
     * Load the grammar after the resolution
     *
     * @param request the request
     */
    private void loadGrammar(final ResourceRequest request) {
        catalog.resolveEntity(executor,
                request.getReference().getPublicId(), request.getReference().getSystemId(), null,
                new ResultReceiver<CatalogResult>() {
                    @Override
                    public void result(CatalogResult result) {
                        loadGrammar(request, result);
                        process();
                    }
                });
    }

    /**
     * Get resource usage from catalog result
     *
     * @param result the result
     * @return the resource usage
     */
    private List<ResourceUsage> getResolutionHistory(CatalogResult result) {
        ArrayList<ResourceUsage> rc = new ArrayList<ResourceUsage>();
        for (CatalogResultTrace trace = result.getTrace(); trace != null; trace = trace.getPrevious()) {
            final CatalogFile catalogFile = trace.getCatalogFile();
            if (catalogFile == null) {
                continue;
            }
            List<ResourceUsage> catalogResourceUsage;
            if (catalogFile.getUsedResources().isEmpty()) {
                catalogResourceUsage = Collections.emptyList();
            } else {
                catalogResourceUsage = new ArrayList<ResourceUsage>();
                for (CatalogResourceUsage resourceUsage : catalogFile.getUsedResources()) {
                    catalogResourceUsage.add(new ResourceUsage(
                            new ResourceReference(resourceUsage.getSystemId(), null),
                            new ResourceDescriptor(resourceUsage.getSystemId(),
                                    StandardGrammars.CATALOG_RESOURCE_TYPE,
                                    resourceUsage.getVersion() == null ? null : resourceUsage.getVersion().toString()),
                            resourceUsage.getRole()
                    ));
                }
            }
            rc.add(new ResourceUsage(
                    new ResourceReference(trace.getCatalogRequest().getSystemId(), null),
                    new ResourceDescriptor(
                            catalogFile.getSystemId(),
                            StandardGrammars.CATALOG_TYPE,
                            catalogFile.getVersion() == null ? null : catalogFile.getVersion().toString(),
                            catalogResourceUsage),
                    StandardGrammars.CATALOG_ROLE));
        }
        return rc;
    }

    /**
     * Load grammar basing on resolution result
     *
     * @param request the catalog request
     * @param result  the resolution
     */
    protected void loadGrammar(ResourceRequest request, final CatalogResult result) {
        final String systemId = result.getResolution();
        final List<ResourceUsage> resolution = getResolutionHistory(result);
        try {
            if (systemId == null) {
                grammarCompilerEngine.fail(request, resolution,
                        new ErrorInfo("grammar.ParseError",
                                Collections.<Object>unmodifiableList(Arrays.<Object>asList(
                                        request.getReference().getSystemId(),
                                        request.getReference().getPublicId(),
                                        "The request was resolved: " + request
                                )), SourceLocation.UNKNOWN, null));
                return;
            }
            final ResolvedObject<Grammar> alreadyProvided = grammarCompilerEngine.getProvided(systemId);
            if (alreadyProvided != null) {
                grammarCompilerEngine.provide(new ResolvedObject<Grammar>(request,
                                resolution,
                                alreadyProvided.getDescriptor(),
                                alreadyProvided.getObject()),
                        null);
            }
            URL url = new URL(systemId);
            ArrayList<ErrorInfo> errors = new ArrayList<ErrorInfo>();
            final TermParserReader reader = new TermParserReader(configuration, url);
            try {
                reader.setResolver(new GrammarResolver() {
                    @Override
                    public void resolve(TermParser termParser) {
                        final ResourceRequest resourceRequest = termParser.grammarRequest();
                        if (StandardGrammars.ETL_GRAMMAR_PUBLIC_ID.equals(
                                resourceRequest.getReference().getPublicId())
                                || StandardGrammars.ETL_GRAMMAR_SYSTEM_ID.equals(
                                resourceRequest.getReference().getPublicId())) {
                            termParser.provideGrammar(new ResolvedObject<CompiledGrammar>(
                                    resourceRequest,
                                    Collections.<ResourceUsage>emptyList(),
                                    BootstrapGrammars.grammarGrammar().getDescriptor(),
                                    BootstrapGrammars.grammarGrammar()), null);
                        } else {
                            final HashSet<String> grammars = new HashSet<String>();
                            grammars.addAll(loadedGrammars);
                            grammars.add(result.getResolution());
                            new DefaultGrammarResolver(configuration,
                                    Collections.unmodifiableSet(grammars)).resolve(termParser);
                        }
                    }
                });
                reader.advance();
                GrammarLiteTermParser parser = new GrammarLiteTermParser(reader);
                if (parser.advance()) {
                    Grammar grammar = (Grammar) parser.current();
                    if (parser.advance()) {
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
                                    StandardGrammars.USED_GRAMMAR_REQUEST_TYPE);
                    // TODO better version
                    grammarCompilerEngine.provide(new ResolvedObject<Grammar>(request,
                            resolution,
                            new ResourceDescriptor(systemId,
                                    StandardGrammars.GRAMMAR_NATURE,
                                    "" + System.currentTimeMillis(),
                                    usedGrammar != null ? Collections.singletonList(usedGrammar) : null),
                            grammar),
                            ErrorInfo.merge(errors));

                } else {
                    grammarCompilerEngine.fail(request, resolution,
                            new ErrorInfo("grammar.EmptyGrammar",
                                    Collections.<Object>unmodifiableList(Arrays.<Object>asList(
                                            request.getReference().getSystemId(),
                                            request.getReference().getPublicId()
                                    )), SourceLocation.UNKNOWN, null));
                }
            } finally {
                reader.close();
            }
        } catch (Throwable ex) {
            grammarCompilerEngine.fail(request, resolution,
                    new ErrorInfo("grammar.ParseError",
                            Collections.<Object>unmodifiableList(Arrays.<Object>asList(
                                    request.getReference().getSystemId(),
                                    request.getReference().getPublicId(),
                                    ex.toString()
                            )), SourceLocation.UNKNOWN, null));
        }
    }
}
