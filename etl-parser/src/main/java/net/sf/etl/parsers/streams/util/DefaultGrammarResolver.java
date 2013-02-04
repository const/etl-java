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

import net.sf.etl.parsers.DefaultTermParserConfiguration;
import net.sf.etl.parsers.ParserException;
import net.sf.etl.parsers.TermParserConfiguration;
import net.sf.etl.parsers.event.TermParser;
import net.sf.etl.parsers.event.grammar.GrammarCompilerSession;
import net.sf.etl.parsers.streams.GrammarResolver;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The caching grammar resolver, it is based on system ids
 */
public class DefaultGrammarResolver implements GrammarResolver {
    /**
     * The resolver instance
     */
    public static final DefaultGrammarResolver INSTANCE = new DefaultGrammarResolver();
    /**
     * The configuration
     */
    private final TermParserConfiguration configuration;

    /**
     * The constructor from default configuration
     */
    public DefaultGrammarResolver() {
        this(DefaultTermParserConfiguration.INSTANCE);
    }

    /**
     * The caching resolver
     *
     * @param configuration the configuration
     */
    public DefaultGrammarResolver(TermParserConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * The resolve the grammar and finish when it is done
     *
     * @param termParser the request
     */
    @Override
    public void resolve(TermParser termParser) {
        final AtomicBoolean finished = new AtomicBoolean(false);
        final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
        new GrammarCompilerSession(
                configuration,
                configuration.getCatalog(termParser.getSystemId()),
                termParser,
                new Executor() {
                    @Override
                    public void execute(Runnable command) {
                        queue.add(command);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        finished.set(true);
                    }
                }
        );
        while (!finished.get()) {
            try {
                queue.take().run();
            } catch (Exception e) {
                throw new ParserException("The grammar resolution process for " +
                        termParser.getSystemId() + " is interrupted", e);
            }
        }
    }
}
