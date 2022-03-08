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
package net.sf.etl.utils; // NOPMD

import net.sf.etl.parsers.DefaultTermParserConfiguration;
import net.sf.etl.parsers.TextPos;
import net.sf.etl.parsers.streams.DefaultTermReaderConfiguration;
import net.sf.etl.parsers.streams.LexerReader;
import net.sf.etl.parsers.streams.PhraseParserReader;
import net.sf.etl.parsers.streams.TermParserReader;
import net.sf.etl.parsers.streams.TermReaderConfiguration;
import net.sf.etl.xml_catalog.blocking.BlockingCatalog;
import net.sf.etl.xml_catalog.blocking.provider.CatalogProviders;
import net.sf.etl.xml_catalog.blocking.provider.CatalogRuntimeProvider;
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
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Abstract class for utilities that convert one set of sources to another.
 *
 * @param <C> the configuration type
 * @author const
 */
public abstract class AbstractFileConverter<C extends AbstractFileConverter.BaseConfig> {
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractFileConverter.class);
    public static final String STDIN_URN = "urn:x-etl:system:in";
    /**
     * Map from source to destinations. Null key means stdin. Null value means
     * stdout.
     */
    private final NavigableMap<String, String> sourceFiles = new TreeMap<>();
    /**
     * The parser configuration.
     */
    private TermReaderConfiguration configuration = DefaultTermReaderConfiguration.INSTANCE;
    /**
     * The parsed config.
     */
    private C config;
    /**
     * The catalog used for stylesheets.
     */
    private BlockingCatalog catalog;

    /**
     * Parse configuration.
     *
     * @param commandLine the command line
     * @return the parsed configuration
     */
    protected abstract C parseConfig(CommandLine commandLine);

    /**
     * @return the command line options
     */
    protected abstract Options getOptions();

    /**
     * @return the configuration
     */
    protected final C getConfig() {
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
    protected final void parseArgs(final String... args) throws Exception {
        final CommandLine commandLine = new PosixParser().parse(getOptions(), args, false);
        config = parseConfig(commandLine);
        prepareFileMapping();
        initConfiguration();
    }

    /**
     * Init configuration if needed. The method is invoked once before processing.
     */
    protected final void initConfiguration() {
        final ClassLoader classloader = config.getClassloader();
        final CatalogRuntimeProvider provider = CatalogProviders.createDefaultCatalogProvider(
                CatalogProviders.DEFAULT_ROOT_REQUEST, classloader,
                true, config.isUserCatalogEnabled(), config.isSystemCatalogEnabled(), config.getCatalogPaths());
        catalog = new BlockingCatalog(CatalogProviders.DEFAULT_ROOT_REQUEST,
                CatalogProviders.createCachedCatalog(provider));
        configuration = new DefaultTermReaderConfiguration(new DefaultTermParserConfiguration(
                config.getTabSize(), config.getCharset()), classloader);
        Thread.currentThread().setContextClassLoader(classloader);
    }

    /**
     * Prepare file mapping.
     */
    protected final void prepareFileMapping() { // NOPMD
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
                    LOG.error("Failed to create directory: {}", outDir);
                }
                final String[] files = inDir.list();
                if (files == null) {
                    throw new InvalidOptionValueException("Unable to list directory: " + inDir);
                }
                final int inPrefixSize = inPrefix.length();
                final int inSuffixSize = inSuffix.length();
                for (final String file : files) {
                    if (file.startsWith(inPrefix) && file.endsWith(inSuffix)) {
                        final int fileNameLength = file.length();
                        final String outputName = outPrefix
                                + file.substring(inPrefixSize, fileNameLength - inSuffixSize) + outSuffix;
                        sourceFiles.put(new File(inDir, file).getAbsoluteFile().toURI().toString(), // NOPMD
                                new File(outDir, outputName).toString()); // NOPMD
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
    public final void start(final String... args) throws Exception {
        parseArgs(args);
        for (final Map.Entry<String, String> me : sourceFiles.entrySet()) {
            FileOutputStream fout = null;
            if (me.getValue() != null) {
                fout = new FileOutputStream(me.getValue()); // NOPMD
            }
            final OutputStream outStream = fout != null ? fout : System.out;
            try {
                // TODO refactor try catch
                TermParserReader p = null;
                try {
                    if (me.getKey() == null) {
                        p = new TermParserReader(configuration, new PhraseParserReader(// NOPMD
                                new LexerReader(configuration,
                                        new InputStreamReader(System.in,
                                                configuration.getParserConfiguration().getEncoding(STDIN_URN)),
                                        STDIN_URN, TextPos.START)));
                    } else {
                        p = new TermParserReader(configuration, new URL(me.getKey())); // NOPMD
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
    public final TermReaderConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * The configuration.
     *
     * @param configuration configuration
     */
    protected final void setConfiguration(final TermReaderConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * @return the catalog
     */
    public final BlockingCatalog getCatalog() {
        return catalog;
    }

    /**
     * The base configuration.
     */
    public static class BaseConfig {
        /**
         * Option for suppressing system catalog.
         */
        private static final String FILE_ENCODING = "file-encoding";
        /**
         * Option for suppressing system catalog.
         */
        private static final String DEFAULT_FILE_ENCODING = StandardCharsets.UTF_8.displayName();
        /**
         * Option for suppressing system catalog.
         */
        private static final String TAB_SIZE = "tab-size";
        /**
         * Option for suppressing system catalog.
         */
        private static final String DEFAULT_TAB_SIZE = "8";
        /**
         * Option for suppressing system catalog.
         */
        private static final String SUPPRESS_SYSTEM_CATALOG = "suppress-system-catalog";
        /**
         * Option for suppressing user catalog.
         */
        private static final String SUPPRESS_USER_CATALOG = "suppress-user-catalog";
        /**
         * The command line.
         */
        private final CommandLine commandLine;
        /**
         * The class loader.
         */
        private ClassLoader classLoader;
        /**
         * The catalog paths (URLs).
         */
        private List<URI> catalogPaths;

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
                    .addOption("C", "catalog", true, "the additional catalog to use (file only). "
                            + "Catalogs specified by this option are consulted before classpath catalogs "
                            + "and system/user catalogs.")
                    .addOption("c", "classpath", true, "the classpath roots (might be separated by '"
                            + File.pathSeparator + "'). It must be file or directory. Used for catalog construction, "
                            + "and class loading if needed.")
                    .addOption(null, SUPPRESS_SYSTEM_CATALOG, false, "suppress usage of system catalog.")
                    .addOption(null, SUPPRESS_USER_CATALOG, false, "suppress usage of user catalog.")
                    .addOption(null, FILE_ENCODING, true, "the file encoding (default: " + DEFAULT_FILE_ENCODING + ")")
                    .addOption(null, TAB_SIZE, true, "the tab size (default: " + DEFAULT_FILE_ENCODING + ")")
                    .addOption("i", "input", true, "input file list or '-' in the case of stdin.")
                    .addOption("o", "output", true, "output file list or '-' in the case of stdout.");
        }

        /**
         * @return the default tab size
         */
        public final int getTabSize() {
            final String optionValue = commandLine.getOptionValue(TAB_SIZE, DEFAULT_TAB_SIZE);
            try {
                final int tabSize = Integer.parseInt(optionValue);
                if (tabSize <= 0) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Invalid tab size: %s using %s".formatted(optionValue, DEFAULT_TAB_SIZE));
                    }
                    return Integer.parseInt(DEFAULT_TAB_SIZE);
                }
                return tabSize;
            } catch (NumberFormatException ex) {
                LOG.error("Invalid tab size: %s using %s".formatted(optionValue, DEFAULT_TAB_SIZE));
                return Integer.parseInt(DEFAULT_TAB_SIZE);
            }
        }


        /**
         * @return the charset for input files.
         */
        public final Charset getCharset() {
            final String optionValue = commandLine.getOptionValue(FILE_ENCODING, DEFAULT_FILE_ENCODING);
            try {
                return Charset.forName(optionValue);
            } catch (UnsupportedCharsetException ex) {
                LOG.error("Unsupported charset: %s (using %s)".formatted(optionValue, StandardCharsets.UTF_8.displayName()));
                return StandardCharsets.UTF_8;
            }
        }

        /**
         * @return true if system catalog is enabled
         */
        public final boolean isSystemCatalogEnabled() {
            return !commandLine.hasOption(SUPPRESS_SYSTEM_CATALOG);
        }

        /**
         * @return true if user catalog is enabled
         */
        public final boolean isUserCatalogEnabled() {
            return !commandLine.hasOption(SUPPRESS_USER_CATALOG);
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
         * @return the paths to catalogs.
         */
        public final List<URI> getCatalogPaths() {
            if (catalogPaths == null) {
                final String[] cs = getCommandLine().getOptionValues('C');
                catalogPaths = new ArrayList<>();
                if (cs != null && cs.length > 0) {
                    for (final String c : cs) {
                        final File file = new File(c); // NOPMD
                        if (file.isFile()) {
                            catalogPaths.add(file.getAbsoluteFile().toURI());
                        } else {
                            if (LOG.isErrorEnabled()) {
                                LOG.error("File does not exists or is not a file: %s".formatted(c));
                            }
                        }
                    }
                } else {
                    catalogPaths = Collections.emptyList();
                }
            }
            return catalogPaths;
        }

        /**
         * @return the classloader for beans.
         */
        public final ClassLoader getClassloader() {
            if (classLoader == null) {
                final ArrayList<URL> urls = new ArrayList<>();
                final String[] cs = getCommandLine().getOptionValues('c');
                if (cs != null && cs.length > 0) {
                    for (final String c : cs) {
                        final String[] pe = c.split(File.pathSeparator);
                        for (final String p : pe) {
                            final String tp = p.trim();
                            if (tp.length() != 0) {
                                final File file = new File(tp); // NOPMD
                                if (file.exists()) { // NOPMD
                                    try {
                                        urls.add(file.getAbsoluteFile().toURI().toURL());
                                    } catch (MalformedURLException e) {
                                        LOG.error("Bad classpath element: " + p, e);
                                    }
                                } else {
                                    LOG.warn("Classpath element {} does not exists, ignoring.", p);
                                }
                            }
                        }
                    }
                }
                if (!urls.isEmpty()) {
                    classLoader = new URLClassLoader(
                            urls.toArray(new URL[0]), BaseConfig.class.getClassLoader());
                } else {
                    classLoader = BaseConfig.class.getClassLoader();
                }
            }
            return classLoader;
        }

        /**
         * @return the command line
         */
        protected final CommandLine getCommandLine() {
            return commandLine;
        }
    }
}
