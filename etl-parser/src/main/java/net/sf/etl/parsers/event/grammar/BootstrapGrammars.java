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

package net.sf.etl.parsers.event.grammar;

import net.sf.etl.parsers.GrammarId;
import net.sf.etl.parsers.ParserIOException;
import net.sf.etl.parsers.StandardGrammars;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.event.ParserState;
import net.sf.etl.parsers.event.grammar.impl.GrammarAssemblyBuilder;
import net.sf.etl.parsers.event.impl.bootstrap.BootstrapETLParserLite;
import net.sf.etl.parsers.event.unstable.model.grammar.Grammar;
import net.sf.etl.parsers.resource.ResolvedObject;
import net.sf.etl.parsers.resource.ResourceDescriptor;
import net.sf.etl.parsers.resource.ResourceRequest;
import net.sf.etl.parsers.streams.DefaultTermReaderConfiguration;
import net.sf.etl.parsers.streams.LexerReader;
import net.sf.etl.parsers.streams.PhraseParserReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * <p>This class contains code that loads standard grammars. These grammars are loaded using
 * the bootstrap parser ({@link net.sf.etl.parsers.event.impl.bootstrap.BootstrapETLParserLite}),
 * and they are compiled using normal compiler, and saved to static variables. After that,
 * they are used by other parsers. Currently it is not possible to replace a bootstrap grammars,
 * because there changes to them will require change of the code that handles them anyway.</p>
 * <p>The grammars loaded using this class are marked as having no dependencies, and this is so
 * because, no other resources are used to load them.</p>
 */
public final class BootstrapGrammars {
    /**
     * The lock that controls compilation of the grammars.
     */
    private static final Object GRAMMAR_LOCK = new Object();
    /**
     * The doctype grammar.
     */
    private static CompiledGrammar doctypeGrammar;
    /**
     * The doctype grammar.
     */
    private static CompiledGrammar defaultGrammar;
    /**
     * The doctype grammar.
     */
    private static CompiledGrammar grammarGrammar;

    /**
     * Private constructor for utility class.
     */
    private BootstrapGrammars() {
        // do nothing
    }

    /**
     * Compile or get doctype grammar.
     *
     * @return load doctype grammar singleton,
     * @throws ParserIOException if grammar fails to load or to parse - an exception is thrown
     */
    public static CompiledGrammar doctypeGrammar() {
        synchronized (GRAMMAR_LOCK) {
            if (doctypeGrammar == null) {
                doctypeGrammar = getCompiledBootstrapGrammar(StandardGrammars.DOCTYPE_GRAMMAR_ID);
            }
            return doctypeGrammar;
        }
    }

    /**
     * Compile or get default grammar.
     *
     * @return load default grammar singleton,
     * @throws ParserIOException if grammar fails to load or to parse - an exception is thrown
     */
    public static CompiledGrammar defaultGrammar() {
        synchronized (GRAMMAR_LOCK) {
            if (defaultGrammar == null) {
                defaultGrammar = getCompiledBootstrapGrammar(StandardGrammars.DEFAULT_GRAMMAR_ID);
            }
            return defaultGrammar;
        }
    }

    /**
     * Compile or get grammar for grammars.
     *
     * @return load default grammar singleton,
     * @throws ParserIOException if grammar fails to load or to parse - an exception is thrown
     */
    public static CompiledGrammar grammarGrammar() {
        synchronized (GRAMMAR_LOCK) {
            if (grammarGrammar == null) {
                grammarGrammar = getCompiledBootstrapGrammar(StandardGrammars.ETL_GRAMMAR_ID);
            }
            return grammarGrammar;
        }
    }

    /**
     * Compile grammar using bootstrap parser.
     *
     * @param grammarId the grammar system id
     * @return the compiled grammar
     */
    private static CompiledGrammar getCompiledBootstrapGrammar(final GrammarId grammarId) {
        final GrammarCompilerEngine compiler = new GrammarAssemblyBuilder();
        final ResourceRequest resourceRequest = new ResourceRequest(
                grammarId,
                "undefined",
                StandardGrammars.GRAMMAR_REQUEST_TYPE);
        compiler.start(resourceRequest);
        while (true) {
            final ParserState state = compiler.process();
            switch (state) {
                case OUTPUT_AVAILABLE:
                    return compiler.read().getObject();
                case RESOURCE_NEEDED:
                    for (final ResourceRequest request : compiler.requests()) {
                        try {
                            final URL url = StandardGrammars.getStandardGrammarUrl(grammarId); // NOPMD
                            String systemId = url.toString();
                            try (InputStream input = url.openStream()) {
                                final BootstrapETLParserLite parser = new BootstrapETLParserLite(// NOPMD
                                        new PhraseParserReader(
                                                new LexerReader(DefaultTermReaderConfiguration.INSTANCE,
                                                        new InputStreamReader(input, StandardCharsets.UTF_8),
                                                        systemId, TextPos.START)));
                                final Grammar grammar = parser.parse();
                                // note that exception is thrown if there was problem with parsing resource
                                compiler.provide(new ResolvedObject<>(request, null, // NOPMD
                                        new ResourceDescriptor(systemId, StandardGrammars.GRAMMAR_NATURE, null),
                                        grammar), null);
                            }
                        } catch (Exception ex) { // NOPMD
                            throw new ParserIOException("Failed to load resource: " + request, ex);
                        }
                    }
                    break;
                case EOF:
                case INPUT_NEEDED:
                    throw new IllegalStateException("Invalid compiler state: " + state);
                default:
                    throw new IllegalStateException("Unknown state: " + state);
            }
        }
    }
}
