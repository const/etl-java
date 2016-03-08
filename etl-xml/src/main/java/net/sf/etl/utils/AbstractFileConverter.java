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
package net.sf.etl.utils;

import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.streams.DefaultTermReaderConfiguration;
import net.sf.etl.parsers.streams.LexerReader;
import net.sf.etl.parsers.streams.PhraseParserReader;
import net.sf.etl.parsers.streams.TermParserReader;
import net.sf.etl.parsers.streams.TermReaderCatalogConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Abstract class for utilities that convert one set of sources to another.
 *
 * @param <Config> the configuration type
 * @author const
 */
public abstract class AbstractFileConverter<Config extends AbstractFileConverter.BaseConfig> {
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractFileConverter.class);
    /**
     * Map from source to destinations. Null key means stdin. Null value means
     * stdout.
     */
    private final NavigableMap<String, String> sourceFiles = new TreeMap<String, String>();
    /**
     * The parser configuration.
     */
    private TermReaderCatalogConfiguration configuration = DefaultTermReaderConfiguration.INSTANCE;
    /**
     * The parsed config.
     */
    private Config config;

    /**
     * Parse configuration.
     *
     * @param commandLine the command line
     * @return the parsed configuration
     */
    protected abstract Config parseConfig(CommandLine commandLine);

    /**
     * @return the command line options
     */
    protected abstract Options getOptions();

    /**
     * @return the configuration
     */
    protected final Config getConfig() {
        return config;
    }

    /**
     * Process content and write it on output stream.
     *
     * @param stream the output stream
     * @param p      the term parser
     * @throws Exception an exception that is thrown in case of problem
     */
    protected abstract void processContent(final OutputStream stream, final TermParserReader p) throws Exception;

    /**
     * parser program arguments.
     *
     * @param args the arguments to parse
     * @throws Exception if there is a problem
     */
    protected final void parseArgs(final String[] args) throws Exception {
        final CommandLine commandLine = new PosixParser().parse(getOptions(), args, false);
        config = parseConfig(commandLine);
        prepareFileMapping();
        initConfiguration();
    }

    /**
     * Init configuration if needed. The method is invoked once before processing.
     */
    protected void initConfiguration() { // NOPMD
        // do something in subclasses
    }

    /**
     * Prepare file mapping.
     *
     * @throws MalformedURLException if there file name error
     */
    protected final void prepareFileMapping() throws MalformedURLException { // NOPMD
        final String inFiles = "-".equals(config.getInput()) ? null : config.getInput();
        final String outFiles = "-".equals(config.getOutput()) ? null : config.getOutput();
        if (inFiles == null) {
            if (outFiles != null && outFiles.indexOf('*') != -1) {
                throw new InvalidOptionValueException(
                        "wildcard is allowed only if there is input file specified: " + outFiles);
            }
            sourceFiles.put(null, outFiles);
        } else if (outFiles == null) {
            if (inFiles.indexOf('*') != -1) {
                throw new InvalidOptionValueException(
                        "wildcard is allowed only if there is output file specified: " + inFiles);
            }
            sourceFiles.put(new File(inFiles).getAbsoluteFile().toURI().toString(), null);
        } else {
            if (inFiles.indexOf('*') != -1) {
                // wildcard
                final File in = new File(inFiles).getAbsoluteFile();
                final File out = new File(outFiles).getAbsoluteFile(); // NOPMD
                final int starPos = in.getName().indexOf('*');
                if (starPos == -1) {
                    throw new InvalidOptionValueException("wildcard is specified in wrong place: " + inFiles);
                }
                final String inPrefix = in.getName().substring(0, starPos); // NOPMD
                final String inSuffix = in.getName().substring(starPos + 1);
                if (inSuffix.indexOf('*') != -1) {
                    // TODO replace with some commandline exception that is logged specially
                    throw new InvalidOptionValueException("Only single wildcard is allowed in input: " + inFiles);
                }
                final int outStarPos = in.getName().indexOf('*');
                if (outStarPos == -1) {
                    throw new InvalidOptionValueException("Wildcard is required in output: " + outFiles);
                }
                final String outPrefix = out.getName().substring(0, outStarPos); // NOPMD
                final String outSuffix = out.getName()
                        .substring(outStarPos + 1);
                if (outSuffix.indexOf('*') != -1) {
                    throw new InvalidOptionValueException("Only single wildcard is allowed in output: " + outFiles);
                }
                final File inDir = in.getParentFile();
                final File outDir = out.getParentFile();
                final boolean mkdirs = outDir.mkdirs();
                if (!mkdirs && !outDir.isDirectory()) {
                    LOG.error("Failed to create directory: " + outDir);
                }
                final String[] files = inDir.list();
                final int inPrefixSize = inPrefix.length();
                final int inSuffixSize = inSuffix.length();
                for (final String file : files) {
                    if (file.startsWith(inPrefix) && file.endsWith(inSuffix)) {
                        final int fileNameLength = file.length();
                        final String outputName = outPrefix
                                + file.substring(inPrefixSize, fileNameLength - inSuffixSize) + outSuffix;
                        sourceFiles.put(new File(inDir, file).getAbsoluteFile()
                                .toURI().toString(), new File(outDir, outputName).toString());
                    }
                }
            } else {
                // normal
                if (outFiles.indexOf('*') != -1) {
                    throw new InvalidOptionValueException("wildcard only allowed for output if it is in input: "
                            + outFiles);
                }
                final String file = new File(inFiles).getAbsoluteFile().toURI().toString();
                sourceFiles.put(file, outFiles);
            }
        }
    }

    /**
     * Application start method.
     *
     * @param args the application arguments
     * @throws Exception in case of IO problem
     */
    public final void start(final String[] args) throws Exception {
        parseArgs(args);
        for (final Map.Entry<String, String> me : sourceFiles.entrySet()) {
            FileOutputStream fout = null;
            if (me.getValue() != null) {
                fout = new FileOutputStream(me.getValue());
            }
            final OutputStream outStream = fout != null ? fout : System.out;
            try {
                // TODO refactor try catch
                TermParserReader p = null;
                try {
                    if (me.getKey() == null) {
                        p = new TermParserReader(configuration, new PhraseParserReader(
                                new LexerReader(configuration,
                                        new InputStreamReader(System.in,
                                                configuration.getParserConfiguration().getEncoding("urn:system:in")),
                                        "urn:system:in", TextPos.START)));
                    } else {
                        p = new TermParserReader(configuration, new URL(me.getKey()));
                    }
                    p.advance();
                    processContent(outStream, p);
                } finally {
                    if (p != null) {
                        p.close();
                    }
                }
            } finally {
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (final Exception ex) { // NOPMD
                        LOG.warn("Problem with closing stream", ex);
                    }
                } else {
                    System.out.flush();
                }
            }
        }
    }

    /**
     * @return the parser configuration.
     */
    public final TermReaderCatalogConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * The configuration.
     *
     * @param configuration configuration
     */
    protected final void setConfiguration(final TermReaderCatalogConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * The base configuration.
     */
    public static class BaseConfig {
        // TODO handle catalog options as well
        /**
         * The command line.
         */
        private final CommandLine commandLine;

        /**
         * The constructor.
         *
         * @param commandLine the command line
         */
        public BaseConfig(final CommandLine commandLine) {
            this.commandLine = commandLine;
        }

        /**
         * @return get base options.
         */
        public static Options getBaseOptions() {
            // TODO catalog options here
            return new Options()
                    .addOption("i", "input", true, "input file list or '-' in the case of stdin.")
                    .addOption("o", "output", true, "output file list or '-' in the case of stdout.");
        }

        /**
         * @return the input files
         */
        public final String getInput() {
            return commandLine.getOptionValue('i');
        }

        /**
         * @return the output files
         */
        public final String getOutput() {
            return commandLine.getOptionValue('o');
        }

        /**
         * @return the command line
         */
        protected final CommandLine getCommandLine() {
            return commandLine;
        }
    }
}
